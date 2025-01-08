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

var_decl:   ('const'? 'static'? | 'static'? 'const'?) type (ref | ID) ('=' expr)? ';'
          | ('const'? 'static'? | 'static'? 'const'?) type ('(' ref ')' | ID) ('[' expr? ']')+ ('=' array)? ';'
          ;

assign  :   (array_item | ID) ('=' | ASSIGN_OP) expr ';' ;

dec_inc :   (DEC_INC_OP (array_item | ID) | (array_item | ID) DEC_INC_OP) ;

fn_decl  :  ('const'? 'static'? | 'static'? 'const'?) ('void' | type) (ref | operator | ID) '(' params? ')' ';'
         |  ('const'? 'static'? | 'static'? 'const'?) ('void' | type) (ID ':' ':')? (ref | operator | ID) '(' params? ')' block
         ;

operator    :   '&' 'operator' ('=' | DEC_INC_OP) ;

abstract_fn : 'virtual' ('void' | type) (ref | operator | ID) '(' params? ')' 'const' '=' INT ';' ;

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
        |   expr '*' expr
        |   expr '/' expr
        |   expr '+' expr
        |   expr '-' expr
        |   expr '==' expr
        |   expr '!=' expr
        |   expr '<=' expr
        |   expr '>=' expr
        |   expr '<' expr
        |   expr '>' expr
        |   expr '&&' expr
        |   expr '||' expr
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

type    :   'int' | 'char' | 'bool' ;

array   :   '{' (args | array (',' array)*) '}' ;
array_item  :   (ref | ID) ('[' expr ']')+ ;

ref :   '&' ID ;

obj_usage   :   ('this' | ID) ('.' (array_item ';' | assign | dec_inc ';' | fn_call ';' | ID ';'))? ;

// Lexer-Regeln
NULL        :   'NULL'  ;
BOOL        :   'true' | 'false' ;
INT         :   [+-]?([0-9] | [1-9][0-9]*);
CHAR        :   ('"' | '\'') (~[\n\r"'])? ('"' | '\'') ;
ID          :   [_a-zA-Z][_a-zA-Z0-9]* ;

DEC_INC_OP  :   '++' | '--' ;
ASSIGN_OP   :   '*=' | '/=' | '+=' | '-=';

MULTI_LINE_COMMENT  : '/*' .*? '*/' -> skip;
COMMENT             :  '//' ~[\n\r]* -> skip ;
WS                  :  [ \t\n\r]+ -> skip ;