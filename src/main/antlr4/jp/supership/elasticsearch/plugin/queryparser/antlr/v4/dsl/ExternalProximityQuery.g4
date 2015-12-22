/*
 * Supership Elasticsearch Query DSL.
 */
grammar ExternalProximityQuery;
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

term       : SINGLE_LITERAL                  # BareTerm
           | DOUBLE_QUOTE query DOUBLE_QUOTE # QuotedTerm
           ;
