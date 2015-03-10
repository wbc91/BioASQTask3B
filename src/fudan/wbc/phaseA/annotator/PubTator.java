package fudan.wbc.phaseA.annotator;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
	private String questionId;
	public Map<String, HashSet<String>> retrievePassages(String pmid) {
		return pubTatorParser.getPassage2Terms(pmid);
	}
	
	public void setId(String id){
		this.questionId = id;
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
			reader = new InputStreamReader(new BufferedInputStream(urlConn.getInputStream()));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();  
			int i;  
			String str = "";
			try{
				float start = System.currentTimeMillis();
				while ((i = reader.read()) != -1) {  
				    baos.write(i);  
				}  
				float end = System.currentTimeMillis();
				System.out.println((end-start)/1000+"s");
				str = baos.toString();
				str = str.replaceAll("< ", "&lt; ");
				str = str.replaceAll(" <", " &lt;");
				str = str.replaceAll("(<)([0-9])", "&lt;$2");
				str = str.replaceAll("<\\.", "&lt;.");
				str = str.replaceAll("> ", "&gt; ");
				str = str.replaceAll(" >", " &gt;");
				str = str.replaceAll("<&lt;/date>", "</date>");
				str = str.replaceAll("</passage>\n</passage>\n</document>\n", "");
				str = str.replaceAll("<or", "(&lt;or");
				str = str.replaceAll("<-->", "&lt;--&gt;");
			}catch(IOException e){
				e.printStackTrace();
			}
			PrintWriter pw = new PrintWriter(new File("../QuestionPubTatorPair/"+questionId+".txt"));
			pw.print(str);
			pw.close();
			InputStream inputStream = new ByteArrayInputStream(str.getBytes());
			pubTatorParser.parseFile(inputStream);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
}
