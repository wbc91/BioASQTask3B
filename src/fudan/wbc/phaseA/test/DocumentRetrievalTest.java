package fudan.wbc.phaseA.test;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import fudan.wbc.phaseA.bioASQ.DocumentRetrieval;
import fudan.wbc.phaseA.macro.Utility;
import fudan.wbc.phaseA.model.PubTatorDatabaseGenerator;

public class DocumentRetrievalTest {
	@Test
	public void createDocumentRetrievalTest() throws Exception{
		DocumentRetrieval dr = new DocumentRetrieval();
		dr.retrieve();
		//		dr.retrieve(new String[]{"sport","risk","commotio cordis"});
	}
	
	@Test
	public void createSentenceSplitTest() throws Exception{
		String tmpAbstract = "Modern NMR spectroscopy has reached an unprecedented level of sophistication in the determination of biomolecular structure and dynamics at atomic resolution in liquids. However, the sensitivity of this technique is still too low to solve a variety of cutting-edge biological problems in solution, especially those that involve viscous samples, very large biomolecules or aggregation-prone systems that need to be kept at low concentration. Despite the challenges, a variety of efforts have been carried out over the years to increase sensitivity of NMR spectroscopy in liquids. This review discusses basic concepts, recent developments and future opportunities in this exciting area of research. \n Copyright © 2014 Elsevier Inc. All rights reserved.";
		Set<String>removeEndSet = new HashSet<String>(Arrays.asList(",",".","!","?","\n",";"));
		String replacedString = tmpAbstract.replaceAll("[\\.\\:\\;\\?\\!\\\n] ","######");
		String[]sentences = tmpAbstract.replaceAll("[\\.\\:\\;\\?\\!\\\n] ","######").split("######");
		for(int i = 0; i < sentences.length; ++i){
			if(i == sentences.length-1){
				Iterator it = removeEndSet.iterator();
				while(it.hasNext()){
					String endChar = (String)it.next();
					if(sentences[i].endsWith(endChar)){
						sentences[i] = sentences[i].substring(0,sentences[i].length()-1-endChar.length());
					}
				}
			}
			if(sentences[i].trim().length() > 0)
				System.out.println(sentences[i]);
		}
	}
	
	@Test
	public void createPubTatorDatabaseGeneratorTest() throws Exception{
		Directory directory = FSDirectory.open(new File(Utility.indexPath));
		IndexReader indexReader = IndexReader.open(directory);
		StringBuffer pmids = new StringBuffer();
		int pmidCount = 0;
		String exceptionInfo = "";
		try{
			PubTatorDatabaseGenerator ptdg = new PubTatorDatabaseGenerator();
			for(int i = 0; i < indexReader.maxDoc(); ++i){
				Document doc = indexReader.document(i);
				++pmidCount;
				if(pmidCount != 800)pmids.append(doc.get("PMID")+",");
				else{
					System.out.println((i+1)+" file has read!");
					pmidCount = 0;
					pmids.append(doc.get("PMID"));
					String pubUrl = "http://www.ncbi.nlm.nih.gov/CBBresearch/Lu/Demo/PubTator/abstract_ann.cgi?format=BioC&Disease=1&Gene=1&Chemical=1&Mutation=1&Species=1&pmid="+pmids.toString();
					exceptionInfo = pubUrl;
					URL url = new URL(pubUrl);
					URLConnection urlConn = url.openConnection();
					
					ptdg.createPubTatorDatabase(urlConn.getInputStream());
					pmids.setLength(0);
				}
			}
		}catch(Exception e){
			System.out.println(exceptionInfo);
			e.printStackTrace();
		}
	}
	@Test
	public void createMultiThreadDownloadTest()throws Exception{
		Directory directory = FSDirectory.open(new File(Utility.indexPath));
		IndexReader indexReader = IndexReader.open(directory);
		StringBuffer pmids = new StringBuffer();
		int pmidCount = 0;
		for(int i = 0; i < indexReader.maxDoc(); ++i){
			Document doc = indexReader.document(i);
			++pmidCount;
			if(pmidCount != 800)pmids.append(doc.get("PMID")+",");
			else{
				pmidCount = 0;
				pmids.append(doc.get("PMID"));
				String pubUrl = "http://www.ncbi.nlm.nih.gov/CBBresearch/Lu/Demo/PubTator/abstract_ann.cgi?format=BioC&Disease=1&Gene=1&Chemical=1&Mutation=1&Species=1&pmid="+pmids.toString();
				long begin = System.currentTimeMillis();
		        new PubTatorMultiDownload().download(pubUrl);
		        System.out.println(System.currentTimeMillis()-begin);
		        System.out.println();
			}
		}
	}
	@Test
	public void createMultiThreadDownloadedFileTest()throws Exception{
		InputStream fileInputStream = new FileInputStream(new File("../MultiThread/1.xml"));
		Reader reader = null;
		reader = new InputStreamReader(new BufferedInputStream(fileInputStream));
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
			PrintWriter pw = new PrintWriter(new File("../MultiThread/1-test.xml"));
			pw.print(str);
			pw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
