/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.Version;
import org.elasticsearch.common.hppc.ObjectFloatOpenHashMap;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.query.QueryParseContext;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl.ExternalQueryParser;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl.InternalQueryParser;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.HandleException;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.TreeHandler;
import jp.supership.elasticsearch.plugin.queryparser.common.util.StringUtils;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.config.DSQParserConfiguration;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.config.DSQParserSettings;
import static jp.supership.elasticsearch.plugin.queryparser.lucene.util.config.QueryParserConfiguration.Wildcard;

/**
 * This class is responsible for instanciating Lucene queries, query parser delegates all sub-query
 * instanciation tasks to this class. This implementation assumes that concrete query handler will be 
 * constructed in accordance to the given ANTLR grammar.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public abstract class ProximityQueryEngine extends SpanQueryBuilder implements ProximityQueryDriver, TreeHandler, DSQParserConfiguration {
    /** Holds ES query parsing context. */
    protected QueryParseContext context;

    /** Holds query-parsing-contect. */
    protected DSQParserConfiguration configuration;

    /**
     * Constructor.
     */
    public ProximityQueryEngine() {
        super(null);
    }

    /**
     * Constructor.
     */
    public ProximityQueryEngine(DSQParserConfiguration configuration) {
        super(null);
        this.configuration = configuration;
    }

    /**
     * Initializes a query parser.
     * @param version  Lucene version to be matched. See <a href="ExternalQueryParser.html#version">here</a>.
     * @param field    the default field for query terms.
     * @param analyzer the analyzer which is applied for the given query..
     */
    public void initialize(Version version, String field, Analyzer analyzer, DSQParserConfiguration configuration) {
        this.initialize(field, analyzer, configuration);
        if (version.onOrAfter(Version.LUCENE_3_1) == false) {
            this.setPhraseQueryAutoGeneration(true);
        }
    }

    /**
     * Initializes a query parser.
     * @param field    the default field for query terms.
     * @param analyzer the analyzer which is applied for the given query..
     */
    public void initialize(String field, Analyzer analyzer, DSQParserConfiguration configuration) {
	this.configuration = configuration;
        this.setAnalyzer(analyzer);
        this.setDefaultField(field);
        this.setPhraseQueryAutoGeneration(false);
    }

    /**
     * Configures engine ain according to the given {@code DSQParserConfiguration}.
     * @param configuration the assigned configuration to be used.
     */
    public abstract void configure(DSQParserConfiguration configuration);

    /**
     * {@inheritDoc}
     */
    @Override
    public void conjugate(List<BooleanClause> clauses, int conjunction, int modifier, Query query) {
        boolean required;
        boolean prohibited;

	// If this term is introduced by DIS, treats the previous clauses and the current one as disjunction-max query.
        if (clauses.size() > 0 && conjunction == InternalQueryParser.CONJUNCTION_DIS) {
	    DisjunctionMaxQuery disMaxQuery = new DisjunctionMaxQuery(this.getTieBreaker());
	    disMaxQuery.add(this.getBooleanQuery(clauses));
	    disMaxQuery.add(query);
	    query = disMaxQuery;
	    clauses = new ArrayList<BooleanClause>();
        }

        // If this term is introduced by AND, make the preceding term required, unless it is already prohibited.
        if (clauses.size() > 0 && conjunction == ExternalQueryParser.CONJUNCTION_AND) {
            BooleanClause previous = clauses.get(clauses.size() - 1);
            if (!previous.isProhibited()) {
                previous.setOccur(BooleanClause.Occur.SHOULD);
            }
        }

        // If this term is introduced by OR, make the preceeding term optional, unless it is prohibited.
        if (clauses.size() > 0 && this.configuration.getDefaultOperator() == ExternalQueryParser.CONJUNCTION_AND && conjunction == ExternalQueryParser.CONJUNCTION_OR) {
            BooleanClause previous = clauses.get(clauses.size() - 1);
            if (!previous.isProhibited()) {
                previous.setOccur(BooleanClause.Occur.SHOULD);
            }
        }

        // A null query might have been passed, that means the term might have been filtered out by the analyzer.
        if (query == null) {
            return;
        }

	// The term is set to be REQUIRED if the term is introduced by AND or +;
        // otherwise, REQUIRED if not PROHIBITED and not introduced by OR.
        if (this.getDefaultOperator() == ExternalQueryParser.CONJUNCTION_OR
	    || this.getDefaultOperator() == InternalQueryParser.CONJUNCTION_DIS) {
            prohibited = (modifier == ExternalQueryParser.MODIFIER_NEGATE);
            required = (modifier == ExternalQueryParser.MODIFIER_REQUIRE);
            if (conjunction == ExternalQueryParser.CONJUNCTION_AND && !prohibited) {
                required = true;
            }
        // The term is set ti be PROHIBITED if the term is introduced by NOT;
        // otherwise, REQIURED if not PROHIBITED and not introduce by OR.
        } else {
            prohibited = (modifier == ExternalQueryParser.MODIFIER_NEGATE);
            required = (!prohibited
			&& conjunction != ExternalQueryParser.CONJUNCTION_OR
			&& conjunction != InternalQueryParser.CONJUNCTION_DIS);
        }

        if (required && !prohibited) {
            clauses.add(this.newBooleanClause(query, BooleanClause.Occur.MUST));
        } else if (!required && !prohibited) {
            clauses.add(this.newBooleanClause(query, BooleanClause.Occur.SHOULD));
        }


	else if (!required && prohibited) {
            clauses.add(this.newBooleanClause(query, BooleanClause.Occur.MUST_NOT));
        } else {
            throw new RuntimeException("clause could not be both required and prohibited.");
        }
    }

    /**
     * ANOTHER IMPLEMENTATION FOR SPAN QUERY FAMILY.
     * {@inheritDoc}
     */
    @Override
    public Query getFieldQuery(Analyzer analyzer, String field, String queryText, boolean quoted, int phraseSlop, boolean inOrder, boolean useDisMax) throws ParseException {
	//    protected Query newFieldQuery(Analyzer analyzer, String field, String queryText, boolean quoted, boolean useDisMax) throws ParseException {
        BooleanClause.Occur occurence = this.getDefaultOperator() == ExternalQueryParser.CONJUNCTION_AND ? BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD;

        String analyzerName = null;
        if (analyzer instanceof NamedAnalyzer) {
            analyzerName = ((NamedAnalyzer) analyzer).name();
        }

        if (analyzerName != null && (analyzerName.startsWith("ngram_"))) {
            List<BooleanClause> clauses = new ArrayList<BooleanClause>();
            String[] tokens = queryText.split(StringUtils.UNICODE_START_OF_HEADING);
            for (String token : tokens) {
                Query query = this.createFieldQuery(analyzer, occurence, field, queryText, true, 0, true, true);
                if (query != null) {
                    clauses.add(new BooleanClause(query, occurence));
                }
            }
            if (clauses.size() == 0) {
                return null;
            }
            return this.getBooleanQuery(clauses, true);
        // TODO: THIS MUST BE HANDLED WITHIN {@code Analyzer} WHICH IS BASED ON SOME KIND OF HEURISTICS AND/OR ML ALGORITHM.
        } else if (quoted == false && queryText.matches("^\\d+(\\.\\d+)?.{1,2}?$")) {
            quoted = true;
            this.setPhraseSlop(0);
        } else {
	    
            quoted = quoted || this.getPhraseQueryAutoGeneration();
        }

        return this.createFieldQuery(analyzer, occurence, field, queryText, quoted, this.getPhraseSlop(), inOrder, useDisMax);
    }

    /**
     * Returns a span query from the analysis chain.
     * @param analyzer analyzer used for this query.
     * @param operator the default boolean operator used for this query.
     * @param field field to create queries against.
     * @param queryText text to be passed to the analysis chain.
     * @param quoted true if phrases should be generated when terms occur at more than one position.
     * @param phraseSlop slop factor for phrase/multiphrase queries.
     * @param inOrder true if the order is important.
     */
    protected Query createFieldQuery(Analyzer analyzer, BooleanClause.Occur operator, String field, String queryText, boolean quoted, int phraseSlop, boolean inOrder, boolean useDisMax) throws ParseException {
        if (!useDisMax) {
            return this.createFieldQuery(analyzer, field, queryText, quoted, phraseSlop, inOrder);
        }

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
	    return this.newSpanTermQuery(new Term(field, BytesRef.deepCopyOf(bytes)));
        } else {
            if (handler.severalTokensAtSamePosition || (!quoted)) {
                // Not a span near query.
                if (handler.positionCount == 1 || (!quoted)) {
                    if (handler.positionCount == 1) {
			SpanOrQuery query = (SpanOrQuery) this.newSpanOrQuery();
                        for (int i = 0; i < handler.numberOfTokens; i++) {
                            try {
                                boolean hasNext = handler.incrementToken();
                                assert hasNext == true;
                                handler.fillBytesRef();
                            } catch (IOException e) {
                                // DO NOTHING, because we know the number of tokens
                            }
                            SpanQuery current = this.newSpanTermQuery(new Term(field, BytesRef.deepCopyOf(bytes)));
			    query.addClause(current);
                        }
                        return query;
                    } else {
                        BooleanQuery query = (BooleanQuery) this.newBooleanQuery(false);
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
                // A complex span near query.
                } else {
		    List<SpanTermQuery> terms = new ArrayList<SpanTermQuery>();
		    List<SpanNearQuery> queries = new ArrayList<SpanNearQuery>();
		    int position = -1;
                    for (int i = 0; i < handler.numberOfTokens; i++) {
                        int positionIncrement = 1;
                        try {
                            boolean hasNext = handler.incrementToken();
                            assert hasNext == true;
                            handler.fillBytesRef();
                            if (handler.positionIncrement != null) {
                                positionIncrement = handler.getPositionIncrement();
                            }
                        } catch (IOException e) {
                            // DO NOTHING, because we know the number of tokens
                        }
                      
                        if (positionIncrement > 0 && terms.size() > 0) {
			    queries.add((SpanNearQuery) this.newSpanNearQuery(terms.toArray(new SpanTermQuery[0]), -1, inOrder));
			    terms.clear();
                        }
                        position += positionIncrement;
			terms.add((SpanTermQuery) this.newSpanTermQuery(new Term(field, BytesRef.deepCopyOf(bytes))));
                    }

		    return this.newSpanNearQuery(queries.toArray(new SpanNearQuery[0]), position, inOrder);
                }
            // A simple span near query.
            } else {
		List<SpanTermQuery> terms = new ArrayList<SpanTermQuery>();
		int position = -1;
                for (int i = 0; i < handler.numberOfTokens; i++) {
                    int positionIncrement = 1;
                    try {
                        boolean hasNext = handler.incrementToken();
                        assert hasNext == true;
                        handler.fillBytesRef();
                        if (handler.positionIncrement != null) {
                            positionIncrement = handler.getPositionIncrement();
                        }
                    } catch (IOException e) {
                        // DO NOTHING, because we know the number of tokens
                    }
		    position += positionIncrement;
		    terms.add((SpanTermQuery) this.newSpanTermQuery(new Term(field, BytesRef.deepCopyOf(bytes))));
                }
		return this.newSpanNearQuery(terms.toArray(new SpanTermQuery[0]), position, inOrder);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query getBooleanQuery(List<BooleanClause> clauses) throws ParseException {
        return this.getBooleanQuery(clauses, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query getBooleanQuery(List<BooleanClause> clauses, boolean disableCoord) throws ParseException {
        if (clauses.size() == 0) {
            return null;
        }

        BooleanQuery query = (BooleanQuery) this.newBooleanQuery(disableCoord);
        for(final BooleanClause clause: clauses) {
            query.add(clause);
        }
        return query;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SpanQuery getSpanTermQuery(String field, String termText, boolean quoted) throws ParseException {
	return this.newSpanTermQuery(new Term(field, termText));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SpanQuery getSpanNotQuery(SpanQuery inclusion, SpanQuery exclusion) throws ParseException {
	return this.newSpanNotQuery(inclusion, exclusion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SpanQuery getSpanNearQuery(int slop, boolean inOrder, SpanQuery... clauses) throws ParseException {
	return this.newSpanNearQuery(clauses, slop, inOrder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SpanQuery getSpanOrQuery(SpanQuery... clauses) throws ParseException {
	return this.newSpanOrQuery(clauses);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <Q extends MultiTermQuery> SpanQuery getSpanMultiTermQueryWrapper(Q wrapped) throws ParseException {
	return this.newSpanMultiTermQueryWrapper(wrapped);
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
        public TokenStreamHandler(TokenStream tokenStream, boolean reset) throws IOException {
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
        public TokenStreamHandler(TokenStream tokenStream) throws IOException {
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
     * Returns assigned context.
     * @return the currently assigned context.
     */
    public QueryParseContext getContext() {
        return this.context;
    }

    /**
     * Sets currently handling context.
     * @param context the currently handling context.
     */
    public void setContext(QueryParseContext context) {
        this.context = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaultField(String defaultField) {
        this.configuration.setDefaultField(defaultField);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultField() {
        return this.configuration.getDefaultField();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaultOperator(int defaultOperator) {
        this.configuration.setDefaultOperator(defaultOperator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDefaultOperator() {
        return this.configuration.getDefaultOperator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPhraseQueryAutoGeneration(boolean phraseQueryAutoGeneration) {
        this.configuration.setPhraseQueryAutoGeneration(phraseQueryAutoGeneration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getPhraseQueryAutoGeneration() {
        return this.configuration.getPhraseQueryAutoGeneration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnablePositionIncrements(boolean positionIncrements) {
        this.configuration.setEnablePositionIncrements(positionIncrements);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getEnablePositionIncrements() {
        return this.configuration.getEnablePositionIncrements();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFuzzyMinSim(float fuzzyMinSim) {
        this.configuration.setFuzzyMinSim(fuzzyMinSim);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getFuzzyMinSim() {
        return this.configuration.getFuzzyMinSim();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFuzzyPrefixLength(int fuzzyPrefixLength) {
        this.configuration.setFuzzyPrefixLength(fuzzyPrefixLength);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFuzzyPrefixLength() {
        return this.configuration.getFuzzyPrefixLength();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPhraseSlop(int phraseSlop) {
        this.configuration.setPhraseSlop(phraseSlop);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPhraseSlop() {
        return this.configuration.getPhraseSlop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAllowLeadingWildcard(boolean allowLeadingWildcard) {
        this.configuration.setAllowLeadingWildcard(allowLeadingWildcard);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getAllowLeadingWildcard() {
        return this.configuration.getAllowLeadingWildcard();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLowercaseExpandedTerms(boolean lowercaseExpandedTerms) {
        this.configuration.setLowercaseExpandedTerms(lowercaseExpandedTerms);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getLowercaseExpandedTerms() {
        return this.configuration.getLowercaseExpandedTerms();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMultiTermRewriteMethod(MultiTermQuery.RewriteMethod multiTermRewriteMethod) {
        this.configuration.setMultiTermRewriteMethod(multiTermRewriteMethod);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultiTermQuery.RewriteMethod getMultiTermRewriteMethod() {
        return this.configuration.getMultiTermRewriteMethod();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLocale(Locale locale) {
        this.configuration.setLocale(locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Locale getLocale() {
        return this.configuration.getLocale();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeZone(TimeZone timeZone) {
        this.configuration.setTimeZone(timeZone);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeZone getTimeZone() {
        return this.configuration.getTimeZone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDateResolution(DateTools.Resolution dateResolution) {
        this.configuration.setDateResolution(dateResolution);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDateResolution(String field, DateTools.Resolution resolution) {
        this.configuration.setDateResolution(field, resolution);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DateTools.Resolution getDateResolution(String field) {
        return this.configuration.getDateResolution(field);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRangeTermAnalysis(boolean value) {
        this.configuration.setRangeTermAnalysis(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getRangeTermAnalysis() {
        return this.configuration.getRangeTermAnalysis();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMaxDeterminizedStates(int max) {
        this.configuration.setMaxDeterminizedStates(max);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxDeterminizedStates() {
        return this.configuration.getMaxDeterminizedStates();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCacheable() {
        return this.configuration.isCacheable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getQueryString() {
        return this.configuration.getQueryString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setQueryString(String queryString) {
	this.configuration.setQueryString(queryString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getBoost() {
        return this.configuration.getBoost();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBoost(float boost) {
	this.configuration.setBoost(boost);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFuzzyMaxExpansions() {
	return this.configuration.getFuzzyMaxExpansions();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFuzzyMaxExpansions(int fuzzyMaxExpansions) {
	this.configuration.setFuzzyMaxExpansions(fuzzyMaxExpansions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultiTermQuery.RewriteMethod getFuzzyRewriteMethod() {
	return this.configuration.getFuzzyRewriteMethod();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFuzzyRewriteMethod(MultiTermQuery.RewriteMethod fuzzyRewriteMethod) {
	this.configuration.setFuzzyRewriteMethod(fuzzyRewriteMethod);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getWildcardAnalysis() {
	return this.configuration.getWildcardAnalysis();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWildcardAnalysis(boolean wildcardAnalysis) {
	this.configuration.setWildcardAnalysis(wildcardAnalysis);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getEscape() {
	return this.configuration.getEscape();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEscape(boolean escape) {
	this.configuration.setEscape(escape);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Analyzer getDefaultAnalyzer() {
	return this.configuration.getDefaultAnalyzer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaultAnalyzer(Analyzer defaultAnalyzer) {
	this.configuration.setDefaultAnalyzer(defaultAnalyzer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Analyzer getDefaultQuoteAnalyzer() {
	return this.configuration.getDefaultQuoteAnalyzer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaultQuoteAnalyzer(Analyzer defaultQuoteAnalyzer) {
	this.configuration.setDefaultQuoteAnalyzer(defaultQuoteAnalyzer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Analyzer getForcedAnalyzer() {
	return this.configuration.getForcedAnalyzer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setForcedAnalyzer(Analyzer forcedAnalyzer) {
	this.configuration.setForcedAnalyzer(forcedAnalyzer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Analyzer getForcedQuoteAnalyzer() {
	return this.configuration.getForcedQuoteAnalyzer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setForcedQuoteAnalyzer(Analyzer forcedQuoteAnalyzer) {
	this.configuration.setForcedQuoteAnalyzer(forcedQuoteAnalyzer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getQuoteFieldSuffix() {
	return this.configuration.getQuoteFieldSuffix();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setQuoteFieldSuffix(String quoteFieldSuffix) {
	this.configuration.setQuoteFieldSuffix(quoteFieldSuffix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultiTermQuery.RewriteMethod getRewriteMethod() {
	return this.configuration.getRewriteMethod();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRewriteMethod(MultiTermQuery.RewriteMethod rewriteMethod) {
	this.configuration.setRewriteMethod(rewriteMethod);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMinimumShouldMatch() {
	return this.configuration.getMinimumShouldMatch();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMinimumShouldMatch(String minimumShouldMatch) {
	this.configuration.setMinimumShouldMatch(minimumShouldMatch);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getLenient() {
	return this.configuration.getLenient();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLenient(boolean lenient) {
	this.configuration.setLenient(lenient);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getFieldRefinement() {
	return this.configuration.getFieldRefinement();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFieldRefinement(boolean fieldRefinement) {
	this.configuration.setFieldRefinement(fieldRefinement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getQueryNegation() {
	return this.configuration.getQueryNegation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setQueryNegation(boolean queryNegation) {
	this.configuration.setQueryNegation(queryNegation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getFields() {
	return this.configuration.getFields();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFields(List<String> fields) {
	this.configuration.setFields(fields);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getQueryTypes() {
	return this.configuration.getQueryTypes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setQueryTypes(Collection<String> queryTypes) {
	this.configuration.setQueryTypes(queryTypes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectFloatOpenHashMap<String> getBoosts() {
	return this.configuration.getBoosts();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBoosts(ObjectFloatOpenHashMap<String> boosts) {
	this.configuration.setBoosts(boosts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getTieBreaker() {
	return this.configuration.getTieBreaker();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTieBreaker(float tieBreaker) {
	this.configuration.setTieBreaker(tieBreaker);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getUseDisMax() {
	return this.configuration.getUseDisMax();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUseDisMax(boolean useDisMax) {
	this.configuration.setUseDisMax(useDisMax);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getInOrder() {
	return this.configuration.getInOrder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInOrder(boolean inOrder) {
	this.configuration.setInOrder(inOrder);
    }
}
