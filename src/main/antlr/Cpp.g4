grammar Cpp;

// Parser-Regeln
program :  stmt* EOF ;

stmt    :   var_decl
        |   assign
        |   dec_inc ';'
        |   main
        |   fn_decl
        |   fn_call ';'
        |   block
        |   while
        |   if
        |   return
        |   class
        |   obj_usage ';'
        ;

var_decl:   type REF? ID ('=' expr)? ';'
          | type ID ('[' expr? ']')+ ('=' array)? ';'
          | type '(' REF ID ')' ('[' expr ']')+ '=' ID ';'
          ;

assign  :   (array_item | ID | obj_usage) ('=' | ASSIGN_OP) expr ';' ;

dec_inc :   (DEC_INC_OP (array_item | ID) | (array_item | ID) DEC_INC_OP) ;

fn_decl  :  ('void' | type) REF? ID '(' params? ')' (';' | block) ;

abstract_fn : 'virtual' ('void' | type) REF? ID '(' params? ')' '=' INT ';' ;

params  :  type REF? ID ('=' expr)? (',' type REF? ID ('=' expr)?)* ;

return  :  'return' expr? ';' ;

block   :   '{' stmt* '}' ;
while   :   'while' '(' expr ')' block ;
if      :   'if' '(' expr ')' block ('else' 'if' '(' expr ')' block)* ('else' block)? ;

fn_call  :   ID? ID '(' args? ')' ;
args    :   expr (',' expr)* ;

expr    :   fn_call                     # Call
        |   array_item                  # Arr_item
        |   dec_inc                     # De_in
        |   e1=expr '*' e2=expr       # Mul
        |   e1=expr '/' e2=expr       # Div
        |   e1=expr '+' e2=expr       # Add
        |   e1=expr '-' e2=expr       # Sub
        |   e1=expr '==' e2=expr      # Equal
        |   e1=expr '!=' e2=expr      # Not_Equal
        |   e1=expr '<=' e2=expr      # Less_Equal
        |   e1=expr '>=' e2=expr      # Greater_Equal
        |   e1=expr '<' e2=expr       # Less
        |   e1=expr '>' e2=expr       # Greater
        |   e1=expr '&&' e2=expr      # And
        |   e1=expr '||' e2=expr      # Or
        |   e1=expr '%' e2=expr       # Mod
        |   '!' e=expr                 # Not
        |   NULL                        # Null
        |   BOOL                        # Bool
        |   NEG? INT                    # Int
        |   CHAR                        # Char
        |   ID                          # Id
        |   obj_usage                   # Obj
        |   '(' e=expr ')'                # Nested
        ;

constructor :   ID '(' params? ')' (':' ID '(' args? ')')? block ;

destructor  :   'virtual'? '~' ID '(' params? ')' block ;

operator    :   ID REF 'operator' '=' '(' params ')' block;

class   :   'class' ID (':' 'public' ID)? '{' 'public' ':' (var_decl | constructor | destructor | operator | 'virtual'? fn_decl | abstract_fn)* '}' ';' ;

main    :   ('void' | type) 'main' '(' params? ')' block ;

type    :   'int' | 'char' | 'bool' | ID ;

array   :   '{' (args | array (',' array)*) '}' ;
array_item  :  ID ('[' expr ']')+ ;

obj_usage   :   ( 'this' '->' | '(' '*' 'this' ')' '.' )? ID ( '.' ID)* ('.' (array_item | dec_inc | fn_call))?
            |   '*'? 'this'
            ;

// Lexer-Regeln
NULL        :   'NULL'  ;
BOOL        :   'true' | 'false' ;
NEG         :   '-' ;
INT         :   ([0-9] | [1-9][0-9]*);
CHAR        :   ('"' | '\'') (~[\n\r"'])? ('"' | '\'') ;
REF         :   '&' ;
ID          :   [_a-zA-Z][_a-zA-Z0-9]* ;

DEC_INC_OP  :   '++' | '--' ;
ASSIGN_OP   :   '*=' | '/=' | '+=' | '-=';

MULTI_LINE_COMMENT  : '/*' .*? '*/' -> skip;
COMMENT             :  '//' ~[\n\r]* -> skip ;
WS                  :  [ \t\n\r]+ -> skip ;