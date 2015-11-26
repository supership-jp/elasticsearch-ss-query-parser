/*
 * Supership Elasticsearch Query DSL.
 */
grammar InternalQuery;
import CommonLexerRules;

query      : expression (conjunction=(CONJUNCTION_AND | CONJUNCTION_OR | CONJUNCTION_DIS)? expression)*
           | LPAREN query RPAREN
           ;

expression : modifier=(MODIFIER_NEGATE | MODIFIER_REQUIRE)? (TERM_FIELD COLON)? terms (HAT NUMBER)?
           ;

terms      : TERM                # BareTerm
           | QUOTED_TERM         # QuotedTerm
           ;
