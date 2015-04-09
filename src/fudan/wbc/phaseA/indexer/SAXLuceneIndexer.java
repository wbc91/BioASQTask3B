package fudan.wbc.phaseA.indexer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.DefaultHandler;

import fudan.wbc.phaseA.analyzer.BioAnalyzer;
import fudan.wbc.phaseA.macro.LuceneVersion;

public class SAXLuceneIndexer extends DefaultHandler{
	private StringBuffer elementBuffer = new StringBuffer();
	private StringBuffer abstractTextBuffer = new StringBuffer();
	private StringBuffer journal_year = new StringBuffer();
	private IndexWriter writer = null;
	private Document document = null;
	private FieldType fieldType = null;
	private Directory myDir = null;
	private Set<String>existedPmids = null;
	private boolean isDuplicate = false;
	private boolean isPmid = true;
	private boolean isJournal = false;
	
	public void indexXML(InputStream xmlStream, Set<String>existedPmids) throws ParserConfigurationException, SAXException, IOException{
		if(myDir == null)
			myDir = FSDirectory.open(new File("../TestIndex"));
		this.existedPmids = existedPmids;
		IndexWriterConfig iwc = new IndexWriterConfig(LuceneVersion.matchVersion, new BioAnalyzer());
		writer = new IndexWriter(myDir, iwc);
		fieldType = new FieldType();
		fieldType.setIndexed(true);
		fieldType.setStored(true);
		fieldType.setTokenized(true);
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(true);
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		try {
			factory.setFeature("http://xml.org/sax/features/validation",false);
		} catch (SAXNotRecognizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
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
		if("MedlineCitation".equals(qName)){
			this.isPmid = true;
			document = new Document();
		}
		if (qName.equals("Journal"))
		      this.isJournal = true;
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
			document.add(new StringField("PMID",elementBuffer.toString(), Field.Store.YES));
			isPmid = false;
			if(!isDuplicate)existedPmids.add(elementBuffer.toString());		
		}
		else if("AbstractText".equals(qName)){
			abstractTextBuffer.append(elementBuffer.toString());
		}
		else if("Abstract".equals(qName)){
			document.add(new Field("Abstract", abstractTextBuffer.toString(),fieldType));
			abstractTextBuffer.setLength(0);
		}
		else if (qName.equals("Year")) {
		      if (this.isJournal) {
		        this.journal_year.append(this.elementBuffer.toString());
		      }
		}
		else if (qName.equals("Journal")) {
			 if ((this.journal_year != null) && (this.journal_year.length() > 0)) {
			        document.add(new Field("Journal_year", this.journal_year.toString(), fieldType));
			        this.journal_year.setLength(0);
			 }
			 this.isJournal = false;
		}
		
		else if("MedlineCitation".equals(qName)){
			if(isDuplicate)isDuplicate=false;
			try {
				writer.addDocument(document);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	@Override
	public void endDocument(){
		try{
			writer.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}







