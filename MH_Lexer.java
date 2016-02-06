
// File:   MH_Lexer.java
// Date:   3/11/15
// Marina Kent

// Java template file for lexer component of Informatics 2A Assignment 1 (2015).
// Concerns lexical classes and lexer for the language MH (`Micro-Haskell').

import java.io.* ;

class MH_Lexer extends GenLexer implements LEX_TOKEN_STREAM {
    
    // will accept a variable - a lowercase letter followed by any number of lower/uppercase letters, digits, and apostrophes
    static class VarAcceptor extends GenAcceptor implements DFA {
	public String lexClass() {return "VAR" ;} 
	public int numberOfStates() {return 3 ;} 
	
	boolean accepting (int state) {return (state == 1) ;}
	boolean dead (int state) {return (state == 2) ;}
	
	int nextState (int state, char c) {
	    switch (state) {
            case 0: if (CharTypes.isSmall(c)) return 1; 
                else return 2 ;
            case 1: if (CharTypes.isSmall(c) || CharTypes.isLarge(c) || CharTypes.isDigit(c) || c == '\'') return 1; 
                else return 2;		
            default: return 2;
	    }
	}
    }
    
    // will accept a number - a digit followed by any number of additional digits 
    static class NumAcceptor extends GenAcceptor implements DFA {
	public String lexClass() {return "NUM" ;} 
	public int numberOfStates() {return 2 ;} 
	
	boolean accepting (int state) {return (state == 0);}
	boolean dead (int state) {return (state == 1);}
	
	int nextState (int state, char c) {
	    switch (state) {
            case 0: if (CharTypes.isDigit(c) ) return 0;
		else return 1 ;
            default: return 1;
	    }
	}	
    }
    
    //will accept a Boolean - either True or False 
    static class BooleanAcceptor extends GenAcceptor implements DFA {
	public String lexClass() {return "BOOLEAN" ;} ; 
	public int numberOfStates() {return 9 ;} ;
	
	boolean accepting (int state) {return (state == 8) ;}
	boolean dead (int state) {return (state == 7) ;}
	
	int nextState (int state, char c) {
	    switch (state) {
	    case 0: if (c == 'F') return 1 ;      
		else if (c == 'T') return 5 ;
		else return 7;
	    case 1: if (c == 'a') return 2;
		else return 7;
	    case 2: if (c == 'l') return 3;
		else return 7;
	    case 3: if (c == 's') return 4;
		else return 7;
	    case 4: if (c == 'e') return 8;
		else return 7;
	    case 5: if (c == 'r') return 6;
		else return 7;
	    case 6: if (c == 'u') return 4;
		else return 7;
	    default: return 7 ;
	    }
	}
    }
    
    // will accept a symbol - any of the predefined symbols followed by any number of additional symbols
    static class SymAcceptor extends GenAcceptor implements DFA {
	
	public String lexClass() {return "SYM" ;} ;
	public int numberOfStates() {return 2 ;} ;
	
	boolean accepting (int state) {return (state == 0) ;}
	boolean dead (int state) {return (state == 1) ;}
	
	int nextState (int state, char c) {
	    switch (state) {
            case 0: if (CharTypes.isSymbolic(c) ) return 0; else return 1 ;		
	    default: return 1 ;
	    }
	}
    }
    
    // will accept whitespace - any whitespace character followed by any number of additional whitespace characters
    static class WhitespaceAcceptor extends GenAcceptor implements DFA {
	
	public String lexClass() {return "" ;} ;
	public int numberOfStates() {return 2 ;} ;
	
	boolean accepting (int state) {return (state == 0) ;}
	boolean dead (int state) {return (state == 1) ;}
	
	int nextState (int state, char c) {
	    switch (state) {
            case 0: if (CharTypes.isWhitespace(c)) return 0; else return 1 ;		
	    default: return 1 ;
	    }
	}
    }    
    
    // will accept a comment - two dashes followed by any number of further characters until a newline
    static class CommentAcceptor extends GenAcceptor implements DFA {
	public String lexClass() {return "" ;} 
	public int numberOfStates() {return 5 ;} 
	
	boolean accepting (int state) {return (state == 3) ;}
	boolean dead (int state) {return (state == 4) ;}
	
	int nextState (int state, char c) {
	    switch (state) {
            case 0: if (c=='-') return 1 ; else return  4;
            case 1: if (c=='-') return 3 ; else return  4;
	    case 2: if (!CharTypes.isNewline(c) && !CharTypes.isSymbolic(c)) return 3; else return 4;
            case 3: if (!CharTypes.isNewline(c)) return 3; else return 4;	
	    default: return 4 ;
	    }
	}
    }   
    
    // will accept tokens - these will be defined in the MH_acceptors array
    static class TokAcceptor extends GenAcceptor implements DFA { //allows you to build a DFA int, if, less than, etc. 
	
	String tok ;
	int tokLen ;
	char[] charArray;
	TokAcceptor (String tok) {this.charArray = tok.toCharArray(); this.tok = tok ; tokLen = tok.length() ;}
	
	public String lexClass() {return tok ;} ;
	public int numberOfStates() {return (tokLen + 2) ;} ;
	
	boolean accepting (int state) {return (state == tokLen) ;}
	boolean dead (int state) {return (state == (tokLen + 1)) ;}    
	
	int nextState (int state, char c) {
	    
	    // will go through the entire token 
	    if (state < charArray.length) {
		if (charArray[state] == c) return state + 1;
		else return tokLen + 1;
	    }
	    else return tokLen + 1;
	}
    }
    
    // array of DFAs in prioritized order
    static DFA[] MH_acceptors = new DFA[] {
	new TokAcceptor("Integer"), 
	new TokAcceptor("Bool"), 
	new TokAcceptor("if"), 
	new TokAcceptor("then"), 
	new TokAcceptor("else"), 
	new TokAcceptor("("), 
	new TokAcceptor(")"),
	new TokAcceptor(";"),
	new VarAcceptor(),
	new BooleanAcceptor(),
	new NumAcceptor(),
	new CommentAcceptor(),
	new SymAcceptor(),
	new WhitespaceAcceptor()
    };
    
    MH_Lexer (Reader reader) {
        super(reader,MH_acceptors) ;
    }
}

// for testing
class LexerDemo2 {
    public static void main (String [] args)
	throws LexError, StateOutOfRange, IOException {
        System.out.print ("Lexer: ");
        Reader reader = new BufferedReader (new InputStreamReader (System.in));
        MH_Lexer demoLexer1 = new MH_Lexer (reader);
        LexToken currTok = demoLexer1.pullProperToken ();
        while (currTok != null) {
            System.out.println (currTok.value() + "\t" + currTok.lexClass());
            currTok = demoLexer1.pullProperToken();
        }   
    }
}
