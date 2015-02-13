package fudan.wbc.phaseA.test;

import fudan.wbc.phaseA.analyzer.*;
import fudan.wbc.phaseA.model.ConceptExtractor;
import fudan.wbc.phaseA.model.SourceFileAnalyzer;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import org.junit.Test;

public class AnalyzerTest {
	@Test
	public void createAnalyzerTest() throws IOException{
		List<String>list = ConceptExtractor.extract();
		final CharArraySet phraseSet = new CharArraySet(Version.LUCENE_46, list,false);
		final String input = "Which virus is Cidofovir(Vistide) indicated for";
		@SuppressWarnings("deprecation")
		WhitespaceTokenizer wt= new WhitespaceTokenizer(Version.LUCENE_46,new StringReader(input));
		BioTokenFilter btf = new BioTokenFilter(Version.LUCENE_46,wt,phraseSet,false);
		CharTermAttribute term = btf.addAttribute(CharTermAttribute.class);
		btf.reset();
		
		boolean hasToken = false;
		do{
			hasToken = btf.incrementToken();
			if(hasToken)System.out.println("token:'"+term.toString()+"'");
		}while(hasToken);
	}
	
	
	
	@Test
	public void createParseQuestionTest() throws Exception{
		List<String>list = ConceptExtractor.extract();
		final CharArraySet phraseSet = new CharArraySet(Version.LUCENE_46, list,false);
		WhitespaceTokenizer wt= null;
		BioTokenFilter btf = new BioTokenFilter(Version.LUCENE_46,wt,phraseSet,false);
		
		String[] questions = SourceFileAnalyzer.parse(new File("../trainingSet/BioASQ-trainingDataset3b.json"));
		for(String input :questions){
			wt = new WhitespaceTokenizer(Version.LUCENE_46,new StringReader(input));
			CharTermAttribute term = btf.addAttribute(CharTermAttribute.class);
			btf.reset();
			
			boolean hasToken = false;
			do{
				hasToken = btf.incrementToken();
				if(hasToken)System.out.println("token:'"+term.toString()+"'");
			}while(hasToken);			
		}
		
		
		
	}
	
}
