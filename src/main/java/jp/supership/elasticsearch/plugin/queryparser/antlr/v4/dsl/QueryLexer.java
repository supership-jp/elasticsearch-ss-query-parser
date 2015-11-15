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
		T__0=1, T__1=2, T__2=3, T__3=4, MINUS=5, LPAREN=6, RPAREN=7, COLON=8, 
		STRING=9, NUMBER=10, FIELD=11;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "DIGIT", "UNICODE_LITERAL", "OCTAL_LITERAL", 
		"ESCAPED_CHARACTER", "SPECIAL_CHARACTER", "FIELD_INITIAL", "FIELD_CHARACTER", 
		"WHITE_SPACE", "QUATABLE_CHARACTER", "MINUS", "LPAREN", "RPAREN", "COLON", 
		"STRING", "NUMBER", "FIELD"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'AND'", "'OR'", "'|'", "'_'", "'-'", "'('", "')'", "':'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, "MINUS", "LPAREN", "RPAREN", "COLON", "STRING", 
		"NUMBER", "FIELD"
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\r\177\b\1\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\4\3\4\3\5"+
		"\3\5\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3"+
		"\b\3\b\5\bI\n\b\3\t\3\t\3\t\3\n\3\n\3\n\3\n\5\nR\n\n\3\13\3\13\5\13V\n"+
		"\13\3\f\3\f\3\f\5\f[\n\f\3\r\3\r\3\16\3\16\5\16a\n\16\3\17\3\17\3\20\3"+
		"\20\3\21\3\21\3\22\3\22\3\23\3\23\7\23m\n\23\f\23\16\23p\13\23\3\23\3"+
		"\23\3\24\6\24u\n\24\r\24\16\24v\3\25\3\25\7\25{\n\25\f\25\16\25~\13\25"+
		"\2\2\26\3\3\5\4\7\5\t\6\13\2\r\2\17\2\21\2\23\2\25\2\27\2\31\2\33\2\35"+
		"\7\37\b!\t#\n%\13\'\f)\r\3\2\13\3\2\62;\5\2\62;CHch\3\2\62\65\3\2\629"+
		"\2\2\t\2))^^ddhhppttvv\13\2\13\f\17\17\"\"$$)+..<<^^\u3002\u3002\6\2\13"+
		"\f\17\17\"\"\u3002\u3002\7\2\"\"$$))..^^\u0080\2\3\3\2\2\2\2\5\3\2\2\2"+
		"\2\7\3\2\2\2\2\t\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2"+
		"\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\3+\3\2\2\2\5/\3\2\2\2\7\62\3\2\2"+
		"\2\t\64\3\2\2\2\13\66\3\2\2\2\r8\3\2\2\2\17H\3\2\2\2\21J\3\2\2\2\23Q\3"+
		"\2\2\2\25U\3\2\2\2\27Z\3\2\2\2\31\\\3\2\2\2\33`\3\2\2\2\35b\3\2\2\2\37"+
		"d\3\2\2\2!f\3\2\2\2#h\3\2\2\2%j\3\2\2\2\'t\3\2\2\2)x\3\2\2\2+,\7C\2\2"+
		",-\7P\2\2-.\7F\2\2.\4\3\2\2\2/\60\7Q\2\2\60\61\7T\2\2\61\6\3\2\2\2\62"+
		"\63\7~\2\2\63\b\3\2\2\2\64\65\7a\2\2\65\n\3\2\2\2\66\67\t\2\2\2\67\f\3"+
		"\2\2\289\7^\2\29:\7w\2\2:;\t\3\2\2;<\t\3\2\2<=\t\3\2\2=>\t\3\2\2>\16\3"+
		"\2\2\2?@\7^\2\2@A\t\4\2\2AB\t\5\2\2BI\t\5\2\2CD\7^\2\2DE\t\5\2\2EI\t\5"+
		"\2\2FG\7^\2\2GI\t\5\2\2H?\3\2\2\2HC\3\2\2\2HF\3\2\2\2I\20\3\2\2\2JK\7"+
		"^\2\2KL\n\6\2\2L\22\3\2\2\2MN\7^\2\2NR\t\7\2\2OR\5\r\7\2PR\5\17\b\2QM"+
		"\3\2\2\2QO\3\2\2\2QP\3\2\2\2R\24\3\2\2\2SV\n\b\2\2TV\5\21\t\2US\3\2\2"+
		"\2UT\3\2\2\2V\26\3\2\2\2W[\5\25\13\2X[\5\21\t\2Y[\7/\2\2ZW\3\2\2\2ZX\3"+
		"\2\2\2ZY\3\2\2\2[\30\3\2\2\2\\]\t\t\2\2]\32\3\2\2\2^a\n\n\2\2_a\5\21\t"+
		"\2`^\3\2\2\2`_\3\2\2\2a\34\3\2\2\2bc\7/\2\2c\36\3\2\2\2de\7*\2\2e \3\2"+
		"\2\2fg\7+\2\2g\"\3\2\2\2hi\7<\2\2i$\3\2\2\2jn\7$\2\2km\5\33\16\2lk\3\2"+
		"\2\2mp\3\2\2\2nl\3\2\2\2no\3\2\2\2oq\3\2\2\2pn\3\2\2\2qr\7$\2\2r&\3\2"+
		"\2\2su\5\13\6\2ts\3\2\2\2uv\3\2\2\2vt\3\2\2\2vw\3\2\2\2w(\3\2\2\2x|\5"+
		"\25\13\2y{\5\27\f\2zy\3\2\2\2{~\3\2\2\2|z\3\2\2\2|}\3\2\2\2}*\3\2\2\2"+
		"~|\3\2\2\2\13\2HQUZ`nv|\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}