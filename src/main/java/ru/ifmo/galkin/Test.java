package ru.ifmo.galkin;

import ru.ifmo.galkin.gparse.LexicalAnalyzer;
import ru.ifmo.galkin.gparse.Parser;
import ru.ifmo.galkin.gparse.Token;
import ru.ifmo.galkin.gparse.Tree;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.ArrayList;

public class Test {
    public static void main(String... args) throws ParseException, IOException {
//        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer(new ByteArrayInputStream("1+2".getBytes()));
//        Parser parser = new Parser(new ByteArrayInputStream("(1+2*4)*4/3".getBytes()));
//        Parser parser = new Parser(new ByteArrayInputStream("((1+2*4)*4/3".getBytes()));
//        Tree res = parser.expr();
//        System.out.println(res.getValue());
//        TreeWriter.treeToDotCode(res);
        Parser parser = new Parser(new ByteArrayInputStream("(())".getBytes()));
        Tree res = parser.s();
        System.out.println(parser.isFinished());
        TreeWriter.treeToDotCode(res);
//        lexicalAnalyzer.nextToken();
//        while (lexicalAnalyzer.getCurToken() != Token.END) {
//            System.out.println(lexicalAnalyzer.getCurTokenString());
//            lexicalAnalyzer.nextToken();
//        }
    }
}
