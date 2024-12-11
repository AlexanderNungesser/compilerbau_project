grammar Cpp;

// Parser-regeln
program :  stmt* EOF ;

stmt    :   define
        |   var_decl
        |   assign
        |   fndecl
        |   block
        |   while
        |   if
        |   return
        ;

define  :   '#define' ID expr ;

var_decl:   type ID ('=' expr)? ';'
        |   type ID '[' (ID | INT) ']' ('=' array)? ';'
        ;

assign  :   ID ASSIGN_OP (expr | array) ';' ;

dec_inc :   (DEC_INC_OP ID | ID DEC_INC_OP) ';' ;

fndecl  :  ('void' | type) ID '(' params? ')' block ;
params  :  type ID (',' type ID)* ;
return  :  'return' expr ';' ;

block   :   '{' stmt* '}' ;
while   :   'while' '(' expr ')' block ;
if      :   'if' '(' expr ')' block ('else' block)? ;

fncall  :   ID '(' args? ')' ';' ;
args    :   expr (',' expr)* ;

expr    :   fncall
        |   dec_inc
        |   expr CALC_OP expr
        |   expr COMPARE_OP expr
        |   array
        |   ID
        |   INT
        |   CHAR
        |   BOOL
        |   '(' expr ')'
        ;

type    :   'int' | 'char' | 'bool' ;
array   :  '{' (expr (',' expr))* '}';

// Lexer-Regeln
ID          :   [_a-zA-Z][_a-zA-Z0-9]* ;

INT         :   [0-9]+ ;
CHAR        :   ('"' | '\'') (~[\n\r"'])? ('"' | '\'') ;
BOOL        :   'true' | 'false' ;

COMPARE_OP  :   '==' | '!=' | '<=' | '>=' | '<' | '>' ;
DEC_INC_OP  :   '++' | '--' ;
CALC_OP     :   '*' | '/' | '+' | '-' ;
ASSIGN_OP   :   '=' | '*=' | '/=' | '+=' | '-=' ;

COMMENT     :  '//' ~[\n\r]* -> skip ;
WS          :  [ \t\n\r]+ -> skip ;