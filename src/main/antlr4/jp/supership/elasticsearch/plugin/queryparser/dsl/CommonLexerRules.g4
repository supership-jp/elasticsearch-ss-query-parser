/*
 * Common Lexer rules.
 */
lexer grammar CommonLexerRules;

fragment DICIMAL
    : [0-9]
    ;

fragment QUATERNARY
    : [0-3]
    ;

fragment OCTAL
    : [0-7]
    ;

fragment HEXADECIMAL
    : [0-9a-fA-F]
    ;

fragment UNICODE_LITERAL
    : '\\' 'u' HEXADECIMAL HEXADECIMAL HEXADECIMAL HEXADECIMAL
    ;

fragment OCTAL_LITERAL
    : '\\' QUATERNARY OCTAL OCTAL
    | '\\' OCTAL OCTAL
    | '\\' OCTAL
    ;

fragment ESCAPED_CHARACTER
    : '\\' ~[]
    ;

fragment SPECIAL_CHARACTER
    : '\\' [btnfr\'\\]
    | UNICODE_LITERAL
    | OCTAL_LITERAL
    ;

fragment FIELD_INITIAL
    : ~[' ', '\t', '\n', '\r', '\u3000', '-', '(', ')', ':', '\"', '\\']
    | ESCAPED_CHARACTER
    ;

fragment FIELD_CHARACTER
    : (FIELD_INITIAL | ESCAPED_CHARACTER | '-')
    ;

fragment WHITE_SPACE
    : (' ' | '\t' | '\n' | '\r' | '\u3000')
    ;

fragment QUATABLE_CHARACTER
    : (~['\"', '\\'] | ESCAPED_CHARACTER)
    ;

MINUS  : '-'                              ;
LPAREN : '('                              ;
RPAREN : ')'                              ;
COLON  : ':'                              ;
STRING : '\"' (QUATABLE_CHARACTER)* '\"'  ;
NUMBER : (DICIMAL)+                       ;
FIELD  : FIELD_INITIAL (FIELD_CHARACTER)* ;
