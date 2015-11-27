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

fragment TERM_INITIAL
    : ~(' ' | '\t' | '\n' | '\r' | '-' | '+' | '(' | ')' | ':' | '\"' | '\\' | '\u3000')
    ;

fragment TERM_CHARACTER
    : (~('\"' | '\\' | ':' | '^' | ' ' | '\t' | '\n' | '\r' | '\f') | ESCAPED_CHARACTER)
    ;

fragment QUATABLE_CHARACTER
    : (~('\"' | '\\') | ESCAPED_CHARACTER)
    ;

fragment INTEGER
    : (DIGIT)+
    ;

fragment EXPONENT
    : [eE] ('+' | '-')? INTEGER
    ;

fragment FLOAT
    : ('+' | '-')? (INTEGER)? '.' INTEGER (EXPONENT)?
    | ('+' | '-')? INTEGER EXPONENT
    ;

WS               : (' ' | '\t' | '\n' | '\r' | '\f')+ {skip();}  ;
LPAREN           : '('                                           ;
RPAREN           : ')'                                           ;
HAT              : '^'                                           ;
COLON            : ':'                                           ;
ASTERISC         : '*'                                           ;
QUESTION         : '?'                                           ;
CONJUNCTION_AND  : [aA][nN][dD]                                  ;
CONJUNCTION_OR   : [oO][rR]                                      ;
CONJUNCTION_DIS  : '|'                                           ;
MODIFIER_NEGATE  : '-'                                           ;
MODIFIER_REQUIRE : '+'                                           ;
QUOTED_TERM      : '\"' (TERM)* '\"'                             ;
TERM             : (STRING | NUMBER)                             ;
STRING           : TERM_INITIAL (TERM_CHARACTER)*                ;
NUMBER           : (INTEGER | FLOAT)                             ;
