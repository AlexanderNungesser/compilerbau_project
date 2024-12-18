grammar Cpp;

// Parser-regeln
program :  stmt* EOF ;

stmt    :   var_decl
        |   assign
        |   dec_inc
        |   fn_decl
        |   fn_call
        |   block
        |   while
        |   if
        |   return
        |   class
        |   main
        ;

var_decl:   'const'? type (ref | ID) ('=' expr)? ';'
        |   'const'? type ('(' ref ')' | ID) ('[' (ID | INT)? ']')+ ('=' array)? ';'
        ;

assign  :   (ID | array_item) ASSIGN_OP expr ';' ;

dec_inc :   (DEC_INC_OP (ID | array_item) | (ID | array_item) DEC_INC_OP) ';' ;

fn_decl  :  ('void' | type) (ID ':' ':')? (ref | ID) '(' params? ')' (';' | block) ;
params  :  type (ref | ID) (',' type (ref | ID))* ;
return  :  'return' expr ';' ;

block   :   '{' stmt* '}' ;
while   :   'while' '(' expr ')' block ;
if      :   'if' '(' expr ')' block ('else' 'if' '(' expr ')' block)* ('else' block)? ;

fn_call  :   ID '(' args? ')' ';' ;
args    :   expr (',' expr)* ;

expr    :   fn_call
        |   dec_inc
        |   expr CALC_OP expr
        |   expr COMPARE_OP expr
        |   array_item
        |   array
        |   ID
        |   INT
        |   CHAR
        |   BOOL
        |   '(' expr ')'
        ;

class   :   'class' ID (':' 'public' ID)? '{' 'public' ':' (var_decl | 'virtual'? fn_decl)* '}' ';' ;

main    :   ('void' | type) 'main' '(' params? ')' (';' | block) ;

type    :   'int' | 'char' | 'bool' ;
array   :   '{' (args | array ',') '}' ;
array_item  :   (ref | ID) ('[' (ID | INT) ']')+ ;

ref :   '&' ID ;

// Lexer-Regeln
ID          :   '~'? [_a-zA-Z][_a-zA-Z0-9]* ;

INT         :   [+-]?[0-9]+ ;
CHAR        :   ('"' | '\'') (~[\n\r"'])? ('"' | '\'') ;
BOOL        :   'true' | 'false' ;

COMPARE_OP  :   '==' | '!=' | '<=' | '>=' | '<' | '>' ;
DEC_INC_OP  :   '++' | '--' ;
CALC_OP     :   '*' | '/' | '+' | '-' ;
ASSIGN_OP   :   '=' | '*=' | '/=' | '+=' | '-=' ;

COMMENT     :  '//' ~[\n\r]* -> skip ;
WS          :  [ \t\n\r]+ -> skip ;