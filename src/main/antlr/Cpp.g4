grammar Cpp;

// Parser-regeln
program :  stmt* main? EOF ;

stmt    :   var_decl
        |   assign
        |   dec_inc ';'
        |   fn_decl
        |   fn_call ';'
        |   block
        |   while
        |   if
        |   return
        |   class
        |   delete
        |   this
        ;

var_decl:   const_static type (ref | ID) ('=' expr)? ';'
        |   const_static type ('(' ref ')' | ID) ('[' expr? ']')+ ('=' array)? ';'
        ;
const_static    :   ('static'? 'const'? | 'const'? 'static'?)? ;

assign  :   (array_item | ID) ('=' | ASSIGN_OP) expr ';' ;

dec_inc :   (DEC_INC_OP (array_item | ID) | (array_item | ID) DEC_INC_OP) ;

fn_decl  :  const_static ('void' | type) (ref | ID) '(' params? ')' ';'
         |  const_static ('void' | type) (ID ':' ':')? (ref | ID) '(' params? ')' block
         ;

abstract_fn : 'virtual' ('void' | type) (ref | ID) '(' params? ')' 'const' '=' '0' ';' ;

params  :  'const'? type (ref | ID) ('=' expr)? (',' 'const'? type (ref | ID) ('=' expr)?)* ;
return  :  'return' expr? ';' ;

block   :   '{' stmt* '}' ;
while   :   'while' '(' expr ')' block ;
if      :   'if' '(' expr ')' block ('else' 'if' '(' expr ')' block)* ('else' block)? ;

fn_call  :   (ID ':' ':')? ID '(' args? ')' ;
args    :   expr (',' expr)* ;

expr    :   fn_call
        |   dec_inc
        |   array_item
        |   array
        |   ref
        |   this
        |   expr CALC_OP expr
        |   expr COMPARE_OP expr
        |   expr BOOL_OP expr
        |   NULL
        |   ID
        |   INT
        |   CHAR
        |   BOOL
        |   '(' expr ')'
        ;

delete : 'delete' ('[' ']')? (this | ID) ';' ;

constructor :   ID '(' params? ')' ':'? (ID '(' args? ')')? ';'
            |  (ID ':' ':')? ID '(' params? ')' ':'? (ID '(' args? ')')? (',' ID '(' args? ')')* block ;

destructor  :   'virtual'? '~' ID '(' params? ')' (';' | block) ;

class   :   'class' ID (':' 'public' ID)? '{' 'public' ':' var_decl* constructor? destructor? ('virtual'? fn_decl | abstract_fn)* '}' ';' ;

main    :   ('void' | type) 'main' '(' params? ')' (';' | block) ;

type    :   'int' | 'char' | 'bool' ;
array   :   '{' (args | array (',' array)*) '}' ;
array_item  :   (ref | ID) ('[' expr ']')+ ;

ref :   '&' ID ;

this        :   'this' ('.' (array_item | assign | dec_inc | fn_call | ID))? ;

// Lexer-Regeln
ID          :   [_a-zA-Z][_a-zA-Z0-9]* ;

INT         :   [+-]?[0-9]+ ;
CHAR        :   ('"' | '\'') (~[\n\r"'])? ('"' | '\'') ;
BOOL        :   'true' | 'false' ;
NULL        :   'NULL'  ;

COMPARE_OP  :   '==' | '!=' | '<=' | '>=' | '<' | '>' ;
BOOL_OP     :   '&&' | '||' ;
DEC_INC_OP  :   '++' | '--' ;
CALC_OP     :   '*' | '/' | '+' | '-' ;
ASSIGN_OP   :   '*=' | '/=' | '+=' | '-=';

COMMENT     :  '//' ~[\n\r]* -> skip ;
WS          :  [ \t\n\r]+ -> skip ;