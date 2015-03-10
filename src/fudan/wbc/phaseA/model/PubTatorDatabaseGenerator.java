package fudan.wbc.phaseA.model;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

import fudan.wbc.phaseA.annotator.PubTatorParser;

public class PubTatorDatabaseGenerator {
	
	private Set<String>existedPmids = null;
	private int fileNode = 1;
	private int fileCount = 0;
	public String read(InputStream inputStream){
		Reader reader = null;
		reader = new InputStreamReader(new BufferedInputStream(inputStream));
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
		return str;
	}
	public void createPubTatorDatabase(InputStream xmlStream) throws Exception{
		++fileCount;
		if(fileCount > 500){
			++fileNode;
			fileCount = 1;
		}
//		InputStream fileInputStream = new FileInputStream(new File("../PubTatorXML/1/1.xml"));
		String str = read(xmlStream);
//		String str = read(fileInputStream);
		InputStream inputStream =new ByteArrayInputStream(str.getBytes());
		PubTatorParser pt = new PubTatorParser();
		try{
			pt.setNeedDatabaseInsertion(true);
			pt.setFileCount(fileCount);
			pt.setFileNode(fileNode);
			pt.parseFile(inputStream);
//			if(pt.getNeedsClean()){
//				System.out.println("data initializing...");
//				StringBuffer sb = init(xmlStream);
//				InputStream inputStream = new ByteArrayInputStream(sb.toString().getBytes());
//				xmlStream  = inputStream;
//				pt = new PubTatorParser();
//				pt.setNeedDatabaseInsertion(true);
//				pt.setFileCount(fileCount);
//				pt.setFileNode(fileNode);
//				pt.parseFile(xmlStream);
//				System.out.println("initializing complete");
//			}else{
//				
//			}
		}catch(Exception e){
			System.out.println("PubTatorDatabaseGenerator::createPubTatorDatabase:fileNode:"+fileNode+" fileCount:"+fileCount);
			e.printStackTrace();
		}
		
		
		String fileFoder = "../PubTatorXML/"+fileNode;
		File foder = new File(fileFoder);
		if(!foder.exists()){
			foder.mkdir();
		}
		PrintWriter pw = new PrintWriter(new File("../PubTatorXML/"+fileNode+"/"+fileCount+".xml"));
		pw.print(str);
		pw.close();
		
		
	}
	
	
}
