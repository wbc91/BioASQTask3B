package fudan.wbc.phaseA.test;

import org.junit.Test;

import fudan.wbc.phaseA.bioASQ.SubmitGenerator;

public class SubmitTest {
	@Test
	public void createSubmit() throws Exception{
		SubmitGenerator sg = new SubmitGenerator();
		sg.generateSubmitFile();
	}
}
