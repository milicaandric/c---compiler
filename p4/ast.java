import java.io.*;
import java.util.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a C-- program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a list (for nodes that may have a variable number of
// children) or as a fixed set of fields.
//
// The nodes for literals and ids contain line and character number
// information; for string literals and identifiers, they also contain a
// string; for integer literals, they also contain an integer value.
//
// Here are all the different kinds of AST nodes and what kinds of children
// they have.  All of these kinds of AST nodes are subclasses of "ASTnode".
// Indentation indicates further subclassing:
//
//     Subclass            Children
//     --------            ----
//     ProgramNode         DeclListNode
//     DeclListNode        linked list of DeclNode
//     DeclNode:
//       VarDeclNode       TypeNode, IdNode, int
//       FnDeclNode        TypeNode, IdNode, FormalsListNode, FnBodyNode
//       FormalDeclNode    TypeNode, IdNode
//       StructDeclNode    IdNode, DeclListNode
//
//     FormalsListNode     linked list of FormalDeclNode
//     FnBodyNode          DeclListNode, StmtListNode
//     StmtListNode        linked list of StmtNode
//     ExpListNode         linked list of ExpNode
//
//     TypeNode:
//       IntNode           -- none --
//       BoolNode          -- none --
//       VoidNode          -- none --
//       StructNode        IdNode
//
//     StmtNode:
//       AssignStmtNode      AssignNode
//       PostIncStmtNode     ExpNode
//       PostDecStmtNode     ExpNode
//       ReadStmtNode        ExpNode
//       WriteStmtNode       ExpNode
//       IfStmtNode          ExpNode, DeclListNode, StmtListNode
//       IfElseStmtNode      ExpNode, DeclListNode, StmtListNode,
//                                    DeclListNode, StmtListNode
//       WhileStmtNode       ExpNode, DeclListNode, StmtListNode
//       RepeatStmtNode      ExpNode, DeclListNode, StmtListNode
//       CallStmtNode        CallExpNode
//       ReturnStmtNode      ExpNode
//
//     ExpNode:
//       IntLitNode          -- none --
//       StrLitNode          -- none --
//       TrueNode            -- none --
//       FalseNode           -- none --
//       IdNode              -- none --
//       DotAccessNode       ExpNode, IdNode
//       AssignNode          ExpNode, ExpNode
//       CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode
//         MinusNode
//         TimesNode
//         DivideNode
//         AndNode
//         OrNode
//         EqualsNode
//         NotEqualsNode
//         LessNode
//         GreaterNode
//         LessEqNode
//         GreaterEqNode
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with linked lists of children, or
// internal nodes with a fixed number of children:
//
// (1) Leaf nodes:
//        IntNode,   BoolNode,  VoidNode,  IntLitNode,  StrLitNode,
//        TrueNode,  FalseNode, IdNode
//
// (2) Internal nodes with (possibly empty) linked lists of children:
//        DeclListNode, FormalsListNode, StmtListNode, ExpListNode
//
// (3) Internal nodes with fixed numbers of children:
//        ProgramNode,     VarDeclNode,     FnDeclNode,     FormalDeclNode,
//        StructDeclNode,  FnBodyNode,      StructNode,     AssignStmtNode,
//        PostIncStmtNode, PostDecStmtNode, ReadStmtNode,   WriteStmtNode
//        IfStmtNode,      IfElseStmtNode,  WhileStmtNode,  RepeatStmtNode,
//        CallStmtNode
//        ReturnStmtNode,  DotAccessNode,   AssignExpNode,  CallExpNode,
//        UnaryExpNode,    BinaryExpNode,   UnaryMinusNode, NotNode,
//        PlusNode,        MinusNode,       TimesNode,      DivideNode,
//        AndNode,         OrNode,          EqualsNode,     NotEqualsNode,
//        LessNode,        GreaterNode,     LessEqNode,     GreaterEqNode
//
// **********************************************************************

// **********************************************************************
// ASTnode class (base class for all other kinds of nodes)
// **********************************************************************

abstract class ASTnode {
    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);

    // this method can be used by the unparse methods to do indenting
    protected void addIndentation(PrintWriter p, int indent) {
        for (int k = 0; k < indent; k++) p.print(" ");
    }
}

// **********************************************************************
// ProgramNode,  DeclListNode, FormalsListNode, FnBodyNode,
// StmtListNode, ExpListNode
// **********************************************************************

class ProgramNode extends ASTnode {
    public ProgramNode(DeclListNode L) {
        myDeclList = L;
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
    }

    // name analyzer (1)
    public void nameAnalyzer() {
        SymTable symtable = new SymTable();
        myDeclList.nameAnalyzer(symtable);
    }

    // one child
    private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {
    public DeclListNode(List < DeclNode > S) {
        myDecls = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode) it.next()).unparse(p, indent);
            }
        } catch (NoSuchElementException e) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

    // name analyzer (2)
    public SymTable nameAnalyzer(SymTable symtable) {
        for (DeclNode declNode: myDecls) {
            declNode.nameAnalyzer(symtable);
        }
        return symtable;
    }

    // name analyzer for StructDecl (3)
    public SymTable nameAnalyzer(SymTable symtable, SymTable structSymTable) {
        for (DeclNode declNode: myDecls) {
            VarDeclNode node = (VarDeclNode) declNode;
            if (node.getSize() == VarDeclNode.NOT_STRUCT) {
                node.nameAnalyzer(structSymTable);
            } else {
                node.nameAnalyzerStruct(symtable, structSymTable);
            }
        }
        return symtable;
    }

    // linked list of DeclNodes 
    private List < DeclNode > myDecls;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(List < FormalDeclNode > S) {
        myFormals = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator < FormalDeclNode > it = myFormals.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) { // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        }
    }

    public LinkedList < String > getTypeList() {
        LinkedList < String > paramTypes = new LinkedList < > ();
        for (FormalDeclNode formalDeclNode: myFormals) {
            paramTypes.add(formalDeclNode.getTypeString());
        }
        return paramTypes;
    }

    // name analyzer (4)
    public SymTable nameAnalyzer(SymTable symtable) {
        for (FormalDeclNode formalDeclNode: myFormals) {
            formalDeclNode.nameAnalyzer(symtable);
        }
        return symtable;
    }

    // linked list of FormalDeclNodes
    private List < FormalDeclNode > myFormals;
}

class FnBodyNode extends ASTnode {
    public FnBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
        myStmtList.unparse(p, indent);
    }

    // name analyzer (5)
    public void nameAnalyzer(SymTable symtable) {
        myDeclList.nameAnalyzer(symtable);
        myStmtList.nameAnalyzer(symtable);
    }

    // two children
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class StmtListNode extends ASTnode {
    public StmtListNode(List < StmtNode > S) {
        myStmts = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator < StmtNode > it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        }
    }

    // name analyzer (6)
    public void nameAnalyzer(SymTable symtable) {
        for (StmtNode stmtNode: myStmts) {
            stmtNode.nameAnalyzer(symtable);
        }
    }

    // linked list of StmtNodes
    private List < StmtNode > myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List < ExpNode > S) {
        myExps = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator < ExpNode > it = myExps.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) { // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        }
    }

    // name analyzer (7)
    public void nameAnalyzer(SymTable symtable) {
        for (ExpNode expNode: myExps) {
            expNode.nameAnalyzer(symtable);
        }
    }

    // linked list of ExpNodes
    private List < ExpNode > myExps;
}

// **********************************************************************
// DeclNode and its subclasses
// **********************************************************************

abstract class DeclNode extends ASTnode {
    abstract public SymTable nameAnalyzer(SymTable symtable);
    abstract public IdNode getIdNode();
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        myType = type;
        myId = id;
        mySize = size;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.println(";");
    }

    // analyzer for normal variable decl (8)
    public SymTable nameAnalyzer(SymTable symtable) {
        if (myType instanceof VoidNode) {
            // the first character of the ID in the bad declaration
            ErrMsg.fatal(this.myId.getLineNum(), this.myId.getCharNum(), "Non-function declared void");
            return symtable;
        }
        if (myType instanceof StructNode) {
            boolean result = this.nameAnalyzerStructName(symtable);
            TSym structSym = new TSym("a");
            try {
                structSym = symtable.lookupGlobal(((StructNode) myType).getId().toString());
            } catch (EmptySymTableException e) {
                System.out.println(e);
            }
            if (structSym == null || result == false) {
                return symtable;
            }
            this.nameAnalyzerVarName(symtable);
            TSym myTsym = new TSym("b");
            try {
                myTsym = symtable.lookupGlobal(myId.toString());
            } catch (EmptySymTableException e) {
                System.out.println(e);
            }
            myId.setStruct(structSym.getStruct(), myTsym);
            return symtable;
        }
        this.nameAnalyzerVarName(symtable);
        return symtable;
    }

    // analyzer for variable name checking (9)
    public void nameAnalyzerVarName(SymTable symtable) {
        TSym newSym = new TSym(this.myType.toString());
        try {
            symtable.addDecl(this.myId.toString(), newSym);
        } catch (DuplicateSymException e) {
            // the first character of the ID in the duplicate declaration
            ErrMsg.fatal(this.myId.getLineNum(), this.myId.getCharNum(), "Multiply declared identifier");
        } catch (EmptySymTableException e) {
            System.out.println(e);
        }
        return;
    }
    
    // analyzer for struct decl checking (10)
    public void nameAnalyzerStruct(SymTable symtable, SymTable symTableStruct) {
        this.nameAnalyzerVarName(symTableStruct);
        this.nameAnalyzerStructName(symtable);
        TSym structSym = new TSym("c");
        TSym myTsym = new TSym("d");
        if (myType instanceof StructNode) {
            try {
                structSym = symtable.lookupGlobal(((StructNode) myType).getId().toString());
                myTsym = symTableStruct.lookupGlobal(myId.toString());
            } catch (EmptySymTableException e) {
                System.out.println(e);
            }
            if (structSym != null && myTsym != null) {
                myId.setStruct(structSym.getStruct(), myTsym);
            }
        }
    }
    // analyzer for only struct decl (11)
    public boolean nameAnalyzerStructName(SymTable symtable) {
        IdNode structId = ((StructNode) this.myType).getId();
        TSym tsym = new TSym("f");
        try {
            tsym = symtable.lookupGlobal(structId.toString());
        } catch (EmptySymTableException e) {
            System.out.println(e);
        }
        if (tsym == null || !tsym.getType().equals("structdecl")) {
            // the first character of the ID corresponding to the struct type in the bad declaration
            ErrMsg.fatal(structId.getLineNum(), structId.getCharNum(), "Invalid name of struct type");
            return false;
        }
        return true;
    }

    // helper getter method for int
    public int getSize() {
        return mySize;
    }

    // helper getter method for IdNode
    public IdNode getIdNode() {
        return this.myId;
    }

    // three children
    private TypeNode myType;
    private IdNode myId;
    private int mySize; // use value NOT_STRUCT if this is not a struct type
    public static int NOT_STRUCT = -1;
}

class FnDeclNode extends DeclNode {
    public FnDeclNode(TypeNode type,
        IdNode id,
        FormalsListNode formalList,
        FnBodyNode body) {
        myType = type;
        myId = id;
        myFormalsList = formalList;
        myBody = body;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.print("(");
        myFormalsList.unparse(p, 0);
        p.println(") {");
        myBody.unparse(p, indent + 4);
        p.println("}\n");
    }

    // name analyzer (12)
    public SymTable nameAnalyzer(SymTable symtable) {
        LinkedList < String > paramTypes = myFormalsList.getTypeList();
        try {
            symtable.addDecl(this.myId.toString(), new FunctionTSym(myType.toString(), paramTypes));
        } catch (DuplicateSymException e) {
            // the first character of the ID in the duplicate declaration
            ErrMsg.fatal(this.myId.getLineNum(), this.myId.getCharNum(), "Multiply declared identifier");
        } catch (EmptySymTableException e) {
            System.out.println(e);
        }
        symtable.addScope();
        myFormalsList.nameAnalyzer(symtable);
        myBody.nameAnalyzer(symtable);
        try {
            symtable.removeScope();
        } catch (EmptySymTableException e) {
            System.out.println(e);
        }
        return symtable;
    }

    // helper getter method for IdNode
    public IdNode getIdNode() {
        return this.myId;
    }

    // four children
    private TypeNode myType;
    private IdNode myId;
    private FormalsListNode myFormalsList;
    private FnBodyNode myBody;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    public void unparse(PrintWriter p, int indent) {
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
    }

    // name analyzer (13)
    public SymTable nameAnalyzer(SymTable symtable) {
        try {
            symtable.addDecl(this.myId.toString(), new TSym(this.myType.toString()));
        } catch (DuplicateSymException e) {
            // the first character of the ID in the duplicate declaration
            ErrMsg.fatal(this.myId.getLineNum(), this.myId.getCharNum(), "Multiply declared identifier");
        } catch (EmptySymTableException e) {
            System.out.println(e);
        }
        return symtable;
    }

    // helper getter method for TypeNode String
    public String getTypeString() {
        return myType.toString();
    }

    // helper getter method for IdNode
    public IdNode getIdNode() {
        return this.myId;
    }

    // two children
    private TypeNode myType;
    private IdNode myId;
}

class StructDeclNode extends DeclNode {
    public StructDeclNode(IdNode id, DeclListNode declList) {
        myId = id;
        myDeclList = declList;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("struct ");
        myId.unparse(p, 0);
        p.println("{");
        myDeclList.unparse(p, indent + 4);
        addIndentation(p, indent);
        p.println("};\n");

    }

    // name analyzer (14)
    public SymTable nameAnalyzer(SymTable symtable) {
        TSym newSym = new TSym("structdecl");
        try{
            symtable.addDecl(this.myId.toString(), newSym);
        } catch  (DuplicateSymException ex){
            // the first character of the ID in the duplicate declaration
            ErrMsg.fatal(this.myId.getLineNum(), this.myId.getCharNum(), "Multiply declared identifier");
            return symtable;
        } catch (EmptySymTableException ex) {
            System.out.println(ex);
        } 
        mySymTable = new SymTable();
        myId.setStruct(this, newSym);
        myDeclList.nameAnalyzer(symtable, mySymTable);
        return symtable;
    }
    
    // helper getter method for SymTable
    public SymTable getSymTable() {
        return mySymTable;
    }

    // helper getter method for IdNode
    public IdNode getIdNode() {
        return this.myId;
    }

    // two children + SymTable
    private IdNode myId;
    private DeclListNode myDeclList;
    private SymTable mySymTable;
}

// **********************************************************************
// TypeNode and its Subclasses
// **********************************************************************

abstract class TypeNode extends ASTnode {}

class IntNode extends TypeNode {
    public IntNode() {}

    public void unparse(PrintWriter p, int indent) {
        p.print("int");
    }

    public String toString() {
        return "int";
    }
}

class BoolNode extends TypeNode {
    public BoolNode() {}

    public void unparse(PrintWriter p, int indent) {
        p.print("bool");
    }

    public String toString() {
        return "bool";
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {}

    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }

    public String toString() {
        return "void";
    }
}

class StructNode extends TypeNode {
    public StructNode(IdNode id) {
        myId = id;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("struct ");
        myId.unparse(p, 0);
    }

    public String toString() {
        return myId.toString();
    }

    public IdNode getId() {
        return myId;
    }

    // one child
    private IdNode myId;
}

// **********************************************************************
// StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
    public abstract void nameAnalyzer(SymTable symtable);
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignNode assign) {
        myAssign = assign;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        myAssign.unparse(p, -1); // no parentheses
        p.println(";");
    }

    // name analyzer (15)
    public void nameAnalyzer(SymTable symtable) {
        myAssign.nameAnalyzer(symtable);
    }

    // one child
    private AssignNode myAssign;
}

class PostIncStmtNode extends StmtNode {
    public PostIncStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        myExp.unparse(p, 0);
        p.println("++;");
    }

    // name analyzer (16)
    public void nameAnalyzer(SymTable symtable) {
        myExp.nameAnalyzer(symtable);
    }

    // one child
    private ExpNode myExp;
}

class PostDecStmtNode extends StmtNode {
    public PostDecStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        myExp.unparse(p, 0);
        p.println("--;");
    }

    // name analyzer (17)
    public void nameAnalyzer(SymTable symtable) {
        myExp.nameAnalyzer(symtable);
    }

    // one child
    private ExpNode myExp;
}

class ReadStmtNode extends StmtNode {
    public ReadStmtNode(ExpNode e) {
        myExp = e;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("cin >> ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    // name analyzer (18)
    public void nameAnalyzer(SymTable symtable) {
        myExp.nameAnalyzer(symtable);
    }

    // 1 child (actually can only be an IdNode or an ArrayExpNode)
    private ExpNode myExp;
}

class WriteStmtNode extends StmtNode {
    public WriteStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("cout << ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    // name analyzer (19)
    public void nameAnalyzer(SymTable symtable) {
        myExp.nameAnalyzer(symtable);
    }

    // one child
    private ExpNode myExp;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myDeclList = dlist;
        myExp = exp;
        myStmtList = slist;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent + 4);
        myStmtList.unparse(p, indent + 4);
        addIndentation(p, indent);
        p.println("}");
    }

    // name analyzer (20)
    public void nameAnalyzer(SymTable symtable) {
        myExp.nameAnalyzer(symtable);
        symtable.addScope();
        myDeclList.nameAnalyzer(symtable);
        myStmtList.nameAnalyzer(symtable);
        try {
            symtable.removeScope();
        } catch (EmptySymTableException e) {
            System.out.println(e);
        }
    }

    // three children
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class IfElseStmtNode extends StmtNode {
    public IfElseStmtNode(ExpNode exp, DeclListNode dlist1,
        StmtListNode slist1, DeclListNode dlist2,
        StmtListNode slist2) {
        myExp = exp;
        myThenDeclList = dlist1;
        myThenStmtList = slist1;
        myElseDeclList = dlist2;
        myElseStmtList = slist2;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myThenDeclList.unparse(p, indent + 4);
        myThenStmtList.unparse(p, indent + 4);
        addIndentation(p, indent);
        p.println("}");
        addIndentation(p, indent);
        p.println("else {");
        myElseDeclList.unparse(p, indent + 4);
        myElseStmtList.unparse(p, indent + 4);
        addIndentation(p, indent);
        p.println("}");
    }

    // name analyzer (21)
    public void nameAnalyzer(SymTable symtable) {
        myExp.nameAnalyzer(symtable);
        symtable.addScope();
        myThenDeclList.nameAnalyzer(symtable);
        myThenStmtList.nameAnalyzer(symtable);
        try {
            symtable.removeScope();
        } catch (EmptySymTableException e) {
            System.out.println(e);
        }
        symtable.addScope();
        myElseDeclList.nameAnalyzer(symtable);
        myElseStmtList.nameAnalyzer(symtable);
        try {
            symtable.removeScope();
        } catch (EmptySymTableException e) {
            System.out.println(e);
        }
    }

    // five children
    private ExpNode myExp;
    private DeclListNode myThenDeclList;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
    private DeclListNode myElseDeclList;
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("while (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent + 4);
        myStmtList.unparse(p, indent + 4);
        addIndentation(p, indent);
        p.println("}");
    }

    // name analyzer (22)
    public void nameAnalyzer(SymTable symtable) {
        myExp.nameAnalyzer(symtable);
        symtable.addScope();
        myDeclList.nameAnalyzer(symtable);
        myStmtList.nameAnalyzer(symtable);
        try {
            symtable.removeScope();
        } catch (EmptySymTableException e) {
            System.out.println(e);
        }
    }

    // three children
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class RepeatStmtNode extends StmtNode {
    public RepeatStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("repeat (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent + 4);
        myStmtList.unparse(p, indent + 4);
        addIndentation(p, indent);
        p.println("}");
    }

    // name analyzer (23)
    public void nameAnalyzer(SymTable symtable) {
        myExp.nameAnalyzer(symtable);
        symtable.addScope();
        myDeclList.nameAnalyzer(symtable);
        myStmtList.nameAnalyzer(symtable);
        try {
            symtable.removeScope();
        } catch (EmptySymTableException e) {
            System.out.println(e);
        }
    }

    // three children
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class CallStmtNode extends StmtNode {
    public CallStmtNode(CallExpNode call) {
        myCall = call;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        myCall.unparse(p, indent);
        p.println(";");
    }

    // name analyzer (24)
    public void nameAnalyzer(SymTable symtable) {
        myCall.nameAnalyzer(symtable);
    }

    // one child
    private CallExpNode myCall;
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("return");
        if (myExp != null) {
            p.print(" ");
            myExp.unparse(p, 0);
        }
        p.println(";");
    }

    // name analyzer (25)
    public void nameAnalyzer(SymTable symtable) {
        if (myExp != null) {
            myExp.nameAnalyzer(symtable);
        }
    }

    // one child
    private ExpNode myExp; // possibly null
}

// **********************************************************************
// ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
    public abstract void nameAnalyzer(SymTable symtable);
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myIntVal = intVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myIntVal);
    }

    // name analyzer (26)
    public void nameAnalyzer(SymTable symtable) {}

    private int myLineNum;
    private int myCharNum;
    private int myIntVal;
}

class StringLitNode extends ExpNode {
    public StringLitNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
    }

    // name analyzer (27)
    public void nameAnalyzer(SymTable symtable) {}

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("true");
    }

    // name analyzer (28)
    public void nameAnalyzer(SymTable symtable) {}

    private int myLineNum;
    private int myCharNum;
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("false");
    }

    // name analyzer (29)
    public void nameAnalyzer(SymTable symtable) {}

    private int myLineNum;
    private int myCharNum;
}

class IdNode extends ExpNode {
    public IdNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
        if (myTsym != null) {
            p.print("(");
            p.print(myTsym.toString());
            p.print(")");
        }
    }

    // helper getter method for int myLineNum
    public int getLineNum() {
        return myLineNum;
    }

    // helper getter method for int myCharNum
    public int getCharNum() {
        return myCharNum;
    }

    // helper toString method
    public String toString() {
        return myStrVal;
    }

    // name analyzer (30)
    public void nameAnalyzer(SymTable symtable) {
        try {
            this.myTsym = symtable.lookupGlobal(myStrVal);
        } catch (EmptySymTableException e) {
            System.out.println(e);
        }
        if (myTsym == null) {
            // the first character of the undeclared identifier
            ErrMsg.fatal(myLineNum, myCharNum, "Undeclared identifier");
        } else {
            this.myStruct = myTsym.getStruct();
        }
        return;
    }

    // helper getter method for TSym
    public TSym getSym() {
        return myTsym;
    }

    // helper setter method for TSym
    public void setSym(TSym myTsym) {
        this.myTsym = myTsym;
    }

    // helper getter method for StructDeclNode
    public StructDeclNode getStruct() {
        return myStruct;
    }

    // helper setter method for StructDeclNode
    public void setStruct(StructDeclNode myStruct, TSym tsym) {
        this.myStruct = myStruct;
        tsym.setStruct(myStruct);
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
    private TSym myTsym; // new field of type TSym
    private StructDeclNode myStruct;
}

class DotAccessExpNode extends ExpNode {
    public DotAccessExpNode(ExpNode loc, IdNode id) {
        myLoc = loc;
        myId = id;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myLoc.unparse(p, 0);
        p.print(").");
        myId.unparse(p, 0);
    }

    // name analyzer (31)
    public void nameAnalyzer(SymTable symtable){
        myLoc.nameAnalyzer(symtable);
        StructDeclNode lhs = this.getLhsStruct(symtable);
        if(lhs == null){
            return;
        }
        SymTable leftSymTable = lhs.getSymTable();
        TSym foundTsym = new TSym("g");
        try {
        foundTsym = leftSymTable.lookupGlobal(myId.toString());
        } catch (EmptySymTableException e) {
                System.out.println(e);
        }
        if(foundTsym == null) {
            // the first character of the ID corresponding to the RHS of the dot-access
            ErrMsg.fatal(((IdNode)myId).getLineNum(), ((IdNode)myId).getCharNum(),  "Invalid struct field name");
        }else{
            myId.setSym(foundTsym);
        }
            
    }
    
    // retrieves LHS struct
    private StructDeclNode getLhsStruct(SymTable symtable){
        if(myLoc instanceof IdNode){
            TSym lookUpSym = new TSym("h");
            try {
            lookUpSym = symtable.lookupGlobal(((IdNode)myLoc).toString());
            } catch (EmptySymTableException e) {
                System.out.println(e);
            }
            if(lookUpSym == null){
                return null;
            }
            if(lookUpSym.getStruct() == null){
                // the first character of the ID corresponding to the LHS of the dot-access
                ErrMsg.fatal(((IdNode)myLoc).getLineNum(), ((IdNode)myLoc).getCharNum(), "Dot-access of non-struct type");
                return null;
            }
            return ((IdNode)myLoc).getStruct();
        }else{
            StructDeclNode lhs = ((DotAccessExpNode) myLoc).getLhsStruct(symtable);
            if(lhs == null){
                return null;
            }
            SymTable leftSymTable = lhs.getSymTable();
            TSym foundTsym = new TSym("i");
            try {
            foundTsym = leftSymTable.lookupGlobal(((DotAccessExpNode)myLoc).myId.toString());
            } catch (EmptySymTableException e) {
                System.out.println(e);
            }
            if(foundTsym == null){
                return null;
            }else{
                return foundTsym.getStruct();
            }
        }
    }

    // two children
    private ExpNode myLoc;
    private IdNode myId;
}

class AssignNode extends ExpNode {
    public AssignNode(ExpNode lhs, ExpNode exp) {
        myLhs = lhs;
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        if (indent != -1) p.print("(");
        myLhs.unparse(p, 0);
        p.print(" = ");
        myExp.unparse(p, 0);
        if (indent != -1) p.print(")");
    }

    // name analyzer (32)
    public void nameAnalyzer(SymTable symtable) {
        myLhs.nameAnalyzer(symtable);
        myExp.nameAnalyzer(symtable);
    }

    // two children
    private ExpNode myLhs;
    private ExpNode myExp;
}

class CallExpNode extends ExpNode {
    public CallExpNode(IdNode name, ExpListNode elist) {
        myId = name;
        myExpList = elist;
    }

    public CallExpNode(IdNode name) {
        myId = name;
        myExpList = new ExpListNode(new LinkedList < ExpNode > ());
    }

    public void unparse(PrintWriter p, int indent) {
        myId.unparse(p, 0);
        p.print("(");
        if (myExpList != null) {
            myExpList.unparse(p, 0);
        }
        p.print(")");
    }

    // name analyzer (33)
    public void nameAnalyzer(SymTable symtable) {
        myId.nameAnalyzer(symtable);
        myExpList.nameAnalyzer(symtable);
    }

    // two children
    private IdNode myId;
    private ExpListNode myExpList; // possibly null
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
    }

    // name analyzer (34)
    public void nameAnalyzer(SymTable symtable) {
        myExp.nameAnalyzer(symtable);
    }

    // one child
    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }

    // name analyzer (35)
    public void nameAnalyzer(SymTable symtable) {
        myExp1.nameAnalyzer(symtable);
        myExp2.nameAnalyzer(symtable);
    }

    // two children
    protected ExpNode myExp1;
    protected ExpNode myExp2;
}

// **********************************************************************
// Subclasses of UnaryExpNode
// **********************************************************************

class UnaryMinusNode extends UnaryExpNode {
    public UnaryMinusNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(-");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

class NotNode extends UnaryExpNode {
    public NotNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(!");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

// **********************************************************************
// Subclasses of BinaryExpNode
// **********************************************************************

class PlusNode extends BinaryExpNode {
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" + ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class MinusNode extends BinaryExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" - ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class TimesNode extends BinaryExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" * ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class DivideNode extends BinaryExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" / ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class AndNode extends BinaryExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" && ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class OrNode extends BinaryExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" || ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class EqualsNode extends BinaryExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" == ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class NotEqualsNode extends BinaryExpNode {
    public NotEqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" != ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessNode extends BinaryExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" < ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterNode extends BinaryExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" > ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessEqNode extends BinaryExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" <= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterEqNode extends BinaryExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" >= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}