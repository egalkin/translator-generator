package ru.ifmo.galkin.generator;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import ru.ifmo.galkin.antlr4.ParserGrammarLexer;
import ru.ifmo.galkin.antlr4.ParserGrammarParser;
import ru.ifmo.galkin.grammar.Grammar;
import ru.ifmo.galkin.grammar.GrammarDescription;

import java.io.File;
import java.io.IOException;

public class Generator {
    private GrammarDescription description;
    private Grammar grammar;
    private final String gparseFolder = "gen/ru/ifmo/galkin/gparse";
    private final String gparsePackage = "ru.ifmo.galkin.gparse";

    public static void main(String... args) {
        new Generator(String.format("grammars/%s", args[0])).generate();
    }

    public void generate() {
        if (!grammar.getBrokenTerminals().keySet().isEmpty()) {
            for (String brokenTerminal : grammar.getBrokenTerminals().keySet()) {
                System.out.println("Broken terminal " + brokenTerminal + " rule: " + grammar.getBrokenTerminals().get(brokenTerminal));
            }
        } else {
            for (String reg : grammar.getRegexpToTermName().keySet())
                System.out.println(reg);
            TreeGenerator.generateTree(gparseFolder, gparsePackage);
            new TokensGenerator().generateTokens(grammar.getTerminals(), gparseFolder, gparsePackage, 0);
            new AnalyzerGenerator().generateAnalyzer(grammar.getRegexpToTermName(), grammar.getValueToTermName(),
                    gparseFolder, gparsePackage, 0);
            new ParserGenerator(grammar.getRules(), description.getImports()).generateParser(gparseFolder, gparsePackage,0);
        }
    }

    public Generator(String path) {
        parseGrammar(path);
        createFolder();
    }

    private boolean createFolder() {
        return new File(gparseFolder).mkdirs();
    }

    private void parseGrammar(String path) {
        ParserGrammarLexer lexer;
        try {
            lexer = new ParserGrammarLexer(CharStreams.fromFileName(path));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            ParserGrammarParser p2 = new ParserGrammarParser(tokens);
            this.description = p2.parseGrammar().grammarDescription;
            this.grammar = description.getGrammar();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

}
