package cn.omsfuk.compiling;


import cn.omsfuk.compiling.interpreter.Interpreter;
import cn.omsfuk.compiling.parser.SyntaxParser;
import cn.omsfuk.compiling.parser.TokenParser;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * A Wrapper for Interpreter & Compiler
 */
public class SimpleWrapper {

    private static Options options = new Options();

    private static void printUsage() {
        ByteOutputStream outputStream = new ByteOutputStream();
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("pl0", options);
    }

    public static void main(String[] args) throws IOException {
        options.addOption("d", "debug", false, "Enable debug info");
        options.addOption("h", "help", false, "");

        options.addOption(Option.builder("c")
                .longOpt("compile")
                .hasArg()
                .argName("filename")
                .desc("Compile")
                .build());

        options.addOption(Option.builder("e")
                .longOpt("execute")
                .hasArg()
                .argName("filename")
                .desc("Execute")
                .build());
        options.addOption(Option.builder("o")
                .longOpt("output")
                .hasArg()
                .argName("filename")
                .desc("Output file")
                .build());

        options.addOption(Option.builder("t")
                .longOpt("syntax-tree")
                .hasArg()
                .argName("filename")
                .desc("Build syntax tree")
                .build());


        CommandLineParser cmdLineParser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = cmdLineParser.parse(options, args);
        } catch (ParseException e) {
            printUsage();
            return ;
        }

        TokenParser tokenParser;
        SyntaxParser syntaxParser;
        Interpreter interpreter;
        if (cmd.hasOption("c")) {
            if (cmd.hasOption('e') || (!cmd.hasOption('o'))) {
                printUsage();
                return ;
            }
            String inputFile = cmd.getOptionValue('c');
            String outputFile = cmd.getOptionValue('o');
            PrintWriter writer = new PrintWriter(outputFile);
            tokenParser = new TokenParser(SimpleWrapper.class.getClassLoader().getResourceAsStream(inputFile));
            syntaxParser = new SyntaxParser(tokenParser);
            syntaxParser.parse();
            syntaxParser.getInstructions().forEach(writer::println);
            writer.close();
        } else if (cmd.hasOption('e')) {
            if (cmd.hasOption('c')) {
                printUsage();
                return;
            }
            String inputFile = cmd.getOptionValue('e');

            tokenParser = new TokenParser(SimpleWrapper.class.getClassLoader().getResourceAsStream(inputFile));
            syntaxParser = new SyntaxParser(tokenParser);
            syntaxParser.parse();
            interpreter = new Interpreter(syntaxParser.getInstructions(), syntaxParser.getStartPointer());
            if (cmd.hasOption('d')) {
                interpreter.setDebugEnable(true);
            }
            interpreter.exec();
        } else if (cmd.hasOption('t')) {
            String inputFile = cmd.getOptionValue('e');

            tokenParser = new TokenParser(SimpleWrapper.class.getClassLoader().getResourceAsStream(inputFile));
            syntaxParser = new SyntaxParser(tokenParser);
            syntaxParser.parse();


        } else if (args == null || args.length == 0) {
            tokenParser = new TokenParser(System.in);
            syntaxParser = new SyntaxParser(tokenParser);
            syntaxParser.setDebugEnable(true);
            syntaxParser.parse();
        } else {
            printUsage();
            return ;
        }
    }
}
