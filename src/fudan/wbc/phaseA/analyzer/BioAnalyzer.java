package fudan.wbc.phaseA.analyzer;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.pt.PortugueseStemFilter;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.Version;

import fudan.wbc.phaseA.macro.LuceneVersion;

public class BioAnalyzer extends Analyzer{
	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
//		Tokenizer tokenizer = new BioTokenizer(reader);
		Tokenizer tokenizer = new GeneTokenizer(reader);
		TokenStreamComponents streamComponents = null;
		streamComponents = new TokenStreamComponents(tokenizer);
		return streamComponents;
	}	
	
	
	public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException{
		return tokenStream(fieldName,reader);
	}
	
	public TokenStream getTokenStream(String fieldName, Reader reader){
		Tokenizer tokenizer = new BioTokenizer(reader);
		TokenStream stream = null;
		stream = new LowerCaseFilter(LuceneVersion.matchVersion, tokenizer);
		return stream;
	}
		
}
