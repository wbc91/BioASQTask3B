package fudan.wbc.phaseA.test;

import java.net.Authenticator;

import org.junit.Test;

import fudan.wbc.phaseA.annotator.BioOntologyAnnotator;
import fudan.wbc.phaseA.bioASQ.ConceptRetrieval;
import fudan.wbc.phaseA.macro.MyAuthenticator;

public class ConceptRetrievalTest {
	@Test
	public void createConceptRetrievalTest() throws Exception{
		ConceptRetrieval cr = new ConceptRetrieval();
		cr.retrieve();
	}

	@Test
	public void createRecommendTest() throws Exception{
		BioOntologyAnnotator boa = new BioOntologyAnnotator();
		String query = "risk+miscarriage+thrombophilia";
		boa.getRecommendation(query);
		
	}
	
	
}
