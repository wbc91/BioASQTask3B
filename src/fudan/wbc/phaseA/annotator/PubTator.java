package fudan.wbc.phaseA.annotator;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

import fudan.wbc.phaseA.macro.Utility;

public class PubTator {
	private String pubUrl = null;
	private PubTatorParser pubTatorParser = new PubTatorParser();
	private String questionId;
	
	public HashSet<String> getPmidSet(){
		return pubTatorParser.getPmidSet();
	}
	
	public Map<String, HashSet<String>> retrievePassages(String pmid) {
		return pubTatorParser.getPassage2Terms(pmid);
	}
	
	public void setId(String id){
		this.questionId = id;
	}
	
	public void parseDocuments(){
		FileInputStream fileInputStream = null;
		try{
			fileInputStream = new FileInputStream(new File(Utility.pubTatorFilesDir+"/"+this.questionId+".xml"));
			pubTatorParser.reset();
			pubTatorParser.parseFile(fileInputStream);
		}catch(Exception e){
			System.err.println("parsing failed: "+this.questionId);
		}
		
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
			InputStream urlInputStream = urlConn.getInputStream();
			
			System.out.println("relevant documents for question: "+this.questionId+" is downloading from PubTator");
			float start = System.currentTimeMillis();
			String str = cleanText(urlInputStream);
			float end = System.currentTimeMillis();
			System.out.println("relevant documents for question: "+this.questionId+" has completed downloading");
			System.out.println((end-start)/1000+"s");

			PrintWriter pw = new PrintWriter(new File("../QuestionPubTatorPair/"+Utility.DirName+"/"+questionId+".xml"));
			pw.print(str);
			pw.close();
			InputStream inputStream = new ByteArrayInputStream(str.getBytes());
			pubTatorParser.parseFile(inputStream);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("format error for question: "+this.questionId);
			e.printStackTrace();
		}
	}

	public  String cleanText(InputStream inputStream) {
		Reader reader = null;
		reader = new InputStreamReader(new BufferedInputStream(inputStream));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		int i;  
		String str = "";
		try{
			while ((i = reader.read()) != -1) {  
			    baos.write(i);  
			}  
			str = baos.toString();
			str = str.replaceAll("< ", "&lt; ");
			str = str.replaceAll(" <", " &lt;");
			str = str.replaceAll("(<)([0-9])", "&lt;$2");
			str = str.replaceAll("<\\.", "&lt;.");
			str = str.replaceAll("> ", "&gt; ");
			str = str.replaceAll(" >", " &gt;");
			str = str.replaceAll("&lt;/date>", "</date>");
			str = str.replaceAll("</passage>\n</passage>\n</document>\n", "");
			str = str.replaceAll("<or", "(&lt;or");
			str = str.replaceAll("<-->", "&lt;--&gt;");
			str = str.replaceAll("&lt;/text>", "</text>");
			str = str.replaceAll("<text&gt;", "<text>");
		}catch(IOException e){
			e.printStackTrace();
		}
		return str;
	}
	
	
}
