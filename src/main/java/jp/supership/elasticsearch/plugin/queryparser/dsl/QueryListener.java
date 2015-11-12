// Generated from jp/supership/elasticsearch/plugin/queryparser/dsl/Query.g4 by ANTLR 4.5
package jp.supership.elasticsearch.plugin.queryparser.dsl;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link QueryParser}.
 */
public interface QueryListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link QueryParser#query}.
	 * @param ctx the parse tree
	 */
	void enterQuery(QueryParser.QueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#query}.
	 * @param ctx the parse tree
	 */
	void exitQuery(QueryParser.QueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link QueryParser#clause}.
	 * @param ctx the parse tree
	 */
	void enterClause(QueryParser.ClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#clause}.
	 * @param ctx the parse tree
	 */
	void exitClause(QueryParser.ClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link QueryParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(QueryParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(QueryParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StringTerm}
	 * labeled alternative in {@link QueryParser#term}.
	 * @param ctx the parse tree
	 */
	void enterStringTerm(QueryParser.StringTermContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StringTerm}
	 * labeled alternative in {@link QueryParser#term}.
	 * @param ctx the parse tree
	 */
	void exitStringTerm(QueryParser.StringTermContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NumberTerm}
	 * labeled alternative in {@link QueryParser#term}.
	 * @param ctx the parse tree
	 */
	void enterNumberTerm(QueryParser.NumberTermContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NumberTerm}
	 * labeled alternative in {@link QueryParser#term}.
	 * @param ctx the parse tree
	 */
	void exitNumberTerm(QueryParser.NumberTermContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SubQueryTerm}
	 * labeled alternative in {@link QueryParser#term}.
	 * @param ctx the parse tree
	 */
	void enterSubQueryTerm(QueryParser.SubQueryTermContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SubQueryTerm}
	 * labeled alternative in {@link QueryParser#term}.
	 * @param ctx the parse tree
	 */
	void exitSubQueryTerm(QueryParser.SubQueryTermContext ctx);
}