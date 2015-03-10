package fudan.wbc.phaseA.analyzer;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class AnalyzerUtils {
	private String[] phrase = new String[100];
	private int count_ph = 0;
	
	public void reset(){
		phrase = new String[100];
		count_ph = 0;
	}
	public String[] getPhrase(){
		return phrase;
	}
	public int getCount(){
		return count_ph;
	}
	public void displayTokens(Analyzer analyzer, String text) throws IOException{
		displayTokens(analyzer.tokenStream("content", new StringReader(text)));
	}
	public void displayTokens(TokenStream stream) throws IOException{
		CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
		stream.reset();
		while(stream.incrementToken()){
//			System.out.print("["+term.term()+"]");
			phrase[count_ph++] = term.toString();
		}
	}
	public int wordCount(Analyzer analyzer, String text)throws IOException{
		TokenStream stream = analyzer.tokenStream("content", new StringReader(text));
		int count = 0;
		CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
		while(stream.incrementToken()){
//			System.out.println(term.term());
			count++;
		}
		return count;
	}
	public String getStemmingQuery(Analyzer analyzer, String text)throws IOException{
		TokenStream stream = analyzer.tokenStream("content", new StringReader(text));
		CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
		String query = "";
		while(stream.incrementToken()){
			query += term.toString()+" ";
		}
		return query.trim();
	}

}
