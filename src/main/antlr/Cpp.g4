grammar Cpp;

// Parser-Regeln
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
        |   obj_usage
        ;

var_decl:   const_static type (ref | ID) ('=' expr)? ';'
          | const_static type ('(' ref ')' | ID) ('[' expr? ']')+ ('=' array)? ';'
          ;

const_static : ('const'? 'static'? | 'static'? 'const'?) ;

assign  :   (array_item | ID) ('=' | ASSIGN_OP) expr ';' ;

dec_inc :   (DEC_INC_OP (array_item | ID) | (array_item | ID) DEC_INC_OP) ;

fn_decl  :  const_static ('void' | type) (ref | operator | ID) '(' params? ')' ';'
         |  const_static ('void' | type) (ID ':' ':')? (ref | operator | ID) '(' params? ')' block
         ;

operator    :   '&' 'operator' ('=' | DEC_INC_OP) ;

abstract_fn : 'virtual' ('void' | type) (ref | operator | ID) '(' params? ')' 'const' '=' '0' ';' ;

params  :  'const'? type (ref | ID) ('=' expr)? (',' 'const'? type (ref | ID) ('=' expr)?)* ;

return  :  'return' expr? ';' ;

block   :   '{' stmt* '}' ;
while   :   'while' '(' expr ')' block ;
if      :   'if' '(' expr ')' block ('else' 'if' '(' expr ')' block)* ('else' block)? ;

fn_call  :   (ID ':' ':')? ID '(' args? ')' ;
args    :   expr (',' expr)* ;

expr    :   fn_call
        |   array_item
        |   dec_inc
        |   ref
        |   expr CALC_OP expr
        |   expr COMPARE_OP expr
        |   expr BOOL_OP expr
        |   NULL
        |   BOOL
        |   INT
        |   CHAR
        |   obj_usage
        |   ID
        |   '(' expr ')'
        ;

delete : 'delete' ('[' ']')? (obj_usage | ID) ';' ;

constructor :   ID '(' params? ')' (':' ID '(' args? ')')? ';'
             |  (ID ':' ':')? ID '(' params? ')' (':' ID '(' args? ')')? (',' ID '(' args? ')')* block ;

destructor  :   'virtual'? '~' ID '(' params? ')' (';' | block) ;

class   :   'class' ID (':' 'public' ID)? '{' 'public' ':' var_decl* constructor+ destructor? ('virtual'? fn_decl | abstract_fn)* '}' ';' ;

main    :   ('void' | type) 'main' '(' params? ')' (';' | block) ;

type    :   'int' | 'char' | 'bool' | ID;

array   :   '{' (args | array (',' array)*) '}' ;
array_item  :   (ref | ID) ('[' expr ']')+ ;

ref :   '&' ID ;

obj_usage   :   ('this' | ID) ('.' (array_item ';' | assign | dec_inc ';' | fn_call ';' | ID ';'))? ;

// Lexer-Regeln
NULL        :   'NULL'  ;
BOOL        :   'true' | 'false' ;
INT         :   [+-]?[0-9]+ ;
CHAR        :   ('"' | '\'') (~[\n\r"'])? ('"' | '\'') ;
ID          :   [_a-zA-Z][_a-zA-Z0-9]* ;

COMPARE_OP  :   '==' | '!=' | '<=' | '>=' | '<' | '>' ;
BOOL_OP     :   '&&' | '||' ;
DEC_INC_OP  :   '++' | '--' ;
CALC_OP     :   '*' | '/' | '+' | '-' ;
ASSIGN_OP   :   '*=' | '/=' | '+=' | '-=';

MULTI_LINE_COMMENT  : '/*' .*? '*/' -> skip;
COMMENT             :  '//' ~[\n\r]* -> skip ;
WS                  :  [ \t\n\r]+ -> skip ;