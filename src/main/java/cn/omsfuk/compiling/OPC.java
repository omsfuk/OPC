package cn.omsfuk.compiling;

import cn.omsfuk.compiling.analyzer.SyntaxAnalyzer;
import cn.omsfuk.compiling.analyzer.TokenAnalyzer;

import java.io.*;

/**
 * OMSFUK PI/0 Compiler. Simple wrapper for SyntaxAnalyzer, TokenAnalyzer
 */
public class OPC {

    public static void main(String[] args) throws IOException {
        InputStream inputStream = System.in;
        if (args.length == 2) {
            if ("-f".equals(args[0])) {
                inputStream = new FileInputStream(args[1]);
            }
        }
        TokenAnalyzer tokenAnalyzer = new TokenAnalyzer(inputStream);
        SyntaxAnalyzer syntaxAnalyzer = new SyntaxAnalyzer(tokenAnalyzer);
        syntaxAnalyzer.setDebugEnable(true);
        syntaxAnalyzer.analyse();
    }
}
