package fudan.wbc.phaseA.model;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.util.Version;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.DefaultHandler;

import fudan.wbc.phaseA.macro.Utility;


public class MedlineDataBaseGenerator extends DefaultHandler{
	private StringBuffer elementBuffer = new StringBuffer();
	private StringBuffer abstractTextBuffer = new StringBuffer();
	private StringBuffer meshList = new StringBuffer();
	private IndexWriter writer = null;
	private Set<String>existedPmids = null;
	private boolean isDuplicate = false;
	private boolean isPmid = true;
	private String mesh_sep = " | ";
	
		
	private int mPmid = 0;
	private String mAbstract = "";
	private String mMesh = "";
	private String mYear = "";
	private String mArticleTitle = "";
	
	private Connection conn = null;
	private PreparedStatement stmt = null;
	private final static String url = Utility.mySQLUrl;
	
	public void createMedlineDatabase(InputStream xmlStream, Set<String>existedPmids) throws Exception{
		this.existedPmids = existedPmids;
		
		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url);	
		}catch(SQLException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		String sql = "insert into medline (PMID, Abstract, ArticleTitle, MeSH, Year) " +
				"values(?,?,?,?,?)";
		stmt = conn.prepareStatement(sql);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(true);
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		try {
			factory.setFeature("http://xml.org/sax/features/validation",false);
		} catch (SAXNotRecognizedException e) {
			e.printStackTrace();
		} catch (SAXNotSupportedException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		SAXParser parser = factory.newSAXParser();
		parser.parse(xmlStream, this);		
	}
	
	@Override
	public void startDocument() throws SAXException{
		
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
		this.elementBuffer.setLength(0);
		if (qName.equals("MedlineCitation")) {
			this.isPmid = true;
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException{
		elementBuffer.append(ch,start,length);
	}
	
	@Override
	public void endElement(String uri, String localName,String qName) throws SAXException{
		if("PMID".equals(qName) && isPmid){
			if(existedPmids.contains(elementBuffer.toString())){
				isDuplicate = true;
			} 
			mPmid = Integer.parseInt(elementBuffer.toString());
			isPmid = false;
			if(!isDuplicate)existedPmids.add(elementBuffer.toString());		
		}
		else if("AbstractText".equals(qName)){
			abstractTextBuffer.append(elementBuffer.toString());
		}
		else if("Abstract".equals(qName)){
			mAbstract = abstractTextBuffer.toString();
			abstractTextBuffer.setLength(0);
		}
		else if("ArticleTitle".equals(qName)){
			mArticleTitle = elementBuffer.toString();
		}
		else if("Year".equals(qName)){
			mYear = elementBuffer.toString();
		}
		else if(qName.equals("DescriptorName")){
			meshList.append(elementBuffer.toString()+mesh_sep);
		}
		else if(qName.equals("MeshHeadingList")){
			mMesh = meshList.toString();
			meshList.setLength(0);
		}
		else if("MedlineCitation".equals(qName)){
			if(isDuplicate)isDuplicate=false;
			else{
				try {
					mArticleTitle = mArticleTitle.replaceAll("'", "\\'");
					mMesh = mMesh.replaceAll("'", "\\'");
					mAbstract = mAbstract.replaceAll("'", "\\'");
					stmt.setInt(1, mPmid);
					stmt.setString(2, mAbstract);
					stmt.setString(3, mArticleTitle);
					stmt.setString(4, mMesh);
					stmt.setString(5, mYear);
					stmt.execute();
					reset();
				} catch (Exception e) {
					System.out.println(mPmid);
					System.out.println(mAbstract);
					e.printStackTrace();
				}
			}
		}
	}
	protected void reset(){
		mPmid = 0;
		mAbstract = "";
		mArticleTitle = "";
		mMesh = "";
		mYear = "";
	}
}
