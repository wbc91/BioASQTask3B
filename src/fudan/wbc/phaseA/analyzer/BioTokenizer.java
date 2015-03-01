package fudan.wbc.phaseA.analyzer;

import java.io.Reader;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.Version;

import fudan.wbc.phaseA.macro.LuceneVersion;

public class BioTokenizer extends CharTokenizer{
	
	private static Set<Character> breakChars;
	
	static{
		breakChars = new TreeSet<Character>();
		breakChars.add(';');
		breakChars.add(',');
		breakChars.add('.');
		breakChars.add('?');
		breakChars.add('"');
		breakChars.add('\'');
		breakChars.add('/');
		breakChars.add('\\');
		breakChars.add('=');
		breakChars.add('(');
		breakChars.add(')');
		breakChars.add('[');
		breakChars.add(']');
		
	}
	
	public BioTokenizer(Version matchVersion, AttributeFactory factory,
			Reader input) {
		super(matchVersion, factory, input);
	}

	public BioTokenizer(Reader in){
		super(LuceneVersion.matchVersion, in);
	}
	
	@Override
	protected boolean isTokenChar(int c) {
		char cc = (char)c;
		return !Character.isWhitespace(cc) && !breakChars.contains(cc);
	}

}
