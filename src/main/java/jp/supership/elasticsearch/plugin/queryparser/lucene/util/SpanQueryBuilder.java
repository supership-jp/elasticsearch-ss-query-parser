/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

/**
 * Creates queries from the {@link Analyzer} chain.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class SpanQueryBuilder {
    /** Holds analyzer which is responsible for handling chain. */
    private Analyzer analyzer;

    /** Holds true if the builder maintains the offset for the stop-words. */
    private boolean enablePositionIncrements = true;

    /**
     * Constructor.
     */
    public SpanQueryBuilder(Analyzer analyzer) {
	this.analyzer = analyzer;
    }

    /** 
     * Creates a boolean query from the query text.
     * @param field field name
     * @param queryText text to be passed to the analyzer
     * @return {@code TermQuery} or {@code BooleanQuery}.
     */
    public Query createBooleanQuery(String field, String queryText) {
	return this.createBooleanQuery(field, queryText, BooleanClause.Occur.SHOULD);
    }

    /** 
     * Creates a boolean query from the query text.
     * @param field field name
     * @param queryText text to be passed to the analyzer
     * @param operator operator used for clauses between analyzer tokens.
     * @return {@code TermQuery} or {@code BooleanQuery}.
     */
    public Query createBooleanQuery(String field, String queryText, BooleanClause.Occur operator) {
	if (operator != BooleanClause.Occur.SHOULD && operator != BooleanClause.Occur.MUST) {
	    throw new IllegalArgumentException("invalid operator: only SHOULD or MUST are allowed");
	}
	return this.createFieldQuery(analyzer, operator, field, queryText, false, 0);
    }

    /** 
     * Creates a phrase query from the query text.
     * This is equivalent to {@code createPhraseQuery(field, queryText, 0)}
     * @param field field name
     * @param queryText text to be passed to the analyzer
     * @return {@code TermQuery}, {@code BooleanQuery}, {@code PhraseQuery}, or {@code MultiPhraseQuery}.
     */
    public Query createPhraseQuery(String field, String queryText) {
	return createPhraseQuery(field, queryText, 0);
    }

    /** 
     * Creates a phrase query from the query text.
     * @param field field name
     * @param queryText text to be passed to the analyzer
     * @param phraseSlop number of other words permitted between words in query phrase
     * @return {@code TermQuery}, {@code BooleanQuery}, {@code PhraseQuery}, or {@code MultiPhraseQuery}.
     */
    public Query createPhraseQuery(String field, String queryText, int phraseSlop) {
	return this.createFieldQuery(analyzer, BooleanClause.Occur.MUST, field, queryText, true, phraseSlop);
    }

    /**
     * Creates a query from the analysis chain.
     * <p>
     * Expert: this is more useful for subclasses such as queryparsers. 
     * If using this class directly, just use {@link #createBooleanQuery(String, String)}
     * and {@link #createPhraseQuery(String, String)}
     * @param analyzer analyzer used for this query
     * @param operator default boolean operator used for this query
     * @param field field to create queries against
     * @param queryText text to be passed to the analysis chain
     * @param quoted true if phrases should be generated when terms occur at more than one position
     * @param phraseSlop slop factor for phrase/multiphrase queries
     */
    protected final Query createFieldQuery(Analyzer analyzer, BooleanClause.Occur operator, String field, String queryText, boolean quoted, int phraseSlop) {
	assert operator == BooleanClause.Occur.SHOULD || operator == BooleanClause.Occur.MUST;

	// Use the analyzer to get all the tokens, and then build an appropriate
	// query based on the analysis chain.

	try (TokenStream source = analyzer.tokenStream(field, queryText);
	     CachingTokenFilter stream = new CachingTokenFilter(source)) {

	    TermToBytesRefAttribute termAtt = stream.getAttribute(TermToBytesRefAttribute.class);
	    PositionIncrementAttribute posIncAtt = stream.addAttribute(PositionIncrementAttribute.class);

	    if (termAtt == null) {
		return null;
	    }

	    // phase 1: read through the stream and assess the situation:
	    // counting the number of tokens/positions and marking if we have any synonyms.

	    int numTokens = 0;
	    int positionCount = 0;
	    boolean hasSynonyms = false;

	    stream.reset();
	    while (stream.incrementToken()) {
		numTokens++;
		int positionIncrement = posIncAtt.getPositionIncrement();
		if (positionIncrement != 0) {
		    positionCount += positionIncrement;
		} else {
		    hasSynonyms = true;
		}
	    }

	    // phase 2: based on token count, presence of synonyms, and options
	    // formulate a single term, boolean, or phrase.

	    if (numTokens == 0) {
		return null;
	    } else if (numTokens == 1) {
		// single term
		return analyzeTerm(field, stream);
	    } else if (quoted && positionCount > 1) {
		// phrase
		if (hasSynonyms) {
		    // complex phrase with synonyms
		    return analyzeMultiPhrase(field, stream, phraseSlop);
		} else {
		    // simple phrase
		    return analyzePhrase(field, stream, phraseSlop);
		}
	    } else {
		// boolean
		if (positionCount == 1) {
		    // only one position, with synonyms
		    return analyzeBoolean(field, stream);
		} else {
		    // complex case: multiple positions
		    return analyzeMultiBoolean(field, stream, operator);
		}
	    }
	} catch (IOException e) {
	    throw new RuntimeException("Error analyzing query text", e);
	}
    }

    /** 
     * Creates simple term query from the cached tokenstream contents 
     */
    private Query analyzeTerm(String field, TokenStream stream) throws IOException {
	TermToBytesRefAttribute termAtt = stream.getAttribute(TermToBytesRefAttribute.class);
	BytesRef bytes = termAtt.getBytesRef();

	stream.reset();
	if (!stream.incrementToken()) {
	    throw new AssertionError();
	}

	termAtt.fillBytesRef();
	return newTermQuery(new Term(field, BytesRef.deepCopyOf(bytes)));
    }

    /** 
     * Creates simple boolean query from the cached tokenstream contents 
     */
    private Query analyzeBoolean(String field, TokenStream stream) throws IOException {
	BooleanQuery q = newBooleanQuery(true);

	TermToBytesRefAttribute termAtt = stream.getAttribute(TermToBytesRefAttribute.class);
	BytesRef bytes = termAtt.getBytesRef();

	stream.reset();
	while (stream.incrementToken()) {
	    termAtt.fillBytesRef();
	    Query currentQuery = newTermQuery(new Term(field, BytesRef.deepCopyOf(bytes)));
	    q.add(currentQuery, BooleanClause.Occur.SHOULD);
	}

	return q;
    }

    /** 
     * Creates complex boolean query from the cached tokenstream contents 
     */
    private Query analyzeMultiBoolean(String field, TokenStream stream, BooleanClause.Occur operator) throws IOException {
	BooleanQuery q = newBooleanQuery(false);
	Query currentQuery = null;

	TermToBytesRefAttribute termAtt = stream.getAttribute(TermToBytesRefAttribute.class);
	BytesRef bytes = termAtt.getBytesRef();

	PositionIncrementAttribute posIncrAtt = stream.getAttribute(PositionIncrementAttribute.class);

	stream.reset();
	while (stream.incrementToken()) {
	    termAtt.fillBytesRef();
	    if (posIncrAtt.getPositionIncrement() == 0) {
		if (!(currentQuery instanceof BooleanQuery)) {
		    Query t = currentQuery;
		    currentQuery = newBooleanQuery(true);
		    ((BooleanQuery)currentQuery).add(t, BooleanClause.Occur.SHOULD);
		}
		((BooleanQuery)currentQuery).add(newTermQuery(new Term(field, BytesRef.deepCopyOf(bytes))), BooleanClause.Occur.SHOULD);
	    } else {
		if (currentQuery != null) {
		    q.add(currentQuery, operator);
		}
		currentQuery = newTermQuery(new Term(field, BytesRef.deepCopyOf(bytes)));
	    }
	}
	q.add(currentQuery, operator);

	return q;
    }

    /** 
     * Creates simple phrase query from the cached tokenstream contents 
     */
    private Query analyzePhrase(String field, TokenStream stream, int slop) throws IOException {
	PhraseQuery pq = newPhraseQuery();
	pq.setSlop(slop);

	TermToBytesRefAttribute termAtt = stream.getAttribute(TermToBytesRefAttribute.class);
	BytesRef bytes = termAtt.getBytesRef();

	PositionIncrementAttribute posIncrAtt = stream.getAttribute(PositionIncrementAttribute.class);
	int position = -1;

	stream.reset();
	while (stream.incrementToken()) {
	    termAtt.fillBytesRef();

	    if (enablePositionIncrements) {
		position += posIncrAtt.getPositionIncrement();
		pq.add(new Term(field, BytesRef.deepCopyOf(bytes)), position);
	    } else {
		pq.add(new Term(field, BytesRef.deepCopyOf(bytes)));
	    }
	}

	return pq;
    }

    /** 
     * Creates complex phrase query from the cached tokenstream contents 
     */
    private Query analyzeMultiPhrase(String field, TokenStream stream, int slop) throws IOException {
	MultiPhraseQuery mpq = newMultiPhraseQuery();
	mpq.setSlop(slop);

	TermToBytesRefAttribute termAtt = stream.getAttribute(TermToBytesRefAttribute.class);
	BytesRef bytes = termAtt.getBytesRef();

	PositionIncrementAttribute posIncrAtt = stream.getAttribute(PositionIncrementAttribute.class);
	int position = -1;

	List<Term> multiTerms = new ArrayList<>();
	stream.reset();
	while (stream.incrementToken()) {
	    termAtt.fillBytesRef();
	    int positionIncrement = posIncrAtt.getPositionIncrement();

	    if (positionIncrement > 0 && multiTerms.size() > 0) {
		if (enablePositionIncrements) {
		    mpq.add(multiTerms.toArray(new Term[0]), position);
		} else {
		    mpq.add(multiTerms.toArray(new Term[0]));
		}
		multiTerms.clear();
	    }
	    position += positionIncrement;
	    multiTerms.add(new Term(field, BytesRef.deepCopyOf(bytes)));
	}

	if (enablePositionIncrements) {
	    mpq.add(multiTerms.toArray(new Term[0]), position);
	} else {
	    mpq.add(multiTerms.toArray(new Term[0]));
	}
	return mpq;
    }

    /**
     * Instanciates a new BooleanQuery.
     * @param  disableCoord set to be true if the coordinate information is not important fot scoring.
     * @return new BooleanQuery instance.
     */
    protected BooleanQuery newBooleanQuery(boolean disableCoord) {
	return new BooleanQuery(disableCoord);
    }

    /**
     * Instanciates a new SpanTermQuery.
     * @param  term the term to be used for query construction.
     * @return new SpanTermQuery instance.
     */
    protected SpanTermQuery newSpanTermQuery(Term term) {
	return new SpanTermQuery(term);
    }

    /**
     * Instanciates a new SpanNearQuery.
     * @param  clauses the clauses constructs the argumented span near query.
     * @param  slop the slop width to be set.
     * @param  inOrder set to be true if the order is important.
     * @return new SpanNearQuery instance.
     */
    protected SpanNearQuery newSpanNearQuery(SpanQuery[] clauses, int slop, boolean inOrder) {
	return new SpanNearQuery(clauses, slop, inOrder);
    }

    /**
     * Instanciates a new SpanNearQuery.
     * @param  clauses the clauses constructs the argumented span near query.
     * @param  slop the slop width to be set.
     * @param  inOrder set to be true if the order is important.
     * @param  collectPayloads set to be true if the payload information is neccessary.
     * @return new SpanNearQuery instance.
     */
    protected SpanNearQuery newSpanNearQuery(SpanQuery[] clauses, int slop, boolean inOrder, boolean collectPayloads) {
	return new SpanNearQuery(clauses, slop, inOrder, collectPayloads);
    }

    /**
     * Instanciates a new SpanMultiTermQueryWrapper.
     * @param  query the multi term query to be wrapped.
     * @return new SpanMultiTermQueryWrapper instance.
     */
    protected <Q extends MultiTermQuery> SpanMultiTermQueryWrapper newSpanMultiTermQueryWrapper(Q query) {
	return new SpanMultiTermQueryWrapper(query);
    }

    /**
     * Returns the analyzer.
     * @return the assigned analyzer.
     */
    public Analyzer getAnalyzer() {
	return this.analyzer;
    }

    /**
     * Sets the analyzer used to tokenize text.
     * @param analyzer the assigned to be set.
     */
    public void setAnalyzer(Analyzer analyzer) {
	this.analyzer = analyzer;
    }

    /**
     * Returns true if position increments are enabled.
     * @return true if the position increment functionality is set to be true.
     */
    public boolean getEnablePositionIncrements() {
	return this.enablePositionIncrements;
    }

    /**
     * Sets the position-increments functionality.
     * @param enablePositionIncrements the position-increments functionality setting value.
     */
    public void setEnablePositionIncrements(boolean enablePositionIncrements) {
	this.enablePositionIncrements = enablePositionIncrements;
    }
}
