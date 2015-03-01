package fudan.wbc.phaseA.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.junit.Test;

import fudan.wbc.phaseA.analyzer.BioAnalyzer;
import fudan.wbc.phaseA.indexer.SAXLuceneIndexer;

public class IndexerTest{
	@Test
	public void createIndexerTest() throws Exception{
		FileInputStream inputStream = new FileInputStream(new File("../trainingText/text.xml"));
		SAXLuceneIndexer sli = new SAXLuceneIndexer();
		sli.indexXML(inputStream);
	}
	
	@Test
	public void createBioTokenizerTest()throws Exception{
		Analyzer analyzer = new BioAnalyzer();
//		String input = "Which virus is Cidofovir (Vistide) indicated for?";
//		String input = "To the ligand of which receptors does Denosumab (Prolia) bind?";
//		String input = "The	 translocation forms a chimeric gene, bcr-abl, which generates BCR-ABL. This fusion protein constitutively activate ABL tyrosine kinase and causes CML. Imatinib mesylate is a selective tyrosine kinase inhibitor on ABL";
//		String input = "MIP-1-alpha";
//		String input = "1,2,3,4-TeCDD!";
//		String input = "((prolia))";
//		String input = "'s 't 123s weqwrt no'";
//		String input = "123 / ";
//		String input = "abc     def";
//		String input = "TrpEb_1";
		String input = "buses";
		
		TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(input));
		CharTermAttribute ta = tokenStream.getAttribute(CharTermAttribute.class);
		OffsetAttribute oa = tokenStream.getAttribute(OffsetAttribute.class);
		tokenStream.reset();
		while(tokenStream.incrementToken()){
			System.out.println(ta.toString()+" start: "+oa.startOffset()+" end:"+oa.endOffset());
		}
	}
}
