/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.util;

import java.util.IllegalFormatCodePointException;

/**
 * A collection of operations that relates to {@code String} instances.
 *
 * @author Shingo OKAWA
 * @since  09/11/2015
 */
public final class StringUtils {
    /** Holds unicode START OF HEADING code point. */
    public static String UNICODE_START_OF_HEADING = "\u0001";

    /**
     * Returns the numeric value of the hexadecimal character.
     * @param  input the handling character.
     * @return the converted integer.
     */
    public static int hexToInt(char input) throws IllegalFormatCodePointException {
	if ('0' <= input && input <= '9') {
	    return input - '0';
	} else if ('a' <= input && input <= 'f'){
	    return input - 'a' + 10;
	} else if ('A' <= input && input <= 'F') {
	    return input - 'A' + 10;
	} else {
	    throw new IllegalFormatCodePointException(input);
	}
    }

    /**
     * Returns a {@code String} where those characters must be escaped will be escaped.
     * @param  input the handling {@code String}.
     * @return the converted {@code String}.
     */
    public static String escape(String input) {
	StringBuilder builder = new StringBuilder();
	for (int i = 0; i < input.length(); i++) {
	    char c = input.charAt(i);
	    if (c == '\\' || c == '+' || c == '-' || c == '!' || c == '(' || c == ')' || c == ':'
		|| c == '^' || c == '[' || c == ']' || c == '\"' || c == '{' || c == '}' || c == '~'
		|| c == '*' || c == '?' || c == '|' || c == '&' || c == '/') {
		builder.append('\\');
	    }
	    builder.append(c);
	}
	return builder.toString();
    }

    /**
     * Returns a String where the escape char has been removed, or kept only once if there was a double escape.
     * @param  input the input String to be operated.
     * @return the translated {@code String}.
     */
    public static String discardEscapeChar(String input) throws IllegalArgumentException {
	char[] buffer = new char[input.length()];
	int length = 0;
	boolean wasEscaped = false;
	int multiplier = 0;
	int codePoint = 0;

	for (int i = 0; i < input.length(); i++) {
	    char current = input.charAt(i);
	    if (multiplier > 0) {
		codePoint += StringUtils.hexToInt(current) * multiplier;
		multiplier >>>= 4;
		if (multiplier == 0) {
		    buffer[length++] = (char)codePoint;
		    codePoint = 0;
		}
	    } else if (wasEscaped) {
		if (current == 'u') {
		    multiplier = 16 * 16 * 16;
		} else {
		    buffer[length] = current;
		    length++;
		}
		wasEscaped = false;
	    } else {
		if (current == '\\') {
		    wasEscaped = true;
		} else {
		    buffer[length] = current;
		    length++;
		}
	    }
	}

	if (multiplier > 0) {
	    throw new IllegalArgumentException("truncated unicode escape sequence.");
	} else if (wasEscaped) {
	    throw new IllegalArgumentException("term can not end with escape character.");
	}

	return new String(buffer, 0, length);
    }
}
