package fudan.wbc.phase.core;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;
import org.json.simple.parser.ParseException;

import fudan.wbc.phaseA.analyzer.BioTokenFilter;
import fudan.wbc.phaseA.model.ConceptExtractor;
import fudan.wbc.phaseA.model.SourceFileAnalyzer;

public class mainLoop {
	public static void main(String[]args) throws Exception{
		Runtime lRuntime = Runtime.getRuntime(); 
		System.out.println("Use  Memory: "+(lRuntime.totalMemory()/1024/1024-lRuntime.freeMemory()/1024/1024)+"M<br/>"); 
		System.out.println("Free  Memory: "+lRuntime.freeMemory()/1024/1024+"M<br/>"); 
		System.out.println("Max   Memory: "+lRuntime.maxMemory()/1024/1024+"M<br/>"); 
		System.out.println("Total Memory: "+lRuntime.totalMemory()/1024/1024+"M<br/>");
		
		
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
