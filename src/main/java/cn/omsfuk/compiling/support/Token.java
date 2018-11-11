package cn.omsfuk.compiling.support;


public class Token {

    /**
    public static final Token CACHED_COMMA = new Token(SYM_COMMA);

    public static final Token CACHED_PERIOD = new Token(SYM_PERIOD);

    public static final Token CACHED_SEMICOLON = new Token(SYM_SEMICOLON);

    public static final Token CACHED_LEFT_BRACKETS = new Token(SYM_LEFT_BRACKETS);

    public static final Token CACHED_RIGHT_BRACKETS = new Token(SYM_RIGHT_BRACKETS);

    public static final Token CACHED_PLUS = new Token(SYM_PLUS);

    public static final Token CACHED_SUB = new Token(SYM_SUB);

    public static final Token CACHED_MUL = new Token(SYM_MUL);

    public static final Token CACHED_DIV = new Token(SYM_DIV);

    public static final Token CACHED_POUND = new Token(SYM_POUND);

    public static final Token CACHED_EQUAL = new Token(SYM_EQUAL);

    public static final Token CACHED_LESS = new Token(SYM_LESS);

    public static final Token CACHED_GREATER = new Token(SYM_GREATER);

    public static final Token CACHED_LESS_EQUAL = new Token(SYM_LESS_EQUAL);

    public static final Token CACHED_GREATER_EQUAL = new Token(SYM_GREATER_EQUAL);

    public static final Token CACHED_ASSIGN = new Token(SYM_ASSIGN);
    */

    private Symbol symbol;

    private String id;

    private Integer slot;

    private Integer row;

    private Integer col;

    public Token() {
    }

    public Token(Symbol symbol, String id, int slot, int row, int col) {
        this.symbol = symbol;
        this.id = id;
        this.slot = slot;
        this.row = row;
        this.col = col;
    }

    public Token(Symbol symbol, int row, int col) {
        this.symbol = symbol;
        this.row = row;
        this.col = col;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getSlot() {
        return slot;
    }

    public void setSlot(Integer slot) {
        this.slot = slot;
    }


    public Integer getRow() {
        return row;
    }

    public void setRow(Integer row) {
        this.row = row;
    }

    public Integer getCol() {
        return col;
    }

    public void setCol(Integer col) {
        this.col = col;
    }

    @Override
    public String toString() {
        return String.format("[%-15s %-9s %-4s %-4s %-4s]", symbol, id, slot, row, col);
    }
}
