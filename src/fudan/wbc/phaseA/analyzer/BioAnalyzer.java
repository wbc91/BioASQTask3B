package fudan.wbc.phaseA.analyzer;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.pt.PortugueseStemFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.Version;

import fudan.wbc.phaseA.macro.LuceneVersion;

public class BioAnalyzer extends Analyzer{
	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		Tokenizer tokenizer = new BioTokenizer(reader);
		TokenStreamComponents streamComponents = null;
		
		//PubMed stopwords list
		Set<String>stopWords = new HashSet(Arrays.asList("a","about", "above","abs","accordingly",
								"across","after","afterwards","again","against","all","almost",
								"alone","along","already","also","although","always","am","among",
								"amongst","an","analyze","and","another","any", "anyhow", "anyone", 
								"anything", "anywhere", "applicable", "apply", "are", "arise", 
								"around", "as", "assume", "at", "be", "became", "because", "become", 
								"becomes", "becoming", "been", "before", "beforehand", "being", 
								"below", "beside", "besides", "between", "beyond", "both", "but", 
								"by", "came", "can", "cannot", "cc", "cm", "come", "compare", "could", 
								"de", "dealing", "department", "depend", "did", "discover", "dl", 
								"do", "does", "done", "due", "during", "each", "ec", "ed", "effected", 
								"eg", "either", "else", "elsewhere", "enough", "especially", "et", 
								"etc", "ever", "every", "everyone", "everything", "everywhere", 
								"except", "find", "for", "found", "from", "further", "gave", "get", 
								"give", "go", "gone", "got", "gov", "had", "has", "have", "having", 
								"he", "hence", "her", "here", "hereafter", "hereby", "herein", 
								 "hereupon", "hers", "herself", "him", "himself", "his", "how", 
								 "however", "hr", "i", "ie", "if", "ii", "iii", "immediately", 
								 "importance", "important", "in", "inc", "incl", "indeed", "into",
								 "investigate", "is", "it", "its", "itself", "just", "keep", "kept", 
								 "kg", "km", "last", "latter", "latterly", "lb", "ld", "letter", 
								 "like", "ltd", "made", "mainly", "make", "many", "may", "me", 
								 "meanwhile", "mg", "might", "ml", "mm", "mo", "more", "moreover", 
								 "most", "mostly", "mr", "much", "mug", "must", "my", "myself", 
								 "namely", "nearly", "necessarily", "neither", "never", "nevertheless", 
								 "next", "no", "nobody", "noone", "nor", "normally", "nos", "not", 
								 "noted", "nothing", "now", "nowhere", "obtained", "of", "off", 
								 "often", "on", "only", "onto", "or", "other", "others", "otherwise", 
								 "ought", "our", "ours", "ourselves", "out", "over", "overall", 
								 "owing", "own", "oz", "particularly", "per", "perhaps", "pm", "precede", 
								 "predominantly", "present", "presently", "previously", "primarily", 
								 "promptly", "pt", "quickly", "quite", "rather", "readily", "really", 
								 "recently", "refs", "regarding", "relate", "said", "same", "seem", 
								 "seemed", "seeming", "seems", "seen", "seriously", "several", "shall", 
								 "she", "should", "show", "showed", "shown", "shows", "significantly", 
								 "since", "slightly", "so", "some", "somehow", "someone", "something", 
								 "sometime", "sometimes", "somewhat", "somewhere", "soon", "specifically", 
								 "still", "strongly", "studied", "sub", "substantially", "such", "sufficiently", 
								 "take", "tell", "th", "than", "that", "the", "their", "theirs", "them", 
								 "themselves", "then", "thence", "there", "thereafter", "thereby", "therefore", 
								 "therein", "thereupon", "these", "they", "this", "thorough", "those", "though", 
								 "through", "throughout", "thru", "thus", "to", "together", "too", "toward", 
								 "towards", "try", "type", "ug", "under", "unless", "until", "up", "upon", "us", 
								 "use", "used", "usefully", "usefulness", "using", "usually", "various", "very", 
								 "via", "was", "we", "were", "what", "whatever", "when", "whence", "whenever", 
								 "where", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", 
								 "whether", "which", "while", "whither", "who", "whoever", "whom", "whose", "why", 
								 "will", "with", "within", "without", "wk", "would", "wt", "yet", "you", "your", 
								 "yours", "yourself", "yourselves", "yr"));
		CharArraySet bioStopWords = new CharArraySet(LuceneVersion.matchVersion,stopWords,false);
		StopFilter stopFilter = new StopFilter(LuceneVersion.matchVersion, tokenizer, bioStopWords);
		
		streamComponents = new TokenStreamComponents(tokenizer, new PorterStemFilter(stopFilter));
		return streamComponents;
	}	
	
	
	public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException{
		return tokenStream(fieldName,reader);
	}
	
	public TokenStream getTokenStream(String fieldName, Reader reader){
		Tokenizer tokenizer = new LucidTokenizer(reader);
		TokenStream stream = null;
		stream = new LowerCaseFilter(LuceneVersion.matchVersion, tokenizer);
		return stream;
	}
		
}
