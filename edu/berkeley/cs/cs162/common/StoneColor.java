package edu.berkeley.cs.cs162.common;

public enum StoneColor {
    BLACK, WHITE, NONE;
}

class sc{
	public static String toString(StoneColor color){
		if( color == StoneColor.BLACK )
			return "Black";
		else if( color == StoneColor.WHITE )
			return "White";
		else
			return "None";
	}
}
