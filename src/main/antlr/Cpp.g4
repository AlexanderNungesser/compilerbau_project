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
        |   obj_usage ';'
        ;

var_decl:   ('const'? 'static'? | 'static'? 'const'?) type (ref | ID) ('=' expr)? ';'
          | ('const'? 'static'? | 'static'? 'const'?) type ('(' ref ')' | ID) ('[' expr? ']')+ ('=' array)? ';'
          ;

assign  :   (array_item | obj_usage | ID) ('=' | ASSIGN_OP) expr ';' ;

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

expr   :   fn_call
        |   array_item
        |   dec_inc
        |   ref
        |   expr1
        |   expr2
        ;

expr1    :   e1=expr1 '*' e2=expr1    # MUL
        |   e1=expr1 '/' e2=expr1     # DIV
        |   e1=expr1 '+' e2=expr1     # ADD
        |   e1=expr1 '-' e2=expr1     # SUB
        |   e1=expr1 '==' e2=expr1    # EQUAL
        |   e1=expr1 '!=' e2=expr1    # NOT_EQUAL
        |   e1=expr1 '<=' e2=expr1    # LESS_EQUAL
        |   e1=expr1 '>=' e2=expr1    # GREATER_EQUAL
        |   e1=expr1 '<' e2=expr1     # LESS
        |   e1=expr1 '>' e2=expr1     # GREATER
        |   e1=expr1 '&&' e2=expr1    # AND
        |   e1=expr1 '||' e2=expr1    # OR
        |   e1=expr1 '%' e2=expr1     # MOD
        |   '!' e=expr1              # NOT
        |   NULL                    # NULL
        |   BOOL                    # BOOL
        |   INT                     # INT
        |   CHAR                    # CHAR
        |   ID                      # ID
        ;

expr2   : obj_usage
                  |   '(' expr ')';

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

obj_usage   :   ('this' | ID) ('.' ID)* ('.' (array_item | dec_inc | fn_call))? ;

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