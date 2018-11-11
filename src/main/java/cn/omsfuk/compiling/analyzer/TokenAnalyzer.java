package cn.omsfuk.compiling.analyzer;

import cn.omsfuk.compiling.support.Symbol;
import cn.omsfuk.compiling.support.Token;
import cn.omsfuk.compiling.exception.SyntaxErrorException;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static cn.omsfuk.compiling.support.Symbol.*;

/**
 * 词法分析器。不适合用面向对象的方式来编程
 */
public class TokenAnalyzer {

    private List<String> constants = new ArrayList<>();

    private ArrayList<String> ids = new ArrayList<>();

    private static final Map<String, Symbol> RESERVE = new HashMap<String, Symbol>() {
        {
            put("procedure", SYM_PROCEDURE);
            put("const", SYM_CONT);
            put("begin", SYM_BEGIN);
            put("end", SYM_END);
            put("var", SYM_VAR);
            put("odd", SYM_ODD);
            put("if", SYM_IF);
            put("then", SYM_THEN);
            put("call", SYM_CALL);
            put("while", SYM_WHILE);
            put("do", SYM_DO);
            put("read", SYM_READ);
            put("write", SYM_WRITE);
        }
    };

    private InputStream source;

    /**
     * 当前字符
     */
    private int ch;

    /**
     * 所处行数
     */
    private int row;

    /**
     * 所处列数
     */
    private int col;

    public TokenAnalyzer(InputStream source) throws IOException {
        this.source = new BufferedInputStream(source);
        nextChar();
    }

    public Token nextToken() throws IOException, SyntaxErrorException {
        Token ans;
        String rawToken;
        StringBuilder sb = new StringBuilder();
        nextNonBlankChar();
        if (ch == -1) {
            return null;
        }
        if (Character.isLetter(ch)) {
            int start = col;
            while (Character.isLetter(ch) || Character.isDigit(ch)) {
                sb.append((char) ch);
                nextChar();
            }
            rawToken = sb.toString();
            Symbol symbol = RESERVE.get(rawToken);
            if (symbol != null) { // 如果为保留字
                ans = new Token(symbol, row, start);
            } else { // 否则为标识符
                int slot = ids.indexOf(rawToken);
                ans = new Token(SYM_IDENT, rawToken, slot, row, start);
            }
        } else if (Character.isDigit(ch)) {
            int start = col;
            while (Character.isDigit(ch)) {
                sb.append((char) ch);
                nextChar();
            }
            rawToken = sb.toString();
            int slot = constants.indexOf(rawToken);
            ans = new Token(SYM_NUMBER, rawToken, slot, row, start);
        } else if (ch == ',') {
            ans =  new Token(SYM_COMMA, row, col);
            nextChar();
        } else if (ch == '.') {
            ans = new Token(SYM_PERIOD, row, col);
            nextChar();
        } else if (ch == '(') {
            ans = new Token(SYM_LEFT_BRACKETS, row, col);
            nextChar();
        } else if (ch == ')') {
            ans = new Token(SYM_RIGHT_BRACKETS, row, col);
            nextChar();
        } else if (ch == ';') {
            ans = new Token(SYM_SEMICOLON, row, col);
            nextChar();
        } else if (ch == '+') {
            ans = new Token(SYM_PLUS, row, col);
            nextChar();
        } else if (ch == '-') {
            ans = new Token(SYM_SUB, row, col);
            nextChar();
        } else if (ch == '*') {
            ans = new Token(SYM_MUL, row, col);
            nextChar();
        } else if (ch == '/') {
            ans = new Token(SYM_DIV, row, col);
            nextChar();
        } else if (ch == '#') {
            ans = new Token(SYM_POUND, row, col);
            nextChar();
        } else if (ch == '=') {
            ans = new Token(SYM_EQUAL, row, col);
            nextChar();
        } else if (ch == '<') {
            nextChar();
            if (ch == '=') {
                ans = new Token(SYM_LESS_EQUAL, row, col - 1);
                nextChar();
            } else {
                ans = new Token(SYM_EQUAL, row, col - 1);
            }
        } else if (ch == '>') {
            nextChar();
            if (ch == '=') {
                ans = new Token(SYM_GREATER_EQUAL, row, col - 1);
                nextChar();
            } else {
                ans = new Token(SYM_GREATER, row, col - 1);
            }
        } else if (ch == ':') {
            nextChar();
            if (ch == '=') {
                ans = new Token(SYM_ASSIGN, row, col - 1);
                nextChar();
            } else {
                throw new SyntaxErrorException(String.format("Syntax error in (%d, %d)", row, col));
            }
        } else {
            throw new SyntaxErrorException(String.format("Syntax error in (%d, %d)", row, col));
        }
        return ans;
    }

    private void nextNonBlankChar() throws IOException {
        while (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') {
            nextChar();
        }
    }

    private void nextChar() throws IOException {
        ch = source.read();
        // TODO 默认以\n为回车换行符
        if (ch == '\n') {
            row ++;
            col = 0;
        } else {
            col ++;
        }
    }

    public static void main(String[] args) throws IOException, SyntaxErrorException {
        TokenAnalyzer analyzer = new TokenAnalyzer(new FileInputStream("simple.fuk"));
        Token token;
        while ((token = analyzer.nextToken()) != null) {
            System.out.println(token);
        }
    }
}
