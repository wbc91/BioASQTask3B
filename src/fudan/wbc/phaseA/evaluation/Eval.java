package fudan.wbc.phaseA.evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Eval {
	private static int questionSize = 100;
	private static File testFile = null;
	private static File result = null;
	private static HashMap<String,HashMap<String,Score>> score = null;
	private static double p = 0.0d;
	private static double r = 0.0d;
	private static double f = 0.0d;
	private static double ap = 0.0d;
	private static double epsi = 0.0000000001d;
	private static HashMap<String,Double>map = null;
	private static HashMap<String,Double>gmap = null;
	private static HashMap<String,Double>meanPrecision = null;
	private static HashMap<String,Double>recall = null;
	private static HashMap<String,Double>fMeasure = null;
	private static String[] fields = new String[]{"documents"};
	private static String[] methods = new String[]{"MeanPrecision","Recall","F2Measure","MAP","GMAP"};
	//precision,recall, F-Measure
	private static void PRF(JSONObject qjo, JSONObject ajo,String field){
		int TP=0, FP=0,FN=0;
		int hit = 0;
		JSONArray ans = (JSONArray) ajo.get(field);
		
		if(ans== null||ans.size() == 0){
			p=r=f=0.0d;
			return;
		}
		if(field.equals("snippets")){
			HashSet<Pair>aset = new HashSet<Pair>();
			for(int i = 0; i < ans.size();i++){
				JSONObject snippet = (JSONObject)ans.get(i);
				int start = Integer.parseInt((Long)snippet.get("offsetInBeginSection")+"");
				int end = Integer.parseInt((Long)snippet.get("offsetInEndSection")+"");
				for(int j = start; j <= end; j++)
					aset.add(new Pair((String)snippet.get("document"),j+""));
			}
			JSONArray ques = (JSONArray)qjo.get(field);
			HashSet<Pair>qset = new HashSet<Pair>();
			for(int i = 0; i < ques.size();i++){
				JSONObject snippet = (JSONObject)ques.get(i);
				int start = Integer.parseInt((Long)snippet.get("offsetInBeginSection")+"");
				int end = Integer.parseInt((Long)snippet.get("offsetInEndSection")+"");
				for(int j = start; j <= end; j++)
					qset.add(new Pair((String)snippet.get("document"),j+""));
			}
			HashSet<Pair>s = new HashSet<Pair>();
			s.addAll(qset);
			s.retainAll(aset);
			p = (double)s.size()/(double)qset.size();
			r = (double)s.size()/(double)aset.size();
		}
		else{
			Set<String>aset = new HashSet<String>();
			for(int i = 0; i < ans.size();i++)aset.add((String)ans.get(i));
			JSONArray ques = (JSONArray)qjo.get(field);
			for(int i = 0; i < ques.size();i++){
				if(aset.contains((String)ques.get(i))){
					hit++;
				}
			}
			TP = hit;
			FP = ques.size()-hit;
			FN = aset.size()-hit;
			p = (double)TP/(double)(TP+FP);
			r = (double)TP/(double)(TP+FN);
		}
		f = 2*p*r/(p+r);
		p = (double)(Math.round(p*1000))/1000;
		r = (double)(Math.round(r*1000))/1000;
		f = (double)(Math.round(f*1000))/1000;
		
	}
	//Average Precision
	private static void AP(JSONObject qjo, JSONObject ajo, String field){
		int hit = 0;
		ap = 0.0d;
		JSONArray ans = (JSONArray)ajo.get(field);
		if(ans == null||ans.size() == 0){
			ap = 0.0d;return;
		}
		if(field.equals("snippets")){
			HashSet<Pair>aset = new HashSet<Pair>();
			for(int i = 0; i < ans.size();i++){
				JSONObject snippet = (JSONObject)ans.get(i);
				int start = Integer.parseInt((Long)snippet.get("offsetInBeginSection")+"");
				int end = Integer.parseInt((Long)snippet.get("offsetInEndSection")+"");
				for(int j = start; j <= end; j++)
					aset.add(new Pair((String)snippet.get("document"),j+""));
			}
			JSONArray ques = (JSONArray)qjo.get(field);
			int count = 1;
			for(int i = 0; i < ques.size();i++){
				JSONObject snippet = (JSONObject)ques.get(i);
				int start = Integer.parseInt((Long)snippet.get("offsetInBeginSection")+"");
				int end = Integer.parseInt((Long)snippet.get("offsetInEndSection")+"");
				Pair pair;
				for(int j = start; j <= end; j++){
					pair = new Pair((String)snippet.get("document"),j+"");
					if(aset.contains(pair)){
						hit++;
						ap+=(double)hit/(double)count;
					}
					count++;
				}
			}
			ap=ap/(double)count;
		}
		else{
			Set<String>aset = new HashSet<String>();
			for(int i = 0; i < ans.size();i++)aset.add((String)ans.get(i));
			JSONArray ques = (JSONArray)qjo.get(field);
			for(int i = 0; i < ques.size(); i++){
				if(aset.contains((String)ques.get(i))){
					hit++;
					ap += (double)hit/(double)(i+1);
				}
			}
	//		double tmap = map.get(field);
	//		tmap+=ap;
	//		double tgmap = gmap.get(field);
	//		tgmap+=Math.log(ap+epsi);
			ap = ap/(double)ans.size();
			ap = (double)(Math.round(ap*1000))/1000;
		}
	}
	//mean Precision
	public static void MeanPrecision(){
		for(String field:fields){
			Double tp = meanPrecision.get(field);
			Iterator<String>iterator = score.keySet().iterator();
			while(iterator.hasNext()){
				String id = (String)iterator.next();
				tp+=score.get(id).get(field).p;
			}
			tp/=questionSize;
			tp=(double)Math.round(tp*1000)/1000;
			meanPrecision.put(field, tp);
		}
	}
	//recall
	public static void Recall(){
		for(String field:fields){
			Double tr = recall.get(field);
			Iterator<String>iterator = score.keySet().iterator();
			while(iterator.hasNext()){
				String id = (String)iterator.next();
				tr+=score.get(id).get(field).r;
			}
			tr/=questionSize;
			tr = (double)Math.round(tr*1000)/1000;
			recall.put(field, tr);
		}
	}
	//F Measure
	public static void F2Measure(){
		for(String field:fields){
			Double tfm = fMeasure.get(field);
			Iterator<String>iterator = score.keySet().iterator();
			while(iterator.hasNext()){
				String id = (String)iterator.next();
				tfm +=score.get(id).get(field).f;
			}
			tfm/=questionSize;
			tfm = (double)Math.round(tfm*1000)/1000;
			fMeasure.put(field, tfm);
		}
	}
	//mean Average Precision
	public static void MAP(){
		for(String field:fields){
			Double tmap = map.get(field);
			Iterator<String>iterator = score.keySet().iterator();
			while(iterator.hasNext()){
				String id = (String)iterator.next();
				tmap+=score.get(id).get(field).ap;
			}
			tmap/=questionSize;
			tmap = (double)Math.round(tmap*1000)/1000;
			map.put(field, tmap);
		}
	}
	//geometric mean Average Precision
	public static void GMAP(){
		for(String field:fields){
			Double tgmap = gmap.get(field);
			Iterator<String>iterator = score.keySet().iterator();
			while(iterator.hasNext()){
				String id = (String)iterator.next();
				tgmap+=Math.log(score.get(id).get(field).ap+epsi);
			}
			tgmap/=questionSize;
			tgmap = Math.exp(tgmap);
			tgmap = (double)Math.round(tgmap*1000)/1000;
			gmap.put(field, tgmap);
		}
	}
	//evaluate per question
	private static void perQuestionEval(JSONObject qjo, JSONObject ajo){
		HashMap<String,Score> scores = new HashMap<String,Score>();
		//documents,concepts,snippets,RDF triples
		for(String field:fields){
			PRF(qjo,ajo,field);
			AP(qjo,ajo,field);
//			if(field.equals("documents"))System.out.println(r);
			scores.put(field, new Score(p,r,f,ap));
			p=r=f=ap=0.0d;
		}
		score.put((String)qjo.get("id"), scores);
	}
	private static void initAll() {
		meanPrecision = new HashMap<String,Double>();
		recall = new HashMap<String,Double>();
		fMeasure = new HashMap<String,Double>();
		map = new HashMap<String,Double>();
		gmap = new HashMap<String,Double>();
		for(String field:fields){
			meanPrecision.put(field, 0.0d);
			recall.put(field, 0.0d);
			fMeasure.put(field,0.0d);
			map.put(field, 0.0d);
			gmap.put(field, 0.0d);
		}
	}
	@SuppressWarnings("unused")
	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException{
//		testFile = new File("E:/laboratory/bioASQ/BioASQ-task2bPhaseB-testset2.txt");
//		testFile = new File("E:/laboratory/bioASQ2015/Task3B/trainingSet/BioASQ-trainingDataset2b.json");
		testFile = new File("E:/laboratory/bioASQ2015/Task3B/dataSet/testDataSet/BioASQ-task3bPhaseB-testset1.txt");
		result = new File("E:/laboratory/bioASQ2015/Task3B/dataSet/BioASQ-task3bPhaseA-testset1/results.json");
//		result = new File("C:/Users/wbc91/Downloads/results_2b_phaseA.json");
		score = new HashMap<String,HashMap<String,Score>>();
		initAll();
		JSONParser parser = new JSONParser();
		JSONObject testset = (JSONObject)parser.parse(new FileReader(result));
		JSONArray questions = (JSONArray)testset.get("questions");
		JSONObject evalset = (JSONObject)parser.parse(new FileReader(testFile));
		JSONArray answers = (JSONArray)evalset.get("questions");
		for(int i = 0; i < questions.size();i++){
			if(i == 31)
				System.out.println();
			JSONObject qjo = (JSONObject)questions.get(i);
			JSONObject ajo = (JSONObject)answers.get(i);
			perQuestionEval(qjo,ajo);
		}
		try {
			Class<?>c=Class.forName("fudan.wbc.phaseA.evaluation.Eval");
			
			for(String name:methods){
				
				Method method = c.getMethod(name, null);
				method.invoke(c, null);
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
//		MeanPrecision();
//		Recall();
//		F2Measure();
//		MAP();
//		GMAP();
		for(String field:fields){
			System.out.println("----------------------"+field+"----------------------");
			System.out.println("\t\tPrecision\tRecall\tF-Measure\tMAP\tGMAP");
			System.out.print("\t\t");
			System.out.print(meanPrecision.get(field)+"\t\t");
			System.out.print(recall.get(field)+"\t");
			System.out.print(fMeasure.get(field)+"\t\t");
			System.out.print(map.get(field)+"\t");
			System.out.print(gmap.get(field)+"\t");
			System.out.println();
		}
		
	}
	
}


/*
public static void documentsEval(String field) throws FileNotFoundException, IOException, ParseException{
	float p = 0.0f;
	float r = 0.0f;
	float f1Measure = 0.0f;
	int TP=0, FP=0, TN=0, FN=0;
	JSONParser parser = new JSONParser();
	JSONObject testset = (JSONObject)parser.parse(new FileReader(result));
	JSONArray questions = (JSONArray)testset.get("questions");
	JSONObject evalset = (JSONObject)parser.parse(new FileReader(testFile));
	JSONArray answers = (JSONArray)evalset.get("questions");
	float map = 0.0f;
	double gmap = 0.0d; double epsi = 0.00000000001d;
	for(int i = 0; i < questions.size();i++){
		int hit = 0;
		float ap = 0.0f;
		JSONObject qjo = (JSONObject)questions.get(i);
		JSONObject ajo = (JSONObject)answers.get(i);
		Set<String>aset = new HashSet<String>(); 
		JSONArray ans = (JSONArray)ajo.get(field);
		Iterator itans = ans.iterator();
		while(itans.hasNext()){
			String doc = (String)itans.next();
			aset.add(doc);
		}
		JSONArray ques = (JSONArray)qjo.get(field);
		for(int j = 0; j < ques.size();j++){
			String doc = (String)ques.get(j);
			if(aset.contains(doc)){
				hit++;
				ap += (float)hit/(float)(j+1);
			}
		}
		ap /= aset.size();
		TP += hit;
		FP += ques.size()-hit;
		FN += aset.size()-hit;
		aset = null;
		map += ap;
		gmap += Math.log((double)(ap+epsi)); 
	}
	p = (float)TP/(float)(TP+FP); p = (float)(Math.round(p*1000))/1000;
	r = (float)TP/(float)(TP+FN); r = (float)(Math.round(r*1000))/1000;
	f1Measure = 2*p*r/(p+r);      f1Measure = (float)(Math.round(f1Measure*1000))/1000;
	map /= questions.size();	  map = (float)(Math.round(map*1000))/1000;
	gmap /= questions.size();
	gmap = Math.exp(gmap);		  gmap = (double)(Math.round(gmap*1000))/1000;
	System.out.print(field+":\t");
	System.out.println(p+"\t"+r+"\t"+f1Measure+"\t"+map+"\t"+gmap+"\t");
//	System.out.println(r);
//	System.out.println(f1Measure);
//	System.out.println(map);
//	System.out.println(gmap);
}

public static void conceptsEval(){
	
}

public static void snippetsEval() throws FileNotFoundException, IOException, ParseException{
	HashSet<Pair>snippetTest = new HashSet<Pair>();
	HashSet<Pair>snippetGolden = new HashSet<Pair>();
	double psnip = 0.0d;
	double rsnip = 0.0d;
	JSONParser parser = new JSONParser();
	JSONObject testSet = (JSONObject)parser.parse(new FileReader(result));
	JSONArray questions = (JSONArray)testSet.get("questions");
	JSONObject evalset = (JSONObject)parser.parse(new FileReader(testFile));
	JSONArray answers = (JSONArray)evalset.get("questions");
	for(int i = 0; i < questions.size();i++){
		JSONObject question = (JSONObject) questions.get(i);
		JSONArray snippets = (JSONArray)question.get("snippets");
		for(int j = 0; j < snippets.size();j++){
			JSONObject snippet = (JSONObject)snippets.get(j);
			Pair pair = new Pair((String)snippet.get("snippets"),(String)snippet.get(""),(String)snippet.get(""));
			snippetTest.add(pair);
		}
		JSONObject answer = (JSONObject) answers.get(i);
		snippets = (JSONArray)answer.get("snippets");
		for(int j = 0; j < snippets.size();j++){
			JSONObject snippet = (JSONObject)snippets.get(j);
			Pair pair = new Pair((String)snippet.get("snippets"),(String)snippet.get(""),(String)snippet.get(""));
			snippetGolden.add(pair);
		}
	}
	HashSet<Pair>tmpSet = new HashSet<Pair>();
	tmpSet.addAll(snippetTest);
	tmpSet.retainAll(snippetGolden);
	psnip = (double)tmpSet.size()/(double)snippetTest.size();
	rsnip = (double)tmpSet.size()/(double)snippetGolden.size();
}

public static void rdfTriplesEval(){
	
}
public static void AP() throws FileNotFoundException, IOException, ParseException{
	
	JSONParser parser = new JSONParser();
	PrintWriter pw = new PrintWriter(new File("E:/laboratory/bioASQ/mapgmap.txt"));
	File file = new File("E:/laboratory/bioASQ/results.json");
	JSONObject testset = (JSONObject)parser.parse(new FileReader(file));
	JSONArray questions = (JSONArray)testset.get("questions");
	file = new File("E:/laboratory/bioASQ/BioASQ-task2bPhaseB-testset3.txt");
	JSONObject evalset = (JSONObject)parser.parse(new FileReader(file));
	JSONArray answers = (JSONArray)evalset.get("questions");
	float map = 0.0f;
	double gmap = 0.0d;double epsi = 0.0000000001d;
	int[] each = new int[11];
	for(int i = 0; i < questions.size();i++){
		int hit = 0;
		float ap = 0.0f;
		JSONObject qjo = (JSONObject)questions.get(i);
		JSONObject ajo = (JSONObject)answers.get(i);
		Set<String> aset = new HashSet<String>();
		JSONArray ans = (JSONArray)ajo.get("documents");
		Iterator itans = ans.iterator();
		while(itans.hasNext()){
			String doc = (String)itans.next();
			aset.add(doc);
		}
		JSONArray ques = (JSONArray)qjo.get("documents");
		for(int j = 0; j < ques.size();j++){
			String doc = (String)ques.get(j);
			if(aset.contains(doc)){
				System.out.println(doc+" "+(j+1));
				hit++;
				ap+=(float)hit/(float)(j+1);
			}
		}
		ap /= aset.size();
		each[(int)(ap*10)]++;
		pw.println(i+1+":"+"ap="+ap);
		System.out.println(hit+"/"+aset.size());
		aset = null;
		System.out.println(ap);
		map += ap;
		gmap += Math.log((double)(ap+epsi)); 
	}
	for(int eachi = 0; eachi < 11; eachi++)pw.println(each[eachi]);
	pw.close();
	map /= questions.size();
	gmap /= questions.size();
	gmap = Math.exp(gmap);
	System.out.println(map);
	System.out.println(gmap);
}
public static void APtest(String number) throws FileNotFoundException, IOException, ParseException{
	JSONParser parser = new JSONParser();
	Set<String>aset = new HashSet<String>();
	File file = new File("E:/laboratory/bioASQ/BioASQ-task2bPhaseB-testset3.txt");
	JSONObject evalset = (JSONObject)parser.parse(new FileReader(file));
	JSONArray answers = (JSONArray)evalset.get("questions");
	JSONObject ajo = (JSONObject)answers.get(Integer.parseInt(number));
	JSONArray ans = (JSONArray)ajo.get("documents");
	Iterator itans = ans.iterator();
	while(itans.hasNext()){
		String doc = (String)itans.next();
		aset.add(doc);
	}
	
	file = new File("E:/laboratory/bioASQ/PMIDs.txt");
	BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
	String line = null;
	int hit = 0;
	float ap = 0.0f;
	int count = 0;
	while((line = br.readLine())!=null){
		if(aset.contains(line)){
			System.out.println(line+" "+(count+1));
			hit++;
			ap+=(float)hit/(float)(count+1);
		}
		count++;
	}
	ap /= aset.size();
	System.out.println(hit+"/"+aset.size());
	aset = null;
	System.out.println(ap);
}
*/
