/*
 * Supership Elasticsearch Query DSL.
 */
grammar InternalQuery;
import CommonLexerRules;

query      : (expression)+
           ;

expression : {_input.LT(1).getType() == CONJUNCTION_AND}? CONJUNCTION_AND clause
           | {_input.LT(1).getType() == CONJUNCTION_DIS}? CONJUNCTION_DIS clause
           | {_input.LT(1).getType() == CONJUNCTION_OR }? CONJUNCTION_OR  clause
           | {_input.LT(1).getType() == LPAREN}? LPAREN query RPAREN
           | clause
           ;

clause     : {_input.LT(1).getType() == MODIFIER_REQUIRE}? MODIFIER_REQUIRE field (HAT NUMBER)?
           | {_input.LT(1).getType() == MODIFIER_NEGATE }? MODIFIER_NEGATE  field (HAT NUMBER)?
           | field (HAT NUMBER)?
           ;

field      : {_input.LT(2).getType() == COLON}? TERM COLON terms
           | terms
           ;

terms      : TERM        # BareTerm
           | QUOTED_TERM # QuotedTerm
           ;
