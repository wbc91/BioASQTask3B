package fudan.wbc.phaseA.bioASQ;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Explanation;
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
import fudan.wbc.phaseA.macro.Utility;
import fudan.wbc.phaseA.model.SourceFileAnalyzer;

public class DocumentRetrieval {
	private SourceFileAnalyzer sfa = SourceFileAnalyzer.getInstance();
	private Directory directory = null;
	private IndexSearcher searcher = null;
	private Connection conn = null;
	private PreparedStatement stmt = null;
	private final static String url = Utility.mySQLUrl;
	private SearcherManager searcherManager;
	
	private ArrayList<String>relevantDocuments = null;
	
	private FileWriter documentWriter = null;
	private JSONArray documentArray = null;
	
	private Map<String, Double>passage2Score = new HashMap<String, Double>();
	private Map<String, Integer>passage2Pmid = new HashMap<String, Integer>();
	private Map<String, HashSet<String>>passage2Terms = new HashMap<String, HashSet<String>>();
	
	private static Map<String,String>replacement = null;
	private static Set<String>unrecognized = null;
	
	private static final int TCOUNT = 1;
	private CountDownLatch latch = new CountDownLatch(TCOUNT);
	
	
	
	static{
		unrecognized = new HashSet<String>();
		unrecognized.add("dnmt3");
		unrecognized.add("under-expression");
		unrecognized.add("drug");
	}
	
	static{
		replacement = new TreeMap<String,String>();
		replacement.put("thyronamines", "thyronamine");
	}
	
	private double termIdf(int df, int max){
		assert df < max;
		return Math.log(max/(df+0.5));
	}
	
	private double wordSimilarity(String term1, String term2){
		if(term1.endsWith(term2)) return 1.0;
		else return 0.0;
	}
	
	public Object getJSONArray(){
		return this.documentArray;
	}
	
	public void retrieve() throws Exception{
		ExecutorService service = Executors.newFixedThreadPool(TCOUNT);
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
				Set<String>otherWords = new HashSet<String>();
				JSONObject question = (JSONObject)questionList.get(i);
				String questionBody = (String)question.get("body");
				String questionId = (String)question.get("id");
				boa.setQuestionId(questionId);
				try {
					documentWriter = new FileWriter(new File("../dataSet/"+Utility.DirName+"/document/"+questionId+".json"));
				} catch (IOException e) {
				
				}
				String tmp = questionBody.trim().replaceAll("\n", " ");
				tmp = tmp.replaceAll("\\?", " ");
				String[] words = tmp.split(" ");
				for(int j = 0; j < words.length; ++j){
					if(replacement.containsKey(words[j].toLowerCase())){
						words[j] = replacement.get(words[j].toLowerCase()).replaceAll(" ","+");
					}else if(unrecognized.contains(words[j].toLowerCase())){
						otherWords.add(words[j].toLowerCase());
					}
					
				}
				tmp = Utility.join(words, '+');
				System.out.println(questionBody+": ");

				try {
					boa.getAnnotation(tmp);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				}
				
				String[] annotatedTerms = boa.getTerms();
				String[] queryTerms = new String[annotatedTerms.length+otherWords.size()];
				int index = 0;
				for(int j = 0; j < annotatedTerms.length; ++j){
					queryTerms[index++] = annotatedTerms[j];
				}
				Iterator<String>iter = otherWords.iterator();
				while(iter.hasNext()){
					queryTerms[index++] = (String)iter.next();
				}
				
				try{
					retrieve(queryTerms,questionId);
				}catch(Exception e){
					e.printStackTrace();
				}
				//write Files
				JSONObject documentObject = new JSONObject();
				JSONArray documentArray = new JSONArray();
				documentArray.addAll(relevantDocuments);
				documentObject.put("documents", documentArray);
				documentObject.put("id", questionId);
				try{
					documentWriter.write(documentObject.toJSONString());
					documentWriter.flush();
					documentWriter.close();
				}catch(Exception e){
					e.printStackTrace();
				}
		
			}
		}
		public void retrieve(String[] queryTerms,String questionId) {
			passage2Score = new HashMap<String, Double>();
			passage2Pmid = new HashMap<String, Integer>();
			passage2Terms = new HashMap<String, HashSet<String>>();
			//retrieve document by using a query and save a document file
			try {
				directory = FSDirectory.open(new File("../TestIndex"));
				searcherManager = new SearcherManager(directory,null);
				searcher = searcherManager.acquire();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			BooleanQuery query = new BooleanQuery();
			QueryBuilder qb = new QueryBuilder(new BioAnalyzer());
			for(int i = 0; i < queryTerms.length; ++i){
				Query q = qb.createPhraseQuery("Abstract", queryTerms[i]);
				query.add(q,BooleanClause.Occur.SHOULD);
			}
			
			TopDocs topDocs = null;
			HashSet<String>pmidSet = new HashSet<String>();
			PubTator pt = new PubTator();
			try{
				topDocs = searcher.search(query, 800);
				
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
						String tmpPassage = (String)passageIter.next();
						exceptionPassage = tmpPassage;
						passage2Pmid.put(tmpPassage, Integer.parseInt(tmpPmid));
						Set<String>terms = passage2Terms.get(tmpPassage);
						Iterator<String>termIter = terms.iterator();
						double passageScore = 0.0d;
						while(termIter.hasNext()){
							String tmpTerm = (String)termIter.next();
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
					System.out.println(sortedPmid);
					pmids.add(sortedPmid);
					if(pmids.size() > 100)break;
					relevantDocuments.add("http://www.ncbi.nlm.nih.gov/pubmed/"+String.valueOf(sortedPmid));
				}
			}
			
		}

		
	}
	
	
		
}
