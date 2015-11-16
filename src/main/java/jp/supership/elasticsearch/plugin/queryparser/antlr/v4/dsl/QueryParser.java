// Generated from jp/supership/elasticsearch/plugin/queryparser/antlr/v4/dsl/Query.g4 by ANTLR 4.5
package jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class QueryParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		LPAREN=1, RPAREN=2, COLON=3, ASTERISC=4, QUESTION=5, CONJUNCTION_AND=6, 
		CONJUNCTION_OR=7, MODIFIER_NEGATE=8, MODIFIER_REQUIRE=9, TERM_STRING=10, 
		TERM_NUMBER=11, TERM_FIELD=12, MODIFIER_REQUIERD=13, FIELD=14, STRING=15, 
		NUMBER=16;
	public static final int
		RULE_query = 0, RULE_expression = 1, RULE_term = 2;
	public static final String[] ruleNames = {
		"query", "expression", "term"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'('", "')'", "':'", "'*'", "'?'", null, null, "'-'", "'_'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "LPAREN", "RPAREN", "COLON", "ASTERISC", "QUESTION", "CONJUNCTION_AND", 
		"CONJUNCTION_OR", "MODIFIER_NEGATE", "MODIFIER_REQUIRE", "TERM_STRING", 
		"TERM_NUMBER", "TERM_FIELD", "MODIFIER_REQUIERD", "FIELD", "STRING", "NUMBER"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Query.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public QueryParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class QueryContext extends ParserRuleContext {
		public Token conjunction;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> CONJUNCTION_AND() { return getTokens(QueryParser.CONJUNCTION_AND); }
		public TerminalNode CONJUNCTION_AND(int i) {
			return getToken(QueryParser.CONJUNCTION_AND, i);
		}
		public List<TerminalNode> CONJUNCTION_OR() { return getTokens(QueryParser.CONJUNCTION_OR); }
		public TerminalNode CONJUNCTION_OR(int i) {
			return getToken(QueryParser.CONJUNCTION_OR, i);
		}
		public QueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_query; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QueryVisitor ) return ((QueryVisitor<? extends T>)visitor).visitQuery(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QueryContext query() throws RecognitionException {
		QueryContext _localctx = new QueryContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_query);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(6);
			expression();
			setState(13);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << CONJUNCTION_AND) | (1L << CONJUNCTION_OR) | (1L << MODIFIER_NEGATE) | (1L << MODIFIER_REQUIERD) | (1L << FIELD))) != 0)) {
				{
				{
				setState(8);
				_la = _input.LA(1);
				if (_la==CONJUNCTION_AND || _la==CONJUNCTION_OR) {
					{
					setState(7);
					((QueryContext)_localctx).conjunction = _input.LT(1);
					_la = _input.LA(1);
					if ( !(_la==CONJUNCTION_AND || _la==CONJUNCTION_OR) ) {
						((QueryContext)_localctx).conjunction = (Token)_errHandler.recoverInline(this);
					} else {
						consume();
					}
					}
				}

				setState(10);
				expression();
				}
				}
				setState(15);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExpressionContext extends ParserRuleContext {
		public Token modifier;
		public TerminalNode FIELD() { return getToken(QueryParser.FIELD, 0); }
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public TerminalNode MODIFIER_NEGATE() { return getToken(QueryParser.MODIFIER_NEGATE, 0); }
		public TerminalNode MODIFIER_REQUIERD() { return getToken(QueryParser.MODIFIER_REQUIERD, 0); }
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QueryVisitor ) return ((QueryVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_expression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(17);
			_la = _input.LA(1);
			if (_la==MODIFIER_NEGATE || _la==MODIFIER_REQUIERD) {
				{
				setState(16);
				((ExpressionContext)_localctx).modifier = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==MODIFIER_NEGATE || _la==MODIFIER_REQUIERD) ) {
					((ExpressionContext)_localctx).modifier = (Token)_errHandler.recoverInline(this);
				} else {
					consume();
				}
				}
			}

			setState(19);
			match(FIELD);
			setState(20);
			match(COLON);
			setState(21);
			term();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TermContext extends ParserRuleContext {
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
	 
		public TermContext() { }
		public void copyFrom(TermContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class NumberTermContext extends TermContext {
		public TerminalNode NUMBER() { return getToken(QueryParser.NUMBER, 0); }
		public NumberTermContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QueryVisitor ) return ((QueryVisitor<? extends T>)visitor).visitNumberTerm(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class StringTermContext extends TermContext {
		public TerminalNode STRING() { return getToken(QueryParser.STRING, 0); }
		public StringTermContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QueryVisitor ) return ((QueryVisitor<? extends T>)visitor).visitStringTerm(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SubQueryTermContext extends TermContext {
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public SubQueryTermContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof QueryVisitor ) return ((QueryVisitor<? extends T>)visitor).visitSubQueryTerm(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_term);
		try {
			setState(29);
			switch (_input.LA(1)) {
			case STRING:
				_localctx = new StringTermContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(23);
				match(STRING);
				}
				break;
			case NUMBER:
				_localctx = new NumberTermContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(24);
				match(NUMBER);
				}
				break;
			case LPAREN:
				_localctx = new SubQueryTermContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(25);
				match(LPAREN);
				setState(26);
				query();
				setState(27);
				match(RPAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\22\"\4\2\t\2\4\3"+
		"\t\3\4\4\t\4\3\2\3\2\5\2\13\n\2\3\2\7\2\16\n\2\f\2\16\2\21\13\2\3\3\5"+
		"\3\24\n\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\5\4 \n\4\3\4\2\2\5\2"+
		"\4\6\2\4\3\2\b\t\4\2\n\n\17\17#\2\b\3\2\2\2\4\23\3\2\2\2\6\37\3\2\2\2"+
		"\b\17\5\4\3\2\t\13\t\2\2\2\n\t\3\2\2\2\n\13\3\2\2\2\13\f\3\2\2\2\f\16"+
		"\5\4\3\2\r\n\3\2\2\2\16\21\3\2\2\2\17\r\3\2\2\2\17\20\3\2\2\2\20\3\3\2"+
		"\2\2\21\17\3\2\2\2\22\24\t\3\2\2\23\22\3\2\2\2\23\24\3\2\2\2\24\25\3\2"+
		"\2\2\25\26\7\20\2\2\26\27\7\5\2\2\27\30\5\6\4\2\30\5\3\2\2\2\31 \7\21"+
		"\2\2\32 \7\22\2\2\33\34\7\3\2\2\34\35\5\2\2\2\35\36\7\4\2\2\36 \3\2\2"+
		"\2\37\31\3\2\2\2\37\32\3\2\2\2\37\33\3\2\2\2 \7\3\2\2\2\6\n\17\23\37";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}