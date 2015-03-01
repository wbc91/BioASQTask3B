package fudan.wbc.phaseA.analyzer;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.util.CharacterUtils;
import org.apache.lucene.analysis.util.CharacterUtils.CharacterBuffer;

import fudan.wbc.phaseA.macro.LuceneVersion;
public class GeneTokenizer extends Tokenizer{
	public GeneTokenizer(AttributeFactory factory, Reader input) {
		super(factory, input);
	}

	public GeneTokenizer(Reader in){
		super(in);
		this.done = false;
	}
	
	private static final int IO_BUFFER_SIZE = 4096;
	
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	
	private boolean done = false;
	
	private char[] ioBuffer = new char[IO_BUFFER_SIZE];
	private char[] words = null;
	
	private int offset = 0, finalOffset = 0;
	private int start = 0;
	private int upto = 0;

	
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
			str = str.replaceAll("[\\.\\:\\;\\,]", " ");
			//heuristic rule 3 
			str = str.replaceAll("\\(([^\\)]*)\\)", "$1");
			str = str.replaceAll("\\[([^\\)]*)\\]", "$1");
			
			//repeat heuristic rule 3 for nested parentheses
			str = str.replaceAll("\\(([^\\)]*)\\)", "$1");
			str = str.replaceAll("\\[([^\\)]*)\\]", "$1");
			
			//heuristic rule 4
			str = str.replaceAll("[\\']", " ");
			str	= str.replaceAll("[\\`]", " ");
			
			//heuristic rule 5
			str = str.replaceAll("[\\'][st] "," ");
			/*
			 * repeat heuristic rule 2 for cases where these symbols were originally followed by other
			 * symbols such as parentheses
			 */
			str = str.replaceAll("[\\.\\:\\;\\,]"," ");
			
			//heuristic rule 6
			str = str.replaceAll("[\\/]+"," ");
			
			//==========================//
			str = str.replaceAll("^\\s+", "");
			str = str.replaceAll("\\s+$", "");
			
			ioBuffer = str.trim().toCharArray();
			upto = str.trim().length();
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
				String tmpWords = String.valueOf(words);
				if(TokenStrategy.bpValue == TokenStrategy.Bp.bp0){
					
				}else if(TokenStrategy.bpValue == TokenStrategy.Bp.bp1){
					while(tmpWords.matches("[^\\(\\)\\[\\]\\-\\_\\/]")){
						
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
	}

}
