/*
 * Supership Elasticsearch Query DSL.
 */
grammar InternalQuery;
import CommonLexerRules;

query      : (expression)+
           ;

expression : CONJUNCTION_AND clause
           | CONJUNCTION_DIS clause
           | CONJUNCTION_OR  clause
           | LPAREN query RPAREN
           | clause
           ;

clause     : MODIFIER_REQUIRE field (HAT SINGLE_LITERAL)?
           | MODIFIER_NEGATE  field (HAT SINGLE_LITERAL)?
           | field (HAT SINGLE_LITERAL)?
           ;

field      : {_input.LT(2).getType() == COLON}? SINGLE_LITERAL COLON term
           | term
           ;

term       : SINGLE_LITERAL # BareTerm
           | PHRASE_LITERAL # QuotedTerm
           ;
