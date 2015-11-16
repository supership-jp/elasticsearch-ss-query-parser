/*
 * Supership Elasticsearch Query DSL.
 */
grammar Query;
import CommonLexerRules;

query      : expression (conjunction=(CONJUNCTION_AND | CONJUNCTION_OR)? expression)*
           ;

expression : modifier=(MODIFIER_NEGATE | MODIFIER_REQUIERD)? FIELD ':' term
           ;

term       : STRING                                                     # StringTerm
           | NUMBER                                                     # NumberTerm
           | '(' query ')'                                              # SubQueryTerm
           ;
