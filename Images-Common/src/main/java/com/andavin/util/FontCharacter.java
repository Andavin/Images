package com.andavin.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on February 06, 2018
 *
 * @author Andavin
 */
public enum FontCharacter {

    A('A', 6),
    a('a', 6),
    B('B', 6),
    b('b', 6),
    C('C', 6),
    c('c', 6),
    D('D', 6),
    d('d', 6),
    E('E', 6),
    e('e', 6),
    F('F', 6),
    f('f', 5),
    G('G', 6),
    g('g', 5),
    H('H', 6),
    h('h', 5),
    I('I', 4),
    i('i', 2),
    J('J', 6),
    j('j', 6),
    K('K', 6),
    k('k', 5),
    L('L', 6),
    l('l', 3),
    M('M', 6),
    m('m', 6),
    N('N', 6),
    n('n', 6),
    O('O', 6),
    o('o', 6),
    P('P', 6),
    p('p', 6),
    Q('Q', 6),
    q('q', 6),
    R('R', 6),
    r('r', 6),
    S('S', 6),
    s('s', 6),
    T('T', 6),
    t('t', 4),
    U('U', 6),
    u('u', 6),
    V('V', 6),
    v('v', 6),
    W('W', 6),
    w('w', 6),
    X('X', 6),
    x('x', 6),
    Y('Y', 6),
    y('y', 6),
    Z('Z', 6),
    z('z', 6),
    NUM_1('1', 6),
    NUM_2('2', 6),
    NUM_3('3', 6),
    NUM_4('4', 6),
    NUM_5('5', 6),
    NUM_6('6', 6),
    NUM_7('7', 6),
    NUM_8('8', 6),
    NUM_9('9', 6),
    NUM_0('0', 6),
    NUM_SIGN('#', 6),
    SPACE(' ', 5),
    EXCLAMATION_POINT('!', 2),
    UPSIDE_DOWN_EXCLAMATION_POINT('¡', 2),
    DOUBLE_QUOTE('"', 5),
    SINGLE_QUOTE('\'', 3),
    DOLLAR_SIGN('$', 6),
    POUND_SIGN('£', 6),
    PERCENT('%', 6),
    AMPERSAND('&', 6),
    ASTERISK('*', 5),
    OPENING_PARENTHESIS('(', 5),
    CLOSING_PARENTHESIS(')', 5),
    OPENING_BRACKET('[', 4),
    CLOSING_BRACKET(']', 4),
    OPENING_BRACE('{', 5),
    CLOSING_BRACE('}', 5),
    VERTICAL_BAR('|', 2),
    PLUS_SIGN('+', 6),
    MINUS_SIGN('-', 6),
    EQUALS_SIGN('=', 6),
    GREATER_THAN_SIGN('>', 5),
    LESS_THAN_SIGN('<', 5),
    COMMA(',', 2),
    PERIOD('.', 2),
    FORWARD_SLASH('/', 6),
    BACK_SLASH('\\', 6),
    COLON(':', 2),
    SEMI_COLON(';', 2),
    QUESTION_MARK('?', 6),
    UPSIDE_DOWN_QUESTION_MARK('¿', 6),
    AT_SIGN('@', 7),
    CARAT('^', 6),
    UNDERSCORE('_', 6),
    TILDE('~', 7),
    GRAVE('`', 3),
    POINTED_SQUARE('⌂', 6),
    LEFT_ARROWS('«', 6),
    RIGHT_ARROWS('»', 6),
    BEARDED_C('Ç', 6),
    BEARDED_C1('ç', 6),
    ACCENTED_U('ü', 6),
    ACCENTED_E('é', 6),
    ACCENTED_E1('ê', 6),
    ACCENTED_E2('ë', 6),
    ACCENTED_E3('è', 6),
    ACCENTED_A('â', 6),
    ACCENTED_A1('ä', 6),
    ACCENTED_A2('à', 6),
    ACCENTED_A3('å', 6),
    ONE_HALF('½', 6),
    ONE_FOURTH('¼', 6),
    CIRCLED_R('®', 6);

    private static final Map<Character, FontCharacter> CHARS;

    static {

        FontCharacter[] chars = FontCharacter.values();
        CHARS = new HashMap<>(chars.length);
        for (FontCharacter c : chars) {
            CHARS.put(c.getCharacter(), c);
        }
    }

    private final int length;
    private final char character;

    FontCharacter(char character, int length) {
        this.character = character;
        this.length = length;
    }

    /**
     * Get the character utility for the given
     * primitive character.
     *
     * @param c The character.
     * @return The utility.
     */
    public static FontCharacter getByCharacter(char c) {
        return CHARS.get(c);
    }

    /**
     * Get the length of this character in pixels.
     *
     * @return The pixel length.
     */
    public int getLength() {
        return this.length;
    }

    /**
     * Get the length of this character in pixels if
     * the character is bold.
     *
     * @return The bold pixel length.
     */
    public int getBold() {
        return this == SPACE ? this.length : this.length + 1;
    }

    /**
     * Get the primitive character this object
     * is utilizing.
     *
     * @return The character.
     */
    public char getCharacter() {
        return this.character;
    }
}
