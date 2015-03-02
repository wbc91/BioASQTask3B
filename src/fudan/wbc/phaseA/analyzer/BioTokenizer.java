package fudan.wbc.phaseA.analyzer;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import fudan.wbc.phaseA.macro.Utility;

public class BioTokenizer extends Tokenizer{
	public BioTokenizer(AttributeFactory factory, Reader input) {
		super(factory, input);
	}

	public BioTokenizer(Reader in){
		super(in);
		this.done = false;
	}
	
	private static final int IO_BUFFER_SIZE = 4096;
	private ArrayList<String> stemmedWordList = null;
	
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	
	private boolean done = false;
	
	private char[] ioBuffer = new char[IO_BUFFER_SIZE];
	private char[] words = null;
	private int offset = 0, finalOffset = 0;
	private int start = 0, lastIndex = 0;
	private int upto = 0;
	

	private static Map<String,String>greek = null;
	
	static{
		greek = new TreeMap<String, String>();
		
		greek.put("alpha", "a");
		greek.put("beta", "b");
		greek.put("gamma", "g");
		greek.put("delta", "d");
		greek.put("epsilon", "e");
		greek.put("zeta", "z");
		greek.put("eta", "e");
		greek.put("theta", "th");
		greek.put("iota", "i");
		greek.put("kappa", "k");
		greek.put("lambda", "l");
		greek.put("mu", "m");
		greek.put("nu", "n");
		greek.put("xi", "x");
		greek.put("omicron", "o");
		greek.put("pi", "p");
		greek.put("rho", "r");
		greek.put("sigma", "s");
		greek.put("tau", "t");
		greek.put("upsilon", "u");
		greek.put("phi", "ph");
		greek.put("chi", "ch");
		greek.put("psi", "ps");
		greek.put("omega", "o");
		
	}
	@Override
	public boolean incrementToken() throws IOException {
		clearAttributes();
		char[] buffer = termAtt.buffer();
		if(!done){
			done = true;
			upto = 0;
			start = 0;
			if(ioBuffer == null)
				ioBuffer = new char[IO_BUFFER_SIZE];
			while(true){
				final int length = input.read(ioBuffer, upto, ioBuffer.length-upto);
				if(length == -1)
					break;
				upto += length;
				if(upto == ioBuffer.length)
					resizeIOBuffer(upto*2);
			}
			String str = String.valueOf(ioBuffer);
			//heuristic rule 1
			str = str.replaceAll("[\\!\\\"\\#\\$\\%\\&\\*\\<\\=\\>\\?\\@\\\\\\|]", " ");
			//heuristic rule 2
			str = str.replaceAll("[\\.\\:\\;\\,] ", " ");
			//heuristic rule 3 
			str = str.replaceAll(" \\(([^\\)]*)\\) ", " $1 ");
			str = str.replaceAll(" \\[([^\\)]*)\\] ", " $1 ");
			
			//repeat heuristic rule 3 for nested parentheses
			str = str.replaceAll(" \\(([^\\)]*)\\) ", " $1 ");
			str = str.replaceAll(" \\[([^\\)]*)\\] ", " $1 ");
			
			//heuristic rule 4
			str = str.replaceAll(" [\\']", " ");
			str	= str.replaceAll("[\\`] ", " ");
			
			//heuristic rule 5
			str = str.replaceAll("[\\'][st] "," ");
			/*
			 * repeat heuristic rule 2 for cases where these symbols were originally followed by other
			 * symbols such as parentheses
			 */
			str = str.replaceAll("[\\.\\:\\;\\,] "," ");
			
			//heuristic rule 6
			str = str.replaceAll("[\\/]+ "," ");
			
			//==========================//
			str = str.replaceAll("^\\s+", "");
			str = str.replaceAll("\\s+$", "");
			
			ioBuffer = str.trim().toCharArray();
			upto = str.trim().length();
		}
		
		if(!(stemmedWordList == null || stemmedWordList.size() == 0)){
			words = handleStemmedWords();
			System.arraycopy(words, 0, buffer, 0, words.length);
			termAtt.setLength(words.length);
			offsetAtt.setOffset(correctOffset(offset), finalOffset = correctOffset(offset+words.length-1));
			offset += words.length;
			return true;
		}
		
		if(start < upto){
			//to get words and increase variable "start" in case of the Whitespace
			words = getWordsAndModifyIndex(upto);
			if(words != null){
				if(words.length >= buffer.length-1){
					buffer = termAtt.resizeBuffer(words.length+2);
				}
				
				//==========================//
				//split the token by break points
				StringBuffer tmpWords = new StringBuffer();
				String[] subwords = null;
				Pattern pattern = null;
				Matcher matcher = null;
				if(TokenStrategy.bpValue == TokenStrategy.Bp.bp0){
					subwords = String.valueOf(words).split(" ");
				}else{
					if(TokenStrategy.bpValue == TokenStrategy.Bp.bp1){
						pattern = Pattern.compile("[^\\(\\)\\[\\]\\-\\_\\/]+");
					}else if(TokenStrategy.bpValue == TokenStrategy.Bp.bp2){
						pattern = Pattern.compile("[A-Za-z0-9]+");
					}else if(TokenStrategy.bpValue == TokenStrategy.Bp.bp3){
						pattern = Pattern.compile("([A-Z][a-z]+)|([A-Z]+)|([a-z]+)|([0-9]+)");
					}
					matcher = pattern.matcher(String.valueOf(words));
					
					
					while(matcher.find()){
						String rep = matcher.group();
						tmpWords.append(rep);
						tmpWords.append(" ");
					}
					subwords = tmpWords.toString().trim().split(" ");
					//transform to lowercase
					
				}
				
				for(int i = 0; i < subwords.length; ++i){
					if(subwords.equals(""))break;
					subwords[i] = subwords[i].toLowerCase();
				}
				
				//Greek alphabet normalization
				if(TokenStrategy.grk){
					pattern = Pattern.compile("[a-z]+");
					for(int i = 0; i < subwords.length; ++i){
						matcher = pattern.matcher(subwords[i]);
						tmpWords =  new StringBuffer();
						while(matcher.find()){
							String rep = matcher.group();
							String grkRep = greek.get(rep);
							if(grkRep != null){
								matcher.appendReplacement(tmpWords, grkRep);
							}else{
								matcher.appendReplacement(tmpWords, rep);
							}
						}
						matcher.appendTail(tmpWords);
						subwords[i] = tmpWords.toString();
					}
				}
				
				//nomalize breakpoints
				
				if(TokenStrategy.bpValue == TokenStrategy.Bp.bp0){
					words = subwords[0].toCharArray();
				}else {
					if(TokenStrategy.normValue == TokenStrategy.Norm.h){
						words = Utility.join(subwords, '-').toCharArray();
					}else if(TokenStrategy.normValue == TokenStrategy.Norm.s){
						stemmedWordList = new ArrayList<String>();
						for(int i = 0; i < subwords.length;++i)
							stemmedWordList.add(subwords[i]);
						words = handleStemmedWords();
					}else if(TokenStrategy.normValue == TokenStrategy.Norm.j){
						words = Utility.join(subwords, '\0').toCharArray();
					}
				}
				
				System.arraycopy(words, 0, buffer, 0, words.length);
				termAtt.setLength(words.length);
				offsetAtt.setOffset(correctOffset(offset), finalOffset = correctOffset(offset+words.length-1));
				offset += words.length;
				return true;
			}
		}
		
		return false;
	}
	
	private char[] getWordsAndModifyIndex(int end) {
		StringBuilder word = new StringBuilder();
		while(start < end){
			if(ioBuffer[start] == ' '){
				if(word.length() == 0){
					++start;
					continue;
				}
				return word.toString().toCharArray();
			}
			word.append(ioBuffer[start]);
			++start;
		}
		if(word.length() > 0){
			return word.toString().toCharArray();
		}
		
		word = null;
		return null;
	}
	
	private char[] handleStemmedWords(){
		return stemmedWordList.remove(0).toCharArray();
	}

	private void resizeIOBuffer(int newSize) {
		if(ioBuffer.length < newSize){
			final char[] newCharBuffer = new char[newSize];
			System.arraycopy(ioBuffer, 0, newCharBuffer, 0, ioBuffer.length);
			ioBuffer = newCharBuffer;
		}
	}

	@Override
	public void end() throws IOException{
		super.end();
		offsetAtt.setOffset(finalOffset, finalOffset);
	}
	
	@Override
	public void reset() throws IOException{
		super.reset();
		ioBuffer =null;
		offset = 0;
		finalOffset = 0;
		done = false;
	}

}
