package fudan.wbc.phaseA.annotator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import fudan.wbc.phaseA.macro.Utility;

public class BioOntologyAnnotator {
	private Set<String> terms = null;
	private String questionId = "";
	public void setQuestionId(String questionId){
		this.questionId = questionId;
	}
	
	
	public String[] getTerms(){
		String[] tmpTerms = new String[terms.size()];
		Iterator<String>iter = terms.iterator();
		int i = 0;
		while(iter.hasNext()){
			tmpTerms[i++]=(String)iter.next();
		}
		return tmpTerms;
	}
	
	public Set<String> getTermSet(){
		return this.terms;
	}
	public void getRecommendation(String query){
		terms = new HashSet<String>();
		String questionUrl = "http://data.bioontology.org/recommender?text="+query;
		Reader reader = null;
		URLConnection urlConn = null;
		try{
			urlConn = connect(questionUrl);
			reader = new InputStreamReader(new BufferedInputStream(urlConn.getInputStream()));
			StringBuffer sb = new StringBuffer();
			int c = 0;
			while((c=reader.read())!=-1){
				sb.append((char)c);
			}
			
			String jsonString = null;
			Pattern pattern = Pattern.compile("(var json = )(.*)(;)");
			Matcher matcher = pattern.matcher(sb.toString());
			if(matcher.find()){
				jsonString = matcher.group(2).toLowerCase();
			}
			if(jsonString == null)return;
			
			System.out.println(jsonString);
			
			StringReader sReader = new StringReader(jsonString);
			JSONParser parser = new JSONParser();
			Object object = parser.parse(sReader);
			JSONArray jsonArray = (JSONArray)object;
			for(int i = 0; i < jsonArray.size(); ++i){
				JSONObject jsonRow = (JSONObject)jsonArray.get(i);
				JSONArray jsonCol = (JSONArray)jsonRow.get("recommendations");
				
			}
		}catch(Exception e){
			
		}
		
	}
	
	
	public void getAnnotation(String question) throws IOException, ParseException, ParserConfigurationException, SAXException{
		terms = new HashSet<String>();
		String questionUrl = "http://data.bioontology.org/annotator?text="+question+"&ontologies=MESH,GO,PR,DOID,DRON,CCO,PSIMOD,GRO,BMT,CCONT,HUPSON,ADO,NCIT"
				+ "&longest_only=true";
		URLConnection urlConn = connect(questionUrl);
		
		Reader reader = null;
		try{
			reader = new InputStreamReader(new BufferedInputStream(urlConn.getInputStream()));
		}catch(Exception e){
			System.out.println();
			System.out.println(question);
			System.out.println(question.replaceAll("\\+", " "));
			e.printStackTrace();
		}
		
//		PrintWriter pw = new PrintWriter(new File("../Annotation/"+Utility.DirName+"/"+questionId+".txt"));
		
		StringBuffer sb = new StringBuffer();
		int c = 0;
		while((c=reader.read())!=-1){
			sb.append((char)c);
		}
		
		String jsonString = null;
		Pattern pattern = Pattern.compile("(var json = )(.*)(;)");
		Matcher matcher = pattern.matcher(sb.toString());
		if(matcher.find()){
			jsonString = matcher.group(2).toLowerCase();
		}
		if(jsonString == null)return;
		
		StringReader sReader = new StringReader(jsonString);
		JSONParser parser = new JSONParser();
		Object object = parser.parse(sReader);
		JSONArray jsonArray = (JSONArray)object;
		for(int i = 0; i < jsonArray.size(); ++i){
			JSONObject jsonRow = (JSONObject)jsonArray.get(i);
			JSONArray jsonCol = (JSONArray)jsonRow.get("annotations");
			for(int j = 0; j < jsonCol.size(); ++j){
				JSONObject jsonEle = (JSONObject)jsonCol.get(j);
				String token = (String)jsonEle.get("text");
				if(token == null || token.equals(""))continue;
//				System.out.print(token);
				terms.add(token);
				if(i != jsonArray.size()-1){
//					System.out.print("||");
				}
			}
			
		}
	}


	protected URLConnection connect(String questionUrl)
			throws MalformedURLException, IOException {
		URL url = new URL(questionUrl);
		URLConnection urlConn = url.openConnection();
		
		urlConn.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		urlConn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.101 Safari/537.36");
		urlConn.addRequestProperty("Cookie", "_bp_session=86d435c5261963d9d857626c555d5e01; ncbo_apikey=8b5b7825-538d-40e0-9e9e-5ab9274a9aeb");
		urlConn.addRequestProperty("Connection", "keep-alive");
		urlConn.addRequestProperty("Cache-Control", "max-age=0");
		return urlConn;
	}
}
