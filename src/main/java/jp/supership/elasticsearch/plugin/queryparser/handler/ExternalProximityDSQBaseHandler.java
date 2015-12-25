/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.handler;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanQuery;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl.ExternalProximityQueryBaseVisitor;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl.ExternalProximityQueryLexer;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl.ExternalProximityQueryParser;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.HandleException;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.TreeHandler;
import jp.supership.elasticsearch.plugin.queryparser.handlers.Initializable;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.ParseException;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.ProximityQueryEngine;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.spans.archetype.ProximityArchetype;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.spans.archetype.ProximityArchetypeTree;
import static jp.supership.elasticsearch.plugin.queryparser.lucene.util.config.QueryParserConfiguration.Wildcard;

/**
 * This class is responsible for handling query parser generated by ANTLR4 and delegates internal
 * Lucene APIs through the internal QueryEngine implementation.
 * The utilizing parser is generated by {@code jp.supership.elasticsearch.plugin.dsl.ExternalProximityQuery#g4},
 * hence the corresponding grammer is for "user land" queries.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
abstract class ExternalProximityDSQBaseHandler extends ExternalProximityQueryBaseVisitor<ProximityArchetype> implements TreeHandler, Initializable {
    /** Holds ES logger. */
    private static ESLogger LOGGER = Loggers.getLogger(ExternalDSQBaseHandler.class);

    /** Holds the currently handling clauses. */
    protected List<BooleanClause> clauses = new ArrayList<BooleanClause>();

    /**
     * Represents domain specific query context, besides holding the query constructing settings
     * this class is also responsible to maintain the currently constructing {@code Query} instance
     * which will be handled with the {@code Engine}.
     */
    protected class Metadata extends TreeHandler.Context {
	/** Holds constructing tree. */
	private ProximityArchetypeTree tree = new ProximityArchetypeTree();
	/** Previously assigned archetype's state. */
	private ProximityArchetype.State state = null;
	/** Holds previously detected cunjuntion. */
	private int conjunction = -1;
	/** Holds previously detected modifier. */
	private int modifier = -1;

	/** Constructor. */
	public Metadata() {
	    // DO NOTHING.
	}

	/** Returns the currently handling tree. */
	public ProximityArchetypeTree getTree() {
	    return this.tree;
	}

	/** Sets the currently handling tree. */
	public void setTree(ProximityArchetypeTree tree) {
	    this.tree = tree;
	}

	/** Returns the currently handling clauses. */
	public List<BooleanClause> getClauses() {
	    return clauses;
	}

	/** Sets the currently handling clauses. */
	public void setClauses(List<BooleanClause> clauses) {
	    clauses = clauses;
	}

	/** Returns the currently handling archetype state. */
	public ProximityArchetype.State getState() {
	    return this.state;
	}

	/** Sets the currently handling archetype state. */
	public void setState(ProximityArchetype.State state) {
	    this.state = state;
	}

	/** Returns the previous assigned conjunction. */
	public int getConjunction() {
	    return this.conjunction;
	}

	/** Sets the previously assigned conjunction. */
	public void setConjunction(int conjunction) {
	    this.conjunction = conjunction;
	}

	/** Returns the previous assigned modifier. */
	public int getModifier() {
	    return this.modifier;
	}

	/** Sets the previously assigned modifier. */
	public void setModifier(int modifier) {
	    this.modifier = modifier;
	}

	/** Clears currently handling properties. */
	public void clear() {
	    this.tree = new ProximityArchetypeTree();
	    this.state = null;
	    this.conjunction = -1;
	    this.modifier = -1;
	    super.clear();
	}
    }

    /** Holds this handler's context. */
    protected Metadata metadata = new Metadata();

    /** Holds this handler's suspended context. */
    protected Stack<Metadata> metadatas = new Stack<Metadata>();

    /** Holds query engine which is reponsible for parsing raw query strings. */
    protected ProximityQueryEngine engine;

    /** Holds query engine which is reponsible for parsing raw query strings. */
    protected Reader input;

    /**
     * {@inheritDoc}
     */
    @Override
    public ProximityArchetype getRoot() {
	ProximityArchetypeTree tree = this.metadata.getTree();
	if (tree != null) {
	    return tree.getRoot();
	} else {
	    return null;
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ascend(boolean refresh) {
	ProximityArchetypeTree tree = this.metadata.getTree();
	if (tree != null) {
	    this.resume();
	    tree.ascend(refresh);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void descend(int index, boolean mark) {
	ProximityArchetypeTree tree = this.metadata.getTree();
	if (tree != null) {
	    this.suspend();
	    tree.descend(index, mark);
	}
    }

    /**
     * Returns the number of children of current node.
     */
    private int getChildCount() {
	ProximityArchetypeTree tree = this.metadata.getTree();
	if (tree != null) {
	    return tree.getChildCount();
	} else {
	    return 0;
	}
    }

    /**
     * Suspends currently handling context.
     */
    private void suspend() {
	this.metadatas.push(this.metadata);
	this.metadata = new Metadata();
    }

    /**
     * Resumes the previously suspended context.
     */
    private void resume() {
	if (!this.metadatas.empty()) {
	    this.metadata = this.metadatas.pop();
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mark() {
	ProximityArchetypeTree tree = this.metadata.getTree();
	if (tree != null) {
	    tree.mark();
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rewind() {
	ProximityArchetypeTree tree = this.metadata.getTree();
	if (tree != null) {
	    tree.rewind();
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
	this.metadata.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert(ProximityArchetype node) {
	ProximityArchetypeTree tree = this.metadata.getTree();
	if (tree != null) {
	    tree.insert(node);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert(ProximityArchetype node, ProximityArchetype.State state) {
	ProximityArchetypeTree tree = this.metadata.getTree();
	if (tree != null) {
	    tree.insert(node, state);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProximityArchetype visitQuery(ExternalProximityQueryParser.QueryContext context) {
	try {
	    for (ExternalProximityQueryParser.ExpressionContext expression : context.expression()) {
		ProximityArchetype archetype = this.visit(expression);
		if (archetype != null) {
		    if (this.metadata.getConjunction() == -1) {
			this.insert(archetype, this.metadata.getState());
		    } else {
			ProximityArchetype root = this.getRoot();
			if (root != null) {
			    SpanQuery query = root.toQuery(this.metadata.getField(), this.metadata.getTree(), this.engine);
			    if (query != null) {
				this.engine.conjugate(this.metadata.getClauses(), this.metadata.getConjunction(), this.metadata.getModifier(), query);
			    }
			}
			this.clear();
		    }
		}
	    }
	    // This root node will be handled within the "handle" method.
	    return this.getRoot();
	} catch (Exception cause) {
	    throw new ParseCancellationException(cause);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProximityArchetype visitExpression(ExternalProximityQueryParser.ExpressionContext context) {
	try {
	    int operator = -1;
	    if (context.CONJUNCTION_OR() != null) {
		operator = ExternalProximityQueryParser.CONJUNCTION_OR;
	    } else if (context.CONJUNCTION_AND() != null) {
		operator = ExternalProximityQueryParser.CONJUNCTION_AND;
	    }
	    this.metadata.setConjunction(operator);
	    return this.visit(context.clause());
	} catch (Exception cause) {
	    throw new ParseCancellationException(cause);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProximityArchetype visitClause(ExternalProximityQueryParser.ClauseContext context) {
	try {
	    this.metadata.setModifier(context.MODIFIER_NEGATE() != null ? ExternalProximityQueryParser.MODIFIER_NEGATE : ExternalProximityQueryParser.MODIFIER_REQUIRE);
	    return this.visit(context.field());
	} catch (Exception cause) {
	    throw new ParseCancellationException(cause);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProximityArchetype visitField(ExternalProximityQueryParser.FieldContext context) {
	try {
	    this.metadata.setField(context.SINGLE_LITERAL() == null ? this.engine.getDefaultField() : context.SINGLE_LITERAL().getText());
	    return this.visit(context.term());
	} catch (Exception cause) {
	    throw new ParseCancellationException(cause);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProximityArchetype visitQuotedTerm(ExternalProximityQueryParser.QuotedTermContext context) {
	try {
	    ProximityArchetype archetype = new ProximityArchetype(
		this.metadata.getField(),
		this.metadata.getTerm(),
		false,
		this.engine.getPhraseSlop(),
		this.metadata.getModifier(),
		this.engine.getInOrder()
	    );
	    ProximityArchetype.State state = new ProximityArchetype.State();
	    state.isNearQuery(true);
	    this.metadata.setState(state);
	    this.insert(archetype, this.metadata.getState());
	    this.descend(this.getChildCount() - 1, false);
	    this.visit(context.query());
	    this.ascend(false);
	    return null;
	} catch (Exception cause) {
	    throw new ParseCancellationException(cause);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProximityArchetype visitBareTerm(ExternalProximityQueryParser.BareTermContext context) {
	try {
	    this.metadata.setTerm(context.SINGLE_LITERAL().getText());
	    ProximityArchetype.State state = new ProximityArchetype.State();
	    state.isTermQuery(true);
	    this.metadata.setState(state);
	    return new ProximityArchetype(
		this.metadata.getField(),
		this.metadata.getTerm(),
		false,
		this.engine.getPhraseSlop(),
		this.metadata.getModifier(),
		this.engine.getInOrder()
	    );
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
            ExternalProximityQueryLexer lexer = new ExternalProximityQueryLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            ExternalProximityQueryParser parser = new ExternalProximityQueryParser(tokens);
            ParseTree tree = parser.query();
	    ProximityArchetype root = this.visit(tree);
	    if (root != null) {
		SpanQuery query = root.toQuery(this.metadata.getField(), this.metadata.getTree(), this.engine);
		if (query != null) {
		    this.engine.conjugate(this.metadata.getClauses(), this.metadata.getConjunction(), this.metadata.getModifier(), query);
		}
	    }
	    return this.engine.getBooleanQuery(this.metadata.getClauses());
        } catch (Exception cause) {
            throw new HandleException(cause);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fetch(Reader input) {
        this.input = input;
    }
}
