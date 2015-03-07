package fudan.wbc.phaseA.test;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Test;

import fudan.wbc.phaseA.annotator.BioOntologyAnnotator;
import fudan.wbc.phaseA.macro.Utility;
import fudan.wbc.phaseA.model.SourceFileAnalyzer;

public class AnnotatorTest {
	private static Map<String,String>replacement = null;
	private static Set<String>unrecognized = null;
	
	static{
		unrecognized = new HashSet<String>();
		unrecognized.add("dnmt3");
		unrecognized.add("under-expression");
		unrecognized.add("drug");
	}
	
	static{
		replacement = new TreeMap<String,String>();
		replacement.put("thyronamines", "thyronamine");
	}
	
	@Test
	public void createAnnotationTest() throws Exception{
		String[] questionList = SourceFileAnalyzer.parse(new File("../trainingSet/BioASQ-trainingDataset2b.json"));
		BioOntologyAnnotator boa = new BioOntologyAnnotator();
		for(int i = 0; i < questionList.length; ++i){
			Set<String>otherWords = new HashSet<String>();
			String tmp = questionList[i].trim().replaceAll("\n", " ");
			tmp = tmp.replaceAll("\\?", " ");
			String[] words = tmp.split(" ");
			for(int j = 0; j < words.length; ++j){
				if(replacement.containsKey(words[j].toLowerCase())){
					words[j] = replacement.get(words[j].toLowerCase()).replaceAll(" ","+");
				}else if(unrecognized.contains(words[j].toLowerCase())){
					otherWords.add(words[j].toLowerCase());
				}
				
			}
			tmp = Utility.join(words, '+');
			System.out.println(questionList[i]+": ");
			boa.getAnnotation(tmp);
			Iterator<String>it = otherWords.iterator();
			while(it.hasNext()){
				System.out.println(" "+(String)it.next());
			}
			System.out.println();
			System.out.println();
		}
	}
}
