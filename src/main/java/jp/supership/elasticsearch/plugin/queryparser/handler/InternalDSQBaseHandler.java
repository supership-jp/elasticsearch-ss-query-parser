/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.handler;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl.InternalQueryBaseVisitor;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl.InternalQueryLexer;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl.InternalQueryParser;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.HandleException;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.QueryHandler;
import jp.supership.elasticsearch.plugin.queryparser.handlers.Initializable;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.ParseException;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.QueryEngine;
import static jp.supership.elasticsearch.plugin.queryparser.lucene.util.config.QueryParserConfiguration.Wildcard;

/**
 * This class is responsible for handling query parser generated by ANTLR4 and delegates internal
 * Lucene APIs through the internal QueryEngine implementation.
 * The utilizing parser is generated by {@code jp.supership.elasticsearch.plugin.dsl.InternalQuery#g4},
 * hence the corresponding grammer is for "user land" queries.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
abstract class InternalDSQBaseHandler extends InternalQueryBaseVisitor<Query> implements QueryHandler, Initializable {
    /** Holds ES logger. */
    private static ESLogger LOGGER = Loggers.getLogger(InternalDSQBaseHandler.class);

    /**
     * Represents domain specific query context, besides holding the query constructing settings
     * this class is also responsible to maintain the currently constructing {@code Query} instance
     * which will be handled with the {@code Engine}.
     */
    protected class Metadata extends QueryHandler.Context {
	/** Holds currently constructing query. */
	public Query query = null;
	/** Holds constructing clauses. */
	public List<BooleanClause> clauses = new ArrayList<BooleanClause>();
	/** Holds previously detected cunjuntion. */
	public int conjunction = -1;
	/** Holds previously detected modifier. */
	public int modifier = -1;
	/** Holds previously detected boosting coefficient. */
	public String boost = null;

	/** Returns the currently handling query. */
	public Query getQuery() {
	    return this.query;
	}

	/** Sets the currently handling query. */
	public void setQuery(Query query) {
	    this.query = query;
	}

	/** Returns the currently handling clauses. */
	public List<BooleanClause> getClauses() {
	    return this.clauses;
	}

	/** Sets the currently handling clauses. */
	public void setClauses(List<BooleanClause> clauses) {
	    this.clauses = clauses;
	}

	/** Returns the assigned conjunction. */
	public int getConjunction() {
	    return this.conjunction;
	}

	/** Sets the conjunction setting. */
	public void setConjunction(int conjunction) {
	    this.conjunction = conjunction;
	}

	/** Returns the assigned modifier. */
	public int getModifier() {
	    return this.modifier;
	}

	/** Sets the modifier setting. */
	public void setModifier(int modifier) {
	    this.modifier = modifier;
	}

	/** Returns the assigned boost value. */
	public String getBoost() {
	    return this.boost;
	}

	/** Sets the boost setting. */
	public void setBoost(String boost) {
	    this.boost = boost;
	}
    }

    /** Holds this handler's context. */
    protected InternalDSQBaseHandler.Metadata metadata = new InternalDSQBaseHandler.Metadata();

    /** Holds query engine which is reponsible for parsing raw query strings. */
    protected QueryEngine engine;

    /** Holds query engine which is reponsible for parsing raw query strings. */
    protected Reader input;

    /**
     * {@inheritDoc}
     */
    @Override
    public Query visitQuery(InternalQueryParser.QueryContext context) {
	try {
	    for (InternalQueryParser.ExpressionContext expression : context.expression()) {
		this.metadata.setQuery(visit(expression));
		if (this.metadata.getBoost() != null && this.metadata.getQuery() != null) {
		    this.metadata.getQuery().setBoost(Float.parseFloat(this.metadata.getBoost()));
		    this.metadata.setBoost(null);
		}
		this.engine.conjugate(this.metadata.getClauses(), this.metadata.getConjunction(), this.metadata.getModifier(), this.metadata.getQuery());
	    }
	    return this.engine.getBooleanQuery(this.metadata.getClauses());
	} catch (Exception cause) {
	    throw new ParseCancellationException(cause);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query visitExpression(InternalQueryParser.ExpressionContext context) {
	try {
	    if (context.CONJUNCTION_OR() != null) {
		this.metadata.setConjunction(InternalQueryParser.CONJUNCTION_OR);
	    } else if (context.CONJUNCTION_DIS() != null) {
		this.metadata.setConjunction(InternalQueryParser.CONJUNCTION_DIS);
	    } else {
		this.metadata.setConjunction(InternalQueryParser.CONJUNCTION_AND);
	    }
	    return visit(context.clause());
	} catch (Exception cause) {
	    throw new ParseCancellationException(cause);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query visitClause(InternalQueryParser.ClauseContext context) {
	try {
	    this.metadata.setModifier(context.MODIFIER_NEGATE() != null ? InternalQueryParser.MODIFIER_NEGATE : InternalQueryParser.MODIFIER_REQUIRE);
	    this.metadata.setBoost(context.NUMBER() == null ? null : context.NUMBER().getText());
	    return visit(context.field());
	} catch (Exception cause) {
	    throw new ParseCancellationException(cause);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query visitField(InternalQueryParser.FieldContext context) {
	try {
	    this.metadata.setField(context.TERM() == null ? this.engine.getDefaultField() : context.TERM().getText());
	    return visit(context.terms());
	} catch (Exception cause) {
	    throw new ParseCancellationException(cause);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query visitQuotedTerm(InternalQueryParser.QuotedTermContext context) {
	try {
	    this.metadata.setTerm(context.QUOTED_TERM().getText());
	    return this.dispatchQuotedToken(this.metadata);
	} catch (Exception cause) {
	    throw new ParseCancellationException(cause);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query visitBareTerm(InternalQueryParser.BareTermContext context) {
	try {
	    this.metadata.setTerm(context.TERM().getText());
	    return this.dispatchBareToken(this.metadata);
	} catch (Exception cause) {
	    throw new ParseCancellationException(cause);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query handle(String queryText) throws HandleException {
        this.fetch(new StringReader(queryText));
        try {
            Query instanciated = this.visit();
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug(instanciated.toString());
	    }
            return instanciated != null ? instanciated : this.engine.getBooleanQuery(false);
        } catch (HandleException cause) {
            HandleException exception = new HandleException("could not parse '" + queryText + "': " + cause.getMessage());
            exception.initCause(cause);
            throw exception;
        } catch (BooleanQuery.TooManyClauses cause) {
            HandleException exception = new HandleException("could not parse '" + queryText + "': too many boolean clauses");
            exception.initCause(cause);
            throw exception;
        }
    }

    /**
     * Visits AST and generates {@code Query}.
     * @throws HandleException if the handling fails.
     */
    private Query visit() throws HandleException {
        try {
            ANTLRInputStream input = new ANTLRInputStream(this.input);
            InternalQueryLexer lexer = new InternalQueryLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            InternalQueryParser parser = new InternalQueryParser(tokens);
            ParseTree tree = parser.query();
            return this.visit(tree);
        } catch (Exception cause) {
            throw new HandleException(cause);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispatch(QueryHandler.Context context) {
	this.engine.dispatch(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query dispatchBareToken(QueryHandler.Context context) throws HandleException {
        return this.engine.dispatchBareToken(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query dispatchQuotedToken(QueryHandler.Context context) throws HandleException {
        return this.engine.dispatchQuotedToken(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fetch(Reader input) {
        this.input = input;
    }
}
