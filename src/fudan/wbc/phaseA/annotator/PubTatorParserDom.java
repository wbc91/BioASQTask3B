package fudan.wbc.phaseA.annotator;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PubTatorParserDom {
	private Map<String, HashMap<String, HashSet<String>>>pmid2PassageAndTerms = null;
	
	private NodeList getDocument(InputStream xmlStream){
		try{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setValidating(true);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			factory.setFeature("http://xml.org/sax/features/validation",false);
			DocumentBuilder build = null;
			build = factory.newDocumentBuilder();
			Document doc = (build.parse(xmlStream));
			return doc.getElementsByTagName("collection");
		}catch(IOException e){
			e.printStackTrace();
		}catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Map<String,HashSet<String>>getPassage2Terms(String pmid){
		return this.pmid2PassageAndTerms.get(pmid);
	}
	
	public void parseFile(InputStream xmlStream){
		if(pmid2PassageAndTerms == null){
			pmid2PassageAndTerms= new HashMap<String, HashMap<String, HashSet<String>>>();
		}
		
		NodeList collectionList = this.getDocument(xmlStream);
		
		HashMap<String, HashSet<String>> passage2Terms = new HashMap<String,HashSet<String>>();
		for(int i = 0; i < collectionList.getLength(); ++i){
			StringBuffer pmidBuffer = new StringBuffer();
			if(collectionList.item(i).getNodeName().equals("document")){
				NodeList documentList = collectionList.item(i).getChildNodes();
				for(int j = 0; j < documentList.getLength(); ++j){
					if(documentList.item(j).getNodeName().equals("id")){
						pmidBuffer.append(documentList.item(j).getNodeValue());
					}
					else if(documentList.item(j).getNodeName().equals("passage")){
						NodeList passageList = documentList.item(j).getChildNodes();
						StringBuffer passageBuffer = new StringBuffer();
						HashSet<String>termSet = new HashSet<String>();
						for(int k = 0; k < passageList.getLength(); ++k){
							if(passageList.item(k).getNodeName().equals("text")){
								passageBuffer.append(passageList.item(k).getNodeValue());
							}
							else if(passageList.item(k).getNodeName().equals("annotation")){
								NodeList annotationList = passageList.item(k).getChildNodes();
								for(int l = 0; l < annotationList.getLength(); ++l){
									if(annotationList.item(l).getNodeName().equals("text")){
										termSet.add(annotationList.item(l).getNodeValue());
									}
								}
							}
						}
						passage2Terms.put(passageBuffer.toString(), termSet);
					}
				}
				pmid2PassageAndTerms.put(pmidBuffer.toString(),passage2Terms);
			}
		}
	}
}
