package fudan.wbc.phaseA.analyzer;

public class TokenStrategy {
	public enum Bp{
		bp0,bp1,bp2,bp3
	}
	
	public enum Norm{
		h,s,j
	}
	public static boolean grk = false;
	public static Bp bpValue = Bp.bp0;
	public static Norm normValue = Norm.h;
}
