package fudan.wbc.phaseA.bioASQ;

import java.io.File;
import java.io.FileNotFoundException;
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
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import fudan.wbc.phaseA.analyzer.AnalyzerUtils;
import fudan.wbc.phaseA.analyzer.BioAnalyzer;
import fudan.wbc.phaseA.annotator.BioOntologyAnnotator;
import fudan.wbc.phaseA.macro.Utility;
import fudan.wbc.phaseA.model.SourceFileAnalyzer;

public class DocumentRetrieval {
	private Directory directory = null;
	private IndexSearcher searcher = null;
	private Connection conn = null;
	private PreparedStatement stmt = null;
	private final static String url = Utility.mySQLUrl;
	private SearcherManager searcherManager;
	private Map<String, Double>sentence2Score = new HashMap<String, Double>();
	private Map<String, Integer>sentence2Pmid = new HashMap<String,Integer>();
	
	private static Map<String,String>replacement = null;
	private static Set<String>unrecognized = null;
	
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
	
	public void retrieve() throws Exception{
		String[] questionList = SourceFileAnalyzer.parse(new File(Utility.fileDir));
		BioOntologyAnnotator boa = new BioOntologyAnnotator();
		for(int i = 0; i < questionList.length; ++i){
			Set<String>otherWords = new HashSet<String>();
			String tmp = questionList[i].trim().replaceAll("\n", " ");
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
			System.out.println(questionList[i]+": ");
			boa.getAnnotation(tmp);
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
			retrieve(queryTerms);
		}
	}
	
	public void retrieve(String[] queryTerms) throws IOException{
		sentence2Pmid = new HashMap<String,Integer>();
		sentence2Score = new HashMap<String, Double>();
		//retrieve document by using a query and save a document file
		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url);
		}catch(SQLException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		directory = FSDirectory.open(new File("../TestIndex"));
		searcherManager = new SearcherManager(directory,null);
		searcher = searcherManager.acquire();
		BooleanQuery query = new BooleanQuery();
		QueryBuilder qb = new QueryBuilder(new BioAnalyzer());
		for(int i = 0; i < queryTerms.length; ++i){
			Query q = qb.createPhraseQuery("Abstract", queryTerms[i]);
			query.add(q,BooleanClause.Occur.MUST);
		}
		String sql = "SELECT pmid,abstract FROM medline WHERE pmid = ?";
		TopDocs topDocs = searcher.search(query, 100);
		try {
			stmt = conn.prepareStatement(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(ScoreDoc match : topDocs.scoreDocs){
//			Explanation explanation = searcher.explain(query, match.doc);
//			System.out.println("---------------");
			int tmpPmid = Integer.parseInt(searcher.doc(match.doc).get("PMID"));
			IndexReader indexReader = searcher.getIndexReader();
			AnalyzerUtils au = new AnalyzerUtils();
			try {
				stmt.setInt(1, tmpPmid);
				ResultSet rs = stmt.executeQuery();
				while(rs.next()){
					String tmpAbstract = rs.getString("abstract");
					Set<String>removeEndSet = new HashSet<String>(Arrays.asList(".","!","?","\n",";"));
					String[]sentences = tmpAbstract.replaceAll("[\\.\\:\\;\\?\\!\\\n] ","######").split("######");
					for(int i = 0; i < sentences.length; ++i){
						if(i == sentences.length-1){
							Iterator it = removeEndSet.iterator();
							while(it.hasNext()){
								String endChar = (String)it.next();
								if(sentences[i].endsWith(endChar)){
									sentences[i] = sentences[i].substring(0,sentences[i].length()-1-endChar.length());
								}
							}
						}
						if(sentences[i].trim().length() > 0){
							double sentenceScore = 0.0d;
							sentence2Pmid.put(sentences[i], tmpPmid);
							BioOntologyAnnotator.getAnnotation(sentences[i].trim().replaceAll(" ", "+"));
							String[] terms = BioOntologyAnnotator.getTerms();
							for(int j = 0; j < terms.length; ++j){
								au.displayTokens(new BioAnalyzer(), terms[i]);
								double idf = 0.0d;
								String[] tmpTerms = au.getPhrase();
								for(int k = 0; j < tmpTerms.length; k++){
									idf += termIdf(indexReader.docFreq(new Term("Abstract",tmpTerms[k])),indexReader.maxDoc());
								}
								for(int k = 0; k < queryTerms.length; k++){
									sentenceScore += idf*wordSimilarity(terms[j], queryTerms[k]);
								}
							}
							sentence2Score.put(sentences[i],sentenceScore);	
						}
					}
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			catch (ParseException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
		}
		//sorting
		List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(sentence2Score.entrySet());
		Collections.sort(list,new Comparator<Map.Entry<String, Double>>(){
			public int compare(Entry<String,Double>o1, Entry<String,Double>o2){
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		
		Set<Integer>pmids = new HashSet<Integer>();
		for(Map.Entry<String,Double>mapping:list){
			int sortedPmid = sentence2Pmid.get(mapping.getKey());
			if(!pmids.contains(sortedPmid)){
				System.out.println(sortedPmid);
				pmids.add(sortedPmid);
			}
		}
		
		
	}
	
}
