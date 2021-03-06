/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.config;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.queryparser.flexible.standard.CommonQueryParserConfiguration;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MultiTermQuery;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl.ExternalQueryParser;
import static org.apache.lucene.util.automaton.Operations.DEFAULT_MAX_DETERMINIZED_STATES;

/**
 * This class represents parsing contex, i.e., parser settings for the Elasticsearch query DSL tailered for Supership, inc.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class QueryParserSettings implements QueryParserConfiguration {
    /** Holds default analyzer. */
    protected Analyzer analyzer = null;

    // TODO: FIX THIS DEFAULT VALUE TO BE APPROPRIATE ONE.
    /** Holds default field for query terms. */
    protected String defaultField = null;

    /** Holds the default operator parsers use to combine query terms. */
    protected int defaultOperator = ExternalQueryParser.CONJUNCTION_OR;

    /** Holds phrase-query-auto-genertion functionality setting. */
    protected boolean phraseQueryAutoGeneration = DEFAULT_PHRASE_QUERY_AUTO_GENERATION;

    /** Holds position-incremention functionality setting. */
    protected boolean positionIncrements = DEFAULT_POSITION_INCREMENTS;

    /** Holds default date resolution. */
    protected DateTools.Resolution dateResolution = null;

    /** Holds mapping between field names and its corresponding date resolutions. */
    protected Map<String, DateTools.Resolution> fieldToDateResolution = null;

    /** Holds range-term-analysis functionality setting. */
    protected boolean rangeTermAnalysis = false;

    /** Holds maximum number of states. */
    protected int maxDeterminizedStates = DEFAULT_MAX_DETERMINIZED_STATES;

    /** Holds minimum value of the fuzzy query similarity. */
    protected float fuzzyMinSim = FuzzyQuery.defaultMinSimilarity;

    /** Holds prefix length of the fuzzy queries. */
    protected int fuzzyPrefixLength = FuzzyQuery.defaultPrefixLength;

    /** Holds slop width for the phrase query. */
    protected int phraseSlop = DEFAULT_PHRASE_SLOP;

    /** Holds leading-wildcard-allowance functionality setting. */
    protected boolean allowLeadingWildcard = DEFAULT_ALLOW_LEADING_WILDCARD;

    /** Holds auto-lowercase-expansion functionality setting. */
    protected boolean lowercaseExpandedTerms = DEFAULT_LOWERCASE_TERM_EXPANSION;

    /** Holds multi-term-query-rewrite-method functionality setting. */
    protected MultiTermQuery.RewriteMethod multiTermRewriteMethod = MultiTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT;

    /** Holds locale. */
    protected Locale locale = Locale.getDefault();

    /** Holds timezone. */
    protected TimeZone timeZone = TimeZone.getDefault();

    /**
     * {@inheritDoc}
     */
    @Override
    public Analyzer getAnalyzer() {
	return this.analyzer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaultField(String defaultField) {
        this.defaultField = defaultField;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultField() {
        return this.defaultField;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaultOperator(int defaultOperator) {
        this.defaultOperator = defaultOperator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDefaultOperator() {
        return this.defaultOperator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPhraseQueryAutoGeneration(boolean phraseQueryAutoGeneration) {
        this.phraseQueryAutoGeneration = phraseQueryAutoGeneration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getPhraseQueryAutoGeneration() {
        return this.phraseQueryAutoGeneration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnablePositionIncrements(boolean positionIncrements) {
        this.positionIncrements = positionIncrements;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getEnablePositionIncrements() {
        return this.positionIncrements;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFuzzyMinSim(float fuzzyMinSim) {
        this.fuzzyMinSim = fuzzyMinSim;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getFuzzyMinSim() {
        return this.fuzzyMinSim;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFuzzyPrefixLength(int fuzzyPrefixLength) {
        this.fuzzyPrefixLength = fuzzyPrefixLength;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFuzzyPrefixLength() {
        return this.fuzzyPrefixLength;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPhraseSlop(int phraseSlop) {
        this.phraseSlop = phraseSlop;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPhraseSlop() {
        return this.phraseSlop;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAllowLeadingWildcard(boolean allowLeadingWildcard) {
        this.allowLeadingWildcard = allowLeadingWildcard;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getAllowLeadingWildcard() {
        return this.allowLeadingWildcard;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLowercaseExpandedTerms(boolean lowercaseExpandedTerms) {
        this.lowercaseExpandedTerms = lowercaseExpandedTerms;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getLowercaseExpandedTerms() {
        return this.lowercaseExpandedTerms;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMultiTermRewriteMethod(MultiTermQuery.RewriteMethod multiTermRewriteMethod) {
        this.multiTermRewriteMethod = multiTermRewriteMethod;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultiTermQuery.RewriteMethod getMultiTermRewriteMethod() {
        return this.multiTermRewriteMethod;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Locale getLocale() {
        return this.locale;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDateResolution(DateTools.Resolution dateResolution) {
        this.dateResolution = dateResolution;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDateResolution(String field, DateTools.Resolution resolution) {
        if (field == null) {
            throw new IllegalArgumentException("field cannot be null.");
        }
        if (this.fieldToDateResolution == null) {
            this.fieldToDateResolution = new HashMap<String, DateTools.Resolution>();
        }
        this.fieldToDateResolution.put(field, resolution);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DateTools.Resolution getDateResolution(String field) {
        if (field == null) {
            throw new IllegalArgumentException("field cannot be null.");
        }
        if (this.fieldToDateResolution == null) {
            return this.dateResolution;
        }
        DateTools.Resolution resolution = this.fieldToDateResolution.get(field);
        if (resolution == null) {
            resolution = this.dateResolution;
        }
        return resolution;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRangeTermAnalysis(boolean value) {
        this.rangeTermAnalysis = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getRangeTermAnalysis() {
        return this.rangeTermAnalysis;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMaxDeterminizedStates(int max) {
        this.maxDeterminizedStates = max;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxDeterminizedStates() {
        return this.maxDeterminizedStates;
    }
}
