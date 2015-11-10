/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.classic.intermediate;

import java.util.ArrayList;
import java.util.List;
// TODO: Check which CharStream is called.
// TODO: Check if the FastCharStream must be implemented or not.
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.analysis.tokenattributes.PoistionIncrementAttreibute;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.queryparser.classic.TokenMgrError;
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

    /**
     * INTERNAL USE ONLY.
     * This class represents internal field-query-parsing context.
     */
    private static class FieldQueryParsingContext {
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
    }

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
     * Returns 
     * @throws ParseException if the parsing fails.
     */
    protected Query getFieldQuery(String field, String queryText, boolean quoted) throws ParseException {
	return this.newFieldQuery(this.getAnalyzer(), field, queryText, quoted);
    }

    /**
     *
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
	} else if (quoted == false) {
	}
    }

    /**
     *
     */
    @Override
    protected Query createFieldQuery(Analyzer analyzer, BooleanClause.Occur operator, String field, String queryText, boolean quoted, int phraseSlop) {
	assert operator == BooleanClause.Occur.SHOULD || operator == BooleanClause.Occur.MUST;
	CachingTokenFilter buffer = null;
	TermToBytesRefAttribute termAtt = null;
	PositionIncrementAttribute posIncrAtt = null;
	int numTokens = 0;
	int positionCount = 0;
	boolean severalTokensAtSamePosition = false;
	boolean hasMoreTokens = false;
      
	TokenStream source = null;
	try {
	    source = analyzer.tokenStream(field, queryText);
	    source.reset();
	    buffer = new CachingTokenFilter(source);
	    buffer.reset();
	    termAtt = buffer.getAttribute(TermToBytesRefAttribute.class);
	    posIncrAtt = buffer.getAttribute(PositionIncrementAttribute.class);

	    if (termAtt != null) {
		try {
		    hasMoreTokens = buffer.incrementToken();
		    while (hasMoreTokens) {
			numTokens++;
			int positionIncrement = (posIncrAtt != null) ? posIncrAtt.getPositionIncrement() : 1;
			if (positionIncrement != 0) {
			    positionCount += positionIncrement;
			} else {
			    severalTokensAtSamePosition = true;
			}
			hasMoreTokens = buffer.incrementToken();
		    }
		} catch (IOException e) {
		    // ignore
		}
	    }
	} catch (IOException e) {
	    throw new RuntimeException("Error analyzing query text", e);
	} finally {
	    IOUtils.closeWhileHandlingException(source);
	}
      
	// rewind the buffer stream
	buffer.reset();
      
	BytesRef bytes = termAtt == null ? null : termAtt.getBytesRef();

	if (numTokens == 0)
	    return null;
	else if (numTokens == 1) {
	    try {
		boolean hasNext = buffer.incrementToken();
		assert hasNext == true;
		termAtt.fillBytesRef();
	    } catch (IOException e) {
		// safe to ignore, because we know the number of tokens
	    }
	    return newTermQuery(new Term(field, BytesRef.deepCopyOf(bytes)));
	  
	} else {
	    if (severalTokensAtSamePosition || (!quoted)) {
		if (positionCount == 1 || (!quoted)) {
		    // no phrase query:
		  
		    if (positionCount == 1) {
			// simple case: only one position, with synonyms
			BooleanQuery q = newBooleanQuery(true);
			for (int i = 0; i < numTokens; i++) {
			    try {
				boolean hasNext = buffer.incrementToken();
				assert hasNext == true;
				termAtt.fillBytesRef();
			    } catch (IOException e) {
				// safe to ignore, because we know the number of tokens
			    }
			    Query currentQuery = newTermQuery(
							      new Term(field, BytesRef.deepCopyOf(bytes)));
			    q.add(currentQuery, BooleanClause.Occur.SHOULD);
			}
			return q;
		    } else {
			// multiple positions
			BooleanQuery q = newBooleanQuery(false);
			Query currentQuery = null;
			for (int i = 0; i < numTokens; i++) {
			    try {
				boolean hasNext = buffer.incrementToken();
				assert hasNext == true;
				termAtt.fillBytesRef();
			    } catch (IOException e) {
				// safe to ignore, because we know the number of tokens
			    }
			    if (posIncrAtt != null && posIncrAtt.getPositionIncrement() == 0) {
				if (!(currentQuery instanceof BooleanQuery)) {
				    Query t = currentQuery;
				    //currentQuery = newBooleanQuery(true);
				    currentQuery = new DisjunctionMaxQuery(0.0f);
				    ((DisjunctionMaxQuery)currentQuery).add(t);
				}
				((DisjunctionMaxQuery)currentQuery).add(newTermQuery(new Term(field, BytesRef.deepCopyOf(bytes))));
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
		} else {
		    // phrase query:
		    MultiPhraseQuery mpq = newMultiPhraseQuery();
		    mpq.setSlop(phraseSlop);
		    List<Term> multiTerms = new ArrayList<>();
		    int position = -1;
		    for (int i = 0; i < numTokens; i++) {
			int positionIncrement = 1;
			try {
			    boolean hasNext = buffer.incrementToken();
			    assert hasNext == true;
			    termAtt.fillBytesRef();
			    if (posIncrAtt != null) {
				positionIncrement = posIncrAtt.getPositionIncrement();
			    }
			} catch (IOException e) {
			    // safe to ignore, because we know the number of tokens
			}
		      
			if (positionIncrement > 0 && multiTerms.size() > 0) {
			    if (getEnablePositionIncrements()) {
				mpq.add(multiTerms.toArray(new Term[0]),position);
			    } else {
				mpq.add(multiTerms.toArray(new Term[0]));
			    }
			    multiTerms.clear();
			}
			position += positionIncrement;
			multiTerms.add(new Term(field, BytesRef.deepCopyOf(bytes)));
		    }
		    if (getEnablePositionIncrements()) {
			mpq.add(multiTerms.toArray(new Term[0]),position);
		    } else {
			mpq.add(multiTerms.toArray(new Term[0]));
		    }
		    return mpq;
		}
	    } else {
		PhraseQuery pq = newPhraseQuery();
		pq.setSlop(phraseSlop);
		int position = -1;
	      
		for (int i = 0; i < numTokens; i++) {
		    int positionIncrement = 1;
		  
		    try {
			boolean hasNext = buffer.incrementToken();
			assert hasNext == true;
			termAtt.fillBytesRef();
			if (posIncrAtt != null) {
			    positionIncrement = posIncrAtt.getPositionIncrement();
			}
		    } catch (IOException e) {
			// safe to ignore, because we know the number of tokens
		    }
		  
		    if (getEnablePositionIncrements()) {
			position += positionIncrement;
			pq.add(new Term(field, BytesRef.deepCopyOf(bytes)),position);
		    } else {
			pq.add(new Term(field, BytesRef.deepCopyOf(bytes)));
		    }
		}
		return pq;
	    }
	}
    }
}
