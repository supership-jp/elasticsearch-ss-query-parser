// Generated from jp/supership/elasticsearch/plugin/queryparser/dsl/Query.g4 by ANTLR 4.5
package jp.supership.elasticsearch.plugin.queryparser.dsl;
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
		"T__0", "T__1", "T__2", "T__3", "DICIMAL", "QUATERNARY", "OCTAL", "HEXADECIMAL", 
		"UNICODE_LITERAL", "OCTAL_LITERAL", "ESCAPED_CHARACTER", "SPECIAL_CHARACTER", 
		"FIELD_INITIAL", "FIELD_CHARACTER", "WHITE_SPACE", "QUATABLE_CHARACTER", 
		"MINUS", "LPAREN", "RPAREN", "COLON", "STRING", "NUMBER", "FIELD"
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\r\u008d\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\3\2\3\2\3"+
		"\2\3\2\3\3\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n"+
		"\3\n\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13"+
		"\3\13\3\13\5\13W\n\13\3\f\3\f\3\f\3\r\3\r\3\r\3\r\5\r`\n\r\3\16\3\16\5"+
		"\16d\n\16\3\17\3\17\3\17\5\17i\n\17\3\20\3\20\3\21\3\21\5\21o\n\21\3\22"+
		"\3\22\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26\7\26{\n\26\f\26\16\26~\13"+
		"\26\3\26\3\26\3\27\6\27\u0083\n\27\r\27\16\27\u0084\3\30\3\30\7\30\u0089"+
		"\n\30\f\30\16\30\u008c\13\30\2\2\31\3\3\5\4\7\5\t\6\13\2\r\2\17\2\21\2"+
		"\23\2\25\2\27\2\31\2\33\2\35\2\37\2!\2#\7%\b\'\t)\n+\13-\f/\r\3\2\13\3"+
		"\2\62;\3\2\62\65\3\2\629\5\2\62;CHch\2\2\t\2))^^ddhhppttvv\13\2\13\f\17"+
		"\17\"\"$$)+..<<^^\u3002\u3002\6\2\13\f\17\17\"\"\u3002\u3002\7\2\"\"$"+
		"$))..^^\u008b\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2#\3\2\2"+
		"\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2"+
		"\3\61\3\2\2\2\5\65\3\2\2\2\78\3\2\2\2\t:\3\2\2\2\13<\3\2\2\2\r>\3\2\2"+
		"\2\17@\3\2\2\2\21B\3\2\2\2\23D\3\2\2\2\25V\3\2\2\2\27X\3\2\2\2\31_\3\2"+
		"\2\2\33c\3\2\2\2\35h\3\2\2\2\37j\3\2\2\2!n\3\2\2\2#p\3\2\2\2%r\3\2\2\2"+
		"\'t\3\2\2\2)v\3\2\2\2+x\3\2\2\2-\u0082\3\2\2\2/\u0086\3\2\2\2\61\62\7"+
		"C\2\2\62\63\7P\2\2\63\64\7F\2\2\64\4\3\2\2\2\65\66\7Q\2\2\66\67\7T\2\2"+
		"\67\6\3\2\2\289\7~\2\29\b\3\2\2\2:;\7a\2\2;\n\3\2\2\2<=\t\2\2\2=\f\3\2"+
		"\2\2>?\t\3\2\2?\16\3\2\2\2@A\t\4\2\2A\20\3\2\2\2BC\t\5\2\2C\22\3\2\2\2"+
		"DE\7^\2\2EF\7w\2\2FG\5\21\t\2GH\5\21\t\2HI\5\21\t\2IJ\5\21\t\2J\24\3\2"+
		"\2\2KL\7^\2\2LM\5\r\7\2MN\5\17\b\2NO\5\17\b\2OW\3\2\2\2PQ\7^\2\2QR\5\17"+
		"\b\2RS\5\17\b\2SW\3\2\2\2TU\7^\2\2UW\5\17\b\2VK\3\2\2\2VP\3\2\2\2VT\3"+
		"\2\2\2W\26\3\2\2\2XY\7^\2\2YZ\n\6\2\2Z\30\3\2\2\2[\\\7^\2\2\\`\t\7\2\2"+
		"]`\5\23\n\2^`\5\25\13\2_[\3\2\2\2_]\3\2\2\2_^\3\2\2\2`\32\3\2\2\2ad\n"+
		"\b\2\2bd\5\27\f\2ca\3\2\2\2cb\3\2\2\2d\34\3\2\2\2ei\5\33\16\2fi\5\27\f"+
		"\2gi\7/\2\2he\3\2\2\2hf\3\2\2\2hg\3\2\2\2i\36\3\2\2\2jk\t\t\2\2k \3\2"+
		"\2\2lo\n\n\2\2mo\5\27\f\2nl\3\2\2\2nm\3\2\2\2o\"\3\2\2\2pq\7/\2\2q$\3"+
		"\2\2\2rs\7*\2\2s&\3\2\2\2tu\7+\2\2u(\3\2\2\2vw\7<\2\2w*\3\2\2\2x|\7$\2"+
		"\2y{\5!\21\2zy\3\2\2\2{~\3\2\2\2|z\3\2\2\2|}\3\2\2\2}\177\3\2\2\2~|\3"+
		"\2\2\2\177\u0080\7$\2\2\u0080,\3\2\2\2\u0081\u0083\5\13\6\2\u0082\u0081"+
		"\3\2\2\2\u0083\u0084\3\2\2\2\u0084\u0082\3\2\2\2\u0084\u0085\3\2\2\2\u0085"+
		".\3\2\2\2\u0086\u008a\5\33\16\2\u0087\u0089\5\35\17\2\u0088\u0087\3\2"+
		"\2\2\u0089\u008c\3\2\2\2\u008a\u0088\3\2\2\2\u008a\u008b\3\2\2\2\u008b"+
		"\60\3\2\2\2\u008c\u008a\3\2\2\2\13\2V_chn|\u0084\u008a\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}