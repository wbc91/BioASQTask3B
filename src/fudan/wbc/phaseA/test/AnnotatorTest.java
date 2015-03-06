package fudan.wbc.phaseA.test;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import fudan.wbc.phaseA.annotator.BioOntologyAnnotator;
import fudan.wbc.phaseA.macro.Utility;
import fudan.wbc.phaseA.model.SourceFileAnalyzer;

public class AnnotatorTest {
	private static Map<String,String>replacement = null;
	
	static{
		replacement = new TreeMap<String,String>();
		replacement.put("thyronamines", "thyronamine");
		replacement.put("DNMT3", "");
	}
	
	@Test
	public void createAnnotationTest() throws Exception{
		String[] questionList = SourceFileAnalyzer.parse(new File("../trainingSet/BioASQ-trainingDataset3b.json"));
		BioOntologyAnnotator boa = new BioOntologyAnnotator();
		for(int i = 0; i < questionList.length; ++i){
			String tmp = questionList[i].trim().replaceAll("\n", " ");
			tmp = tmp.replaceAll("\\?", " ");
			String[] words = tmp.split(" ");
			for(int j = 0; j < words.length; ++j){
				if(replacement.containsKey(words[j].toLowerCase())){
					words[j] = replacement.get(words[j].toLowerCase());
				}
			}
			tmp = Utility.join(words, '+');
			System.out.println(questionList[i]+": ");
			boa.getAnnotation(tmp);
			System.out.println();
			System.out.println();
		}
	}
}
