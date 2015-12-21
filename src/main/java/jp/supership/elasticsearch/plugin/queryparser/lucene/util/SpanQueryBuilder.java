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
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
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

    /**
     * Constructor.
     */
    public SpanQueryBuilder(Analyzer analyzer) {
	this.analyzer = analyzer;
    }

    /**
     * Creates a span query from the analysis chain.
     * @param analyzer analyzer used for this query.
     * @param field field to create queries against.
     * @param queryText text to be passed to the analysis chain.
     * @param quoted true if phrases should be generated when terms occur at more than one position.
     * @param phraseSlop slop factor for phrase/multiphrase queries.
     * @param inOrder true if the order is important.
     */
    protected final SpanQuery createFieldQuery(Analyzer analyzer, String field, String queryText, boolean quoted, int phraseSlop, boolean inOrder) {
	try (TokenStream source = analyzer.tokenStream(field, queryText); CachingTokenFilter stream = new CachingTokenFilter(source)) {
	    TermToBytesRefAttribute termAttribute = stream.getAttribute(TermToBytesRefAttribute.class);
	    PositionIncrementAttribute positionAttribute = stream.addAttribute(PositionIncrementAttribute.class);

	    if (termAttribute == null) {
		return null;
	    }

	    // phase 1: read through the stream and assess the situation:
	    // counting the number of tokens/positions and marking if we have any synonyms.
	    int numberOfTokens = 0;
	    int positionCount = 0;
	    boolean hasSynonyms = false;
	    stream.reset();
	    while (stream.incrementToken()) {
		numberOfTokens++;
		int positionIncrement = positionAttribute.getPositionIncrement();
		if (positionIncrement != 0) {
		    positionCount += positionIncrement;
		} else {
		    hasSynonyms = true;
		}
	    }

	    // phase 2: based on token count, presence of synonyms, and options
	    // formulate a single term, boolean, or phrase.
	    if (numberOfTokens == 0) {
		return null;
	    } else if (numberOfTokens == 1) {
		// single term
		return this.analyzeSpanTerm(field, stream);
	    } else if (quoted && positionCount > 1) {
		// span near
		if (hasSynonyms) {
		    // complex span near with synonyms
		    return this.analyzeMultiSpanNear(field, stream, phraseSlop, inOrder);
		} else {
		    // simple span near
		    return this.analyzeSpanNear(field, stream, phraseSlop, inOrder);
		}
	    } else {
		// span or
		if (positionCount == 1) {
		    // only one position, with synonyms
		    return this.analyzeSpanOr(field, stream);
		} else {
		    // complex case: multiple positions
		    return this.analyzeMultiSpanOr(field, stream, operator, inOrder);
		}
	    }
	} catch (IOException cause) {
	    throw new RuntimeException("Error analyzing query text", cause);
	}
    }

    /**
     * Creates simple span-term query from the cached tokenstream contents 
     * @param field the currently hnadling field.
     * @param stream the currently hnadling token stream.
     */
    private SpanQuery analyzeSpanTerm(String field, TokenStream stream) throws IOException {
	TermToBytesRefAttribute termAttribute = stream.getAttribute(TermToBytesRefAttribute.class);
	BytesRef bytes = termAttribute.getBytesRef();
	stream.reset();
	if (!stream.incrementToken()) {
	    throw new AssertionError();
	}
	termAttribute.fillBytesRef();
	return this.newSpanTermQuery(new Term(field, BytesRef.deepCopyOf(bytes)));
    }

    /**
     * Creates simple span-or query from the cached tokenstream contents 
     * @param field the currently hnadling field.
     * @param stream the currently hnadling token stream.
     */
    private SpanQuery analyzeSpanOr(String field, TokenStream stream) throws IOException {
	TermToBytesRefAttribute termAttribute = stream.getAttribute(TermToBytesRefAttribute.class);
	BytesRef bytes = termAttribute.getBytesRef();

	List<SpanTermQuery> terms = new ArrayList<SpanTermQuery>();
	stream.reset();
	while (stream.incrementToken()) {
	    termAttribute.fillBytesRef();
	    terms.add(this.newSpanTermQuery(new Term(field, BytesRef.deepCopyOf(bytes))));
	}

	return this.newSpanOrQuery(terms);
    }

    /**
     * Creates complex span-or query from the cached tokenstream contents 
     * @param field the currently hnadling field.
     * @param stream the currently hnadling token stream.
     * @param inOrder true if the order is important.
     */
    private SpanQuery analyzeMultiSpanOr(String field, TokenStream stream, boolean inOrder) throws IOException {
	TermToBytesRefAttribute termAttribute = stream.getAttribute(TermToBytesRefAttribute.class);
	PositionIncrementAttribute positionAttribute = stream.getAttribute(PositionIncrementAttribute.class);
	BytesRef bytes = termAttribute.getBytesRef();

	SpanOrQuery query = this.newSpanOrQuery();
	List<SpanTermQuery> terms = new ArrayList<SpanTermQuery>();
	List<SpanNearQuery> queries = new ArrayList<SpanNearQuery>();
	stream.reset();
	while (stream.incrementToken()) {
	    termAttribute.fillBytesRef();
	    if (positionAttribute.getPositionIncrement() == 0) {
		terms.add(this.newSpanTermQuery(new Term(field, BytesRef.deepCopyOf(bytes))));
	    } else {
		query.addClause(this.newSpanNearQuery(terms, -1, inOrder));
		terms.clear();
	    }
	}

	if (!terms.isEmpty()) {
	    query.addClause(this.newSpanNearQuery(terms, -1, inOrder));
	}

	return query;
    }

    /**
     * Creates span-near query from the cached tokenstream contents.
     * @param field the currently hnadling field.
     * @param stream the currently hnadling token stream.
     * @param inOrder true if the order is important.
     */
    private SpanQuery analyzeSpanNear(String field, TokenStream stream, int slop) throws IOException {
	TermToBytesRefAttribute termAttribute = stream.getAttribute(TermToBytesRefAttribute.class);
	PositionIncrementAttribute positionAttribute = stream.getAttribute(PositionIncrementAttribute.class);
	BytesRef bytes = termAttribute.getBytesRef();
	int position = -1;

	List<SpanTermQuery> terms = new ArrayList<SpanTermQuery>();
	stream.reset();
	while (stream.incrementToken()) {
	    termAttribute.fillBytesRef();
	    int positionIncrement = positionAttribute.getPositionIncrement();
	    position += positionIncrement;
	    terms.add(this.newSpanTermQuery(new Term(field, BytesRef.deepCopyOf(bytes))));
	}

	return this.newSpanNearQuery(terms, position, inOrder);
    }

    /**
     * Creates complex span-near query from the cached tokenstream contents.
     * @param field the currently hnadling field.
     * @param stream the currently hnadling token stream.
     * @param inOrder true if the order is important.
     */
    private SpanQuery analyzeMultiSpanNear(String field, TokenStream stream, boolean inOrder) throws IOException {
	TermToBytesRefAttribute termAttribute = stream.getAttribute(TermToBytesRefAttribute.class);
	PositionIncrementAttribute positionAttribute = stream.getAttribute(PositionIncrementAttribute.class);
	BytesRef bytes = termAttribute.getBytesRef();
	int position = -1;

	List<SpanTermQuery> terms = new ArrayList<SpanTermQuery>();
	List<SpanNearQuery> queries = new ArrayList<SpanNearQuery>();
	stream.reset();
	while (stream.incrementToken()) {
	    termAttribute.fillBytesRef();
	    int positionIncrement = positionAttribute.getPositionIncrement();
	    if (positionIncrement > 0 && terms.size() > 0) {
		queries.add(this.newSpanNearQuery(terms, -1, inOrder));
		terms.clear();
	    }
	    position += positionIncrement;
	    terms.add(this.newSpanTermQuery(new Term(field, BytesRef.deepCopyOf(bytes))));
	}

	return this.newSpanNearQuery(queries, position, inOrder);
    }

    /**
     * Instanciates a new SpanTermQuery.
     * @param  term the term to be used for query construction.
     * @return new SpanTermQuery instance.
     */
    protected SpanQuery newSpanTermQuery(Term term) {
	return new SpanTermQuery(term);
    }

    /**
     * Instanciates a new SpanOrQuery.
     * @return new SpanOrQuery instance.
     */
    protected SpanQuery newSpanOrQuery() {
	return new SpanOrQuery();
    }

    /**
     * Instanciates a new SpanOrQuery.
     * @param  clauses the clauses constructs the argumented span near query.
     * @return new SpanOrQuery instance.
     */
    protected SpanQuery newSpanOrQuery(SpanQuery[] clauses) {
	return new SpanOrQuery(clauses);
    }

    /**
     * Instanciates a new SpanNearQuery.
     * @param  clauses the clauses constructs the argumented span near query.
     * @param  slop the slop width to be set.
     * @param  inOrder set to be true if the order is important.
     * @return new SpanNearQuery instance.
     */
    protected SpanQuery newSpanNearQuery(SpanQuery[] clauses, int slop, boolean inOrder) {
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
    protected SpanQuery newSpanNearQuery(SpanQuery[] clauses, int slop, boolean inOrder, boolean collectPayloads) {
	return new SpanNearQuery(clauses, slop, inOrder, collectPayloads);
    }

    /**
     * Instanciates a new SpanNotQuery.
     * @param  inclusion the clauses which is to be included.
     * @param  exclusion the clauses which is to be excluded.
     * @return new SpanNotQuery instance.
     */
    protected SpanQuery newSpanNotQuery(SpanQuery[] inclusion, SpanQuery[] exclusion) {
	return new SpanNotQuery(inclusion, exclusion);
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
}
