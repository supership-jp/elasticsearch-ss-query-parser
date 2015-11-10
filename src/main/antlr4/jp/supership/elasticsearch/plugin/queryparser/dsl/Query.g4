/*
 * Supership Elasticsearch Query DSL.
 */
grammar Query;
import CommonLexerRules;

query      : (clause)*
           ;

clause     : expression (conjunction=('AND' | 'OR' | '|')? expression)*
           ;

expression : modifier=('-' | '_')? FIELD ':' term
           ;

term       : STRING                                          # StringTerm
           | NUMBER                                          # NumberTerm
           | '(' query ')'                                   # SubQueryTerm
           ;
