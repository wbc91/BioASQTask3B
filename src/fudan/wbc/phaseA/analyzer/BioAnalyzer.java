package fudan.wbc.phaseA.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

class BioAnalyzer extends Analyzer{

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		return null;
	}
		
}
