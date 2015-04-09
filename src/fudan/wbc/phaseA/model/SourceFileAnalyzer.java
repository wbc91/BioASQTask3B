package fudan.wbc.phaseA.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class SourceFileAnalyzer {
	public static SourceFileAnalyzer getInstance(){
		if(instance == null){
			instance = new SourceFileAnalyzer();
		}
		return instance;
	}
	
	private static SourceFileAnalyzer instance = null;
	
	private SourceFileAnalyzer(){
		
	}
	
	private JSONArray questionArray = null;
	
	public JSONArray getQuestionArray(){
		return questionArray;
	}
	
	public void parse(File file) throws FileNotFoundException, IOException, ParseException{
		JSONParser parser = new JSONParser();
		Object object = parser.parse(new FileReader(file));
		JSONObject jsonObject = (JSONObject)object;
		JSONArray jsonArray = (JSONArray)jsonObject.get("questions");
		
		if(questionArray == null){
			questionArray = new JSONArray();
		}
		questionArray = jsonArray;
	}
	
}
