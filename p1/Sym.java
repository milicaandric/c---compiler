/*
 * Class that represnets symbols in the symbol table.
 */
public class Sym {
  private String type; // the type of the symbol

  /*
   * Sym constructor that should initialize the Sym to have the given type.
   *
   * @param type -- String representing the type of the symbol object
   */
  public Sym(String type) {
    this.type = type;
  }

  /*
   * Returns given symbol object's type.
   *
   * @return String Sym object's type
   */
  public String getType() {
    return this.type;
  }

  /* Returns this symbol object's type. (This method will be changed later in a
   * future project when more information is stored in a Sym.)
   * 
   * @return String Sym object's type
   */
  public String toString() {
    return this.type;
  }
}
