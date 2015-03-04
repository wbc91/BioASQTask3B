package fudan.wbc.phaseA.test;

import java.io.File;

import org.junit.Test;

import fudan.wbc.phaseA.annotator.BioOntologyAnnotator;
import fudan.wbc.phaseA.model.SourceFileAnalyzer;

public class AnnotatorTest {
	@Test
	public void createAnnotationTest() throws Exception{
		String[] questionList = SourceFileAnalyzer.parse(new File("../trainingSet/BioASQ-trainingDataset3b.json"));
		BioOntologyAnnotator boa = new BioOntologyAnnotator();
		for(int i = 0; i < questionList.length; ++i){
			String tmp = questionList[i].replaceAll(" ", "+");
			tmp = tmp.replaceAll("\n", "+");
			boa.getAnnotation(tmp);
			System.out.println();
		}
	}
}
