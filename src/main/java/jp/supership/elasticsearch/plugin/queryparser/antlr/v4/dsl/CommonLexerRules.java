// Generated from jp/supership/elasticsearch/plugin/queryparser/antlr/v4/dsl/CommonLexerRules.g4 by ANTLR 4.5
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
		"DIGIT", "UNICODE_LITERAL", "OCTAL_LITERAL", "ESCAPED_CHARACTER", "SPECIAL_CHARACTER", 
		"FIELD_INITIAL", "FIELD_CHARACTER", "WHITE_SPACE", "QUATABLE_CHARACTER", 
		"MINUS", "LPAREN", "RPAREN", "COLON", "STRING", "NUMBER", "FIELD"
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\tl\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\3\2\3\2\3"+
		"\3\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\5\4\66"+
		"\n\4\3\5\3\5\3\5\3\6\3\6\3\6\3\6\5\6?\n\6\3\7\3\7\5\7C\n\7\3\b\3\b\3\b"+
		"\5\bH\n\b\3\t\3\t\3\n\3\n\5\nN\n\n\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16"+
		"\3\17\3\17\7\17Z\n\17\f\17\16\17]\13\17\3\17\3\17\3\20\6\20b\n\20\r\20"+
		"\16\20c\3\21\3\21\7\21h\n\21\f\21\16\21k\13\21\2\2\22\3\2\5\2\7\2\t\2"+
		"\13\2\r\2\17\2\21\2\23\2\25\3\27\4\31\5\33\6\35\7\37\b!\t\3\2\13\3\2\62"+
		";\5\2\62;CHch\3\2\62\65\3\2\629\2\2\t\2))^^ddhhppttvv\13\2\13\f\17\17"+
		"\"\"$$)+..<<^^\u3002\u3002\6\2\13\f\17\17\"\"\u3002\u3002\7\2\"\"$$))"+
		"..^^m\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2"+
		"\2\37\3\2\2\2\2!\3\2\2\2\3#\3\2\2\2\5%\3\2\2\2\7\65\3\2\2\2\t\67\3\2\2"+
		"\2\13>\3\2\2\2\rB\3\2\2\2\17G\3\2\2\2\21I\3\2\2\2\23M\3\2\2\2\25O\3\2"+
		"\2\2\27Q\3\2\2\2\31S\3\2\2\2\33U\3\2\2\2\35W\3\2\2\2\37a\3\2\2\2!e\3\2"+
		"\2\2#$\t\2\2\2$\4\3\2\2\2%&\7^\2\2&\'\7w\2\2\'(\t\3\2\2()\t\3\2\2)*\t"+
		"\3\2\2*+\t\3\2\2+\6\3\2\2\2,-\7^\2\2-.\t\4\2\2./\t\5\2\2/\66\t\5\2\2\60"+
		"\61\7^\2\2\61\62\t\5\2\2\62\66\t\5\2\2\63\64\7^\2\2\64\66\t\5\2\2\65,"+
		"\3\2\2\2\65\60\3\2\2\2\65\63\3\2\2\2\66\b\3\2\2\2\678\7^\2\289\n\6\2\2"+
		"9\n\3\2\2\2:;\7^\2\2;?\t\7\2\2<?\5\5\3\2=?\5\7\4\2>:\3\2\2\2><\3\2\2\2"+
		">=\3\2\2\2?\f\3\2\2\2@C\n\b\2\2AC\5\t\5\2B@\3\2\2\2BA\3\2\2\2C\16\3\2"+
		"\2\2DH\5\r\7\2EH\5\t\5\2FH\7/\2\2GD\3\2\2\2GE\3\2\2\2GF\3\2\2\2H\20\3"+
		"\2\2\2IJ\t\t\2\2J\22\3\2\2\2KN\n\n\2\2LN\5\t\5\2MK\3\2\2\2ML\3\2\2\2N"+
		"\24\3\2\2\2OP\7/\2\2P\26\3\2\2\2QR\7*\2\2R\30\3\2\2\2ST\7+\2\2T\32\3\2"+
		"\2\2UV\7<\2\2V\34\3\2\2\2W[\7$\2\2XZ\5\23\n\2YX\3\2\2\2Z]\3\2\2\2[Y\3"+
		"\2\2\2[\\\3\2\2\2\\^\3\2\2\2][\3\2\2\2^_\7$\2\2_\36\3\2\2\2`b\5\3\2\2"+
		"a`\3\2\2\2bc\3\2\2\2ca\3\2\2\2cd\3\2\2\2d \3\2\2\2ei\5\r\7\2fh\5\17\b"+
		"\2gf\3\2\2\2hk\3\2\2\2ig\3\2\2\2ij\3\2\2\2j\"\3\2\2\2ki\3\2\2\2\13\2\65"+
		">BGM[ci\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}