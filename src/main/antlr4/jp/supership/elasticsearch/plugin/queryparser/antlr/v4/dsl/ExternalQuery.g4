/*
 * Supership Elasticsearch Query DSL.
 */
grammar ExternalQuery;
import CommonLexerRules;

query      : (expression)+
           ;

expression : CONJUNCTION_AND clause
           | CONJUNCTION_OR  clause
           | clause
           ;

clause     : MODIFIER_REQUIRE field
           | MODIFIER_NEGATE  field
           | field
           ;

field      : {_input.LT(2).getType() == COLON}? SINGLE_LITERAL COLON term
           | term
           ;

term       : SINGLE_LITERAL # BareTerm
           | PHRASE_LITERAL # QuotedTerm
           ;
