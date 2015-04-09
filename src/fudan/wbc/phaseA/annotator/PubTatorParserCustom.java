package fudan.wbc.phaseA.annotator;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PubTatorParserCustom {
	private Map<String, HashMap<String, HashSet<String>>>pmid2PassageAndTerms = null;
	
	public void parseFile(InputStream inputStream){
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new InputStreamReader(inputStream));
			StringBuffer sb = new StringBuffer();
			String line = "";
			StringBuffer pmid = new StringBuffer();
			
			while((line = reader.readLine())!= null){
				
			}
			
			System.out.println(sb.toString());
		
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
