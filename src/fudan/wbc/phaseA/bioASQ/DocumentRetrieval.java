package fudan.wbc.phaseA.bioASQ;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Authenticator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import fudan.wbc.phaseA.analyzer.AnalyzerUtils;
import fudan.wbc.phaseA.analyzer.BioAnalyzer;
import fudan.wbc.phaseA.annotator.BioOntologyAnnotator;
import fudan.wbc.phaseA.annotator.PubTator;
import fudan.wbc.phaseA.macro.MyAuthenticator;
import fudan.wbc.phaseA.macro.Utility;
import fudan.wbc.phaseA.model.LinguisticModel;
import fudan.wbc.phaseA.model.PureQueryExpansionModel;
import fudan.wbc.phaseA.model.RelevanceFeedbackModel;
import fudan.wbc.phaseA.model.SourceFileAnalyzer;

public class DocumentRetrieval {
	private SourceFileAnalyzer sfa = SourceFileAnalyzer.getInstance();
	private Directory directory = null;
	private IndexSearcher searcher = null;
	private SearcherManager searcherManager;
	private ArrayList<String>relevantDocuments = null;
	private FileWriter documentWriter = null;
	private JSONArray documentArray = null;
	private Map<String, Double>passage2Score = new HashMap<String, Double>();
	private Map<String, Integer>passage2Pmid = new HashMap<String, Integer>();
	private Map<String, HashSet<String>>passage2Terms = new HashMap<String, HashSet<String>>();
	private static final int TCOUNT = 1;
	private CountDownLatch latch = new CountDownLatch(TCOUNT);
	
//	static{
//		//proxy
//		System.setProperty("http.proxyHost", "proxy.fudan.edu.cn");
//    	System.setProperty("http.proxyPort", "8080");
//    	System.setProperty("https.proxyHost", "proxy.fudan.edu.cn");
//    	System.setProperty("https.proxyPort", "8080");
//    	System.setProperty("ftp.proxyHost", "proxy.fudan.edu.cn");
//    	System.setProperty("ftp.proxyPort", "8080");
//    	System.setProperty("socksProxyHost", "proxy.fudan.edu.cn");
//    	System.setProperty("socksProxyPort", "8080");
//    	Authenticator.setDefault(new MyAuthenticator("12210240068","11456181"));
//    	
//    	//proxy end
//	}
	
	
	private double termIdf(int df, int max){
		assert df < max;
		return Math.log(max/(df+0.5));
	}
	
	private double wordSimilarity(String term1, String term2){
		if(term1.equals(term2)) return 1.0;
		else return 0.0;
	}
	
	public Object getJSONArray(){
		return this.documentArray;
	}
	
	public void retrieve() throws Exception{
		ExecutorService service = Executors.newFixedThreadPool(TCOUNT);
		Utility.initializeConceptSet();
		sfa.parse(new File(Utility.fileDir));
		JSONArray questionList = sfa.getQuestionArray();
		int packageLength = questionList.size()/TCOUNT;
		int start = 0;
		int end = 0;
		for(int i = 0; i < TCOUNT; ++i){
			if(i == TCOUNT-1)end = questionList.size();
			else end+=packageLength;
			service.execute(new DocumentMultiRetrieval(start, end, questionList));
			start = end;
		}
		latch.await();
		System.out.println("done!"); 
	}
	class DocumentMultiRetrieval implements Runnable{
		private int begin;
		private int end;
		private JSONArray questionList;
		
		DocumentMultiRetrieval(int begin, int end, JSONArray questionList){
			this.begin = begin;
			this.end = end;
			this.questionList = questionList;
		}
		
		public void run(){
			BioOntologyAnnotator boa = new BioOntologyAnnotator();
			for(int i = begin; i < end; ++i){
				JSONObject question = (JSONObject)questionList.get(i);
				String questionBody = (String)question.get("body");
				String questionId = (String)question.get("id");
//				if(!questionId.equals("52bf1d3c03868f1b0600000d")){
//					continue;
//				}
				boa.setQuestionId(questionId);
				try {
					File file = new File("../dataSet/"+Utility.DirName+"/document/"+questionId+".json");
//					long time = file.lastModified();
//					if(System.currentTimeMillis()-time < 21600000)continue;
					documentWriter = new FileWriter(file);
				} catch (IOException e) {
				
				}
				
				HashSet<String>termSet = null;
				try {
//					termSet = questionBodyPreprocess(boa, questionBody);
					termSet = new HashSet<String>();
				} catch (Exception e1) {
					e1.printStackTrace();
				} 
				System.out.println(questionBody+": ");
				String[] queryTerms = new String[termSet.size()];
				
				Iterator<String>termIter = termSet.iterator();
				int index = 0;
				while(termIter.hasNext()){
					queryTerms[index++] = termIter.next();
				}
				
				//retrieve document by using a query and save a document file
				try {
					directory = FSDirectory.open(new File("../TestIndex"));
					searcherManager = new SearcherManager(directory,null);
					searcher = searcherManager.acquire();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				try{
					//BM25 only
					relevantDocuments = PureQueryExpansionModel.retrieve(questionBody);
					
					//Synonym and Hypernyms
//					relevantDocuments = LinguisticModel.retrieve(queryTerms);
					//pseudo relevant feedback
//					relevantDocuments = RelevanceFeedbackModel.retrieve(queryTerms);
					//local
//					localPassageModel(queryTerms, questionId);
					//global
//					retrieve(queryTerms,questionId);
				}catch(Exception e){
					e.printStackTrace();
				}
				//write Files
				JSONObject documentObject = new JSONObject();
				JSONArray documentArray = new JSONArray();
				documentArray.addAll(relevantDocuments);
				relevantDocuments = null;
//				documentObject.put("documents", documentArray);
//				documentObject.put("id", questionId);
				try{
					documentWriter.write(documentArray.toJSONString());
					documentWriter.flush();
					documentWriter.close();
				}catch(Exception e){
					e.printStackTrace();
				}
		
			}
		}

		protected HashSet<String> questionBodyPreprocess(BioOntologyAnnotator boa,
				String questionBody) throws IOException, ParseException,
				ParserConfigurationException, SAXException {
			String tmp = questionBody.trim().replaceAll("\n", " ");
			tmp = tmp.replaceAll("\\?", " ");
			tmp = tmp.replaceAll("\"", "");
			String[] words = tmp.split(" ");
			
			for(int j = 0; j < words.length; ++j){
				words[j] = words[j].toLowerCase();
				if(Utility.replacement.containsKey(words[j])){
					words[j] = Utility.replacement.get(words[j]);
				}
			}
			tmp = Utility.join(words, '+');
			boa.getAnnotation(tmp);
			HashSet<String>termSet = (HashSet<String>) boa.getTermSet();
			for(int j = 0; j < words.length; ++j){
				if(Utility.unrecognized.contains(words[j]) && !termSet.contains(words[j])){
					termSet.add(words[j]);
				}
				else if(Utility.prohibited.contains(words[j]) && termSet.contains(words[j])){
					termSet.remove(words[j]);
				}
			}
			return termSet;
		}
		
		
		public void retrieve(String[] queryTerms,String questionId) {
			passage2Score = new HashMap<String, Double>();
			passage2Pmid = new HashMap<String, Integer>();
			passage2Terms = new HashMap<String, HashSet<String>>();
			
			
			BooleanQuery query = new BooleanQuery();
			query.setMinimumNumberShouldMatch(queryTerms.length-1);
			QueryBuilder qb = new QueryBuilder(new BioAnalyzer());
			for(int i = 0; i < queryTerms.length; ++i){
				Query q = qb.createPhraseQuery("Abstract", queryTerms[i]);
				if(q!=null)query.add(q,BooleanClause.Occur.SHOULD);
			}
			
			TopDocs topDocs = null;
			try {
				topDocs = searcher.search(query, 800);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			passageModel(queryTerms, questionId, topDocs);
			
		}
		
		protected void localPassageModel(String[] queryTerms,String questionId){
			passage2Score = new HashMap<String, Double>();
			passage2Pmid = new HashMap<String, Integer>();
			passage2Terms = new HashMap<String, HashSet<String>>();
			
			PubTator pt = new PubTator();
			pt.setId(questionId);
			pt.parseDocuments();
			HashSet<String>pmidSet = pt.getPmidSet();
			calculatePassageScore(queryTerms, questionId, pmidSet, pt);
		}
		
		protected void passageModel(String[] queryTerms, String questionId,
				TopDocs topDocs) {
			HashSet<String>pmidSet = new HashSet<String>();
			PubTator pt = new PubTator();
			try{
				
				for(ScoreDoc match : topDocs.scoreDocs){
					String tmpPmid = searcher.doc(match.doc).get("PMID");
					pmidSet.add(tmpPmid);
				}
				pt.setId(questionId);
				pt.parseDocuments(pmidSet);
			}catch(Exception e){
				System.out.println("search failed for question: "+questionId);
				for(int qterm = 0; qterm < queryTerms.length; ++qterm){
					System.out.print(queryTerms[qterm]);
					if(qterm != queryTerms.length-1)System.out.print(" ");
				}
			}
			calculatePassageScore(queryTerms, questionId, pmidSet, pt);
		}

		protected void calculatePassageScore(String[] queryTerms,
				String questionId, HashSet<String> pmidSet, PubTator pt) {
			Iterator<String>pmidSetIter = pmidSet.iterator();
			IndexReader indexReader = searcher.getIndexReader();
			AnalyzerUtils au = new AnalyzerUtils();
			String exceptionPassage = "";
			String tmpPmid = "";
			try {
				while(pmidSetIter.hasNext()){
					tmpPmid = pmidSetIter.next();
					passage2Terms = pt.retrievePassages(tmpPmid);
					Iterator<String>passageIter = passage2Terms.keySet().iterator();
					while(passageIter.hasNext()){
						String tmpPassage = passageIter.next();
						exceptionPassage = tmpPassage;
						passage2Pmid.put(tmpPassage, Integer.parseInt(tmpPmid));
						Set<String>terms = passage2Terms.get(tmpPassage);
						if(terms == null)continue;
						Iterator<String>termIter = terms.iterator();
						double passageScore = 0.0d;
						while(termIter.hasNext()){
							String tmpTerm = termIter.next();
							tmpTerm = tmpTerm.replaceAll("\"", "");
							tmpTerm = tmpTerm.toLowerCase();
							au.reset();
							au.displayTokens(new BioAnalyzer(), tmpTerm);
							double idf = 0.0d;
							String[] tmpTerms = au.getPhrase();
							for(int k = 0; k < tmpTerms.length; k++){
								if(tmpTerms[k] == null)break;
								idf += termIdf(indexReader.docFreq(new Term("Abstract",tmpTerms[k])),indexReader.maxDoc());
							}
							for(int k = 0; k < queryTerms.length; k++){
								passageScore += idf*wordSimilarity(tmpTerm, queryTerms[k]);
							}
						}
						
						passage2Score.put(tmpPassage, passageScore);
					}
				}
			}catch(Exception e){
				System.out.println(tmpPmid+": "+exceptionPassage);
				e.printStackTrace();
			}
			
			
			
			//sorting
			List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(passage2Score.entrySet());
			Collections.sort(list,new Comparator<Map.Entry<String, Double>>(){
				public int compare(Entry<String,Double>o1, Entry<String,Double>o2){
					return o2.getValue().compareTo(o1.getValue());
				}
			});
			
			if(relevantDocuments == null){
				relevantDocuments = new ArrayList<String>();
			}
			Set<Integer>pmids = new HashSet<Integer>();
			for(Map.Entry<String,Double>mapping:list){
				int sortedPmid = passage2Pmid.get(mapping.getKey());
				if(!pmids.contains(sortedPmid)){
					pmids.add(sortedPmid);
					if(pmids.size() > 10)break;
					relevantDocuments.add("http://www.ncbi.nlm.nih.gov/pubmed/"+String.valueOf(sortedPmid));
				}
			}
			System.out.println("relevant documents size for question: "+questionId+" is:" +relevantDocuments.size());
		}
	}
}
