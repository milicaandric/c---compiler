# c---compiler

1.) P1:
  - P1 Part 1: 15/15
  - P1 Part 2: 82/85 -- Print method always outputs to a file called output.txt. This is fine for debugging purposes, but in general, it should be printing to stdout as that is the program specification. A better way to do this kind of debugging/testing is to have some function that is a wrapper around print, or to have the function print to stdout by default and to a file in special circumstances (-3).

2.) P2: 85/100 
  - Doesn't update CharNum at all for a bad integer literal (-3).
  - Doesn't return a token for a bad integer literal (-3).
  - Prints the strings themselves in malformed integer literal errors messages (-2).
  - Allows a newline character to be a part of "string literal with bad escaped character ignored" case (-5).
  - No testing comments (-2).
 
3.) P3: 94/100
- && should have higher precedence than || (-2).
- Missing keyword "struct" in variable declarations (-1).
- Some missing tests (-3).

4.) P4: 87.5/100
- Still unparses even when there is an error (-1).
- Some missing error coverage (-4.5).
- Some missing test coverage (-3).

5.) P5: 89/100
- Missing tests for function calls (non-function and wrong type) (-2).
- Exceptions in CallExpNode: Exception in thread "main" java.lang.ClassCastException: TSym cannot be cast to FnSym and Exception in thread "main" java.lang.ClassCastException: StructSym cannot be cast to FnSym (-8).
- Minor off by 1 error with charnum in cases of unary expressions (-1).
