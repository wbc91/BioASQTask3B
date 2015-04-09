package fudan.wbc.phaseA.ontologyCall;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import fudan.wbc.phaseA.macro.Utility;

public class ConceptDatabase {
	private BufferedReader br = null;
	private Connection dbconn= null;
	
	public ConceptDatabase(){
		dbconn = Utility.getConn();
	}
	
	public void meshDatabase(){
		PreparedStatement stmt = null;
		try {
			br = new BufferedReader(new FileReader(new File("../mesh_2015.obo")));
			String line = "";
			String id = "";
			String name = "";
			String namespace = "";
			String sql = "insert into mesh (id, namespace, name) values(?,?,?)";
			stmt = dbconn.prepareStatement(sql);
			while((line = br.readLine())!= null){
				if("[Term]".equals(line)){
					if(!id.equals("")){
						stmt.setString(1, id);
						stmt.setString(2, namespace);
						stmt.setString(3, name);
						stmt.execute();
					}
					id = ""; name = ""; namespace = "";
				}
				if(line.startsWith("id:")){
					id = line.substring(3).trim();
				}
				
				if(line.startsWith("name:")){
					name = line.substring(6).trim();
				}
				
				if(line.startsWith("namespace:")){
					namespace = line.substring(10);
				}
				
			}
			
			
			stmt.setString(1, id);
			stmt.setString(2, namespace);
			stmt.setString(3, name);
			stmt.execute();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void uniprotDatabase(){
		String defaultNamespace = "http://www.uniprot.org/uniprot/";
		PreparedStatement stmt = null;
		try{
			br = new BufferedReader(new FileReader(new File("../uniprot_sprot_output.txt")));
			String line = "";
			String id = "";
			String name = "";
			String sql = "insert into uniprot (id, namespace, name) values(?,?,?)";
			stmt = dbconn.prepareStatement(sql);
			br.readLine();
			while((line = br.readLine())!= null){
				String[] tmpArray = new String[2];
				tmpArray = line.split(";")[0].split("\t");
				id = tmpArray[0];
				name = tmpArray[1];
				stmt.setString(1, id);
				stmt.setString(2, defaultNamespace);
				stmt.setString(3, name);
				stmt.execute();
				id = ""; name = "";
				if(line.startsWith("id:")){
					id = line.substring(3).trim();
				}
				
				if(line.startsWith("name:")){
					name = line.substring(6).trim();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void goDatabase(){
		String defaultNamespace = "http://amigo.geneontology.org/cgi-bin/amigo/term_details?term=";
		PreparedStatement stmt = null;
		try{
			int count = 0;
			br = new BufferedReader(new FileReader(new File("../gene_ontology.1_2_changed.obo")));
			String line = "";
			String id = "";
			String name = "";
			String sql = "insert into go (id, namespace, name) values(?,?,?)";
			stmt = dbconn.prepareStatement(sql);
			while((line = br.readLine())!= null){
				if("[Term]".equals(line)){
					count++;
					if(!id.equals("")){
						stmt.setString(1, id);
						stmt.setString(2, defaultNamespace);
						stmt.setString(3, name);
						stmt.execute();
					}
					id = ""; name = "";
				}
				if(line.startsWith("id:")){
					id = line.substring(3).trim();
				}
				
				if(line.startsWith("name:")){
					name = line.substring(6).trim();
				}
			}
			count++;
			stmt.setString(1, id);
			stmt.setString(2, defaultNamespace);
			stmt.setString(3, name);
			stmt.execute();
			assert count == 36741;
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void jochemDatabase(){
		String defaultNamespace = "http://www.biosemantics.org/jochem#";
		PreparedStatement stmt = null;
		try{
			int count = 0;
			br = new BufferedReader(new FileReader(new File("../gene_ontology.1_2_changed.obo")));
			String line = "";
			String id = "";
			String name = "";
			String sql = "insert into jochem (id, namespace, name) values(?,?,?)";
			stmt = dbconn.prepareStatement(sql);
			while((line = br.readLine())!= null){
				if("[Term]".equals(line)){
					count++;
					if(!id.equals("")){
						stmt.setString(1, id);
						stmt.setString(2, defaultNamespace);
						stmt.setString(3, name);
						stmt.execute();
					}
					id = ""; name = "";
				}
				if(line.startsWith("id:")){
					id = line.substring(3).trim();
				}
				
				if(line.startsWith("name:")){
					name = line.substring(6).trim();
				}
			}
			count++;
			stmt.setString(1, id);
			stmt.setString(2, defaultNamespace);
			stmt.setString(3, name);
			stmt.execute();
			assert count == 278579;
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void doDatabase(){
		String defaultNamespace = "http://www.disease-ontology.org/api/metadata/DOID:";
		PreparedStatement stmt = null;
		try{
			int count = 0;
			br = new BufferedReader(new FileReader(new File("../HumanDOcorrected.obo")));
			String line = "";
			String id = "";
			String name = "";
			String sql = "insert into do (id, namespace, name) values(?,?,?)";
			stmt = dbconn.prepareStatement(sql);
			while((line = br.readLine())!= null){
				if("[Term]".equals(line)){
					count++;
					if(!id.equals("")){
						stmt.setString(1, id);
						stmt.setString(2, defaultNamespace);
						stmt.setString(3, name);
						try{
							stmt.execute();
						}catch(SQLException e){
							System.out.println(name);
						}
					}
					id = ""; name = "";
				}
				if(line.startsWith("id: DOID:")){
					id = line.substring(9).trim();
				}
				
				if(line.startsWith("name:")){
					name = line.substring(6).trim();
				}
			}
			count++;
			stmt.setString(1, id);
			stmt.setString(2, defaultNamespace);
			stmt.setString(3, name);
			stmt.execute();
			assert count == 6286;
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		ConceptDatabase cd = new ConceptDatabase();
		cd.uniprotDatabase();
	}
}
