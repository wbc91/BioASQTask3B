package fudan.wbc.phaseA.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;

import org.junit.Test;

import fudan.wbc.phaseA.bioASQ.SubmitGenerator;

public class SubmitTest {
	@Test
	public void createSubmit() throws Exception{
		SubmitGenerator sg = new SubmitGenerator();
		sg.generateSubmitFile();
	}
	
	@Test
	public void createCleanTextTest() throws Exception{
		File f = new File("../dataSet/BioASQ-task3bPhaseA-testset2/concept");
		for(File file:f.listFiles()){
			PrintWriter pw = new PrintWriter("../temp/"+file.getName());
			InputStream inputStream = new FileInputStream(file);
			StringBuffer sb = new StringBuffer();
			int tempByte;
			while((tempByte = inputStream.read())!= -1){
				char ch = (char)tempByte;
				if(ch != '[' && ch != ']')sb.append(ch);
			}
			String result = "["+sb.toString()+"]";
			pw.print(result);
			pw.close();
		}
	}
}
