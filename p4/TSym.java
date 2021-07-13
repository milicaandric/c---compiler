import java.util.LinkedList;	
public class TSym {	
    private String type;	
    private SymTable symtable;
    private StructDeclNode struct;		
    	
    public TSym(String type) {	
        this.type = type;	
    }	
    
    public void setSymTable(SymTable symtable) {	
        this.symtable = symtable;	
    }	
    public SymTable getSymTable(SymTable symtable) {	
        return this.symtable;	
    }	
    public String getType() {	
        return type;	
    }	
    	
    public String toString() {	
        return type;	
    }	
    
    public void setStruct(StructDeclNode struct){	
        this.struct = struct;	
    }	
    
    public StructDeclNode getStruct(){	
        return this.struct;	
    }	
}	

class FunctionTSym extends TSym {	
    private LinkedList<String> paramTypes;	
    private String returnType;	
    public FunctionTSym(String returnType, LinkedList<String> paramTypes) {	
        super("function");	
        this.paramTypes = paramTypes;	
        this.returnType = returnType;		
    }	
    public int getParamNum(){	
        return paramTypes.size();	
    }	
    public String toString() {	
        String params = String.join(", ", paramTypes);	
        if (params.equals("")) {	
        return ("->" + returnType);
	
        }	
        return params + "->" + returnType;	
    }	
}