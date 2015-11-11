/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.classic.intermediate;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
// TODO: Check which CharStream is called.
// TODO: Check if the FastCharStream must be implemented or not.
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.analysis.tokenattributes.PoistionIncrementAttreibute;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.TokenMgrError;
import org.apache.lucene.queryparser.flexible.standard.CommonQueryParserConfiguration;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.Version;
import org.apache.lucene.util.automaton.RegExp;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import jp.supership.elasticsearch.plugin.queryparser.util.StringUtils;

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

    /**
     * This interface specifies the implementing class can be configured in accordance with the
     * Lucene's {@code CommonQueryParserConfiguration} API and some additional context.
     *
     * @author Shingo OKAWA
     * @since  08/11/2015
     */
    public static interface Context extends CommonQueryParserConfiguration {
	/**
	 * Holds pre-defined operators.
	 */
	public static enum Operator {
	    NONE("", 0),
	    AND("AND", 1),
	    OR("OR", 1)

	    // Holds actual string expression.
	    private String expression;
	    // Holds actual string expression.
	    private int precedence;

	    // Constructor.
	    private Operator(String expression, int precedence) {
		this.expression = expression;
		this.precedence = precedence;
	    }

	    // Returns corresponding enum instance from the given expression.
	    public static Operator find(String expression) {
		for (Operator operator : Operator.values()) {
		    if (expression.equals(operator.expression)) {
			return operator;
		    }
		}
		return Operator.NONE;
	    }

	    /** @inheritDoc */
	    @Override
	    public String toString() {
		return this.expression;
	    }
	}

	/**
	 * Holds pre-defined modifiers.
	 */
	public static enum Modifier {
	    NONE("", 0),
	    NOT("-", 1),
	    REQUIRED("_", 1)

	    // Holds actual string expression.
	    private String expression;
	    // Holds actual string expression.
	    private int precedence;

	    // Constructor.
	    private Modifier(String expression, int precedence) {
		this.expression = expression;
		this.precedence = precedence;
	    }

	    // Returns corresponding enum instance from the given expression.
	    public static Modifier find(String expression) {
		for (Modifier modifier : Modifier.values()) {
		    if (expression.equals(modifier.expression)) {
			return modifier;
		    }
		}
		return Modifier.NONE;
	    }

	    /** @inheritDoc */
	    @Override
	    public String toString() {
		return this.expression;
	    }
	}

	/**
	 * Holds pre-defined conjuinctions.
	 */
	public static enum Conjunction {
	    NONE("", 0),
	    AND("AND", 1),
	    OR("OR", 1)

	    // Holds actual string expression.
	    private String expression;
	    // Holds actual string expression.
	    private int precedence;

	    // Constructor.
	    private Conjunction(String expression, int precedence) {
		this.expression = expression;
		this.precedence = precedence;
	    }

	    // Returns corresponding enum instance from the given expression.
	    public static Conjunction find(String expression) {
		for (Conjunction conjunction : Conjunction.values()) {
		    if (expression.equals(conjunction.expression)) {
			return conjunction;
		    }
		}
		return Conjunction.NONE;
	    }

	    /** @inheritDoc */
	    @Override
	    public String toString() {
		return this.expression;
	    }
	}

	/**
	 * Holds pre-defined wildcards.
	 */
	public static enum Wildcard {
	    NONE("", 0),
	    ANY_STRING("*", 1),
	    ANY_CHARACTER("?", 1)

	    // Holds actual string expression.
	    private String expression;
	    // Holds actual string expression.
	    private int precedence;

	    // Constructor.
	    private Wildcard(String expression, int precedence) {
		this.expression = expression;
		this.precedence = precedence;
	    }

	    // Returns corresponding enum instance from the given expression.
	    public static Wildcard find(String expression) {
		for (Wildcard wildcard : Wildcard.values()) {
		    if (expression.equals(wildcard.expression)) {
			return wildcard;
		    }
		}
		return Wildcard.NONE;
	    }

	    /** @inheritDoc */
	    @Override
	    public String toString() {
		return this.expression;
	    }
	}

	/**
	 * Sets the default field.
	 * @param defaultField the default field to be set.
	 */
	public void setDefaultField(String defaultField);

	/**
	 * Returns the default field.
	 * @return the assigned default field.
	 */
	public String getDefaultField();

	/**
	 * Sets the boolean operator for the query parser.
	 * In default, <code>Operators.OR</code> is set, i.e., terms without any modifiers are considered optional:
	 * for example <code>capital of Hungary</code> is equal to <code>capital OR of OR Hungary</code>.<br/>
	 * @param defaultOperator the boolean operator to be set.
	 */
	public void setDefaultOperator(Operator defaultOperator);

	/**
	 * Returns the default boolean operator, which will be either <code>Operators.AND</code> or <code>Operators.OR</code>.
	 * @return the assigned default boolean operator.
	 */
	public Operator getDefaultOperator();

	/**
	 * {@link PhraseQuery}s will be automatically generated when the analyzer returns more than one term
	 * from whitespace-delimited text, if this value is set to be true. This behavior may not be
	 * appropriate for some languages.
	 * @param phraseQueryAutoGeneration the value to be set.
	 */
	public void setPhraseQueryAutoGeneration(boolean phraseQueryAutoGeneration);

	/**
	 * Returns the configured value of the phase-query-auto-generation functionality.
	 * @return the assigned value of phase-query-auto-generation functionality.
	 */
	public boolean getPhraseQueryAutoGeneration();

	/**
	 * Sets the date resolution used by range queries for a specific field.
	 * @param field          field for which the date resolution is to be set.
	 * @param dateResolution date resolution to set.
	 */
	public void setDateResolution(String field, DateTools.Resolution dateResolution);

	/**
	 * Returns the date resolution that is used by RangeQueries for the given field.
	 * @return null if no default or field specific date resolution has been set for the given field.
	 */
	public DateTools.Resolution getDateResolution(String field);

	/**
	 * {@link TermRangeQuery}s will be analyzed if this value is set to be true.
	 * For example, setting this to true can enable analyzing terms into collation keys for locale-sensitive
	 * {@link TermRangeQuery}.
	 * @param rangeTermAnalysis whether or not terms should be analyzed for RangeQuerys
	 */
	public void setRangeTermAnalysis(boolean rangeTermAnalysis);

	/**
	 * Returns the configured value of the range-term-analysis functionality.
	 * @return whether or not to analyze range terms when constructing {@link TermRangeQuery}s.
	 */
	public boolean getRangeTermAnalysis();

	/**
	 * Sets the maximum number of states that determinizing a regexp query can result in.
	 * @param maxDeterminizedStates the maximum number of states to be set.
	 */
	public void setMaxDeterminizedStates(int maxDeterminizedStates);

	/**
	 * Returns the configured maximum number of states.
	 * @return the maximum number of states that determinizing a regexp query can result in.
	 */
	public int getMaxDeterminizedStates();
    }

    /** Holds query-parsing-contect. */
    protected QueryParser.Context context;

    /**
     * Constructor.
     */
    protected QueryParser() {
        this.super(null);
        this.context = new DefaultQueryParserContext();
    }

    /**
     * Constructor.
     */
    protected QueryParser(QueryParser.Context context) {
        this.super(null);
        this.context = context;
    }

    /**
     * Initializes a query parser.
     * @param version  Lucene version to be matched. See <a href="QueryParser.html#version">here</a>.
     * @param field    the default field for query terms.
     * @param analyzer the analyzer which is applied for the given query..
     */
    public void init(Version version, String field, Analyzer analyzer) {
        this.init(field, analyzer);
        if (version.onOrAfter(Version.LUCENE_3_1) == false) {
            this.context.setPhaseQueryAutoGeneration(true);
        }
    }

    /**
     * Initializes a query parser.
     * @param field    the default field for query terms.
     * @param analyzer the analyzer which is applied for the given query..
     */
    public void init(String field, Analyzer analyzer) {
        this.setAnalyzer(analyzer);
        this.context.setDefaultField(field);
        this.context.setPhaseQueryAutoGeneration(false);
    }

    /**
     * Parses a query string and instanciates {@link org.apache.lucene.search.Query}.
     * @param  queryText the query string to be parsed.
     * @throws ParseException if the parsing fails.
     */
    public Query parse(String queryText) throws ParseException {
        this.fetch(new FastCharStream(new StringReader(queryText)));
        try {
            Query instanciated = this.handle(this.context.getDefaultField());
            return instanciated != null ? instanciated : this.newBooleanQuery(false);
        } catch (ParseException cause) {
            ParseException exception = new ParseException("could not parse '" + queryText + "': " + cause.getMessage());
            exception.initCause(cause);
            throw exception;
        } catch (TokenMgrError cause) {
            ParseException exception = new ParseException("could not parse '" + queryText + "': " + cause.getMessage());
            exception.initCause(cause);
            throw exception;
        } catch (BooleanQuery.TooManyClauses cause) {
            ParseException exception = new ParseException("could not parse '" + queryText + "': too many boolean clauses");
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
    protected void conjugate(List<BooleanClause> clauses, Context.Conjunction conjunction, Context.Modifier modifier, Query query) {
        boolean required;
        boolean prohibited;

        // If this term is introduced by AND, make the preceding term required, unless it is already prohibited.
        if (clauses.size() > 0 && conjunction == Context.Conjunction.AND) {
            BooleanClause clause = clauses.get(clauses.size() - 1);
            if (!clause.isProhibited()) {
                clause.setOccur(BooleanClause.Occur.SHOULD);
            }
        }

        // If this term is introduced by OR, make the preceeding term optional, unless it is prohibited.
        if (cluases.size() > 0 && this.defaultOperator == Context.Operator.AND && conjunction == Context.Conjunction.OR) {
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
        if (this.defaultOperator == Context.Operator.OR) {
            prohibited = (modifier == Context.Modifier.NOT);
            required = (modifier == Context.Modifier.REQUIRED);
            if (conjunction == Context.Conjunction.AND && !prohibited) {
                required = true;
            }
        // The term is set ti be PROHIBITED if the term is introduced by NOT;
        // otherwise, REQIURED if not PROHIBITED and not introduce by OR.
        } else {
            prohibited = (modifier == Context.Modifier.NOT);
            required = (!prohibited && conjunction != Context.Conjunction.OR);
        }

        if (required && !prohibited) {
            clauses.add(this.newBooleanClause(query, BooleanClause.Occur.MUST));
        } else if (!required && !prohibited) {
            clauses.add(this.newBooleanClause(query, BooleanClause.Occur.SHOULD));
        } else if (!required && prohibited) {
            clauses.add(this.newBooleanClause(query, BooleanClause.Occur.MUST_NOT));
        } else {
            throw new RuntimeException("clause could not be both required and prohibited.");
        }
    }

    /**
     * Returns {@code FieldQuery} in accordance to the assigned configuration.
     * @param  field the currently handling field.
     * @param  queryText the currently handling raw query string.
     * @param  quoted this value must be ser true if the handling query is considered to be quoted.
     * @return the resulting {@code Query} instance.
     * @throws ParseException if the parsing fails.
     */
    protected Query getFieldQuery(String field, String queryText, boolean quoted) throws ParseException {
        return this.newFieldQuery(this.getAnalyzer(), field, queryText, quoted);
    }

    /**
     * Base implementation delegates to {@link #getFieldQuery(String, String, boolean)}.
     * This method may be overridden, for example, to return a SpanNearQuery instead of a PhraseQuery.
     * @exception org.apache.lucene.queryparser.classic.ParseException throw in overridden method to disallow
     */
    protected Query getFieldQuery(String field, String queryText, int phraseSlop) throws ParseException {
        Query query = this.getFieldQuery(field, queryText, true);

        if (query instanceof PhraseQuery) {
            ((PhraseQuery) query).setSlop(phraseSlop);
        }
        if (query instanceof MultiPhraseQuery) {
            ((MultiPhraseQuery) query).setSlop(phrasSlop);
        }

        return query;
    }

    /**
     * Returns {@code FieldQuery} in accordance to the assigned configuration.
     * @param  analyzer the analyzer instance which is responsible for the raw query analysis.
     * @param  field the currently handling field.
     * @param  queryText the currently handling raw query string.
     * @param  quoted this value must be ser true if the handling query is considered to be quoted.
     * @return the resulting {@code Query} instance.
     * @throws ParseException if the parsing fails.
     */
    protected Query newFieldQuery(Analyzer analyzer, String field, String queryText, boolean quoted) throws ParseException {
        BooleanClause.Occur occurence = this.context.getDefaultOperator() == Context.Operator.AND ? BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD;

        String analyzerName = null;
        if (this.analyzer instanceof NamedAnalyzer) {
            analyzerName = ((NamedAnalyzer) this.analyzer).name();
        }

        if (analyzerName != null && (analyzerName.startsWith("ngram_"))) {
            List<BooleanClause> clauses = new ArrayList<BooleanClause>();
            String tokens = queryText.split(StringUtils.UNICODE_START_OF_HEADING);
            for (String token : tokens) {
                Query query = this.createFieldQuery(this.analyzer, occurence, field, queryText, true, 0);
                if (query != null) {
                    clauses.add(new BooleanClause(query, occurence));
                }
            }
            if (clauses.size() == 0) {
                return null;
            }
            return this.getBooleanQuery(clauses, true);
        // TODO: THIS MUST BE HANDLED WITHIN {@code TokenFileter} WHICH IMPLEMENTS NAIVE BAYSIAN FILTER.
        } else if (quoted == false && queryText.matches("^\\d+(\\.\\d+)?.{1,2}?$")) {
	    quoted = true;
	    this.context.setPhraseSlop(0);
        } else {
	    quoted = quoted || this.context.getPhraseQueryAutoGeneration();
	}

	return this.createFieldQuery(analyzer, occurence, field, queryText, quoted, this.context.getPhraseSlop());
    }

    /**
     * @inheritDoc
     */
    @Override
    protected Query createFieldQuery(Analyzer analyzer, BooleanClause.Occur operator, String field, String queryText, boolean quoted, int phraseSlop) {
        assert operator == BooleanClause.Occur.SHOULD || operator == BooleanClause.Occur.MUST;
        TokenStream source = null;
        TokenStreamHandler handler = null;

        try {
            source = analyzer.tokenStream(field, queryText);
            handler = new TokenStreamHandler(source);
        } catch (IOException cause) {
            throw new RuntimeException("error analyzing query text", cause);
        } finally {
            IOUtils.closeWhileHandlingException(source);
        }

        BytesRef bytes = handler.getBytesRef();
        if (handler.numberOfTokens == 0) {
            return null;
        } else if (handler.numberOfTokens == 1) {
            try {
                boolean hasNext = handler.incrementToken();
                assert hasNext == true;
                handler.fillBytesRef();
            } catch (IOException e) {
                // DO NOTHING, because we know the number of tokens
            }
            return this.newTermQuery(new Term(field, BytesRef.deepCopyOf(bytes)));
        } else {
            if (handler.severalTokensAtSamePosition || (!quoted)) {
                // Not a phrase query.
                if (handler.positionCount == 1 || (!quoted)) {
                    if (handler.positionCount == 1) {
                        BooleanQuery query = this.newBooleanQuery(true);
                        for (int i = 0; i < handler.numberOfTokens; i++) {
                            try {
                                boolean hasNext = handler.incrementToken();
                                assert hasNext == true;
                                handler.fillBytesRef();
                            } catch (IOException e) {
                                // DO NOTHING, because we know the number of tokens
                            }
                            Query current = this.newTermQuery(new Term(field, BytesRef.deepCopyOf(bytes)));
                            query.add(current, BooleanClause.Occur.SHOULD);
                        }
                        return query;
                    } else {
                        BooleanQuery query = this.newBooleanQuery(false);
                        Query current = null;
                        for (int i = 0; i < handler.numberOfTokens; i++) {
                            try {
                                boolean hasNext = handler.incrementToken();
                                assert hasNext == true;
                                handler.fillBytesRef();
                            } catch (IOException e) {
                                // DO NOTHING, because we know the number of tokens
                            }
                            if (handler.positionIncrement != null && handler.getPositionIncrement() == 0) {
                                if (!(current instanceof BooleanQuery)) {
                                    Query _current = current;
                                    current = new DisjunctionMaxQuery(0.0f);
                                    ((DisjunctionMaxQuery) current).add(_current);
                                }
                                ((DisjunctionMaxQuery) current).add(this.newTermQuery(new Term(field, BytesRef.deepCopyOf(bytes))));
                            } else {
                                if (current != null) {
                                    query.add(current, operator);
                                }
                                current = this.newTermQuery(new Term(field, BytesRef.deepCopyOf(bytes)));
                            }
                        }
                        query.add(current, operator);
                        return query;
                    }
                // A phrase query
                } else {
                    MultiPhraseQuery query = this.newMultiPhraseQuery();
                    query.setSlop(phraseSlop);
                    List<Term> terms = new ArrayList<Term>();
                    int position = -1;
                    for (int i = 0; i < handler.numberOfTokens; i++) {
                        int increment = 1;
                        try {
                            boolean hasNext = buffer.incrementToken();
                            assert hasNext == true;
                            handler.fillBytesRef();
                            if (handler.positionIncrement != null) {
                                increment = handler.getPositionIncrement();
                            }
                        } catch (IOException e) {
                            // DO NOTHING, because we know the number of tokens
                        }
                      
                        if (increment > 0 && terms.size() > 0) {
                            if (this.getEnablePositionIncrements()) {
                                query.add(terms.toArray(new Term[0]), position);
                            } else {
                                query.add(terms.toArray(new Term[0]));
                            }
                            terms.clear();
                        }
                        position += increment;
                        terms.add(new Term(field, BytesRef.deepCopyOf(bytes)));
                    }
                    if (this.getEnablePositionIncrements()) {
                        query.add(terms.toArray(new Term[0]), position);
                    } else {
                        query.add(terms.toArray(new Term[0]));
                    }
                    return query;
                }
            // A phrase query
            } else {
                PhraseQuery query = this.newPhraseQuery();
                query.setSlop(phraseSlop);
                int position = -1;
              
                for (int i = 0; i < handler.numberOfTokens; i++) {
                    int increment = 1;
                  
                    try {
                        boolean hasNext = handler.incrementToken();
                        assert hasNext == true;
                        handler.fillBytesRef();
                        if (handler.positionIncrement != null) {
                            increment = handler.getPositionIncrement();
                        }
                    } catch (IOException e) {
                        // DO NOTHING, because we know the number of tokens
                    }
                  
                    if (this.getEnablePositionIncrements()) {
                        position += increment;
                        query.add(new Term(field, BytesRef.deepCopyOf(bytes)), position);
                    } else {
                        query.add(new Term(field, BytesRef.deepCopyOf(bytes)));
                    }
                }
                return query;
            }
        }
    }

    /**
     * Returns {@code TermRangeQuery} in accordance to the assigned configuration.
     * @param  field the currently handling field.
     * @param  infinimum the assigned range's infinimum.
     * @param  supremum the assigned range's supremum.
     * @param  leftInclusive true if the infinimum of the range is inclusive
     * @param  rightInclusive true if the supremum of the range is inclusive
     * @return new {@link TermRangeQuery} instance
     * @throws ParseException if the parsing fails.
     */
    protected Query getRangeQuery(String field, String infinimum, String supremum, boolean leftInclusive, boolean rightInclusive) throws ParseException {
        if (this.context.getLowercaseExpandedTerms()) {
            infinimum = infinimum == null ? null : infinimum.toLowerCase(this.context.getLocale());
            supremum = supremum == null ? null : supremum.toLowerCase(this.context.getLocale());
        }

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, this.context.getLocale());
        dateFormat.setLenient(true);
        DateTools.Resolution resolution = this.context.getDateResolution(field);

        try {
            infinimum = DateTools.dateToString(dateFormat.parse(infinimum), resolution);
        } catch (Exception e) {
	    // DO NOTHING, we know the boundaries are filled with somewhat instances.
	}

        try {
            Date until = dateFormat.parse(supremum);
            if (rightInclusive) {
                // The user can only specify the date, not the time, so make sure
                // the time is set to the latest possible time of that date to really
                // include all documents:
                Calendar calendar = Calendar.getInstance(this.context.getTimeZone(), this.context.getLocale());
                calendar.setTime(until);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 999);
                until = calendar.getTime();
            }
            supremum = DateTools.dateToString(until, resolution);
        } catch (Exception e) {
	    // DO NOTHING, we know the boundaries are filled with somewhat instances.
	}

        return this.newRangeQuery(field, infinimum, supremum, leftInclusive, rightInclusive);
    }

    /**
     * Returns {@code TermRangeQuery} in accordance to the assigned configuration.
     * @param  field the currently handling field.
     * @param  infinimum the assigned range's infinimum.
     * @param  supremum the assigned range's supremum.
     * @param  leftInclusive true if the infinimum of the range is inclusive
     * @param  rightInclusive true if the supremum of the range is inclusive
     * @return new {@link TermRangeQuery} instance
     */
    protected Query newRangeQuery(String field, String infinimum, String supremum, boolean leftInclusive, boolean rightInclusive) {
        final BytesRef left;
        final BytesRef right;
     
        if (infinimum == null) {
            left = null;
        } else {
            left = this.context.getRangeTermAnalysis() ? this.analyzeAsSingleMultiTerm(field, infinimum) : new BytesRef(infinimum);
        }
     
        if (supremum == null) {
            right = null;
        } else {
            right = this.context.getRangeTermAnalysis() ? this.analyzeAsSingleMultiTerm(field, supremum) : new BytesRef(supremum);
        }
      
        final TermRangeQuery query = new TermRangeQuery(field, left, right, leftInclusive, rightInclusive);
        query.setRewriteMethod(this.context.getMultiTermRewriteMethod());
        return query;
    }

    /**
     * Returns {@code Query} in accordance to the assigned configuration.
     * Can be overridden by extending classes, to modify query being  returned.
     * @param  clauses list that contains {@link BooleanClause} instances to join.
     * @return the Resulting {@link Query} instance.
     * @throws ParseException if the parsing fails.
     */
    protected Query getBooleanQuery(List<BooleanClause> clauses) throws ParseException {
	return this.getBooleanQuery(clauses, false);
    }

    /**
     * Returns {@code Query} in accordance to the assigned configuration.
     * Can be overridden by extending classes, to modify query being  returned.
     * @param  clauses list that contains {@link BooleanClause} instances to join.
     * @param  disableCoord true if coord scoring should be disabled.
     * @return the Resulting {@link Query} instance.
     * @throws ParseException if the parsing fails.
     */
    protected Query getBooleanQuery(List<BooleanClause> clauses, boolean disableCoord) throws ParseException {
	if (clauses.size() == 0) {
	    // all clause words were filtered away by the analyzer.
	    return null;
	}

	BooleanQuery query = this.newBooleanQuery(disableCoord);
	for(final BooleanClause clause: clauses) {
	    query.add(clause);
	}
	return query;
    }

    /**
     * Returns {@code BooleanClause} in accordance to the assigned configuration.
     * @param  query the currently handling sub query.
     * @param  occurence how this clause should occur when matching documents.
     * @return new {@link BooleanClause} instance.
     */
    protected BooleanClause newBooleanClause(Query query, BooleanClause.Occur occurence) {
	return new BooleanClause(query, occurence);
    }

    /**
     * Returns {@code PrefixQuery} in accordance to the assigned configuration.
     * @param  prefix the currently handlung prefix term.
     * @return new {@link PrefixQuery} instance.
     */
    protected Query newPrefixQuery(Term prefix){
	PrefixQuery query = new PrefixQuery(prefix);
	query.setRewriteMethod(this.context.getMultiTermRewriteMethod());
	return query;
    }

    /**
     * Factory method for generating a query. Called when parser
     * parses an input term token that contains a regular expression
     * query.
     *<p>
     * Depending on settings, pattern term may be lower-cased
     * automatically. It will not go through the default Analyzer,
     * however, since normal Analyzers are unlikely to work properly
     * with regular expression templates.
     *<p>
     * Can be overridden by extending classes, to provide custom handling for
     * regular expression queries, which may be necessary due to missing analyzer
     * calls.
     *
     * @param field Name of the field query will use.
     * @param termStr Term token that contains a regular expression
     *
     * @return Resulting {@link org.apache.lucene.search.Query} built for the term
     * @exception org.apache.lucene.queryparser.classic.ParseException throw in overridden method to disallow
     */
    protected Query getRegexpQuery(String field, String termStr) throws ParseException {
	if (this.context.getLowercaseExpandedTerms()) {
	    termStr = termStr.toLowerCase(this.context.getLocale());
	}
	Term t = new Term(field, termStr);
	return newRegexpQuery(t);
    }

    /**
     * Returns {@code RegexpQuery} in accordance to the assigned configuration.
     * @param  regexp the currently handling regexp term
     * @return new {@link RegexpQuery} instance
     */
    protected Query newRegexpQuery(Term regexp) {
	RegexpQuery query = new RegexpQuery(regexp, RegExp.ALL, maxDeterminizedStates);
	query.setRewriteMethod(this.context.getMultiTermRewriteMethod());
	return query;
    }

    /**
     * Returns {@code FuzzyQuery} in accordance to the assigned configuration.
     * @param  term the currently handling term.
     * @param  minimumSimilarity the minimum value of similarity to be assigned.
     * @param  prefixLength the prefix lenght to be assigned.
     * @return new {@link FuzzyQuery} instance
     */
    protected Query newFuzzyQuery(Term term, float minimumSimilarity, int prefixLength) {
	// FuzzyQuery doesn't yet allow constant score rewrite
	String text = term.text();
	int numberOfEditions = FuzzyQuery.floatToEdits(minimumSimilarity, text.codePointCount(0, text.length()));
	return new FuzzyQuery(term, numberOfEditions, prefixLength);
    }

    /**
     * Returns {@code MatchAllDocsQuery} in accordance to the assigned configuration.
     * @return new {@link MatchAllDocsQuery} instance.
     */
    protected Query newMatchAllDocsQuery() {
	return new MatchAllDocsQuery();
    }

    /**
     * Returns {@code WildcardQuery} in accordance to the assigned configuration.
     * @param  wildcard the currently handling wildcard term.
     * @return new {@link WildcardQuery} instance.
     */
    protected Query newWildcardQuery(Term wildcard) {
	WildcardQuery query = new WildcardQuery(wildcard);
	query.setRewriteMethod(this.context.getMultiTermRewriteMethod());
	return query;
    }

    /**
     * Returns {@code WildcardQuery} in accordance to the assigned configuration.
     * Can be overridden by extending classes, to provide custom handling for
     * wildcard queries, which may be necessary due to missing analyzer calls.
     *
     * @param  field the currently handling field..
     * @param  termText term that contains one or more wild card characters (? or *), but is not simple prefix term.
     * @return new {@link Query} instance.
     * @throws ParseException if the parsing fails.
     */
    protected Query getWildcardQuery(String field, String termText) throws ParseException {
	Context.Wildcard fieldWildcard = Context.Wildcard.find(file);
	Context.Wildcard termWildcard = Context.Wildcard.find(termText);

	if (fieldWildcard == Context.Wildcard.ANY_STRING && termWildcard == Context.Wildcard.ANY_STRING)) {
	    return this.newMatchAllDocsQuery();
	}

        if (!allowLeadingWildcard
	    && (termText.startsWith(Context.Wildcard.ANY_STRING.toString())
		|| termText.startsWith(Context.Wildcard.ANY_CHARACTER.toString()))) {
	    throw new ParseException("'*' or '?' not allowed as first character in WildcardQuery");
	}

        if (this.context.getLowercaseExpandedTerms()) {
	    termText = termText.toLowerCase(this.context.getLocale());
	}

	Term term = new Term(field, termText);
	return this.newWildcardQuery(term);
    }

    /**
     * INTERNAL USE ONLY.
     * This class represents internal field-query-parsing context.
     */
    protected class TokenStreamHandler {
        // Holds CachingTokenFilter for enabling lookup functionality.
        public CachingTokenFilter buffer = null;
        // Holds byte-data refference.
        public TermToBytesRefAttribute termToBytesRef = null;
        // Holds relative position.
        public PositionIncrementAttribute positionIncrement = null;
        // Holds number of tokens.
        public int numberOfTokens = 0;
        // Holds position count.
        public int positionCount = 0;
        // true if several tokens appears at the same position.
        public boolean severalTokensAtSamePosition = false;
        // true if buffer has more tokens.
        public boolean hasMoreTokens = false;

        // Constructor.
        public TokenStreamHandler(TokenStream tokenStream, boolean reset) {
            if (reset) {
                tokenStream.reset();
            }
            this.buffer = new CachingTokenFilter(tokenStream);
            this.buffer.reset();
            this.termToBytesRef = this.buffer.getAttribute(TermToBytesRefAttribute.class);
            this.positionIncrement = this.buffer.getAttribute(PositionIncrementAttribute.class);
            this.prepare();
        }

        // Constructor.
        public TokenStreamHandler(TokenStream tokenStream) {
            this(tokenStream, true);
        }

        // Prepares for parsing.
        private void prepare() {
            if (this.termToBytesRef != null) {
                try {
                    this.hasMoreTokens = this.incrementToken();
                    while (this.hasMoreTokens) {
                        this.numberOfTokens++;
                        int increment = (this.positionIncrement != null) ? this.getPositionIncrement() : 1;
                        if (increment != 0) {
                            this.positionCount += increment;
                        } else {
                            this.severalTokensAtSamePosition = true;
                        }
                        this.hasMoreTokens = this.buffer.incrementToken();
                    }
                } catch (IOException e) {
                    // DO NOTHING.
                }
            }
            this.buffer.reset();
        }

	// Delegates {@code TokenStream}'s method.
	public void resetTokenStream() throws IOException {
	    this.tokenStream.reset();
	}

	// Delegates {@code TokenStream}'s method.
	public void endTokenStream() throws IOException {
	    this.tokenStream.end();
	}

        // Delegates {@code CachingTokenFilter}'s method.
        public boolean incrementToken() throws IOException {
            return this.buffer.incrementToken();
        }

        // Delegates {@code PositionIncrementAttribute}'s method.
        public int getPositionIncrement() {
            return this.positionIncrement == null ? 0 : this.positionIncrement.getPositionIncrement();
        }

        // Delegates {@code PositionIncrementAttribute}'s method.
        public BytesRef getBytesRef() {
            return this.termToBytesRef == null ? null : this.termToBytesRef.getBytesRef();
        }

        // Delegates {@code PositionIncrementAttribute}'s method.
        public void fillBytesRef() {
            this.termToBytesRef.fillBytesRef();
        }
    }

    /**
     * Analyzes the given query part as a single {@code MultiTermQuery} in accordance to the assigned configuration.
     * @param  field the currently handling field.
     * @param  queryPart the assigned range's infinimum.
     * @return new {@link BytesRef} instance
     */
    protected BytesRef analyzeAsSingleMultiTerm(String field, String queryPart) {
        return this.analyzeMultitermTerm(field, queryPart, this.getAnalyzer());
    }

    /**
     * Analyzes the given query part as a single {@code MultiTermQuery} in accordance to the assigned configuration.
     * @param  field the currently handling field.
     * @param  queryPart the assigned range's infinimum.
     * @param  analyzer the analyzer instance which is responsible for the raw query analysis.
     * @return new {@link BytesRef} instance
     */
    protected BytesRef analyzeAzSingleMultiTerm(String field, String queryPart, Analyzer analyzer) {
        if (analyzer == null) {
	    analyzer = this.getAnalyzer();
	}
        TokenStream source = null;
	TokenStreamHandler handler = null;

        try {
            source = analyzer.tokenStream(field, queryPart);
	    handler = new TokenStreamHandler(source);
            BytesRef bytes = handler.getBytesRef();

            if (!handler.incrementToken()) {
                throw new IllegalArgumentException("analyzer returned no terms for multiTerm term: " + queryPart);
	    }
            handler.fillBytesRef();
            if (handler.incrementToken()) {
                throw new IllegalArgumentException("analyzer returned too many terms for multiTerm term: " + queryPart);
	    }
            handler.endTokenStream();
            return BytesRef.deepCopyOf(bytes);
        } catch (IOException cause) {
            throw new RuntimeException("error analyzing multiTerm term: " + queryPart, cause);
        } finally {
            IOUtils.closeWhileHandlingException(source);
        }
    }
}
