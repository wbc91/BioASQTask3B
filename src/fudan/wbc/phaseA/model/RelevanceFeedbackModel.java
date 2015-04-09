package fudan.wbc.phaseA.model;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.QueryBuilder;
import org.apache.lucene.util.Version;

import fudan.wbc.phaseA.analyzer.AnalyzerUtils;
import fudan.wbc.phaseA.analyzer.BioAnalyzer;
import fudan.wbc.phaseA.macro.Utility;

public class RelevanceFeedbackModel {
	
	private static Set<String>stopWords = new HashSet(Arrays.asList("a","about", "above","abs","accordingly",
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
	
	
	private static int maxNum = 10000;
	private static AnalyzerUtils au = new AnalyzerUtils();
	public static ArrayList<String> retrieve(String[] queryTerms) throws SQLException, IOException {
		BooleanQuery query = new BooleanQuery();
		QueryBuilder qb = new QueryBuilder(new BioAnalyzer());
		Directory directory = FSDirectory.open(new File(Utility.indexPath));
		
		
		for(int i = 0; i < queryTerms.length; ++i){
			String term = queryTerms[i];
			Query q = qb.createPhraseQuery("Abstract", term);
			if(q!= null) query.add(q, BooleanClause.Occur.MUST);
		}
		
		IndexSearcher searcher = null;
		try {
			directory = FSDirectory.open(new File("../TestIndex"));
			SearcherManager searcherManager = new SearcherManager(directory,null);
			searcher = searcherManager.acquire();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		
		TopDocs tds = searcher.search(query, maxNum);
		if(tds.totalHits == 0)return new ArrayList<String>();
		
		RAMDirectory ramDirectory = reIndex(tds,searcher);
		SearcherManager sm = new SearcherManager(ramDirectory, null);
		IndexSearcher searcher2 = sm.acquire();
		IndexReader indexReader = searcher2.getIndexReader();
		
		HashMap<String, Integer>termOccur = new HashMap<String,Integer>();
		HashMap<String,Double>documentScore =new HashMap<String, Double>();
		
		int matchCount = 0;
		for(ScoreDoc match:tds.scoreDocs){
			if(++matchCount > 100)break;
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
				String tmpTerm = tmpAbstracts[i];
				if(stopWords.contains(tmpTerm) || tmpTerm.equals(""))
					continue;
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
		int size = tds.totalHits;
		if(size > 100) size = 100;
		
		while(occurIt.hasNext()){
			++count;
			String phrase = occurIt.next();
			
			double wtd = 0.0d;
			if(documentScore.containsKey(phrase) && !stopWords.contains(phrase))
				wtd = documentScore.get(phrase);
			double pt = (double)termOccur.get(phrase)/(double)size;
			double pc = 0.0d;
			Query tmpPq = qb.createPhraseQuery("Abstract", phrase);			
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
//			System.out.println(count+"/"+termOccur.keySet().size());
		}
		List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(scores.entrySet());
		Collections.sort(list,new Comparator<Map.Entry<String, Double>>(){
			public int compare(Entry<String,Double>o1, Entry<String,Double>o2){
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		
		
		BooleanQuery query2 = new BooleanQuery();
		for(int i = 0; i < queryTerms.length; ++i){
			String term = queryTerms[i];
			Query q = qb.createPhraseQuery("Abstract", term);
			if(q!= null) query2.add(q, BooleanClause.Occur.SHOULD);
		}
		
		int countExpansionTerm = 0;
		for(Map.Entry<String,Double>mapping:list){
			if(++countExpansionTerm>2)break;
			Query q = qb.createPhraseQuery("Abstract", mapping.getKey());
			if(q!=null)query2.add(q,BooleanClause.Occur.SHOULD);
		}
		
		searcher2.setSimilarity(new LMJelinekMercerSimilarity((float) 0.7));
		TopDocs result = searcher2.search(query2, maxNum);
		ArrayList<String>list2 = new ArrayList<String>();
		
		for(ScoreDoc match:result.scoreDocs){
			Document doc = searcher2.doc(match.doc);
			list2.add("http://www.ncbi.nlm.nih.gov/pubmed/"+doc.get("PMID"));
			if(list2.size() > 100)break;
		}
		
		return list2;
	}
	
	private static RAMDirectory reIndex(TopDocs tds, IndexSearcher searcher) throws IOException {
		Analyzer analyzer = new BioAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_46,analyzer);
		RAMDirectory directory = new RAMDirectory();
		IndexWriter writer = new IndexWriter(directory,iwc);
		double documentSize = tds.totalHits;
		double freqTokenSize = 0.0d;
		
		for(ScoreDoc sd:tds.scoreDocs){
			Document doc = searcher.doc(sd.doc);
			Document docNew = new Document();
			int docLength = 0;
			if(doc.get("PMID")!= null&&doc.get("PMID").length() > 0){
				docNew.add(new Field("PMID",doc.get("PMID"),Field.Store.YES,Field.Index.NOT_ANALYZED_NO_NORMS,Field.TermVector.YES));
			}
			
			if(doc.get("Abstract")!= null&&doc.get("Abstract").length() > 0){
//				text+=" "+doc.get("Abstract");
				docNew.add(new Field("Abstract",doc.get("Abstract"),Field.Store.YES,Field.Index.ANALYZED,Field.TermVector.YES));
			}

			freqTokenSize+=docLength;
			docNew.add(new Field("docLength",docLength+"",Field.Store.YES,Index.NO));
			writer.addDocument(docNew);
		}
		writer.close();
		return directory;
	}
	
}
