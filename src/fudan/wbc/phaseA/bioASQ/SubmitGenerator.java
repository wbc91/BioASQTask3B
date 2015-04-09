package fudan.wbc.phaseA.bioASQ;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import fudan.wbc.phaseA.macro.Utility;

public class SubmitGenerator {
	private String[] fields = new String[]{"documents","concepts"};
	
	public void generateSubmitFile(){
		FileWriter fw = null;
		JSONParser parser = new JSONParser();
		JSONArray outputQuestions = new JSONArray();
		try{
			fw = new FileWriter(new File("../dataSet/"+Utility.DirName+"/results.json"));
		}catch (Exception e){
			e.printStackTrace();
		}
		
		File inputFile = new File(Utility.fileDir);
		JSONObject dataSource = null;
		try{
			dataSource = (JSONObject)parser.parse(new FileReader(inputFile));
		}catch (Exception e){
			e.printStackTrace();
		}
		JSONArray inputQuestions = (JSONArray)dataSource.get("questions");
		for(int i = 0; i < inputQuestions.size(); ++i){
			JSONObject jo = (JSONObject)inputQuestions.get(i);
			String questionId = jo.get("id").toString();
			JSONObject question = new JSONObject();
			question.put("id", questionId);
//			document
			File document = new File("../dataSet/"+Utility.DirName+"/document/"+questionId+".json");
			JSONArray documentArray = null;
			try{
				documentArray=(JSONArray)parser.parse(new FileReader(document));
			}catch (IOException e) {
				System.out.println("document file not found :"+questionId);
				e.printStackTrace();
			}catch (ParseException e) {
				e.printStackTrace();
			}
			finally{
				if(documentArray == null){
					question.put("documents", new JSONArray());
				}
				else {
					question.put("documents",documentArray);
				}
			}
			
			//concepts
			File concept = new File("../dataSet/"+Utility.DirName+"/concept/"+questionId+".json");
			JSONArray conceptArray = null;
			try{
				conceptArray=(JSONArray)parser.parse(new FileReader(concept));
			}catch (IOException e) {
				System.out.println("concept file not found :"+questionId);
				e.printStackTrace();
			}catch (ParseException e) {
				e.printStackTrace();
			}
			finally{
				if(conceptArray == null){
					question.put("concepts", new JSONArray());
				}
				else {
					question.put("concepts",conceptArray);
				}
			}
			
			outputQuestions.add(question);
		}
		
		JSONObject results = new JSONObject();
		results.put("username","nijuyoumo");
		results.put("password","1a2s3d4f");
		results.put("system","BMQAnswer5");
		results.put("questions",outputQuestions);
		
		try {
			results.writeJSONString(fw);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
