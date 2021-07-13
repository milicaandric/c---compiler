###
# This Makefile can be used to make a scanner for the C-- language
# (Yylex.class) and to make a program that tests the scanner (P2.class).
#
# The default makes both the scanner and the test program.
#
# make clean removes all generated files.
#
# Note: P2.java will not compile unless Yylex.class exists.
#
###

# define the java compiler to be used and the flags
JC = javac
FLAGS = -g -cp $(CP) 
CP = ./deps:.

P2.class: P2.java Yylex.class sym.class
	$(JC) $(FLAGS) P2.java

Yylex.class: cminusminus.jlex.java ErrMsg.class sym.class
	$(JC) $(FLAGS) cminusminus.jlex.java

cminusminus.jlex.java: cminusminus.jlex sym.class
	java -cp $(CP) JLex.Main cminusminus.jlex

sym.class: sym.java
	$(JC) $(FLAGS) sym.java

ErrMsg.class: ErrMsg.java
	$(JC) $(FLAGS) ErrMsg.java

	
###
# testing - add more here to run your tester and compare its results
# to expected results
###
test:
	java -cp $(CP) P2
	diff allTokens.in allTokens.out
	diff stringLiterals.out stringLiterals_expected.out
	diff integerLiterals.out integerLiterals_expected.out
	diff allSymbols.out allSymbols_expected.out
	diff anyIdentifiers.out anyIdentifiers_expected.out

###
# clean up
###

clean:
	rm -f *~ *.class cminusminus.jlex.java

cleantest:
	rm -f allTokens.out
	rm -f stringLiterals.out
	rm -f integerLiterals.out
	rm -f allSymbols.out
	rm -f anyIdentifiers.out
