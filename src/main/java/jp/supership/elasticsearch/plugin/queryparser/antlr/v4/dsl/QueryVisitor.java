// Generated from jp/supership/elasticsearch/plugin/queryparser/antlr/v4/dsl/Query.g4 by ANTLR 4.5
package jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link QueryParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface QueryVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link QueryParser#query}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuery(QueryParser.QueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link QueryParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(QueryParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StringTerm}
	 * labeled alternative in {@link QueryParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringTerm(QueryParser.StringTermContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NumberTerm}
	 * labeled alternative in {@link QueryParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberTerm(QueryParser.NumberTermContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FieldTerm}
	 * labeled alternative in {@link QueryParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldTerm(QueryParser.FieldTermContext ctx);
	/**
	 * Visit a parse tree produced by the {@code SubQueryTerm}
	 * labeled alternative in {@link QueryParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubQueryTerm(QueryParser.SubQueryTermContext ctx);
}