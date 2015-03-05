package fudan.wbc.phaseA.test;

import org.junit.Test;

import fudan.wbc.phaseA.bioASQ.DocumentRetrieval;

public class DocumentRetrievalTest {
	@Test
	public void createDocumentRetrievalTest() throws Exception{
		DocumentRetrieval dr = new DocumentRetrieval();
		dr.retrieve(new String[]{"rheumatoid arthritis","men","women"});
	}
}
