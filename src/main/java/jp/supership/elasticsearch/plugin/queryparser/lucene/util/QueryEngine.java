/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
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
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.QueryBuilder;
import org.apache.lucene.util.Version;
import org.apache.lucene.util.automaton.RegExp;
import org.elasticsearch.common.hppc.ObjectFloatOpenHashMap;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.query.QueryParseContext;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl.ExternalQueryParser;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl.InternalQueryParser;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.HandleException;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.QueryHandler;
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
public abstract class QueryEngine extends QueryBuilder implements QueryDriver, QueryHandler, DSQParserConfiguration {
    /** Holds ES query parsing context. */
    protected QueryParseContext context;

    /** Holds query-parsing-contect. */
    protected DSQParserConfiguration configuration;

    /**
     * Constructor.
     */
    public QueryEngine() {
        super(null);
    }

    /**
     * Constructor.
     */
    public QueryEngine(DSQParserConfiguration configuration) {
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
        } else if (!required && prohibited) {
            clauses.add(this.newBooleanClause(query, BooleanClause.Occur.MUST_NOT));
        } else {
            throw new RuntimeException("clause could not be both required and prohibited.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query getFieldQuery(String field, String queryText, boolean quoted) throws ParseException {
        return this.newFieldQuery(this.getAnalyzer(), field, queryText, quoted);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query getFieldQuery(String field, String queryText, boolean quoted, boolean useDisMax) throws ParseException {
        return this.newFieldQuery(this.getAnalyzer(), field, queryText, quoted, useDisMax);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query getFieldQuery(String field, String queryText, int phraseSlop) throws ParseException {
        Query query = this.getFieldQuery(field, queryText, true);

        if (query instanceof PhraseQuery) {
            ((PhraseQuery) query).setSlop(phraseSlop);
        }
        if (query instanceof MultiPhraseQuery) {
            ((MultiPhraseQuery) query).setSlop(phraseSlop);
        }

        return query;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query getFieldQuery(String field, String queryText, int phraseSlop, boolean useDisMax) throws ParseException {
        Query query = this.getFieldQuery(field, queryText, true, useDisMax);

        if (query instanceof PhraseQuery) {
            ((PhraseQuery) query).setSlop(phraseSlop);
        }
        if (query instanceof MultiPhraseQuery) {
            ((MultiPhraseQuery) query).setSlop(phraseSlop);
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
        return this.newFieldQuery(analyzer, field, queryText, quoted, true);
    }

    /**
     * Returns {@code FieldQuery} in accordance to the assigned configuration.
     * @param  analyzer the analyzer instance which is responsible for the raw query analysis.
     * @param  field the currently handling field.
     * @param  queryText the currently handling raw query string.
     * @param  quoted this value must be ser true if the handling query is considered to be quoted.
     * @param  useDisMax true if the yet-another disjunction-max-conjugation query construction is used.
     * @return the resulting {@code Query} instance.
     * @throws ParseException if the parsing fails.
     */
    protected Query newFieldQuery(Analyzer analyzer, String field, String queryText, boolean quoted, boolean useDisMax) throws ParseException {
        BooleanClause.Occur occurence = this.getDefaultOperator() == ExternalQueryParser.CONJUNCTION_AND ? BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD;

        String analyzerName = null;
        if (analyzer instanceof NamedAnalyzer) {
            analyzerName = ((NamedAnalyzer) analyzer).name();
        }

        if (analyzerName != null && (analyzerName.startsWith("ngram_"))) {
            List<BooleanClause> clauses = new ArrayList<BooleanClause>();
            String[] tokens = queryText.split(StringUtils.UNICODE_START_OF_HEADING);
            for (String token : tokens) {
                Query query = this.createFieldQuery(analyzer, occurence, field, queryText, true, 0, true);
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

        return this.createFieldQuery(analyzer, occurence, field, queryText, quoted, this.getPhraseSlop(), useDisMax);
    }

    /**
     * Creates a query from the analysis chain.
     * @param analyzer the analyzer for this query.
     * @param operator the default boolean operator used for this query.
     * @param field the field to create queries against.
     * @param queryText the text to be passed to the analysis chain.
     * @param quoted true if phrases should be generated when terms occur at more than one position.
     * @param phraseSlop slop factor for phrase/multiphrase queries.
     * @param useDisMax true if the yet-another disjunction-max-conjugation query construction is used.
     */
    protected Query createFieldQuery(Analyzer analyzer, BooleanClause.Occur operator, String field, String queryText, boolean quoted, int phraseSlop, boolean useDisMax) {
        if (!useDisMax) {
            return this.createFieldQuery(analyzer, operator, field, queryText, quoted, phraseSlop);
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
                            boolean hasNext = handler.incrementToken();
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
     * {@inheritDoc}
     */
    @Override
    public Query getRangeQuery(String field, String infinimum, String supremum, boolean leftInclusive, boolean rightInclusive) throws ParseException {
        if (this.getLowercaseExpandedTerms()) {
            infinimum = infinimum == null ? null : infinimum.toLowerCase(this.getLocale());
            supremum = supremum == null ? null : supremum.toLowerCase(this.getLocale());
        }

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, this.getLocale());
        dateFormat.setLenient(true);
        DateTools.Resolution resolution = this.getDateResolution(field);

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
                Calendar calendar = Calendar.getInstance(this.getTimeZone(), this.getLocale());
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
            left = this.getRangeTermAnalysis() ? this.analyzeAsSingleMultiTerm(field, infinimum) : new BytesRef(infinimum);
        }
     
        if (supremum == null) {
            right = null;
        } else {
            right = this.getRangeTermAnalysis() ? this.analyzeAsSingleMultiTerm(field, supremum) : new BytesRef(supremum);
        }
      
        final TermRangeQuery query = new TermRangeQuery(field, left, right, leftInclusive, rightInclusive);
        query.setRewriteMethod(this.getMultiTermRewriteMethod());
        return query;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query getBooleanQuery(boolean disableCoord) throws ParseException {
	return this.newBooleanQuery(disableCoord);
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
     * {@inheritDoc}
     */
    @Override
    public Query getPrefixQuery(String field, String termText) throws ParseException {
        if (!this.getAllowLeadingWildcard() && termText.startsWith(Wildcard.STRING.toString()))
            throw new ParseException("'*' not allowed as first character in PrefixQuery");
        if (this.getLowercaseExpandedTerms()) {
            termText = termText.toLowerCase(this.getLocale());
        }
        Term term = new Term(field, termText);
        return this.newPrefixQuery(term);
    }

    /**
     * Returns {@code PrefixQuery} in accordance to the assigned configuration.
     * @param  prefix the currently handlung prefix term.
     * @return new {@link PrefixQuery} instance.
     */
    protected Query newPrefixQuery(Term prefix){
        PrefixQuery query = new PrefixQuery(prefix);
        query.setRewriteMethod(this.getMultiTermRewriteMethod());
        return query;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query getRegexpQuery(String field, String termText) throws ParseException {
        if (this.getLowercaseExpandedTerms()) {
            termText = termText.toLowerCase(this.getLocale());
        }
        Term term = new Term(field, termText);
        return this.newRegexpQuery(term);
    }

    /**
     * Returns {@code RegexpQuery} in accordance to the assigned configuration.
     * @param  regexp the currently handling regexp term
     * @return new {@link RegexpQuery} instance
     */
    protected Query newRegexpQuery(Term regexp) {
        RegexpQuery query = new RegexpQuery(regexp, RegExp.ALL, this.getMaxDeterminizedStates());
        query.setRewriteMethod(this.getMultiTermRewriteMethod());
        return query;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query getFuzzyQuery(String field, String termText, float minimumSimilarity) throws ParseException {
        if (this.getLowercaseExpandedTerms()) {
            termText = termText.toLowerCase(this.getLocale());
        }
        Term term = new Term(field, termText);
        return this.newFuzzyQuery(term, minimumSimilarity, this.getFuzzyPrefixLength());
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
        query.setRewriteMethod(this.getMultiTermRewriteMethod());
        return query;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query getWildcardQuery(String field, String termText) throws ParseException {
        Wildcard fieldWildcard = Wildcard.find(field);
        Wildcard termWildcard = Wildcard.find(termText);

        if (fieldWildcard == Wildcard.STRING && termWildcard == Wildcard.STRING) {
            return this.newMatchAllDocsQuery();
        }

        if (!this.getAllowLeadingWildcard()
            && (termText.startsWith(Wildcard.STRING.toString())
                || termText.startsWith(Wildcard.CHARACTER.toString()))) {
            throw new ParseException("'*' or '?' not allowed as first character in WildcardQuery");
        }

        if (this.getLowercaseExpandedTerms()) {
            termText = termText.toLowerCase(this.getLocale());
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
     * Analyzes the given query part as a single {@code MultiTermQuery} in accordance to the assigned configuration.
     * @param  field the currently handling field.
     * @param  queryPart the assigned range's infinimum.
     * @return new {@link BytesRef} instance
     */
    protected BytesRef analyzeAsSingleMultiTerm(String field, String queryPart) {
        return this.analyzeAsSingleMultiTerm(field, queryPart, this.getAnalyzer());
    }

    /**
     * Analyzes the given query part as a single {@code MultiTermQuery} in accordance to the assigned configuration.
     * @param  field the currently handling field.
     * @param  queryPart the assigned range's infinimum.
     * @param  analyzer the analyzer instance which is responsible for the raw query analysis.
     * @return new {@link BytesRef} instance
     */
    protected BytesRef analyzeAsSingleMultiTerm(String field, String queryPart, Analyzer analyzer) {
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
            source.end();
            return BytesRef.deepCopyOf(bytes);
        } catch (IOException cause) {
            throw new RuntimeException("error analyzing multiTerm term: " + queryPart, cause);
        } finally {
            IOUtils.closeWhileHandlingException(source);
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
}
