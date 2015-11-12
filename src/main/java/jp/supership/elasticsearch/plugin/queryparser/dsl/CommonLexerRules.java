// Generated from jp/supership/elasticsearch/plugin/queryparser/dsl/CommonLexerRules.g4 by ANTLR 4.5
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
public class CommonLexerRules extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		MINUS=1, LPAREN=2, RPAREN=3, COLON=4, STRING=5, NUMBER=6, FIELD=7;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"DICIMAL", "QUATERNARY", "OCTAL", "HEXADECIMAL", "UNICODE_LITERAL", "OCTAL_LITERAL", 
		"ESCAPED_CHARACTER", "SPECIAL_CHARACTER", "FIELD_INITIAL", "FIELD_CHARACTER", 
		"WHITE_SPACE", "QUATABLE_CHARACTER", "MINUS", "LPAREN", "RPAREN", "COLON", 
		"STRING", "NUMBER", "FIELD"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'-'", "'('", "')'", "':'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "MINUS", "LPAREN", "RPAREN", "COLON", "STRING", "NUMBER", "FIELD"
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


	public CommonLexerRules(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "CommonLexerRules.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\tz\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\6\3\6\3"+
		"\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\5\7D\n\7\3\b\3"+
		"\b\3\b\3\t\3\t\3\t\3\t\5\tM\n\t\3\n\3\n\5\nQ\n\n\3\13\3\13\3\13\5\13V"+
		"\n\13\3\f\3\f\3\r\3\r\5\r\\\n\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21"+
		"\3\22\3\22\7\22h\n\22\f\22\16\22k\13\22\3\22\3\22\3\23\6\23p\n\23\r\23"+
		"\16\23q\3\24\3\24\7\24v\n\24\f\24\16\24y\13\24\2\2\25\3\2\5\2\7\2\t\2"+
		"\13\2\r\2\17\2\21\2\23\2\25\2\27\2\31\2\33\3\35\4\37\5!\6#\7%\b\'\t\3"+
		"\2\13\3\2\62;\3\2\62\65\3\2\629\5\2\62;CHch\2\2\t\2))^^ddhhppttvv\13\2"+
		"\13\f\17\17\"\"$$)+..<<^^\u3002\u3002\6\2\13\f\17\17\"\"\u3002\u3002\7"+
		"\2\"\"$$))..^^x\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3"+
		"\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\3)\3\2\2\2\5+\3\2\2\2\7-\3\2\2\2\t/\3\2"+
		"\2\2\13\61\3\2\2\2\rC\3\2\2\2\17E\3\2\2\2\21L\3\2\2\2\23P\3\2\2\2\25U"+
		"\3\2\2\2\27W\3\2\2\2\31[\3\2\2\2\33]\3\2\2\2\35_\3\2\2\2\37a\3\2\2\2!"+
		"c\3\2\2\2#e\3\2\2\2%o\3\2\2\2\'s\3\2\2\2)*\t\2\2\2*\4\3\2\2\2+,\t\3\2"+
		"\2,\6\3\2\2\2-.\t\4\2\2.\b\3\2\2\2/\60\t\5\2\2\60\n\3\2\2\2\61\62\7^\2"+
		"\2\62\63\7w\2\2\63\64\5\t\5\2\64\65\5\t\5\2\65\66\5\t\5\2\66\67\5\t\5"+
		"\2\67\f\3\2\2\289\7^\2\29:\5\5\3\2:;\5\7\4\2;<\5\7\4\2<D\3\2\2\2=>\7^"+
		"\2\2>?\5\7\4\2?@\5\7\4\2@D\3\2\2\2AB\7^\2\2BD\5\7\4\2C8\3\2\2\2C=\3\2"+
		"\2\2CA\3\2\2\2D\16\3\2\2\2EF\7^\2\2FG\n\6\2\2G\20\3\2\2\2HI\7^\2\2IM\t"+
		"\7\2\2JM\5\13\6\2KM\5\r\7\2LH\3\2\2\2LJ\3\2\2\2LK\3\2\2\2M\22\3\2\2\2"+
		"NQ\n\b\2\2OQ\5\17\b\2PN\3\2\2\2PO\3\2\2\2Q\24\3\2\2\2RV\5\23\n\2SV\5\17"+
		"\b\2TV\7/\2\2UR\3\2\2\2US\3\2\2\2UT\3\2\2\2V\26\3\2\2\2WX\t\t\2\2X\30"+
		"\3\2\2\2Y\\\n\n\2\2Z\\\5\17\b\2[Y\3\2\2\2[Z\3\2\2\2\\\32\3\2\2\2]^\7/"+
		"\2\2^\34\3\2\2\2_`\7*\2\2`\36\3\2\2\2ab\7+\2\2b \3\2\2\2cd\7<\2\2d\"\3"+
		"\2\2\2ei\7$\2\2fh\5\31\r\2gf\3\2\2\2hk\3\2\2\2ig\3\2\2\2ij\3\2\2\2jl\3"+
		"\2\2\2ki\3\2\2\2lm\7$\2\2m$\3\2\2\2np\5\3\2\2on\3\2\2\2pq\3\2\2\2qo\3"+
		"\2\2\2qr\3\2\2\2r&\3\2\2\2sw\5\23\n\2tv\5\25\13\2ut\3\2\2\2vy\3\2\2\2"+
		"wu\3\2\2\2wx\3\2\2\2x(\3\2\2\2yw\3\2\2\2\13\2CLPU[iqw\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}