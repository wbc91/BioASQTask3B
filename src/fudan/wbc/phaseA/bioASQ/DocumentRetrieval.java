package fudan.wbc.phaseA.bioASQ;

import java.io.File;
import java.io.IOException;

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

import fudan.wbc.phaseA.analyzer.BioAnalyzer;

public class DocumentRetrieval {
	private Directory directory = null;
	private IndexSearcher searcher = null;
	public void retrieve(String[] queryTerms) throws IOException{
		//retrieve document by using a query and save a document file
		directory = FSDirectory.open(new File("../TestIndex"));
		SearcherManager sm = new SearcherManager(directory,null);
		searcher = sm.acquire();
		BooleanQuery query = new BooleanQuery();
		QueryBuilder qb = new QueryBuilder(new BioAnalyzer());
		for(int i = 0; i < queryTerms.length; ++i){
			Query q = qb.createPhraseQuery("Abstract", queryTerms[i]);
			query.add(q,BooleanClause.Occur.MUST);
		}
		TopDocs topDocs = searcher.search(query, 10);
		for(ScoreDoc match : topDocs.scoreDocs){
			Explanation explanation = searcher.explain(query, match.doc);
//			System.out.println("---------------");
			System.out.println(searcher.doc(match.doc).get("PMID"));
//			System.out.println(explanation.toString());
		}
		
	}
	
}
