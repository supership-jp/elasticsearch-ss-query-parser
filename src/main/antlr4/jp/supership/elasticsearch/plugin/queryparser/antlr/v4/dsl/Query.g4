/*
 * Supership Elasticsearch Query DSL.
 */
grammar Query;
import CommonLexerRules;

query      : expression (conjunction=(CONJUNCTION_AND | CONJUNCTION_OR)? expression)*
           ;

expression : modifier=(MODIFIER_NEGATE | MODIFIER_REQUIRE)? FIELD ':' term
           ;

term       : TERM_STRING   # StringTerm
           | TERM_NUMBER   # NumberTerm
           | TERM_FIELD    # FieldTerm
           | '(' query ')' # SubQueryTerm
           ;
