package fudan.wbc.phaseA.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
//check mac work
public class ConceptExtractor {
	private static BufferedReader reader = null;
	private static ArrayList<String>conceptList = null;
	private static PrintWriter pw = null;
	
	public static List<String>extract() throws IOException{
		reader = new BufferedReader(new FileReader(new File("../dict.txt")));
		conceptList = new ArrayList<String>();
		String tmpConcept = "";
		while((tmpConcept = reader.readLine())!= null){
			if(tmpConcept != null && tmpConcept != "")
				conceptList.add(tmpConcept);
		}
		
		return conceptList;
	}

	protected static void WriteConceptDict() throws FileNotFoundException,
			IOException {
		File fileList = new File("../PubTator");
		pw = new PrintWriter(new File("../dict.txt"));
		for(File file:fileList.listFiles()){
			long startTime=System.currentTimeMillis();
			if(file.getName().equals("chemical2pubtator")){
				writeConceptListToFile(file);
				long endTime=System.currentTimeMillis();
				System.out.println("chemical cost:"+(endTime-startTime)+"ms");
			}
			else if(file.getName().equals("disease2pubtator")){
				writeConceptListToFile(file);
				long endTime=System.currentTimeMillis();
				System.out.println("disease cost:"+(endTime-startTime)+"ms");
			}
			else if(file.getName().equals("gene2pubtator")){
				writeConceptListToFile(file);
				long endTime=System.currentTimeMillis();
				System.out.println("gene cost:"+(endTime-startTime)+"ms");
			}
			else if(file.getName().equals("mutation2pubtator")){
				writeConceptListToFile(file);
				long endTime=System.currentTimeMillis();
				System.out.println("mutation cost:"+(endTime-startTime)+"ms");
			}
			else if(file.getName().equals("species2pubtator")){
				writeConceptListToFile(file);
				long endTime=System.currentTimeMillis();
				System.out.println("species cost:"+(endTime-startTime)+"ms");
			}
		}
		pw.close();
	}
	
	private static void writeConceptListToFile(File file) throws IOException{
		reader = new BufferedReader(new FileReader(file));
		String tmpString = "";
		String[] concepts = null;
		while((tmpString = reader.readLine())!= null){
			concepts = tmpString.split("\t")[2].split("\\|");
			for(String concept:concepts){
				if(concept.equals("Mentions"))continue;
				pw.println(concept);
//				conceptList.add(concept);
			}
			concepts = null;
		}
		reader.close();
	}
	
	public static void main(String[] args){
		try {
			WriteConceptDict();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
