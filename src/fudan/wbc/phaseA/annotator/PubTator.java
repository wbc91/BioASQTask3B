package fudan.wbc.phaseA.annotator;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class PubTator {
	private String pubUrl = null;
	private PubTatorParser pubTatorParser = new PubTatorParser();
	public Map<String, HashSet<String>> retrievePassages(String pmid) {
		return pubTatorParser.getPassage2Terms(pmid);
	}

	public void parseDocuments(HashSet<String>tmpPmids) {
		String pmids = "";
		Iterator<String>pmidIter = tmpPmids.iterator();
		while(true){
			pmids += (String)pmidIter.next();
			if(pmidIter.hasNext())pmids+=",";
			else break;
		}
		pubUrl = "http://www.ncbi.nlm.nih.gov/CBBresearch/Lu/Demo/PubTator/abstract_ann.cgi?format=BioC&Disease=1&Gene=1&Chemical=1&Mutation=1&Species=1&pmid="+pmids;
//		pubUrl = "http://www.ncbi.nlm.nih.gov/CBBresearch/Lu/Demo/PubTator/abstract_ann.cgi?Disease=1&Gene=1&Chemical=1&Mutation=1&Species=1&pmid="+pmids;
		try {
			URL url = new URL(pubUrl);
			URLConnection urlConn = url.openConnection();
			
			Reader reader = null;
			try{
				reader = new InputStreamReader(new BufferedInputStream(urlConn.getInputStream()));
			}catch(Exception e){
				e.printStackTrace();
			}
			StringBuffer sb = new StringBuffer();
			int c = 0;
			char pre =' ', now = ' ';
			int countChar = 0;
			while((c=reader.read())!=-1){
				now = (char)c;
				if(now == '<' ){
					if(pre != '>' && pre != '\n'&&countChar!=0){
						c = reader.read();
						char next = (char)c;
						if(next != '/'){
							sb.append("&lt;");
						}
						else {
							sb.append(now);
						}
						sb.append(next);
						now = next; pre = now; ++countChar;++countChar;
					}
				}
				else {
					sb.append((char)c);
					pre = now; ++countChar;
				}
			}
			System.out.println(sb.toString());
			InputStream inputStream = new ByteArrayInputStream(sb.toString().getBytes());
			pubTatorParser.parseFile(inputStream);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
}
