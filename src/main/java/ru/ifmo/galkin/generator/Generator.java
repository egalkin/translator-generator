package ru.ifmo.galkin.generator;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import ru.ifmo.galkin.antlr4.ParserGrammarLexer;
import ru.ifmo.galkin.antlr4.ParserGrammarParser;
import ru.ifmo.galkin.excaption.NotLL1GrammarException;
import ru.ifmo.galkin.grammar.Grammar;
import ru.ifmo.galkin.grammar.GrammarDescription;
import ru.ifmo.galkin.grammar.Terminal;
import ru.ifmo.galkin.utils.FirstFollowUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class Generator {
    private GrammarDescription description;
    private Grammar grammar;
    private final String gparseFolder = "gen/ru/ifmo/galkin/gparse";
    private final String gparsePackage = "ru.ifmo.galkin.gparse";

    public static void main(String... args) throws NotLL1GrammarException {
        new Generator(String.format("grammars/%s", args[0])).generate();
    }

    public void generate() throws NotLL1GrammarException {
        if (!grammar.getBrokenTerminals().keySet().isEmpty()) {
            for (String brokenTerminal : grammar.getBrokenTerminals().keySet()) {
                System.out.println("Broken terminal " + brokenTerminal + " rule: " + grammar.getBrokenTerminals().get(brokenTerminal));
            }
        } else {
            TreeGenerator.generateTree(gparseFolder, gparsePackage);
            HashMap<String, HashSet<Terminal>> first = FirstFollowUtils.countFirst(grammar.getNonTerminals(),
                    grammar.getRules());
            HashMap<String, HashSet<Terminal>> follow = FirstFollowUtils.countFollow(grammar.getNonTerminals(),
                    grammar.getRules(), first);
            FirstFollowUtils.checkLL1(grammar.getNonTerminals(), grammar.getRules(), first, follow);
            new TokensGenerator().generateTokens(grammar.getTerminals(), gparseFolder, gparsePackage, 0);
            new AnalyzerGenerator().generateAnalyzer(grammar.getRegexpToTermName(), grammar.getValueToTermName(),
                    gparseFolder, gparsePackage, 0);
            new ParserGenerator(grammar.getNonTerminals(), grammar.getRules(),
                    description.getImports(), first, follow).generateParser(gparseFolder, gparsePackage, 0);
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
