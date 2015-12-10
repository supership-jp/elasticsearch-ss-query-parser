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
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IOUtils;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl.ExternalQueryParser;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl.InternalQueryParser;
import jp.supership.elasticsearch.plugin.queryparser.common.util.StringUtils;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.config.DSQParserConfiguration;

/**
 * This class is responsible for instanciating Lucene queries, query parser delegates all sub-query
 * instanciation tasks to this class. This implementation assumes that concrete query handler will be 
 * constructed in accordance to the given ANTLR grammar.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public abstract class ProximityQueryEngine extends QueryEngine implements ProximityQueryDriver {
    /**
     * Constructor.
     */
    public ProximityQueryEngine() {
        super();
    }

    /**
     * Constructor.
     */
    public ProximityQueryEngine(DSQParserConfiguration configuration) {
        super();
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void conjugate(List<SpanQuery> clauses, int conjunction, int modifier, int slop, boolean inOrder, SpanQuery query) {
    }

    /**
     * ANOTHER IMPLEMENTATION FOR SPAN QUERY FAMILY.
     * {@inheritDoc}
     */
    @Override
    public Query getFieldQuery(String field, String queryText, boolean quoted) throws ParseException {
        return this.newFieldQuery(this.getAnalyzer(), field, queryText, quoted);
    }

    /**
     * ANOTHER IMPLEMENTATION FOR SPAN QUERY FAMILY.
     * {@inheritDoc}
     */
    @Override
    public Query getFieldQuery(String field, String queryText, boolean quoted, boolean useDisMax) throws ParseException {
        return this.newFieldQuery(this.getAnalyzer(), field, queryText, quoted, useDisMax);
    }

    /**
     * ANOTHER IMPLEMENTATION FOR SPAN QUERY FAMILY.
     * {@inheritDoc}
     */
    @Override
    public Query getFieldQuery(String field, String queryText, int phraseSlop) throws ParseException {
        Query query = this.getFieldQuery(field, queryText, true);

	//        if (query instanceof SpanNearQuery) {
	//            ((SpanNearQuery) query).setSlop(phraseSlop);
	//        }

        return query;
    }

    /**
     * ANOTHER IMPLEMENTATION FOR SPAN QUERY FAMILY.
     * {@inheritDoc}
     */
    @Override
    public Query getFieldQuery(String field, String queryText, int phraseSlop, boolean useDisMax) throws ParseException {
        Query query = this.getFieldQuery(field, queryText, true, useDisMax);

	//        if (query instanceof SpanNearQuery) {
	//            ((SpanNearQuery) query).setSlop(phraseSlop);
	//        }

        return query;
    }

    /**
     * ANOTHER IMPLEMENTATION FOR SPAN QUERY FAMILY.
     * {@inheritDoc}
     */
    @Override
    protected Query newFieldQuery(Analyzer analyzer, String field, String queryText, boolean quoted) throws ParseException {
        return this.newFieldQuery(analyzer, field, queryText, quoted, true);
    }

    /**
     * ANOTHER IMPLEMENTATION FOR SPAN QUERY FAMILY.
     * {@inheritDoc}
     */
    @Override
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
     * ANOTHER IMPLEMENTATION FOR SPAN QUERY FAMILY.
     * {@inheritDoc}
     */
    @Override
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
	    return this.newSpanTermQuery(new Term(field, BytesRef.deepCopyOf(bytes)));
        } else {
            if (handler.severalTokensAtSamePosition || (!quoted)) {
                // Not a phrase query.
                if (handler.positionCount == 1 || (!quoted)) {
                    if (handler.positionCount == 1) {
			SpanOrQuery query = (SpanOrQuery) this.newSpanOrQuery(new SpanQuery[handler.numberOfTokens]);
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
    public SpanQuery getSpanTermQuery(String field, String termText, boolean quoted) throws ParseException {
	return this.newSpanTermQuery(new Term(field, termText));
    }

    /**
     * Returns {@code SpanTermQuery} in accordance to the assigned configuration.
     * @param  query the currently handling sub query.
     * @return new {@link SpanTermQuery} instance.
     */
    protected SpanQuery newSpanTermQuery(Term term) {
	return new SpanTermQuery(term);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SpanQuery getSpanNotQuery(SpanQuery inclusion, SpanQuery exclusion) throws ParseException {
	return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SpanQuery getSpanNearQuery(int slop, boolean inOrder, SpanQuery... clauses) throws ParseException {
	return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SpanQuery getSpanOrQuery(SpanQuery... clauses) throws ParseException {
	return this.newSpanOrQuery(clauses);
    }

    /**
     * Returns {@code SpanOrQuery} in accordance to the assigned configuration.
     * @param  clauses the currently handling sub queries.
     * @return new {@link SpanTermQuery} instance.
     */
    protected SpanQuery newSpanOrQuery(SpanQuery... clauses) {
	return new SpanOrQuery(clauses);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <Q extends MultiTermQuery> SpanQuery getSpanMultiTermQueryWrapper(Q wrapped) throws ParseException {
	return null;
    }
}
