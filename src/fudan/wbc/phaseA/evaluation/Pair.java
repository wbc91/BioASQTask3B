package fudan.wbc.phaseA.evaluation;

public class Pair {
	String doc;
	String offset;
	public Pair(){
		
	}
	public Pair(String doc, String offset){
		this.doc = doc;this.offset = offset; 
	}
	@Override
	public boolean equals(Object obj){
		if(this == obj)return true;
		if(obj == null)return false;
		if(getClass()!=obj.getClass())return false;
		Pair pair = (Pair)obj;
		return (this.doc.equals(pair.doc)&&this.offset.equals(pair.offset));
	}
	
	@Override
	public int hashCode(){
		int result = 17;
		result = 37*result +this.doc.hashCode();
		result = 37*result +this.offset.hashCode();
		return result;
	}
}
