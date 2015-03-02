package fudan.wbc.phaseA.macro;

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
}
