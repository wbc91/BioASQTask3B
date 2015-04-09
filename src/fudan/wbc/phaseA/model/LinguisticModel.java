package fudan.wbc.phaseA.model;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.LMSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.QueryBuilder;
import org.apache.lucene.util.Version;

import fudan.wbc.phaseA.analyzer.AnalyzerUtils;
import fudan.wbc.phaseA.analyzer.BioAnalyzer;
import fudan.wbc.phaseA.macro.Utility;

public class LinguisticModel {
	

	public static ArrayList<String> retrieve(String[] queryTerms) throws SQLException, IOException {
		String meshUrl = "jdbc:mysql://localhost:3306/mesh?"
	            + "user=root&password=123456&useUnicode=true&characterEncoding=UTF8";
		String geneUrl = "jdbc:mysql://localhost:3306/entrez_gene?"
	            + "user=root&password=123456&useUnicode=true&characterEncoding=UTF8";
		Connection meshConn = null;
		Connection geneConn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			meshConn = DriverManager.getConnection(meshUrl);
			geneConn = DriverManager.getConnection(geneUrl);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		Directory directory = FSDirectory.open(new File(Utility.indexPath));
		HashSet<String>expansionTermSet = new HashSet<String>();
		
		
		String meshSql = "select * from meshsynonyms where concept = ? or synonyms = ?";
		String geneSql = "select * from gene_info where symbol = ?";
		PreparedStatement meshPreparedStatement = meshConn.prepareStatement(meshSql);
		PreparedStatement genePreparedStatement = geneConn.prepareStatement(geneSql);
		
		BooleanQuery query = new BooleanQuery();
		QueryBuilder qb = new QueryBuilder(new BioAnalyzer());
		ResultSet rs = null;
		for(int i = 0; i < queryTerms.length; ++i){
			HashSet<String>tmpTermSet = new HashSet<String>();
			BooleanQuery subQuery = new BooleanQuery();
			String term = queryTerms[i];
			term = term.trim().toLowerCase();
			meshPreparedStatement.setString(1, term);
			meshPreparedStatement.setString(2, term);
			rs = meshPreparedStatement.executeQuery();
			while(rs.next()){
				tmpTermSet.add(rs.getString("synonyms").trim().toLowerCase());
				tmpTermSet.add(rs.getString("concept").trim().toLowerCase());
			}
			
			//gene
			String meshExistSql = "select * from meshtree where name = ?";
			PreparedStatement meshTreeStatement = meshConn.prepareStatement(meshExistSql);
			meshTreeStatement.setString(1, term);
			rs = meshTreeStatement.executeQuery();
			
			if(rs.next()){
//				String meshId = rs.getString("treenum");
//				String treeSql = "select * from meshtree where treenum REGEXP ?";
//				PreparedStatement meshTreePreparedStatement = meshConn.prepareStatement(treeSql);
//				meshTreePreparedStatement.setString(1, meshId+".[[:alnum:]]{3}$");
//				ResultSet rs1 = meshTreePreparedStatement.executeQuery();
//				while(rs1.next()){
//					tmpTermSet.add(rs1.getString("name").trim().toLowerCase());
//				}
			}
			else{
				genePreparedStatement.setString(1, term);
				rs = genePreparedStatement.executeQuery();
				while(rs.next()){
					String[] geneSyn = rs.getString("synonyms").trim().toLowerCase().split("\\|");
					for(int j = 0; j < geneSyn.length; ++j){
						if(geneSyn[j].equals("-"))continue;
						tmpTermSet.add(geneSyn[j].trim().toLowerCase());
					}
				}
			}
			
			if(tmpTermSet.contains(term))
				tmpTermSet.remove(term);
			expansionTermSet.addAll(tmpTermSet);
			Query q = qb.createPhraseQuery("Abstract", term);
			if(q!= null) subQuery.add(q, BooleanClause.Occur.SHOULD);
			Iterator<String> iter = tmpTermSet.iterator();
			while(iter.hasNext()){
				q = qb.createPhraseQuery("Abstract", iter.next());
				if(q!= null)subQuery.add(q, BooleanClause.Occur.SHOULD);
			}
			
			if(subQuery.clauses().size() > 0)query.add(subQuery,BooleanClause.Occur.MUST);
		}
		
		IndexSearcher searcher = null;
		try {
			directory = FSDirectory.open(new File("../TestIndex"));
			SearcherManager searcherManager = new SearcherManager(directory,null);
			searcher = searcherManager.acquire();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		
		TopDocs tds = searcher.search(query, 10000);
		RAMDirectory ramDirectory = reIndex(tds,searcher);
		SearcherManager sm = new SearcherManager(ramDirectory, null);
		IndexSearcher searcher2 = sm.acquire();
		
//		searcher2.setSimilarity(new BM25Similarity());
		searcher2.setSimilarity(new LMJelinekMercerSimilarity((float) 0.7));
		
		BooleanQuery query2 = new BooleanQuery();
		for(int i = 0; i < queryTerms.length; ++i){
			Query q = qb.createPhraseQuery("Abstract", queryTerms[i]);
			if(q!=null)query2.add(q, BooleanClause.Occur.SHOULD);
		}
		
		Iterator<String>expansionIter = expansionTermSet.iterator();
		while(expansionIter.hasNext()){
			Query q = qb.createPhraseQuery("Abstract", expansionIter.next());
			if(q!=null)query.add(q, BooleanClause.Occur.SHOULD);
		}
		
		TopDocs result = searcher2.search(query2, 10000);
		
		
		ArrayList<String>list = new ArrayList<String>();
		
		for(ScoreDoc match:result.scoreDocs){
			Document doc = searcher2.doc(match.doc);
			list.add("http://www.ncbi.nlm.nih.gov/pubmed/"+doc.get("PMID"));
			if(list.size() > 100)break;
		}
		
		if(meshConn != null)meshConn.close();
		if(geneConn != null)geneConn.close();
		
		return list;
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
