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
	private static String[] questionList = null;
	public static String[] parse(File file) throws FileNotFoundException, IOException, ParseException{
		JSONParser parser = new JSONParser();
		Object object = parser.parse(new FileReader(file));
		JSONObject jsonObject = (JSONObject)object;
		JSONArray jsonArray = (JSONArray)jsonObject.get("questions");
		questionList = new String[jsonArray.size()];
		for(int i = 0 ; i < jsonArray.size(); ++i){
			JSONObject row = (JSONObject)jsonArray.get(i);
			questionList[i] = (String)row.get("body");
		}
		
		
		return questionList;
	}
	
}
