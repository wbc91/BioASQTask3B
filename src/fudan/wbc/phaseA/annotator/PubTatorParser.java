package fudan.wbc.phaseA.annotator;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.DefaultHandler;

import fudan.wbc.phaseA.macro.Utility;



public class PubTatorParser extends DefaultHandler{
	private StringBuffer elementBuffer = new StringBuffer();
	private StringBuffer passageBuffer = new StringBuffer();
	private StringBuffer pmidBuffer = new StringBuffer();
	private HashSet<String>termSet = null;
	private HashSet<String>pmidSet = new HashSet<String>();
	private HashMap<String, HashSet<String>> passage2Terms = null;
	private Map<String, HashMap<String, HashSet<String>>>pmid2PassageAndTerms = null;
	
	private boolean isPassage = false;
	private boolean isAnnotation = false;
	private boolean isNeedDatabaseInsertion = false;
	private boolean needsClean = false;
	
	private int docCount = 0;
	
	private Connection dbconn = null;
	private int fileNode;
	private int fileCount;
	
	public void reset(){
		pmidSet = new HashSet<String>();
	}
	public void setFileNode(int fileNode){
		this.fileNode = fileNode;
	}
	public void setFileCount(int fileCount2){
		this.fileCount = fileCount2;
	}
	
	public PubTatorParser(){
		dbconn = Utility.getConn();
	}
	
	public HashSet<String> getPmidSet(){
		return this.pmidSet;
	}
	
	public boolean getNeedsClean(){
		return this.needsClean;
	}
	public void setNeedDatabaseInsertion(boolean flag){
		this.isNeedDatabaseInsertion = flag;
	}
	
	public void parseFile(InputStream xmlStream) throws Exception{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		SAXParser parser = factory.newSAXParser();
		parser.parse(xmlStream, this);
	
	}
	
	@Override
	public void startDocument(){
		
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes){
		elementBuffer.setLength(0);
		if("passage".equals(qName)){
			isPassage = true;
		}
		else if("annotation".equals(qName)){
			isAnnotation = true;
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length){
		elementBuffer.append(ch, start, length);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName){
		if("document".equals(qName)){
			if(pmid2PassageAndTerms == null){
				pmid2PassageAndTerms = new HashMap<String, HashMap<String, HashSet<String>>>();
			}
			pmid2PassageAndTerms.put(pmidBuffer.toString(), passage2Terms);
			pmidBuffer.setLength(0);
			passage2Terms = new HashMap<String, HashSet<String>>();
		}
		else if("passage".equals(qName)){
			if(passage2Terms == null){
				passage2Terms = new HashMap<String, HashSet<String>>();
			}
			passage2Terms.put(passageBuffer.toString(),termSet);
			
			isPassage = false;
			isAnnotation = false;
			passageBuffer.setLength(0);
			termSet = new HashSet<String>();
			
		}
		else if("text".equals(qName)){
			if(isPassage == true && isAnnotation == false){
				passageBuffer.append(elementBuffer.toString());
			}
			else if(isPassage == true && isAnnotation == true){
				if(termSet == null){
					termSet = new HashSet<String>();
				}
				termSet.add(elementBuffer.toString());
			}
		}
		else if("id".equals(qName)){
			pmidBuffer.append(elementBuffer.toString());
			pmidSet.add(pmidBuffer.toString());
			++docCount;
			if(isNeedDatabaseInsertion){
				PreparedStatement stmt = null;
				try{
					String sql = "insert into pubtator (pmid, directory) " +
							"values(?,?)";
					stmt = dbconn.prepareStatement(sql);
					stmt.setInt(1, Integer.parseInt(pmidBuffer.toString()));
					stmt.setString(2, fileNode+"-"+fileCount+"-"+docCount);
					stmt.execute();
				}catch(SQLException e){
					e.printStackTrace();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public void endDocument(){
		
	}

	public Map<String,HashSet<String>> getPassage2Terms(String pmid) {
		return pmid2PassageAndTerms.get(pmid);
	}
}
