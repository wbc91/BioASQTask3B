package fudan.wbc.phaseA.test;

import fudan.wbc.phaseA.analyzer.*;
import fudan.wbc.phaseA.model.ConceptExtractor;
import fudan.wbc.phaseA.model.SourceFileAnalyzer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


import java.util.Set;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;
import org.junit.Test;

public class AnalyzerTest {
	@Test
	public void createAnalyzerTest() throws IOException{
//		List<String>list = ConceptExtractor.extract();
		final CharArraySet phraseSets = new CharArraySet(Version.LUCENE_46, Arrays.asList(
		        "gray platelet syndrome","platelet syndrome"), false);
		         
		final String input = "which genes have been found mutated in gray platelet syndrome patients";
//		final String input = "gray platelet syndrome";
		

		@SuppressWarnings("deprecation")
		WhitespaceTokenizer wt= new WhitespaceTokenizer(Version.LUCENE_46,new StringReader(input));
		BioTokenFilter btf = new BioTokenFilter(Version.LUCENE_46,wt,phraseSets,false);
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
		Set<String>pList = ConceptExtractor.extract();
		final CharArraySet phraseSet = new CharArraySet(Version.LUCENE_46, pList,false);
		WhitespaceTokenizer wt= null;
		BioTokenFilter btf = null;
		
		PrintWriter pw = new PrintWriter("../tokenizerOutput.txt");
		
		Iterator charIt = phraseSet.iterator();
//		while(charIt.hasNext()){
//			System.out.println((char[])charIt.next());
//		}
		
		String[] questions = SourceFileAnalyzer.parse(new File("../trainingSet/BioASQ-trainingDataset3b.json"));
		for(String input :questions){
			pw.println(input);
			input = input.toLowerCase();
			input = input.replaceAll("[\\pP������������]","");
			wt = new WhitespaceTokenizer(Version.LUCENE_46,new StringReader(input));
			btf = new BioTokenFilter(Version.LUCENE_46,wt,phraseSet,false);
			CharTermAttribute term = btf.addAttribute(CharTermAttribute.class);
			btf.reset();
			
			boolean hasToken = false;
			do{
				hasToken = btf.incrementToken();
				if(hasToken){
					System.out.println("token:'"+term.toString()+"'");
					pw.println("token:'"+term.toString()+"'");
				}
			}while(hasToken);
			pw.println();
		}
		
		pw.close();
		
	}
	
	@Test
	public void createAnsjAnalyzerTest() throws Exception{
		String str = "which genes have been found mutated in gray platelet syndrome patients";
		List<Term>terms = ToAnalysis.paser(str);
		Iterator<Term>termIt = terms.iterator();
		while(termIt.hasNext()){
			Term tmpTerm = (Term)termIt.next();
			String tmpString = tmpTerm.toString();
			System.out.println(tmpString);
		}
		
	}
	
}
