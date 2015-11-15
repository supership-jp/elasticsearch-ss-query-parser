/*
 * Common Lexer rules.
 */
lexer grammar CommonLexerRules;

fragment DIGIT
    : [0-9]
    ;

fragment UNICODE_LITERAL
    : '\\' 'u' [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F]
    ;

fragment OCTAL_LITERAL
    : '\\' [0-3] [0-7] [0-7]
    | '\\' [0-7] [0-7]
    | '\\' [0-7]
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
NUMBER : (DIGIT)+                         ;
FIELD  : FIELD_INITIAL (FIELD_CHARACTER)* ;
