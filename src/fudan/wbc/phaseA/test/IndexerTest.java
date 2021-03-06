package fudan.wbc.phaseA.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.junit.Test;

import fudan.wbc.phaseA.analyzer.BioAnalyzer;
import fudan.wbc.phaseA.analyzer.TokenStrategy;
import fudan.wbc.phaseA.indexer.SAXLuceneIndexer;
import fudan.wbc.phaseA.model.MedlineDataBaseGenerator;

public class IndexerTest{
	@Test
	public void createIndexerTest() throws Exception{
		File f = new File("../../../medline/");
		int filecount = 0;
		Set<String>existedPmids = new HashSet<String>();
		SAXLuceneIndexer sli = new SAXLuceneIndexer();
		for(int i = f.listFiles().length-1;i >= 0; i--){
	    	File file = f.listFiles()[i];
	    	filecount++;  
	    	if(filecount == 66)continue;
	        if (filecount > 863) break;
	        System.out.println("file " + (i+1) + " is being indexing");
	        System.out.println(existedPmids.size());
	        FileInputStream inputStream = new FileInputStream(file);
	        sli.indexXML(inputStream,existedPmids);
		}
	}
	@Test
	public void createDatabaseTest() throws Exception{
		File f = new File("../../../medline");
		int filecount = 0;
		Set<String>existedPmids = new HashSet<String>();
		MedlineDataBaseGenerator mdg = new MedlineDataBaseGenerator();
		for(int i = f.listFiles().length-1; i>=0; i--){
			File file = f.listFiles()[i];
			filecount++;
			if(filecount <= 66)continue;
			if(filecount > 863)break;
			System.out.println("xml document "+(i+1)+" is being parsed");
			System.out.println(existedPmids.size());
			FileInputStream inputStream = new FileInputStream(file);
			mdg.createMedlineDatabase(inputStream, existedPmids);
		}
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
//		String input = "(MIP)-1alpha 0.20 20,000 1,2,3,4-TeCDD";
//		String input = "cytosine-5";
//		String input = "0.20 20,000 1,2,3,4-TeCDD";
//		String input = "L-3,5,3',5'-Tetraiodothyronine";
		String input = "O-(4-Hydroxy-3,5-diiodophenyl) 3,5-diiodo-L-tyrosine";
		
		TokenStrategy.bpValue = TokenStrategy.Bp.bp3;
		TokenStrategy.normValue = TokenStrategy.Norm.s;
		TokenStrategy.grk = false;
		TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(input));
		CharTermAttribute ta = tokenStream.getAttribute(CharTermAttribute.class);
		OffsetAttribute oa = tokenStream.getAttribute(OffsetAttribute.class);
		tokenStream.reset();
		while(tokenStream.incrementToken()){
			System.out.println(ta.toString()+" start: "+oa.startOffset()+" end:"+oa.endOffset());
		}
		tokenStream.close();
		
	}
}
