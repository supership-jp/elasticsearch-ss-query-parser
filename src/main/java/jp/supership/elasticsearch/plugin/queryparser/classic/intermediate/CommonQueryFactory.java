/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.classic.intermediate;

// TODO: Check which CharStream is called.
// TODO: Check if the FastCharStream must be implemented or not.
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.queryparser.classic.TokenMgrError;
import org.apache.lucene.util.Version;

/**
 * This class is responsible for instanciating Lucene internal queries, query parser delegates
 * all sub-query instanciation tasks to this class.
 * This implementation assumes that concrete query handler will be constructed in accordance to
 * the given ANTLR grammar.
 *
 * @author Shingo OKAWA
 * @since  08/11/2015
 */
public abstract class CommonQueryFactory extends QueryBuilder {
    /**
     * DO NOT CATCH THIS EXCEPTION.
     * This exception will be thrown when you are using methods that should not be used any longer.
     */
    public static class DeprecatedMethodCall extends Throwable {}

    /** Holds default field for query terms. */
    protected String defaultField;

    /** Holds the default operator parsers use to combine query terms. */
    protected Operator defaultOperator = Operators.OR;

    /** Holds default field for query terms. */
    protected boolean phaseQueryAutoGeneration;

    /**
     * Creates {@link org.apache.lucene.search.Query} in accordance with the given raw query string.
     * @param  field the default field for query terms.
     * @throws ParseException if the parsing fails.
     */
    public abstract Query parse(String field) throws ParseException;

    // This method is temporal.
    public abstract void fetch();

    /**
     * Constructor.
     */
    protected CommonQueryFactory() {
	this.super(null);
    }

    /**
     * Initializes a query factory.
     * @param version  Lucene version to be matched. See <a href="QueryParser.html#version">here</a>.
     * @param field    the default field for query terms.
     * @param analyzer the analyzer which is applied for the given query..
     */
    public void init(Version version, String field, Analyzer analyzer) {
	this.init(field, analyzer);
	if (version.onOrAfter(Version.LUCENE_3_1) == false) {
	    this.setPhaseQueryAutoGeneration(true);
	}
    }

    /**
     * Initializes a query factory.
     * @param field    the default field for query terms.
     * @param analyzer the analyzer which is applied for the given query..
     */
    public void init(String field, Analyzer analyzer) {
	this.setAnalyzer(analyzer);
	this.setDefaultField(field);
	this.setPhaseQueryAutoGeneration(false);
    }

    /**
     * Parses a query string and instanciates {@link org.apache.lucene.search.Query}.
     * @param  query the query string to be parsed.
     * @throws ParseException if the parsing fails.
     */
    public Query create(String query) throws ParseException {
	this.fetch(new FastCharStream(new StringReader(query)));
	try {
	    Query instanciated = this.parse(this.defaultField);
	    return instanciated != null ? instanciated : this.newBooleanQuery(false);
	} catch (ParseException cause) {
	    ParseException exception = new ParseException("could not parse '" + query + "': " + cause.getMessage());
	    exception.initCause(cause);
	    throw exception;
	} catch (TokenMgrError cause) {
	    ParseException exception = new ParseException("could not parse '" + query + "': " + cause.getMessage());
	    exception.initCause(cause);
	    throw exception;
	} catch (BooleanQuery.TooManyClauses cause) {
	    ParseException exception = new ParseException("could not parse '" + query + "': too many boolean clauses");
	    exception.initCause(cause);
	    throw exception;
	}
    }
}
