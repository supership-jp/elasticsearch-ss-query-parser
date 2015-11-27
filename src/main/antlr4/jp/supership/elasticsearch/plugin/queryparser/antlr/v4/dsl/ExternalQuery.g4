/*
 * Supership Elasticsearch Query DSL.
 */
grammar ExternalQuery;
import CommonLexerRules;

query      : (expression)+
           ;

expression : {_input.LT(1).getType() == CONJUNCTION_AND}? CONJUNCTION_AND clause
           | {_input.LT(1).getType() == CONJUNCTION_OR }? CONJUNCTION_OR  clause
           | clause
           ;

clause     : {_input.LT(1).getType() == MODIFIER_REQUIRE}? MODIFIER_REQUIRE field
           | {_input.LT(1).getType() == MODIFIER_NEGATE }? MODIFIER_NEGATE  field
           | field
           ;

field      : {_input.LT(2).getType() == COLON}? TERM COLON terms
           | terms
           ;

terms      : TERM        # BareTerm
           | QUOTED_TERM # QuotedTerm
           ;
