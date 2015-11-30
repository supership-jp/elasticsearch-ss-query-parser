/*
 * Common Lexer rules.
 */
lexer grammar CommonLexerRules;

fragment ESCAPED_CHARACTER
    : '\\' ~[]
    ;

fragment TERM_INITIAL
    : ~(' ' | '\t' | '\n' | '\r' | '-' | '+' | '(' | ')' | ':' | '\"' | '\\' | '\u3000' | '^')
    ;

fragment TERM_CHARACTER
    : (~('\"' | '\\' | ':' | '^' | ' ' | '\t' | '\n' | '\r' | '\f') | ESCAPED_CHARACTER)
    ;

fragment QUATABLE_CHARACTER
    : (~('\"' | '\\') | ESCAPED_CHARACTER)
    ;

fragment INTEGER
    : ('0'..'9')+
    ;

fragment EXPONENT
    : ('e' | 'E') ('+' | '-')? INTEGER
    ;

fragment FLOAT
    : ('+' | '-')? (INTEGER)? '.' INTEGER (EXPONENT)?
    | ('+' | '-')? INTEGER EXPONENT
    ;

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
PHRASE_LITERAL   : '\"' (SINGLE_LITERAL)* '\"'                   ;
SINGLE_LITERAL   : (NUMBER | STRING)                             ;
NUMBER           : (INTEGER | FLOAT)                             ;
STRING           : TERM_INITIAL (TERM_CHARACTER)*                ;
WS               : (' ' | '\t' | '\n' | '\r' | '\f')+ {skip();}  ;
