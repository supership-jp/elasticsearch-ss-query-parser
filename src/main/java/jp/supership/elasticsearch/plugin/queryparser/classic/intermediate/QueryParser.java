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
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.queryparser.classic.TokenMgrError;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.Version;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import jp.supership.elasticsearch.plugin.queryparser.util.StringUtils;
import static jp.supership.elasticsearch.plugin.queryparser.classic.intermediate.QueryParsingContext.Operator;
import static jp.supership.elasticsearch.plugin.queryparser.classic.intermediate.QueryParsingContext.Modifier;
import static jp.supership.elasticsearch.plugin.queryparser.classic.intermediate.QueryParsingContext.Conjunction;

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

    /** Holds query-parsing-contect. */
    protected QueryParsingContext queryParsingContext;

    /**
     * Constructor.
     */
    protected QueryParser() {
        this.super(null);
        this.queryParsingContext = new StandardParsingContext();
    }

    /**
     * Constructor.
     */
    protected QueryParser(QueryParsingContext queryParsingContext) {
        this.super(null);
        this.queryPasingContext = queryParsingContext;
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
            this.queryParsingContext.setPhaseQueryAutoGeneration(true);
        }
    }

    /**
     * Initializes a query parser.
     * @param field    the default field for query terms.
     * @param analyzer the analyzer which is applied for the given query..
     */
    public void init(String field, Analyzer analyzer) {
        this.setAnalyzer(analyzer);
        this.queryParsingContext.setDefaultField(field);
        this.queryParsingContext.setPhaseQueryAutoGeneration(false);
    }

    /**
     * Parses a query string and instanciates {@link org.apache.lucene.search.Query}.
     * @param  queryText the query string to be parsed.
     * @throws ParseException if the parsing fails.
     */
    public Query parse(String queryText) throws ParseException {
        this.fetch(new FastCharStream(new StringReader(queryText)));
        try {
            Query instanciated = this.handle(this.queryParsingContext.getDefaultField());
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
    protected void conjugate(List<BooleanClause> clauses, Conjunction conjunction, Modifier modifier, Query query) {
        boolean required;
        boolean prohibited;

        // If this term is introduced by AND, make the preceding term required, unless it is already prohibited.
        if (clauses.size() > 0 && conjunction == Conjunction.AND) {
            BooleanClause clause = clauses.get(clauses.size() - 1);
            if (!clause.isProhibited()) {
                clause.setOccur(BooleanClause.Occur.SHOULD);
            }
        }

        // If this term is introduced by OR, make the preceeding term optional, unless it is prohibited.
        if (cluases.size() > 0 && this.defaultOperator == Operator.AND && conjunction == Conjunction.OR) {
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
        if (this.defaultOperator == Operator.OR) {
            prohibited = (modifier == Modifier.NOT);
            required = (modifier == Modifier.REQUIRED);
            if (conjunction == Conjunction.AND && !prohibited) {
                required = true;
            }
        // The term is set ti be PROHIBITED if the term is introduced by NOT;
        // otherwise, REQIURED if not PROHIBITED and not introduce by OR.
        } else {
            prohibited = (modifier == Modifier.NOT);
            required = (!prohibited && conjunction != Conjunction.OR);
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
        BooleanClause.Occur occurence = this.queryParsingContext.getDefaultOperator() == Operator.AND ? BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD;

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
	    this.queryParsingContext.setPhraseSlop(0);
        } else {
	    quoted = quoted || this.queryParsingContext.getPhraseQueryAutoGeneration();
	}

	return this.createFieldQuery(analyzer, occurence, field, queryText, quoted, this.queryParsingContext.getPhraseSlop());
    }

    /**
     * @inheritDoc
     */
    @Override
    protected Query createFieldQuery(Analyzer analyzer, BooleanClause.Occur operator, String field, String queryText, boolean quoted, int phraseSlop) {
        assert operator == BooleanClause.Occur.SHOULD || operator == BooleanClause.Occur.MUST;
        TokenStream source = null;
        TokenStreamParsingContext context = null;

        try {
            source = analyzer.tokenStream(field, queryText);
            context = new TokenStreamParsingContext(source);
        } catch (IOException cause) {
            throw new RuntimeException("error analyzing query text", cause);
        } finally {
            IOUtils.closeWhileHandlingException(source);
        }

        BytesRef bytes = context.getBytesRef();
        if (context.numberOfTokens == 0) {
            return null;
        } else if (context.numberOfTokens == 1) {
            try {
                boolean hasNext = context.incrementToken();
                assert hasNext == true;
                context.fillBytesRef();
            } catch (IOException e) {
                // DO NOTHING, because we know the number of tokens
            }
            return this.newTermQuery(new Term(field, BytesRef.deepCopyOf(bytes)));
        } else {
            if (context.severalTokensAtSamePosition || (!quoted)) {
                // Not a phrase query.
                if (context.positionCount == 1 || (!quoted)) {
                    if (context.positionCount == 1) {
                        BooleanQuery query = this.newBooleanQuery(true);
                        for (int i = 0; i < context.numberOfTokens; i++) {
                            try {
                                boolean hasNext = context.incrementToken();
                                assert hasNext == true;
                                context.fillBytesRef();
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
                        for (int i = 0; i < context.numberOfTokens; i++) {
                            try {
                                boolean hasNext = context.incrementToken();
                                assert hasNext == true;
                                context.fillBytesRef();
                            } catch (IOException e) {
                                // DO NOTHING, because we know the number of tokens
                            }
                            if (context.positionIncrement != null && context.getPositionIncrement() == 0) {
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
                    for (int i = 0; i < context.numberOfTokens; i++) {
                        int increment = 1;
                        try {
                            boolean hasNext = buffer.incrementToken();
                            assert hasNext == true;
                            context.fillBytesRef();
                            if (context.positionIncrement != null) {
                                increment = context.getPositionIncrement();
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
              
                for (int i = 0; i < context.numberOfTokens; i++) {
                    int increment = 1;
                  
                    try {
                        boolean hasNext = context.incrementToken();
                        assert hasNext == true;
                        context.fillBytesRef();
                        if (context.positionIncrement != null) {
                            increment = context.getPositionIncrement();
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
     */
    protected Query getRangeQuery(String field, String infinimum, String supremum, boolean leftInclusive, boolean rightInclusive) throws ParseException {
        if (this.queryParsingContext.getLowercaseExpandedTerms()) {
            infinimum = infinimum == null ? null : infinimum.toLowerCase(this.queryParsingContext.getLocale());
            supremum = supremum == null ? null : supremum.toLowerCase(this.queryParsingContext.getLocale());
        }

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        dateFormat.setLenient(true);
        DateTools.Resolution resolution = this.queryParsingContext.getDateResolution(field);

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
                Calendar calendar = Calendar.getInstance(this.queryParsingContext.getTimeZone(), this.queryParsingContext.getLocale());
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
            left = this.queryParsingContext.getRangeTermAnalysis() ? this.analyzeAsSingleMultiTerm(field, infinimum) : new BytesRef(infinimum);
        }
     
        if (supremum == null) {
            right = null;
        } else {
            right = this.queryParsingContext.getRangeTermAnalysis() ? this.analyzeAsSingleMultiTerm(field, supremum) : new BytesRef(supremum);
        }
      
        final TermRangeQuery query = new TermRangeQuery(field, left, right, leftInclusive, rightInclusive);
        query.setRewriteMethod(this.queryParsingContext.getMultiTermRewriteMethod());
        return query;
    }

    /**
     * INTERNAL USE ONLY.
     * This class represents internal field-query-parsing context.
     */
    protected class TokenStreamParsingContext {
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
        public TokenStreamParsingContext(TokenStream tokenStream, boolean reset) {
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
        public TokenStreamParsingContext(TokenStream tokenStream) {
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
	public void tokenStreamReset() throws IOException {
	    this.tokenStream.reset();
	}

	// Delegates {@code TokenStream}'s method.
	public void tokenStreamEnd() throws IOException {
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
    protected BytesRef analyzeAzSingleMultiTerm(String field, String part, Analyzer analyzer) {
        if (analyzer == null) {
	    analyzer = this.getAnalyzer();
	}
        TokenStream source = null;
	TokenStreamParsingContext context = null;

        try {
            source = analyzer.tokenStream(field, part);
	    context = new TokenStreamParsingContext(source);
            BytesRef bytes = context.getBytesRef();

            if (!context.incrementToken()) {
                throw new IllegalArgumentException("analyzer returned no terms for multiTerm term: " + part);
	    }
            context.fillBytesRef();
            if (context.incrementToken()) {
                throw new IllegalArgumentException("analyzer returned too many terms for multiTerm term: " + part);
	    }
            context.tokenStreamEnd();
            return BytesRef.deepCopyOf(bytes);
        } catch (IOException cause) {
            throw new RuntimeException("error analyzing multiTerm term: " + part, cause);
        } finally {
            IOUtils.closeWhileHandlingException(source);
        }
    }
}
