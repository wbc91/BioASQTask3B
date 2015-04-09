package fudan.wbc.phaseA.macro;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Utility {
	public static String join(Object[] array, char separator){
		if(array == null)
			return null;
		int arraySize = array.length;
		int bufSize = (arraySize == 0?0:((array[0] == null?16:array[0].toString().length())+1)*arraySize);
		StringBuffer buf = new StringBuffer(bufSize);
		
		for(int i = 0; i < arraySize; ++i){
			if(i > 0){
				buf.append(separator);
			}
			if(array[i] != null){
				buf.append(array[i]);
			}
		}
		return buf.toString();
	}
	
	private static String url = "jdbc:mysql://localhost:3306/bioasq2015?"
            + "user=root&password=123456&useUnicode=true&characterEncoding=UTF8";
	
	private static Connection conn = null;
	static{
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	public static Map<String,String>replacement = null;
	public static Set<String>unrecognized = null;
	public static Set<String>prohibited = null;
	
	
	public static void initializeConceptSet(){
		unrecognized = new HashSet<String>();
		unrecognized.add("dnmt3");
		unrecognized.add("under-expression");
		unrecognized.add("drug");
		unrecognized.add("oncotype");
		unrecognized.add("dx");
		unrecognized.add("polypill");
		unrecognized.add("clinical");
		unrecognized.add("trial");
		unrecognized.add("off-label");
		unrecognized.add("mammaprint");
		unrecognized.add("vaccinology");
		unrecognized.add("proteomic");
		unrecognized.add("ruler");
		unrecognized.add("gliolan");
		unrecognized.add("dermaroller");
		unrecognized.add("yamanaka");
		unrecognized.add("talen");
		unrecognized.add("scenar");
		unrecognized.add("fec-75");
		unrecognized.add("stupp");
		unrecognized.add("lincrna");
		unrecognized.add("insulator");
		unrecognized.add("foxo");
		unrecognized.add("snord116");
		unrecognized.add("remodeling");
		
		replacement = new TreeMap<String,String>();
		replacement.put("thyronamines", "thyronamine");
		replacement.put("rnas", "rna");
		replacement.put("trials","trial");
		replacement.put("drugs", "drug");
		replacement.put("talens", "talen");
		replacement.put("insulators","insulator");
		replacement.put("foxos", "foxo");
		
		
		prohibited = new HashSet<String>();
		prohibited.add("how");
		prohibited.add("work");
		prohibited.add("do");
		prohibited.add("does");
		prohibited.add("list");
		prohibited.add("have");
		prohibited.add("play");
		prohibited.add("found");
		prohibited.add("use");
		prohibited.add("present");
		prohibited.add("how many");
		prohibited.add("associated with");
		prohibited.add("provide");
		prohibited.add("like");
		prohibited.add("known");
	}
	
	
	public static Connection getConn(){
		return conn;
	}
	
	public final static String mySQLUrl = "jdbc:mysql://localhost:3306/bioasq2015?"
            + "user=root&password=123456&useUnicode=true&characterEncoding=UTF8";
//	public final static String DirName = "BioASQ-trainingDataset2b";
//	public final static String DirName = "BioASQ-trainingDataset3b";
//	public final static String DirName = "BioASQ-task2bPhaseA-testset5";
//	public final static String testDir = "BioASQ-task2bPhaseB-testset5";
	public final static String testDir = "BioASQ-task3bPhaseB-testset3";
	public final static String DirName = "BioASQ-task3bPhaseA-testset3";

	//	public final static String fileDir = "../trainingSet/BioASQ-trainingDataset2b.json";
//	public final static String fileDir = "../trainingSet/BioASQ-task3bPhaseA-testset1.txt";
	public final static String fileDir = "../trainingSet/"+DirName+".txt";
	
	public final static String indexPath = "../TestIndex";
	
	public final static String pubTatorFilesDir = "../QuestionPubTatorPair/"+DirName+"/";
	
	public final static Set<String>stopWords = new HashSet(Arrays.asList("a","about", "above","abs","accordingly",
			"across","after","afterwards","again","against","all","almost",
			"alone","along","already","also","although","always","am","among",
			"amongst","an","analyze","and","another","any", "anyhow", "anyone", 
			"anything", "anywhere", "applicable", "apply", "are", "arise", 
			"around", "as", "assume", "at", "be", "became", "because", "become", 
			"becomes", "becoming", "been", "before", "beforehand", "being", 
			"below", "beside", "besides", "between", "beyond", "both", "but", 
			"by", "came", "can", "cannot", "cc", "cm", "come", "compare", "could", 
			"de", "dealing", "department", "depend", "did", "discover", "dl", 
			"do", "does", "done", "due", "during", "each", "ec", "ed", "effected", 
			"eg", "either", "else", "elsewhere", "enough", "especially", "et", 
			"etc", "ever", "every", "everyone", "everything", "everywhere", 
			"except", "find", "for", "found", "from", "further", "gave", "get", 
			"give", "go", "gone", "got", "gov", "had", "has", "have", "having", 
			"he", "hence", "her", "here", "hereafter", "hereby", "herein", 
			 "hereupon", "hers", "herself", "him", "himself", "his", "how", 
			 "however", "hr", "i", "ie", "if", "ii", "iii", "immediately", 
			 "importance", "important", "in", "inc", "incl", "indeed", "into",
			 "investigate", "is", "it", "its", "itself", "just", "keep", "kept", 
			 "kg", "km", "last", "latter", "latterly", "lb", "ld", "letter", 
			 "like", "ltd", "made", "mainly", "make", "many", "may", "me", 
			 "meanwhile", "mg", "might", "ml", "mm", "mo", "more", "moreover", 
			 "most", "mostly", "mr", "much", "mug", "must", "my", "myself", 
			 "namely", "nearly", "necessarily", "neither", "never", "nevertheless", 
			 "next", "no", "nobody", "noone", "nor", "normally", "nos", "not", 
			 "noted", "nothing", "now", "nowhere", "obtained", "of", "off", 
			 "often", "on", "only", "onto", "or", "other", "others", "otherwise", 
			 "ought", "our", "ours", "ourselves", "out", "over", "overall", 
			 "owing", "own", "oz", "particularly", "per", "perhaps", "pm", "precede", 
			 "predominantly", "present", "presently", "previously", "primarily", 
			 "promptly", "pt", "quickly", "quite", "rather", "readily", "really", 
			 "recently", "refs", "regarding", "relate", "said", "same", "seem", 
			 "seemed", "seeming", "seems", "seen", "seriously", "several", "shall", 
			 "she", "should", "show", "showed", "shown", "shows", "significantly", 
			 "since", "slightly", "so", "some", "somehow", "someone", "something", 
			 "sometime", "sometimes", "somewhat", "somewhere", "soon", "specifically", 
			 "still", "strongly", "studied", "sub", "substantially", "such", "sufficiently", 
			 "take", "tell", "th", "than", "that", "the", "their", "theirs", "them", 
			 "themselves", "then", "thence", "there", "thereafter", "thereby", "therefore", 
			 "therein", "thereupon", "these", "they", "this", "thorough", "those", "though", 
			 "through", "throughout", "thru", "thus", "to", "together", "too", "toward", 
			 "towards", "try", "type", "ug", "under", "unless", "until", "up", "upon", "us", 
			 "use", "used", "usefully", "usefulness", "using", "usually", "various", "very", 
			 "via", "was", "we", "were", "what", "whatever", "when", "whence", "whenever", 
			 "where", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", 
			 "whether", "which", "while", "whither", "who", "whoever", "whom", "whose", "why", 
			 "will", "with", "within", "without", "wk", "would", "wt", "yet", "you", "your", 
			 "yours", "yourself", "yourselves", "yr"));
	
}
