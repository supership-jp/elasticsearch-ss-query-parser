/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.classic;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.lucene.search.Query;
import jp.supership.elasticsearch.plugin.queryparser.dsl.QueryBaseVisitor;
import jp.supership.elasticsearch.plugin.queryparser.dsl.QueryParser;
import jp.supership.elasticsearch.plugin.queryparser.classic.intermediate.QueryEngine;

/**
 * PLACEHOLDER
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class DSQHandler extends QueryBaseVisitor<Query> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Query visitQuery(QueryParser.QueryContext context) {
	return this.visitChildren(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query visitClause(QueryParser.ClauseContext context) {
	return this.visitChildren(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query visitExpression(QueryParser.ExpressionContext context) {
	return this.visitChildren(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query visitStringTerm(QueryParser.StringTermContext context) {
	return this.visitChildren(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query visitNumberTerm(QueryParser.NumberTermContext context) {
	return this.visitChildren(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query visitSubQueryTerm(QueryParser.SubQueryTermContext context) {
	return this.visitChildren(context);
    }
}
