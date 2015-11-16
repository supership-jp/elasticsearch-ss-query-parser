// Generated from jp/supership/elasticsearch/plugin/queryparser/antlr/v4/dsl/Query.g4 by ANTLR 4.5
package jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class QueryLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		LPAREN=1, RPAREN=2, COLON=3, ASTERISC=4, QUESTION=5, CONJUNCTION_AND=6, 
		CONJUNCTION_OR=7, MODIFIER_NEGATE=8, MODIFIER_REQUIRE=9, TERM_STRING=10, 
		TERM_NUMBER=11, TERM_FIELD=12;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"DIGIT", "UNICODE_LITERAL", "OCTAL_LITERAL", "ESCAPED_CHARACTER", "SPECIAL_CHARACTER", 
		"FIELD_INITIAL", "FIELD_CHARACTER", "WHITE_SPACE", "QUATABLE_CHARACTER", 
		"LPAREN", "RPAREN", "COLON", "ASTERISC", "QUESTION", "CONJUNCTION_AND", 
		"CONJUNCTION_OR", "MODIFIER_NEGATE", "MODIFIER_REQUIRE", "TERM_STRING", 
		"TERM_NUMBER", "TERM_FIELD"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'('", "')'", "':'", "'*'", "'?'", null, null, "'-'", "'_'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "LPAREN", "RPAREN", "COLON", "ASTERISC", "QUESTION", "CONJUNCTION_AND", 
		"CONJUNCTION_OR", "MODIFIER_NEGATE", "MODIFIER_REQUIRE", "TERM_STRING", 
		"TERM_NUMBER", "TERM_FIELD"
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


	public QueryLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Query.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\16\u0083\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\3\2\3\2\3\3\3\3\3\3\3\3"+
		"\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\5\4@\n\4\3\5\3\5\3\5"+
		"\3\6\3\6\3\6\3\6\5\6I\n\6\3\7\3\7\5\7M\n\7\3\b\3\b\3\b\5\bR\n\b\3\t\3"+
		"\t\3\n\3\n\5\nX\n\n\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20"+
		"\3\20\3\20\3\20\3\21\3\21\3\21\3\22\3\22\3\23\3\23\3\24\3\24\7\24q\n\24"+
		"\f\24\16\24t\13\24\3\24\3\24\3\25\6\25y\n\25\r\25\16\25z\3\26\3\26\7\26"+
		"\177\n\26\f\26\16\26\u0082\13\26\2\2\27\3\2\5\2\7\2\t\2\13\2\r\2\17\2"+
		"\21\2\23\2\25\3\27\4\31\5\33\6\35\7\37\b!\t#\n%\13\'\f)\r+\16\3\2\20\3"+
		"\2\62;\5\2\62;CHch\3\2\62\65\3\2\629\2\2\t\2))^^ddhhppttvv\13\2\13\f\17"+
		"\17\"\"$$)+..<<^^\u3002\u3002\6\2\13\f\17\17\"\"\u3002\u3002\7\2\"\"$"+
		"$))..^^\4\2CCcc\4\2PPpp\4\2FFff\4\2QQqq\4\2TTtt\u0084\2\25\3\2\2\2\2\27"+
		"\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2"+
		"\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\3-\3\2\2\2"+
		"\5/\3\2\2\2\7?\3\2\2\2\tA\3\2\2\2\13H\3\2\2\2\rL\3\2\2\2\17Q\3\2\2\2\21"+
		"S\3\2\2\2\23W\3\2\2\2\25Y\3\2\2\2\27[\3\2\2\2\31]\3\2\2\2\33_\3\2\2\2"+
		"\35a\3\2\2\2\37c\3\2\2\2!g\3\2\2\2#j\3\2\2\2%l\3\2\2\2\'n\3\2\2\2)x\3"+
		"\2\2\2+|\3\2\2\2-.\t\2\2\2.\4\3\2\2\2/\60\7^\2\2\60\61\7w\2\2\61\62\t"+
		"\3\2\2\62\63\t\3\2\2\63\64\t\3\2\2\64\65\t\3\2\2\65\6\3\2\2\2\66\67\7"+
		"^\2\2\678\t\4\2\289\t\5\2\29@\t\5\2\2:;\7^\2\2;<\t\5\2\2<@\t\5\2\2=>\7"+
		"^\2\2>@\t\5\2\2?\66\3\2\2\2?:\3\2\2\2?=\3\2\2\2@\b\3\2\2\2AB\7^\2\2BC"+
		"\n\6\2\2C\n\3\2\2\2DE\7^\2\2EI\t\7\2\2FI\5\5\3\2GI\5\7\4\2HD\3\2\2\2H"+
		"F\3\2\2\2HG\3\2\2\2I\f\3\2\2\2JM\n\b\2\2KM\5\t\5\2LJ\3\2\2\2LK\3\2\2\2"+
		"M\16\3\2\2\2NR\5\r\7\2OR\5\t\5\2PR\7/\2\2QN\3\2\2\2QO\3\2\2\2QP\3\2\2"+
		"\2R\20\3\2\2\2ST\t\t\2\2T\22\3\2\2\2UX\n\n\2\2VX\5\t\5\2WU\3\2\2\2WV\3"+
		"\2\2\2X\24\3\2\2\2YZ\7*\2\2Z\26\3\2\2\2[\\\7+\2\2\\\30\3\2\2\2]^\7<\2"+
		"\2^\32\3\2\2\2_`\7,\2\2`\34\3\2\2\2ab\7A\2\2b\36\3\2\2\2cd\t\13\2\2de"+
		"\t\f\2\2ef\t\r\2\2f \3\2\2\2gh\t\16\2\2hi\t\17\2\2i\"\3\2\2\2jk\7/\2\2"+
		"k$\3\2\2\2lm\7a\2\2m&\3\2\2\2nr\7$\2\2oq\5\23\n\2po\3\2\2\2qt\3\2\2\2"+
		"rp\3\2\2\2rs\3\2\2\2su\3\2\2\2tr\3\2\2\2uv\7$\2\2v(\3\2\2\2wy\5\3\2\2"+
		"xw\3\2\2\2yz\3\2\2\2zx\3\2\2\2z{\3\2\2\2{*\3\2\2\2|\u0080\5\r\7\2}\177"+
		"\5\17\b\2~}\3\2\2\2\177\u0082\3\2\2\2\u0080~\3\2\2\2\u0080\u0081\3\2\2"+
		"\2\u0081,\3\2\2\2\u0082\u0080\3\2\2\2\13\2?HLQWrz\u0080\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}