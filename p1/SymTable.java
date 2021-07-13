import java.util.*;
import java.io.PrintStream;
import java.io.FileNotFoundException;

/*
 * A class with various functions representing a symbol table that is 
 * used to store symbols.
 */
public class SymTable {
  private ArrayList<HashMap<String, Sym>> list; // an ArrayList of HashMaps that map a String to a Sym

  /* SymTable constructor that should initialize the SymTable's ArrayList field to contain a single,
   * empty HashMap.
   */
  public SymTable() {
    list = new ArrayList<HashMap<String, Sym>>();
    list.add(new HashMap<String, Sym>());
  }

  /* If this SymTable's list is empty, throw an EmptySymTableException. If either name or sym (or
   * both) is null, throw a IllegalArgumentException. If the first HashMap in the list already
   * contains the given name as a key, throw a DuplicateSymException. Otherwise, add the given name
   * and sym to the first HashMap in the list.
   *
   * @param name -- String representing HashMap's name variable (key)
   * @ param sym -- Sym object represneting HashMap's sym variable (value)
   */
  public void addDecl(String name, Sym sym) throws DuplicateSymException, EmptySymTableException {
    // throws EmptySymTableException if list is empty
    if (list.isEmpty()) {
      throw new EmptySymTableException();
    }
    // throws IllegalArgumentException if either name or sym (or both) is null
    if (name == null || sym == null) {
      throw new IllegalArgumentException();
    }
    // throws DuplicateSymException if the first HashMap in the list already contains the given name
    // as a key
    if (list.get(0).containsKey(name)) {
      throw new DuplicateSymException();
      // otherwise, adds the given name and sym to the first HashMap in the list
    } else {
      list.get(0).put(name, sym);
    }
  }

  /* 
   * Method that adds a new, empty HashMap to the front of the list.
   */
  public void addScope() {
    HashMap<String, Sym> map = new HashMap<String, Sym>(); // create new empty HashMap
    list.add(0, map); // adds empty HashMap to front of list
  }

  /*
   * If this SymTable's list is empty, throw an EmptySymTableException. Otherwise, if the first
   * HashMap in the list contains name as a key, return the associated Sym; otherwise, return null.
   *
   * @param name -- String representing HashMap's name variable (key)
   * @return Sym object associated with name as a key, else return null
   */
  public Sym lookupLocal(String name) throws EmptySymTableException {
    // throws EmptySymTableException if this SymTable's list is empty
    if (list.isEmpty()) {
      throw new EmptySymTableException();
    }
    // otherwise, if the first HashMap in the list contains name as a key, return the associated Sym
    else if (list.get(0).containsKey(name)) {
      return list.get(0).get(name);
    }
   // otherwise, return null 
   else {
	   return null;
   }
  }

  /* 
   * If this SymTable's list is empty, throw an EmptySymTableException. If any HashMap in the list 
   * contains name as a key, return the first associated Sym (i.e., the one from the HashMap that is 
   * closest to the front of the list); otherwise, return null.
   *
   * @param name -- String representing HashMap's name variable (key)
   * @return first Sym object associated with name as a key, else return null
   */
  public Sym lookupGlobal(String name) throws EmptySymTableException {
    // throws EmptySymTableException if this SymTable's list is empty
    if (list.isEmpty()) {
      throw new EmptySymTableException();
    } // if any HashMap in the list contains name as a key, return the first associated Sym
    for (int i = 0; i < list.size(); i++) {
      if (list.get(i).containsKey(name)) {
        return list.get(i).get(name);
      }
    }
    // otherwise, return null
    return null;
  }

  /* 
   * If this SymTable's list is empty, throw an EmptySymTableException; otherwise, remove the
   * HashMap from the front of the list. To clarify, throw an exception only if before attempting to 
   * remove, the list is empty (i.e. there are no HashMaps to remove).
   */
  public void removeScope() throws EmptySymTableException {
    // throws EmptySymTableException if this SymTable's list is empty
    if (list.isEmpty()) {
      throw new EmptySymTableException();
    }
    // otherwise, remove the HashMap from the front of the list
    else {
      list.remove(0);
    }
  }

  /* 
   * This method is for debugging. First, print “\nSym Table\n”. Then, for each HashMap M in the 
   * list, print M.toString() followed by a newline. Finally, print one more newline. All output 
   * should go to System.out.
   */
  public void print() throws FileNotFoundException {
	 //  StringBuilder sb = new StringBuilder();
	 try {
	 PrintStream out = new PrintStream("./output.txt");
	 System.setOut(out);
	 System.out.println("\nSym Table\n");
    for (int i = 0; i < list.size(); i++) {
      System.out.println(list.get(i).toString() + "\n");
    }
    System.out.println("\n");
	 } catch (FileNotFoundException e) {
	 }
    // System.out.println(sb.toString());
}
}
