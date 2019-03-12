# translator-generator

## Description

Implementation of LL-1 grammar translator generator inspired by ANTLR.

## Features
1. Tokens supports regular expressions
2. Supports synthesized and inherited attributes.
3. Recognize left recursion and right branching.
4. Visualization of parse tree.

## Run

To generate grammar you should do this steps.
1. Generate grammar parser with ANTLR placed in ru.ifmo.galkin.antlr4
2. Create instance of ru.ifmo.galkin.generator.Generator and run generate method.

## Grammar Example

```
grammar logicExpressions

expr: impl exprprime;
exprprime: IMPL impl exprprime | ;
impl : or implprime;
implprime : OR or implprime | ;
or : xor orprime;
orprime : XOR xor orprime | ;
xor : and xorprime;
xorprime: AND and xorprime | ;
and : NOT and | not;
not : ID | LPAREN expr RPAREN;

IMPL : '->';
OR : '|';
XOR : '^';
AND : '&';
NOT : '!';
ID : [a-z];
LPAREN : '(';
RPAREN : ')';
```

Another grammars examples placed in grammars folder.
