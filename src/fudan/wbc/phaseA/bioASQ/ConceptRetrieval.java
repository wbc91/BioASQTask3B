package fudan.wbc.phaseA.bioASQ;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import fudan.wbc.phaseA.annotator.BioOntologyAnnotator;
import fudan.wbc.phaseA.macro.Utility;
import fudan.wbc.phaseA.model.SourceFileAnalyzer;
import fudan.wbc.phaseA.ontologyCall.ConceptCall;

public class ConceptRetrieval {
	
	private SourceFileAnalyzer sfa = SourceFileAnalyzer.getInstance();
	
	private String reProcessId = "514a0f0ad24251bc05000052";
	
	private boolean reprocess =false;
	public ConceptRetrieval(){
		Utility.initializeConceptSet();
	}
	public void retrieve(){
		try {
			sfa.parse(new File(Utility.fileDir));
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		JSONArray questionList = sfa.getQuestionArray();
		BioOntologyAnnotator boa = new BioOntologyAnnotator();
		ConceptCall ccc = new ConceptCall();
		for(int i = 0; i < questionList.size(); ++i){
			JSONObject question = (JSONObject)questionList.get(i);
			String questionBody = (String)question.get("body");
			String questionId = (String)question.get("id");
			if(reprocess){
				if(!questionId.equals(reProcessId))continue;
			}
			Set<String>otherWords = new HashSet<String>();
			boa.setQuestionId(questionId);
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
			try{
				boa.getAnnotation(tmp);
			}catch(Exception e){
				e.printStackTrace();
			}
			
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
				PrintWriter pw = new PrintWriter(new File("../Concept/"+Utility.DirName+"/"+questionId+".txt"));
				pw.println(questionBody);
				Iterator<String>iter = termSet.iterator();
				String concept = "";
				while(true){
					if(iter.hasNext()){
						concept = iter.next();
						pw.print(concept+"||");
					}
					else {
						concept = concept.substring(0, concept.length()-2);
						break;
					}
				}
				pw.println();
				pw.close();
				
				
				String query = concept.replaceAll("||", " ");
				FileWriter file = new FileWriter("../dataSet/"+Utility.DirName+"/concept/"+questionId+".json");
				
				JSONArray findings = ccc.retrieveConcept(query);
				JSONArray concepts = new JSONArray();
				for(int fi = 0; fi < findings.size(); fi++){
		        	if(fi == 10)break;
		        	JSONObject jo = (JSONObject)findings.get(fi);
		        	JSONObject jccps = (JSONObject)jo.get("concept");
		        	concepts.add(jccps.get("uri"));
		        }
		        concepts.writeJSONString(file);
		        
		        file.flush();
		        file.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		System.out.println("Concepts done");
		
	}
}
