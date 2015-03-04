package fudan.wbc.phaseA.annotator;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class RequestParser extends DefaultHandler{
	private boolean isBodyAppear = false;
	
	private String jsonString = null;
	
	private StringBuffer elementBuffer = new StringBuffer(); 
	
	public String getJsonString(){
		return this.jsonString;
	}
	
	private void generateJsonString(String string){
		Pattern pattern = Pattern.compile("var json=[\\.];var");
		Matcher matcher = pattern.matcher(string);
		if(matcher.find()){
			this.jsonString = matcher.group();
		}
	}
	
	public void parseHtml(InputStream htmlStream) throws ParserConfigurationException, SAXException, IOException{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		parser.parse(htmlStream, this);
	}
	
	@Override
	public void startDocument() throws SAXException{
		isBodyAppear = false;
	}
	
	@Override
	public void startElement(String uri,String localName, String qName, Attributes attributes)throws SAXException{
		elementBuffer.setLength(0);
		if("body".equals(qName)) isBodyAppear = true;
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException{
		elementBuffer.append(ch,start,length);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException{
		if(isBodyAppear&&"script".equals(qName)){
			this.generateJsonString(elementBuffer.toString());
		}
	}
	
	@Override
	public void endDocument(){
		
	}
	
}
