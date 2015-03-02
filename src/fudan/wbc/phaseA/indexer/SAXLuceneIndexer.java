package fudan.wbc.phaseA.indexer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fudan.wbc.phaseA.analyzer.BioAnalyzer;
import fudan.wbc.phaseA.macro.LuceneVersion;

public class SAXLuceneIndexer extends DefaultHandler{
	private StringBuffer elementBuffer = new StringBuffer();
	private StringBuffer abstractTextBuffer = new StringBuffer();
	private IndexWriter writer = null;
	private Document document = null;
	private String pmid = "";
	private FieldType fieldType = null;
	
	public void indexXML(InputStream xmlStream) throws ParserConfigurationException, SAXException, IOException{
		Directory dir = FSDirectory.open(new File("../TestIndex"));
		IndexWriterConfig iwc = new IndexWriterConfig(LuceneVersion.matchVersion, new BioAnalyzer());
		writer = new IndexWriter(dir, iwc);
		fieldType = new FieldType();
		fieldType.setIndexed(true);
		fieldType.setStored(false);
		fieldType.setTokenized(true);
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		parser.parse(xmlStream, this);
	}
	
	@Override
	public void startDocument() throws SAXException{
		
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
		if("MedlineCitation".equals(qName)){
			document = new Document();
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException{
		elementBuffer.setLength(0);
		elementBuffer.append(ch,start,length);
	}
	
	@Override
	public void endElement(String uri, String localName,String qName) throws SAXException{
		if("PMID".equals(qName)){
			if("".equals(pmid)){
				document.add(new StringField("PMID",elementBuffer.toString(), Field.Store.YES));
				pmid = elementBuffer.toString();
			}
			elementBuffer.setLength(0);
		}
		else if("AbstractText".equals(qName)){
			abstractTextBuffer.append(elementBuffer.toString());
			elementBuffer.setLength(0);
		}
		else if("Abstract".equals(qName)){
			document.add(new Field("Abstract", abstractTextBuffer.toString(),fieldType));
			abstractTextBuffer.setLength(0);
		}
		else if("MedlineCitation".equals(qName)){
			pmid = "";
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







