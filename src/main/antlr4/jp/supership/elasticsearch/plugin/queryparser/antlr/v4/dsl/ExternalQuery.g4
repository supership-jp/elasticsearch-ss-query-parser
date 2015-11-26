/*
 * Supership Elasticsearch Query DSL.
 */
grammar ExternalQuery;
import CommonLexerRules;

query      : expression (conjunction=(CONJUNCTION_AND | CONJUNCTION_OR)? expression)*
           ;

expression : modifier=(MODIFIER_NEGATE | MODIFIER_REQUIRE)? (TERM_FIELD COLON)? terms
           ;

terms      : TERM                # BareTerm
           | QUOTED_TERM         # QuotedTerm
           ;
