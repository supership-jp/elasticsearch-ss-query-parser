/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.classic.intermediate;

import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.queryparser.flexible.standard.CommonQueryParserConfiguration;
import static org.apache.lucene.util.automaton.Operations.DEFALUT_MAX_DETERMINIZED_STATES;
import static jp.supership.elasticsearch.plugin.queryparser.classic.intermediate.QueryParsingContext.Operators;

/**
 * This interface specifies the implementing class can be configured in accordance with the
 * Lucene's {@code CommonQueryParserConfiguration} API and some additional context.
 *
 * @author Shingo OKAWA
 * @since  08/11/2015
 */
public class DSQParsingContext implements QueryParsingContext {
    // TODO: FIX THIS DEFAULT VALUE TO BE APPROPRIATE ONE.
    /** Holds default field for query terms. */
    protected String defaultField = "";

    /** Holds the default operator parsers use to combine query terms. */
    protected Operator defaultOperator = Operators.OR;

    /** Holds phrase-query-auto-genertion functionality setting. */
    protected boolean phraseQueryAutoGeneration = false;

    /** Holds default date resolution. */
    protected DateTools.Resolution dateResolution = null;

    /** Holds mapping between field names and its corresponding date resolutions. */
    protected Map<String, DateTools.Resolution> fieldToDateResolution = null;

    /** Holds range-term-analysis functionality setting. */
    protected boolean rangeTermAnalysis = false;

    /** Holds maximum number of states. */
    protected int maxDeterminizedStates = DEFALUT_MAX_DETERMINIZED_STATES;

    /**
     * @inheritdoc
     */
    @Override
    public void setDefaultField(String field) {
	this.defaultField = field;
    }

    /**
     * @inheritdoc
     */
    @Override
    public String getDefaultField() {
	return this.defaultField;
    }

    /**
     * @inheritdoc
     */
    @Override
    public void setDefaultOperator(Operator operator) {
	this.defaultOperator = operator;
    }

    /**
     * @inheritdoc
     */
    @Override
    public Operator getDefaultOperator() {
	return this.defaultOperator;
    }

    /**
     * @inheritdoc
     */
    @Override
    public void setPhraseQueryAutoGeneration(boolean value) {
	this.phraseQueryAutoGeneration = value;
    }

    /**
     * @inheritdoc
     */
    @Override
    public boolean getPhraseQueryAutoGeneration() {
	return this.phraseQueryAutoGeneration;
    }

    /**
     * @inheritdoc
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
     * @inheritdoc
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
	    resulution = this.dateResolution;
	}
	return resolution;
    }

    /**
     * @inheritdoc
     */
    @Override
    public void setRangeTermAnalysis(boolean value) {
	this.rangeTermAnalysis = value;
    }

    /**
     * @inheritdoc
     */
    @Override
    public boolean getRangeTermAnalysis() {
	return this.rangeTermAnalysis;
    }

    /**
     * @inheritdoc
     */
    @Override
    public void setMaxDeterminizedStates(int max) {
	this.maxDeterminizedStates = max;
    }

    /**
     * @inheritdoc
     */
    @Override
    public int getMaxDeterminizedStates() {
	return this.maxDeterminizedStates;
    }
}
