grammar Cpp;

// Parser-regeln
program : EOF ;

// Lexer-Regeln
ID      :   [_a-zA-Z]+[_a-zA-Z0-9]* ;
INT  :   [0-9]+ ;
CHAR  :   ('"' | '\'') (~[\n\r"'])? ('"' | '\'') ;
BOOL    :   'true' | 'false' ;

COMMENT :  '//' ~[\n\r]* -> skip ;
WS      :  [ \t\n\r]+ -> skip ;