package fudan.wbc.phaseA.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;

import fudan.wbc.phaseA.analyzer.AnalyzerUtils;
import fudan.wbc.phaseA.analyzer.BioAnalyzer;
import fudan.wbc.phaseA.macro.Utility;

public class PureQueryExpansionModel {
	private static AnalyzerUtils au = new AnalyzerUtils();
	public static ArrayList<String> retrieve(String question) throws Exception{
		HashSet<String> termSet = tokenGram(question);
		Directory directory = FSDirectory.open(new File(Utility.indexPath));
		QueryBuilder qb = new QueryBuilder(new BioAnalyzer());
		au.reset();
		Iterator<String>termIter =termSet.iterator();
		BooleanQuery query = new BooleanQuery();
		SearcherManager searcherManager = new SearcherManager(directory,null);
		IndexSearcher searcher = searcherManager.acquire();
		while(termIter.hasNext()){
			String term = termIter.next();
			Query q = qb.createPhraseQuery("Abstract", term);
			if(q!= null) query.add(q, BooleanClause.Occur.SHOULD);
		}
		
//		searcher.setSimilarity(new BM25Similarity());
		searcher.setSimilarity(new LMJelinekMercerSimilarity((float) 0.7));
		TopDocs tds = searcher.search(query, 100);
		
		ArrayList<String>list = new ArrayList<String>();
		HashSet<String>pmidSet = new HashSet<String>();
		for(ScoreDoc match:tds.scoreDocs){
			Document doc = searcher.doc(match.doc);
			if(pmidSet.contains(doc.get("PMID")))continue;
			list.add("http://www.ncbi.nlm.nih.gov/pubmed/"+doc.get("PMID"));
			pmidSet.add(doc.get("PMID"));
			if(pmidSet.size() == 10)break;
		}
		return list;
	}
	private static HashSet<String> tokenGram(String input){
		input = input.replaceAll("[\\!\\\"\\#\\$\\%\\&\\*\\<\\=\\>\\?\\@\\\\\\|]", " ");
		input = input.replaceAll("[\\.\\:\\;\\,] ", " ");
		input = input.replaceAll(" \\(([^\\)]*)\\) ", " $1 ");
		input = input.replaceAll(" \\[([^\\)]*)\\] ", " $1 ");
		
		input = input.replaceAll(" \\(([^\\)]*)\\) ", " $1 ");
		input = input.replaceAll(" \\[([^\\)]*)\\] ", " $1 ");
		
		input = input.replaceAll(" [\\']", " ");
		input	= input.replaceAll("[\\`] ", " ");
		
		input = input.replaceAll("[\\'][st] "," ");
		input = input.replaceAll("[\\.\\:\\;\\,] "," ");
		
		input = input.replaceAll("[\\/]+ "," ");
		
		input = input.replaceAll("^\\s+", "");
		input = input.replaceAll("\\s+$", "");
		
		HashSet<String>tokens = new HashSet<String>();
		
		String[] inputs = input.split(" ");
		String[] filteredInputs = new String[inputs.length];
		int index = 0;
		for(int  i = 0; i < inputs.length; ++i){
			if(!Utility.stopWords.contains(inputs[i])){
				filteredInputs[index++] = inputs[i];
			}
		}
		
		for(int i = 0; i < index; ++i){
			if(filteredInputs[i] == null)break;
			for(int j = 1; j <= 3; ++j){
				StringBuffer sb = new StringBuffer();
				switch(j){
					case 1:{
						tokens.add(filteredInputs[i]);
						break;
					}
					case 2:{
						if(i+1 < index){
							sb.append(filteredInputs[i]);
							sb.append(" ");
							sb.append(filteredInputs[i+1]);
							tokens.add(sb.toString());
						}
						break;
					}
					case 3:{
						if(i+2 < index){
							sb.append(filteredInputs[i]);
							sb.append(" ");
							sb.append(filteredInputs[i+1]);
							sb.append(" ");
							sb.append(filteredInputs[i+2]);
							tokens.add(sb.toString());
						}
					}
					default:break;
				}
			}
		}
		
		return tokens;
	}
}
