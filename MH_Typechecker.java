
// File:   MH_Typechecker.java
// Date:   3/11/15
// Marina Kent

// Java template file for typechecker component of Informatics 2A Assignment 1.

import java.util.* ;
import java.io.* ;

class MH_Typechecker {
    
    static MH_Parser MH_Parser = MH_Type_Impl.MH_Parser ;    
    static MH_TYPE IntegerType = MH_Type_Impl.IntegerType ;
    static MH_TYPE BoolType = MH_Type_Impl.BoolType ;
    
    // compute type will determine the type of a given expression and make sure it is well typed and does not return conflicting types
    static MH_TYPE computeType (MH_EXP exp, TYPE_ENV env) throws TypeError, UnknownVariable {
	
	//checks if type is VAR. This one is simple - if it is a VAR, will return that type.
    	if (exp.isVAR()) { 
	    return env.typeOf(exp.value());     
    	} 
	
    	// will check if type is NUM. If each char is a digit, will return type is Integer. Otherwise will throw error. 
    	else if (exp.isNUM()) {
	    
	    //create an array to check all of the chars given 
	    char [] charArray = (exp.value()).toCharArray();
	    
	    for (int i = 0; i < (charArray.length); i++) {
		if (charArray[i] != '0' && 
		    charArray[i] != '1' &&
		    charArray[i] != '2' &&
		    charArray[i] != '3' &&
		    charArray[i] != '4' &&
		    charArray[i] != '5' &&
		    charArray[i] != '6' &&
		    charArray[i] != '7' &&
		    charArray[i] != '8' &&
		    charArray[i] != '9') {
		    throw new TypeError ("Non digit character found in integer expression: " + charArray[i]);
		}
		return IntegerType;
	    }    	
	} 
	
    	// will check if type is Boolean. If the type is not boolean will throw an error.
    	else if (exp.isBOOLEAN()) {
	    if ((exp.value()).equals("True") || (exp.value()).equals("False")) {
		return BoolType;
	    }
	    throw new TypeError ("Non Boolean symbol found in Boolean expression: " + exp.value());	
    	} 
	
    	// will check if type is an application. This is when a function is applied to an argument, so we need to check different parts of the expression to make sure it is well typed.
    	else if (exp.isAPP()) {

	    // there are two types to compare - the first and second parts of the argument
	    MH_TYPE type1 = computeType(exp.first(), env);
	    MH_TYPE type2 = computeType(exp.second(), env);
	    
	    // first type must be arrow, as that is what arguments look like in Haskell
	    if (!type1.isArrow()) {
		throw new TypeError ("First expression is not an arrow: " + type1);
	    }
	    
	    // make sure the expression is well typed
	    if ((type1.left()).equals (type2)) {
		return type1.right();
	    }
	    throw new TypeError ("There are two different types: " + type1.left() + " and " + type2);
	}  
	
    	// will check the types of infix expressions. 
    	else if (exp.isINFIX()) {
	    
	    // infix expressions have two arguments to be checked
	    MH_TYPE type1 = computeType (exp.first(), env);
	    MH_TYPE type2 = computeType (exp.second(), env);
	    
	    // infix expressions are used with two integers - must check to make sure both types are Integer
	    if (!(type1.equals(IntegerType) && type2.equals(IntegerType))) {
		throw new TypeError ("Non integers in expression: " + exp.infixOp());
	    }
	    
	    // if the two arguments are being compared, will return a Boolean type 
	    if (exp.infixOp() == "==" || exp.infixOp() == "<=") {
		return BoolType;
	    } 
	    
	    // if the two arguments are being added or subtracted, will return another Integer type
	    else if (exp.infixOp() == "+" || exp.infixOp() == "-") {
		return IntegerType;
	    } 
	    
	    // if not returning a Boolean or Integer, is incorrectly typed - throws error.
	    throw new TypeError ("Incorrect infixOp used: " + exp.infixOp());
     	} 
	
     	// checks to see if expression is if/else statment
     	else if (exp.isIF()) {
	    
	    // if/else statements have three parts.
	    MH_TYPE type1 = computeType (exp.first(), env);
	    MH_TYPE type2 = computeType (exp.second(), env);
	    MH_TYPE type3 = computeType (exp.third(), env);
	    
	    // first part of if/else statement must be a Boolean. If not, throws error.
	    if (!(type1.equals(BoolType))) {
		throw new TypeError ("Statment must begin with BoolType. Instead begins with: " + type1);
	    }
	    
	    // second and third parts of if/else statment must be of same type.
	    if (type2.equals(type3)) {
		return type2;
	    }
	    throw new TypeError ("Types two and three must be of same type. Instead are: " + type2 + " and " + type3);
    	} 
	
    	// will throw error if can not pattern match the type
	throw new TypeError ("Could not match type.");
    }
    
    // Type environments:   
    interface TYPE_ENV {
	MH_TYPE typeOf (String var) throws UnknownVariable ;
    }
    
    static class MH_Type_Env implements TYPE_ENV {
	
	TreeMap env ;
	
	public MH_TYPE typeOf (String var) throws UnknownVariable {
	    MH_TYPE t = (MH_TYPE)(env.get(var)) ;
	    if (t == null) throw new UnknownVariable(var) ;
	    else return t ;
	}

	// Constructor for cloning a type env
	MH_Type_Env (MH_Type_Env given) {
	    this.env = (TreeMap)given.env.clone() ;
	}

	// Constructor for building a type env from the type decls 
	// appearing in a program
	MH_Type_Env (TREE prog) throws DuplicatedVariable {
	    this.env = new TreeMap() ;
	    TREE prog1 = prog ;
	    while (prog1.getRhs() != MH_Parser.epsilon) {
		TREE typeDecl = prog1.getChildren()[0].getChildren()[0] ;
		String var = typeDecl.getChildren()[0].getValue() ;
		MH_TYPE theType = MH_Type_Impl.convertType 
		    (typeDecl.getChildren()[2]);
		if (env.containsKey(var)) 
		    throw new DuplicatedVariable(var) ;
		else env.put(var,theType) ;
		prog1 = prog1.getChildren()[1] ;
	    }
	    System.out.println ("Type conversions successful.") ;
	}

	// Augmenting a type env with a list of function arguments.
	// Takes the type of the function, returns the result type.
	MH_TYPE addArgBindings (TREE args, MH_TYPE theType) 
	    throws DuplicatedVariable, TypeError {
	    TREE args1=args ;
	    MH_TYPE theType1 = theType ;
	    while (args1.getRhs() != MH_Parser.epsilon) {
		if (theType1.isArrow()) {
		    String var = args1.getChildren()[0].getValue() ;
		    if (env.containsKey(var)) {
			throw new DuplicatedVariable(var) ;
		    } else {
			this.env.put(var, theType1.left()) ;
			theType1 = theType1.right() ;
			args1 = args1.getChildren()[1] ;
		    }
		} else throw new TypeError ("Too many function arguments");
	    } ;
	    return theType1 ;
	}
    }

    static MH_Type_Env compileTypeEnv (TREE prog) 
	throws DuplicatedVariable{
	return new MH_Type_Env (prog) ;
    }

    // Building a closure (using lambda) from argument list and body
    static MH_EXP buildClosure (TREE args, MH_EXP exp) {
	if (args.getRhs() == MH_Parser.epsilon) 
	    return exp ;
	else {
	    MH_EXP exp1 = buildClosure (args.getChildren()[1], exp) ;
	    String var = args.getChildren()[0].getValue() ;
	    return new MH_Exp_Impl (var, exp1) ;
	}
    }

    // Name-closure pairs (result of processing a TermDecl).
    static class Named_MH_EXP {
	String name ; MH_EXP exp ;
	Named_MH_EXP (String name, MH_EXP exp) {
	    this.name = name; this.exp = exp ;
	}
    }

    static Named_MH_EXP typecheckDecl (TREE decl, MH_Type_Env env) 
	throws TypeError, UnknownVariable, DuplicatedVariable,
	       NameMismatchError {
    // typechecks the given decl against the env, 
    // and returns a name-closure pair for the entity declared.
	String theVar = decl.getChildren()[0].getChildren()[0].getValue();
	String theVar1= decl.getChildren()[1].getChildren()[0].getValue();
	if (!theVar.equals(theVar1)) 
	    throw new NameMismatchError(theVar,theVar1) ; 
	MH_TYPE theType = 
	    MH_Type_Impl.convertType (decl.getChildren()[0].getChildren()[2]) ;
	MH_EXP theExp =
	    MH_Exp_Impl.convertExp (decl.getChildren()[1].getChildren()[3]) ;
	TREE theArgs = decl.getChildren()[1].getChildren()[1] ;
	MH_Type_Env theEnv = new MH_Type_Env (env) ;
	MH_TYPE resultType = theEnv.addArgBindings (theArgs, theType) ;
	MH_TYPE expType = computeType (theExp, theEnv) ;
	if (expType.equals(resultType)) {
	    return new Named_MH_EXP (theVar,buildClosure(theArgs,theExp));
	}
	else throw new TypeError ("RHS of declaration of " +
				  theVar + " has wrong type") ;
    }

    static MH_Exp_Env typecheckProg (TREE prog, MH_Type_Env env)
	throws TypeError, UnknownVariable, DuplicatedVariable,
	       NameMismatchError {
	TREE prog1 = prog ;
	TreeMap treeMap = new TreeMap() ;
	while (prog1.getRhs() != MH_Parser.epsilon) {
	    TREE theDecl = prog1.getChildren()[0] ;
	    Named_MH_EXP binding = typecheckDecl (theDecl, env) ;
	    treeMap.put (binding.name, binding.exp) ;
	    prog1 = prog1.getChildren()[1] ;
	}
	System.out.println ("Typecheck successful.") ;
	return new MH_Exp_Env (treeMap) ;
    }

    // For testing:

    public static void main (String[] args) throws Exception {
	Reader reader = new BufferedReader (new FileReader (args[0])) ;
	// try {
	    LEX_TOKEN_STREAM MH_Lexer = 
		new CheckedSymbolLexer (new MH_Lexer (reader)) ;
	    TREE prog = MH_Parser.parseTokenStream (MH_Lexer) ;
	    MH_Type_Env typeEnv = compileTypeEnv (prog) ;
	    MH_Exp_Env runEnv = typecheckProg (prog, typeEnv) ;
	// } catch (Exception x) {
        //  System.out.println ("MH Error: " + x.getMessage()) ;
	// }
    }
}
