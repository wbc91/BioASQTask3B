package fudan.wbc.phaseA.test;

import fudan.wbc.phaseA.analyzer.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;


import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;
import org.junit.Test;

public class AnalyzerTest {
	@Test
	public void createAnalyzerTest() throws IOException{
		final CharArraySet phraseSet = new CharArraySet(Version.LUCENE_46, Arrays.asList( "virus", "Cidofovir Vistide"),false);
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
}
