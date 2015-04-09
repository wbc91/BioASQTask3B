package fudan.wbc.phaseA.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;
import org.junit.Test;

import fudan.wbc.phaseA.analyzer.AnalyzerUtils;
import fudan.wbc.phaseA.analyzer.BioAnalyzer;
import fudan.wbc.phaseA.analyzer.TokenStrategy;

@SuppressWarnings("deprecation")
public class ThesisTest {
	
	private static Directory directory = null;
	private static IndexReader indexReader = null;
	private AnalyzerUtils au = new AnalyzerUtils();
	private HashSet<String>pmidSet = new HashSet<String>();
	private String query = "Yamanaka factors";
	private IndexSearcher searcher = null;
	private Set<String>stopWords = new HashSet(Arrays.asList("a","about", "above","abs","accordingly",
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
	
	
	static{
		try {
			directory = FSDirectory.open(new File("../TestIndex"));
			indexReader = IndexReader.open(directory);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	@Test
	public void createSimpleSearchTest() throws IOException{
		SearcherManager sm = new SearcherManager(directory,null);
		searcher = sm.acquire();
		QueryBuilder qBuilder = new QueryBuilder(new BioAnalyzer());
		Query query = qBuilder.createPhraseQuery("Abstract", "vierbuchen");
		TopDocs docs = searcher.search(query, 1);
		System.out.println(docs.totalHits);
	}
	
	
	@Test
	public void createTest() throws IOException{
		SearcherManager sm = new SearcherManager(directory,null);
		searcher = sm.acquire();
		
		pmidSet = new HashSet<String>(Arrays.asList("25468557","25232932","25513856","25209165","25523810","25457955","25473442","25410286","25173869","25248676"
												   ,"24841084","25150886","24370212","24150221","24706886","24553769","24083387","24531722","24449888","24486105"
												   ,"25170299","24411172","25408883","24838880","24744003","24740298","24406535","24378537","23939864","24129109"
												   ,"24080286","24014886","24483317","23773820","23750005","23104133","23612755","23653361","23797497","23212180"));
		Iterator<String>iterator = pmidSet.iterator();
		
		QueryBuilder qBuilder = new QueryBuilder(new BioAnalyzer());
		BooleanQuery bQuery = new BooleanQuery();
		while(iterator.hasNext()){
			String tmp = iterator.next();
			Query q = qBuilder.createPhraseQuery("PMID", tmp);
			bQuery.add(q, BooleanClause.Occur.SHOULD);
		}
		TopDocs docs = searcher.search(bQuery,1000);
		HashMap<String, Integer>termOccur = new HashMap<String,Integer>();
		HashMap<String,Double>documentScore =new HashMap<String, Double>();
		for(ScoreDoc match:docs.scoreDocs){
			String tmpAbstract = searcher.doc(match.doc).get("Abstract");
			tmpAbstract = tmpAbstract.replaceAll("[\\!\\\"\\#\\$\\%\\&\\*\\<\\=\\>\\?\\@\\\\\\|]", " ");
			tmpAbstract = tmpAbstract.replaceAll("[\\.\\:\\;\\,] ", " ");
			tmpAbstract = tmpAbstract.replaceAll(" \\(([^\\)]*)\\) ", " $1 ");
			tmpAbstract = tmpAbstract.replaceAll(" \\[([^\\)]*)\\] ", " $1 ");
			
			tmpAbstract = tmpAbstract.replaceAll(" \\(([^\\)]*)\\) ", " $1 ");
			tmpAbstract = tmpAbstract.replaceAll(" \\[([^\\)]*)\\] ", " $1 ");
			
			tmpAbstract = tmpAbstract.replaceAll(" [\\']", " ");
			tmpAbstract	= tmpAbstract.replaceAll("[\\`] ", " ");
			
			tmpAbstract = tmpAbstract.replaceAll("[\\'][st] "," ");
			tmpAbstract = tmpAbstract.replaceAll("[\\.\\:\\;\\,] "," ");
			
			tmpAbstract = tmpAbstract.replaceAll("[\\/]+ "," ");
			
			tmpAbstract = tmpAbstract.replaceAll("^\\s+", "");
			tmpAbstract = tmpAbstract.replaceAll("\\s+$", "");
			tmpAbstract = tmpAbstract.toLowerCase();
			String[] tmpAbstracts = tmpAbstract.split(" ");
			double maxWeight = 0.0;
			
			HashMap<String,Double>perDocumentScore = new HashMap<String,Double>();
			HashSet<String> expansionTerms = new HashSet<String>();
			for(int i = 0;i < tmpAbstracts.length; ++i){
				if(expansionTerms.contains(tmpAbstracts[i])){
					double tmpScore = perDocumentScore.get(tmpAbstracts[i]);
					tmpScore+=1.0d;
					if(tmpScore > maxWeight)maxWeight = tmpScore;
					perDocumentScore.put(tmpAbstracts[i], tmpScore);
				}
				else {
					expansionTerms.add(tmpAbstracts[i]);
					perDocumentScore.put(tmpAbstracts[i], 1.0d);
					if(1.0d > maxWeight)maxWeight = 1.0d;
				}
			}
			
			Iterator<String>perDocIt = perDocumentScore.keySet().iterator();
			while(perDocIt.hasNext()){
				String tmpTerm = (String)perDocIt.next();
				double perScore = 1.0d+0.3*perDocumentScore.get(tmpTerm)/maxWeight;
				if(documentScore.keySet().contains(tmpTerm)){
					double tolScore = documentScore.get(tmpTerm);
					tolScore+=perScore;
					documentScore.put(tmpTerm, tolScore);
				}
				else{
					documentScore.put(tmpTerm, perScore);
				}
			}
			
			Iterator<String>expansionIt = expansionTerms.iterator();
			while(expansionIt.hasNext()){
				String expansionTerm = expansionIt.next();
				if(termOccur.containsKey(expansionTerm)){
					int occurrence = termOccur.get(expansionTerm);
					++occurrence;
					termOccur.put(expansionTerm, occurrence);
				}else{
					termOccur.put(expansionTerm, 1);
				}
			}
		}
		
		HashMap<String, Double>scores = new HashMap<String, Double>();
		Iterator<String>occurIt = termOccur.keySet().iterator();
		
		int count = 0;
		
		
		while(occurIt.hasNext()){
			++count;
			String phrase = occurIt.next();
			
			double wtd = 0.0d;
			if(documentScore.containsKey(phrase) && !stopWords.contains(phrase))
				wtd = documentScore.get(phrase);
			double pt = (double)termOccur.get(phrase)/(double)pmidSet.size();
			double pc = 0.0d;
			Query tmpPq = qBuilder.createPhraseQuery("Abstract", phrase);			
			int df = 0;
			if(tmpPq == null){
				df = indexReader.maxDoc()-1;
				System.out.println(phrase);
			}
			else {
				au.reset();
				au.displayTokens(new BioAnalyzer(), phrase);
				int length = 0;
				String[] tmpQueryTerms = au.getPhrase();
				for(int j = 0; j < tmpQueryTerms.length; ++j){
					if(tmpQueryTerms[j]!=null)length++;
					else break;
				}
				
				if(length > 1)
					df = searcher.search(tmpPq, 1).totalHits;
				else 
					df = indexReader.docFreq(new Term("Abstract",au.getPhrase()[0]));
			}
			pc = ((double)df+0.5)/(double)indexReader.maxDoc();
			
//			KLD
//			double score = pt*Math.log(pt/pc);
			
//			Chi-square
//			double score = (pt-pc)*(pt-pc)/pc;
			
			//BIM
//			double score = Math.log(pt*(1.0-pc)/(pc*(1.0-pt)));
			
			//Rocchio
//			double score = wtd;
			
			//RSV
			double score = wtd*(pt-pc);
			scores.put(phrase, score);
			System.out.println(count+"/"+termOccur.keySet().size());
		}
		
		
		List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(scores.entrySet());
		Collections.sort(list,new Comparator<Map.Entry<String, Double>>(){
			public int compare(Entry<String,Double>o1, Entry<String,Double>o2){
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		
		for(Map.Entry<String,Double>mapping:list){
			System.out.println(mapping.getKey()+" : "+mapping.getValue());
		}
	}
}
