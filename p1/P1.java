import java.util.ArrayList;
import java.util.HashMap;
import java.io.FileNotFoundException;

/*
 * main file: P1.java
 * other files: Sym.java, SymTable.java, DuplicateSymException.java, EmptySymTableException.java
 *
 * author: Milica Andric
 * email: andric@wisc.edu
 * cs login: milica 
 *
 * This is a class whose sole purpose is to test the SymTable class, which provides the following
 * operations:
 *
 * no-arg constructor -- initialize the SymTable's List field to contain a single, empty HashMap
 * void addDecl(String name, Sym sym) -- add the given name and sym to the first HashMap in the list
 * void addScope() -- add new, empty HashMap to the front of the list
 * void removeScope() -- remove the HashMap from the front of the list 
 * Sym lookupLocal(String name) -- if the first HashMap in the list contains name as a key, return the
 *                                 associated Sym; otherwise, return null 
 * Sym lookupGlobal(String name) -- if any HashMap in the list contains name as a key, return the first 
 *                                  associated Sym (i.e., the one from the HashMap that is closest to
 *                                  the front of the list); otherwise, return null
 * void print() -- method used for debugging that first prints “\nSym Table\n”, then, for each HashMap 
 * 		   M in the list, prints M.toString() followed by a newline, and finally, prints one
 * 		   more newline
 *
 * This code tests every SymTable operation, including both correct and bad calls to the operation that 
 * can throw an exception. It produces output ONLY if a test fails.
 */
public class P1 {
  
  /* Test method for Sym constructor and getType() method.
   *
   * @return boolean value false with appropriate error message if test fails and true otherwise   
   */
  public static boolean testSym() {
    Sym sym = new Sym("a"); // create new Sym object of type a
    if (!sym.getType().equals("a")) {
      System.out.println("testSym(): failed!");
      return false;
    }
    return true;
  }

  /*
   * Test method for addDecl method and SymTable constructor.
   *
   * @throws DuplicateSymException, EmptySymTableException
   * @return boolean value false with appropriate error message if test fails and true otherwise
   */
  public static boolean testAddDecl() throws DuplicateSymException, EmptySymTableException {
    Sym sym = new Sym("a"); // create new Sym object of type a
    SymTable symtable = new SymTable(); // create new SymTable object
    
    // null name and/or sym
    try {
      symtable.addDecl(null, null);
      System.out.println(
          "addDecl() did not throw an IllegalArgumentException when the name and/or sym was null.");
      return false;
    } catch (IllegalArgumentException e) {
    } 

    // duplicate name
    try {
      symtable.addDecl("name", sym);
      symtable.addDecl("name", sym);
      System.out.println(
          "addDecl() did not throw a DuplicateSymException when the list already contained this name.");
      return false;
    } catch (DuplicateSymException e) {
    }
    return true;
  }

  /*
   * Test method for addScope() and removeScope() methods.
   *
   * @throws EmptySymTableException
   * @return boolean value false with appropriate error message if test fails and true otherwise
   */
  public static boolean testRemoveScope() throws EmptySymTableException {
    Sym sym = new Sym("a");
    SymTable symtable = new SymTable(); // create new SymTable object (list size : 1)
    
    // add one scope and remove two scopes to have empty symtable
    symtable.addScope(); // add empty HashMap to list (list size : 2)
    symtable.removeScope(); // remove empty HashMap from list (list size : 1)
    symtable.removeScope(); // remove empty HashMap from list (list size : 0)
    
    // tests if EmptySymTableException is thrown in removeScope()
    try {
      symtable.removeScope(); // remove empty HashMap from list (list size : -1)
      System.out.println(
          "removeScope() did not throw an EmptySymTableException when the list was empty.");
      return false;
    } catch (EmptySymTableException e) { 
    }
    return true;
  }

  /* 
   * Test method for addScope(), removeScope(), and lookupLocal() methods.
   *
   * @throws DuplicateSymException, EmptySymTableException
   * @return boolean value false with appropriate error message if test fails and true otherwise
   */
  public static boolean testLookupLocal() throws DuplicateSymException, EmptySymTableException {
    Sym sym = new Sym("a");
    SymTable symtable = new SymTable(); // create new SymTable object (list size : 1)
    symtable.addScope(); // add empty HashMap to list (list size : 2)
    symtable.removeScope(); // remove empty HashMap from list (list size : 1)
    symtable.removeScope(); // remove empty HashMap from list (list size : 0)
   
    // test if EmptySymTableException is thrown in lookupLocal()
    try {
      symtable.lookupLocal("name");
      System.out.println(
          "lookupLocal() did not throw an EmptySymTableException when the list was empty.");
      return false;
    } catch (EmptySymTableException e) {
    }

    // testing functionality of lookupLocal() with one scope (a), two scopes (b), and three scopes (c)
    symtable.addScope();
    Sym sym1 = new Sym("b");
    Sym sym2 = new Sym("c");
    Sym sym3 = new Sym("d");
    Sym sym4 = new Sym("e");
    symtable.addDecl("name", sym);
    symtable.addDecl("name1", sym1);
    symtable.addDecl("name2", sym2); 
    
    // (a) testing functionality with one scope
    if (!symtable.lookupLocal("name2").getType().equals("c")) { 
	    System.out.println("testLookupLocal(): failed!");
	    return false;
    }
    // (b) testing functionality with two scopes
    symtable.addScope();
    symtable.addDecl("name3", sym3);
    if (!symtable.lookupLocal("name3").getType().equals("d")) {
	    System.out.println("testLookupLocal(): failed!");
	    return false;
    }
   // (c) testing functionality with three scopes
   symtable.addScope();
   symtable.addDecl("name4", sym4);
    if (!symtable.lookupLocal("name4").getType().equals("e")) {
	    System.out.println("testLookupLocal(): failed!");
		    return false;
    }
    // testing functionality outside of first scope
    if (symtable.lookupLocal("name") != null) {
	    System.out.println("testLookupLocal(): failed!");
	    return false;
    }
    return true;
  }

  /*
   * Test method for  addScope(), removeScope(), addDecl() and lookupGlobal() methods.
   *
   * @throws DuplicateSymException, EmptySymTableException
   * @return boolean value false with appropriate error message if test fails and true otherwise
   */
  public static boolean testLookupGlobal() throws DuplicateSymException, EmptySymTableException {
    Sym sym = new Sym("a"); // create new Sym object of type a
    SymTable symtable = new SymTable(); // create new SymTable object
    symtable.removeScope(); // remove empty HashMap from list (list size : 0)
   
    // test if EmptySymTableException is thrown in lookupGlobal()
    try {
      symtable.lookupGlobal("name");
      System.out.println(
          "lookupGlobal() did not throw an EmptySymTableException when the list was empty.");
      return false;
    } catch (EmptySymTableException e) {
    }
   
    // testing functionality of lookupGlobal() with one scope (a), two scopes (b), and three scopes (c)
    symtable.addScope();
    Sym sym1 = new Sym("b");
    Sym sym2 = new Sym("c");
    Sym sym3 = new Sym("d");
    Sym sym4 = new Sym("e");
    symtable.addDecl("name", sym);
    symtable.addDecl("name1", sym1);
    symtable.addDecl("name2", sym2);

    // (a) testing functionality with one scope
    if (!symtable.lookupGlobal("name").getType().equals("a")) {
      System.out.println("testLookUpGlobal(): failed!");
      return false;
    }
   
    // (b) testing functionality with two scopes
    symtable.addScope();
    symtable.addDecl("name3", sym3);
    if (!symtable.lookupGlobal("name1").getType().equals("b")) {
	    System.out.println("testLookupGlobal(): failed!");
	    return false;
    }
    if (!symtable.lookupGlobal("name3").getType().equals("d")) {
	    System.out.println("testLookupGlobal(): failed!");
	    return false;
    }

    // (c) testing functionality with three scopes
    symtable.addScope();
    symtable.addDecl("name4", sym4);
    if (!symtable.lookupGlobal("name2").getType().equals("c")) {
	    System.out.println("testLookupGlobal(): failed!");
	    return false;
    }
    if (!symtable.lookupGlobal("name4").getType().equals("e")) {
	    System.out.println("testLookupGlobal(): failed!");
	    return false;
    }

    // testing functionality with decl not in any scope
    if (symtable.lookupGlobal("name5") != null) {
	    System.out.println("testLookupGlobal(): failed!");
	    return false;
    }
    return true;
  }

  /*
   * Test method for print() method.
   *
   * @throws DuplicateSymException, EmptySymTableException
   */
  public static void testPrint() throws DuplicateSymException, EmptySymTableException { 
	  SymTable symtable = new SymTable();
	  Sym sym = new Sym("a");
	  Sym sym1 = new Sym("b");
	  Sym sym2 = new Sym("c");
	  Sym sym3 = new Sym("d");
	  symtable.addDecl("name", sym);
	  symtable.addDecl("name1", sym1);
	  symtable.addScope();
	  symtable.addDecl("name2", sym2);
	  symtable.addScope();
	  symtable.addDecl("name3", sym3);
	  try {
		  symtable.print();
	  } catch (FileNotFoundException e) {
	  }
  }

  /*
   * Driver for test methods.
   */
  public static void main(String[] args) throws DuplicateSymException, EmptySymTableException {
    P1.testSym();
    P1.testAddDecl();
    P1.testRemoveScope();
    P1.testLookupLocal();
    P1.testLookupGlobal();
    P1.testPrint();
  }
}
