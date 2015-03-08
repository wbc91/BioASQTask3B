package fudan.wbc.phaseA.test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import fudan.wbc.phaseA.bioASQ.DocumentRetrieval;

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
}
