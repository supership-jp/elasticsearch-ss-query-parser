/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.classic.intermediate;

import java.util.List;
// TODO: Check which CharStream is called.
// TODO: Check if the FastCharStream must be implemented or not.
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.queryparser.classic.TokenMgrError;
import org.apache.lucene.util.Version;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import static jp.supership.elasticsearch.plugin.queryparser.classic.intermediate.QueryParsingContext.Operator;
import static jp.supership.elasticsearch.plugin.queryparser.classic.intermediate.QueryParsingContext.Modifier;
import static jp.supership.elasticsearch.plugin.queryparser.classic.intermediate.QueryParsingContext.Conjunction;

/**
 * This class is responsible for instanciating Lucene queries, query parser delegates all sub-query
 * instanciation tasks to this class. This implementation assumes that concrete query handler will be 
 * constructed in accordance to the given ANTLR grammar.
 *
 * @author Shingo OKAWA
 * @since  08/11/2015
 */
public abstract class QueryParser extends QueryBuilder implements QueryHandler {
    /**
     * DO NOT CATCH THIS EXCEPTION.
     * This exception will be thrown when you are using methods that should not be used any longer.
     */
    public static class DeprecatedMethodCall extends Throwable {}

    /** Holds query-parsing-contect. */
    protected QueryParsingContext queryParsingContext;

    /**
     * Constructor.
     */
    protected QueryParser() {
	this.super(null);
	this.queryParsingContext = new StandardParsingContext();
    }

    /**
     * Constructor.
     */
    protected QueryParser(QueryParsingContext queryParsingContext) {
	this.super(null);
	this.queryPasingContext = queryParsingContext;
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
	    this.queryParsingContext.setPhaseQueryAutoGeneration(true);
	}
    }

    /**
     * Initializes a query factory.
     * @param field    the default field for query terms.
     * @param analyzer the analyzer which is applied for the given query..
     */
    public void init(String field, Analyzer analyzer) {
	this.setAnalyzer(analyzer);
	this.queryParsingContext.setDefaultField(field);
	this.queryParsingContext.setPhaseQueryAutoGeneration(false);
    }

    /**
     * Parses a query string and instanciates {@link org.apache.lucene.search.Query}.
     * @param  query the query string to be parsed.
     * @throws ParseException if the parsing fails.
     */
    public Query parse(String query) throws ParseException {
	this.fetch(new FastCharStream(new StringReader(query)));
	try {
	    Query instanciated = this.handle(this.queryParsingContext.getDefaultField());
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

    /**
     * Conjugates the given clause into the currently handling parsing context.
     * @param clauses     the preceding clauses which is currently handled by the query parser.
     * @param conjunction the assigen conjunction, this determines the proceeding process.
     * @param midifier    the preceeding modifier which midifies the handling clause.
     * @param query       the currently handling query.
     */
    protected void conjugate(List<BooleanClause> clauses, Conjunction conjunction, Modifier modifier, Query query) {
	boolean required;
	boolean prohibited;

	// If this term is introduced by AND, make the preceding term required, unless it is already prohibited.
	if (clauses.size() > 0 && conjunction == Conjunction.AND) {
	    BooleanClause clause = clauses.get(clauses.size() - 1);
	    if (!clause.isProhibited()) {
		clause.setOccur(BooleanClause.Occur.SHOULD);
	    }
	}

	// If this term is introduced by OR, make the preceeding term optional, unless it is prohibited.
	if (cluases.size() > 0 && this.defaultOperator == Operator.AND && conjunction == Conjunction.OR) {
	    BooleanCaluse clause = cluases.get(clauses.get(clauses.size() - 1));
	    if (!clause.isProhibited()) {
		clause.setOccur(BooleanClause.Occur.SHOULD);
	    }
	}

	// A null query might have been passed, that means the term might have been filtered out by the analyzer.
	if (query == null) {
	    return;
	}

	// The term is set to be REQUIRED if the term is introduced by AND or +;
	// otherwise, REQUIRED if not PROHIBITED and not introduced by OR.
	if (this.defaultOperator == Operator.OR) {
	    prohibited = (modifier == Modifier.NOT);
	    required = (modifier == Modifier.REQUIRED);
	    if (conjunction == Conjunction.AND && !prohibited) {
		required = true;
	    }
	// The term is set ti be PROHIBITED if the term is introduced by NOT;
        // otherwise, REQIURED if not PROHIBITED and not introduce by OR.
	} else {
	    prohibited = (modifier == Modifier.NOT);
	    required = (!prohibited && conjunction != Conjunction.OR);
	}

	if (required && !prohibited) {
	    clauses.add(this.createBooleanClause(query, BooleanClause.Occur.MUST));
	} else if (!required && !prohibited) {
	    clauses.add(this.createBooleanClause(query, BooleanClause.Occur.SHOULD));
	} else if (!required && prohibited) {
	    clauses.add(this.createBooleanClause(query, BooleanClause.Occur.MUST_NOT));
	} else {
	    throw new RuntimeException("clause could not be both required and prohibited.");
	}
    }

    /**
     * Returns 
     * @throws ParseException if the parsing fails.
     */
    protected Query getFieldQuery(String field, String query, boolean quoted) throws ParseException {
	return this.newFieldQuery(this.getAnalyzer(), field, query, quoted);
    }

    /**
     *
     * @throws ParseException if the parsing fails.
     */
    protected Query newFieldQuery(Analyzer analyzer, String field, String query, boolean quoted) throws ParseException {
	BooleanClause.Occur occurence = this.queryParsingContext.getDefaultOperator() == Operator.AND ? BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD;

	String analyzerName = null;
	if (this.analyzer instanceof NamedAnalyzer) {
	    analyzerName = ((NamedAnalyzer) this.analyzer).name();
	}

	if (analyzerName != null && ()) {
	}
    }
}
