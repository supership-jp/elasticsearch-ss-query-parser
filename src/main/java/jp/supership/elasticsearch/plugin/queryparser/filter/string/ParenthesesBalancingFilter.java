/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.filter.string;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import jp.supership.elasticsearch.plugin.queryparser.common.util.ConfigUtils;
import jp.supership.elasticsearch.plugin.queryparser.common.util.StringUtils;
import jp.supership.elasticsearch.plugin.queryparser.filter.ChainableFilter;
import jp.supership.elasticsearch.plugin.queryparser.filter.FilterContext;
import jp.supership.elasticsearch.plugin.queryparser.filter.FilterException;
import jp.supership.elasticsearch.plugin.queryparser.filters.Initializable;

/**
 * This {@code ChainableFilter} implementation is responsible for removing excess spaces form
 * the given {@code String}.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class ParenthesesBalancingFilter extends ChainableFilter<String> implements Initializable {
    /** Holds JSON entry key for left parenheses entity. */
    public static final String JSON_LEFT_PARENTHESIS = "left_parenthesis";

    /** Holds JSON entry key for right parenheses entity. */
    public static final String JSON_RIGHT_PARENTHESIS = "right_parenthesis";

    /** Holds left parentheses. */
    private String leftParenthesis;

    /** Holds right parentheses. */
    private String rightParenthesis;

    // Represents parenthesis.
    private class CharacterWithOffset {
	// Holds appering offset.
	private int offset;
	// Holds parenthesis expression.
	private char character;

	// Constructor.
	public CharacterWithOffset(int offset, char character) {
	    this.offset = offset;
	    this.character = character;
	}

	// Sets the apperence offset.
	public void setOffset(int offset) {
	    this.offset = offset;
	}

	// Sets the apperence offset.
	public int getOffset() {
	    return this.offset;
	}

	// Sets the apperence expression.
	public void setCharacter(char character) {
	    this.character = character;
	}

	// Sets the apperence expression.
	public char getCharacter() {
	    return this.character;
	}
    }

    // Represents internal buffer.
    private class Buffer implements Iterable<CharacterWithOffset>, Iterator<CharacterWithOffset> {
	// Holds consuming string.
	private String source;
	// Holds internal buffer.
	private StringBuilder internal;
        // Holds current position.
	private int position = 0;
	// Holds marked position.
	private int mark = 0;
	// Holds previously consumed left parenthesis position.
	private int left = -1;
	// Holds previously consumed right parenthesis position.
	private int right = -1;

	// Constructor.
	public Buffer(String source) {
	    this.source = source;
	    this.internal = new StringBuilder();
	}

	@Override
	public Iterator<CharacterWithOffset> iterator() {
	    return this;
	}

	@Override
	public boolean hasNext() {
	    return this.position < this.source.length();
	}

	@Override
	public CharacterWithOffset next() {
	    return new CharacterWithOffset(this.position, this.source.charAt(this.position++));
	}

	@Override
	public void remove() {
	    throw new UnsupportedOperationException();
	}

	// Sets the current mark.
	public void resetBilateral() {
	    this.left = this.right = -1;
	}

	// Sets the current mark.
	public void setMark() {
	    if (this.position >= 0 && this.position < this.source.length()) {
		this.mark = this.position;
	    }
	}

	// Sets the current mark.
	public void setMark(int mark) {
	    if (mark >= 0 && mark < this.source.length()) {
		this.mark = mark;
	    }
	}

	// Returns the currently marked position.
	public int getMark() {
	    return this.mark;
	}

	// Sets the previously consumed left parenthesis position.
	public void setLeft(int left) {
	    if (left >= 0 && left < this.source.length()) {
		this.left = left;
	    }
	}

	// Returnss the previously consumed left parenthesis position.
	public int getLeft() {
	    return this.left;
	}

	// Sets the previously consumed right parenthesis position.
	public void setRight(int right) {
	    if (right >= 0 && right < this.source.length()) {
		this.right = right;
	    }
	}

	// Returnss the previously consumed right parenthesis position.
	public int getRight() {
	    return this.right;
	}

	// Consumes currently buffered data.
	public String consume() {
	    String result = this.internal.toString();
	    this.internal = new StringBuilder();
	    this.resetBilateral();
	    return result;
	}

	// Buffers currently unconsumed string into the internal buffer.
	public void buffer(int left, int right) {
	    if (this.getLeft() < 0 || this.getRight() < 0) {
		this.appendBuffer(left, right);
		this.setLeft(left);
		this.setRight(right);
	    } else {
		this.prependBuffer(left, this.getLeft());
		this.appendBuffer(this.getRight(), right);
		this.setLeft(left);
		this.setRight(right);
	    }
	}

	// Prepends given position's character into the internal buffer.
	public void prependBuffer(int position) {
	    this.internal.insert(0, this.source.charAt(position));
	}

	// Appends given position's character into the internal buffer.
	public void appendBuffer(int position) {
	    this.internal.append(this.source.charAt(position));
	}

	// Prepends given range's string into the internal buffer.
	public void prependBuffer(int left, int right) {
	    this.internal.insert(0, this.source.substring(left, right));
	}

	// Appends given range's string into the internal buffer.
	public void appendBuffer(int left, int right) {
	    this.internal.append(this.source.substring(left, right));
	}
    }

    /**
     * Constructor.
     */
    public ParenthesesBalancingFilter() {
	// DO NOTHING.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(Map<String, Object> settings) throws IllegalArgumentException {
	this.leftParenthesis = ConfigUtils.getStringValue(settings, JSON_LEFT_PARENTHESIS);
	if (StringUtils.isEmpty(this.leftParenthesis)) {
	    throw new IllegalArgumentException("'left_parenthesis' element not defined for filter " + ParenthesesBalancingFilter.class.getName());
	}
	this.rightParenthesis = ConfigUtils.getStringValue(settings, JSON_RIGHT_PARENTHESIS);
	if (StringUtils.isEmpty(this.rightParenthesis)) {
	    throw new IllegalArgumentException("'right_parenthesis' element not defined for filter " + ParenthesesBalancingFilter.class.getName());
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String doFilter(String target, FilterContext<String, Object> context) throws FilterException {
	Buffer buffer = new Buffer(target);
	StringBuilder result = new StringBuilder();
	Stack<CharacterWithOffset> stack = new Stack<CharacterWithOffset>();

	for (CharacterWithOffset current : buffer) {
	    if (this.checkIfLeftParenthesis(current)) {
		stack.push(current);
	    } else if (this.checkIfRightParenthesis(current)) {
		CharacterWithOffset candidate = stack.peek();
		if (candidate == null) {
		    buffer.buffer(buffer.getMark(), current.getOffset() - 1);
		    result.append(buffer.consume());
		    buffer.setMark(current.getOffset());
		} else if (!this.checkIfBalanced(candidate, current)) {
		    candidate = stack.pop();
		    buffer.buffer(candidate.getOffset() + 1, current.getOffset() - 1);
		} else {
		    candidate = stack.pop();
		    buffer.buffer(candidate.getOffset(), current.getOffset());
		}
	    }
	}

	result.append(buffer.consume());
	return result.toString();
    }

    /**
     * Returns true if the given pair is considered to be parenthesis pair.
     * @param  left the left parenthesis candidatae.
     * @param  right the right parenthesis candidatae.
     * @return true if the given pair is considerted to be parenthesis pair.
     */
    private boolean checkIfBalanced(CharacterWithOffset left, CharacterWithOffset right) {
	return this.leftParenthesis.indexOf(left.getCharacter()) == this.rightParenthesis.indexOf(right.getCharacter());
    }

    /**
     * Returns true if the given character is left-parenthesis.
     * @param  candidate the charadcter to be checked.
     * @return true if the candidate is left parenthesis.
     */
    private boolean checkIfLeftParenthesis(CharacterWithOffset candidate) {
	return this.leftParenthesis.indexOf(candidate.getCharacter()) > -1;
    }

    /**
     * Returns true if the given character is right-parenthesis.
     * @param  candidate the charadcter to be checked.
     * @return true if the candidate is right parenthesis.
     */
    private boolean checkIfRightParenthesis(CharacterWithOffset candidate) {
	return this.rightParenthesis.indexOf(candidate.getCharacter()) > -1;
    }
}
