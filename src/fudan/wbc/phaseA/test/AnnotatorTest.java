package fudan.wbc.phaseA.test;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;

import fudan.wbc.phaseA.annotator.BioOntologyAnnotator;
import fudan.wbc.phaseA.annotator.PubTator;
import fudan.wbc.phaseA.macro.Utility;
import fudan.wbc.phaseA.model.SourceFileAnalyzer;

public class AnnotatorTest {
	
	
	@Test
	public void createAnnotationTest() throws Exception{
		Utility.initializeConceptSet();
		SourceFileAnalyzer sfa = SourceFileAnalyzer.getInstance();
		sfa.parse(new File(Utility.fileDir));
		JSONArray questionList = sfa.getQuestionArray();
		BioOntologyAnnotator boa = new BioOntologyAnnotator();
		for(int i = 0; i < questionList.size(); ++i){
			JSONObject question = (JSONObject)questionList.get(i);
			String questionBody = (String)question.get("body");
			String questionId = (String)question.get("id");
			String tmp = questionBody.trim().replaceAll("\n", " ");
			tmp = tmp.replaceAll("\\?", " ");
			tmp = tmp.replaceAll("\"", "");
			String[] words = tmp.split(" ");
			
			for(int j = 0; j < words.length; ++j){
				words[j] = words[j].toLowerCase();
				if(Utility.replacement.containsKey(words[j])){
					words[j] = Utility.replacement.get(words[j]);
				}
			}
			tmp = Utility.join(words, '+');
			boa.getAnnotation(tmp);
			HashSet<String>termSet = (HashSet<String>) boa.getTermSet();
			for(int j = 0; j < words.length; ++j){
				if(Utility.unrecognized.contains(words[j]) && !termSet.contains(words[j])){
					termSet.add(words[j]);
				}
				else if(Utility.prohibited.contains(words[j]) && termSet.contains(words[j])){
					termSet.remove(words[j]);
				}
			}
			
			try {
				PrintWriter pw = new PrintWriter(new File("../Annotation/"+Utility.DirName+"/"+questionId+".txt"));
				pw.println(questionBody);
				Iterator<String>iter = termSet.iterator();
				String concept = "";
				while(true){
					if(iter.hasNext()){
						concept = iter.next();
						pw.print(concept+"||");
						if(termSet.size() >= 2)
							System.out.println(questionId+":"+concept);
					}
					else {
						concept = concept.substring(0, concept.length()-2);
						break;
					}
				}
				pw.println();
				pw.close();
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
	}
	
	@Test
	public void createSentenceAnnotationTest() throws Exception{
		BioOntologyAnnotator boa = new BioOntologyAnnotator();
//		String tmp = "Super-SILAC is a method used in quantitative proteomics. What is the super-SILAC mix? (SILAC: Stable Isotopic labelling by aminoacids in cell culture)";
//		String tmp = "Are transcription and splicing connected";
		String tmp = "What is the \"Proteomic ruler\"?";
		tmp = tmp.replaceAll("%", "");
		tmp = tmp.replaceAll("\"", "");
		tmp = tmp.replaceAll("\\?", "");
		tmp = tmp.replaceAll(" ", "+");
		
		
		boa.getAnnotation(tmp);
	}
	
	@Test
	public void createPubTatorParserTest() throws Exception{
		PubTator pt = new PubTator();
		HashSet<String>pmids = new HashSet<String>(Arrays.asList("12723987"));
		pt.parseDocuments(pmids);
	}
}
