package ru.ifmo.galkin;

import ru.ifmo.galkin.gparse.LexicalAnalyzer;
import ru.ifmo.galkin.gparse.Parser;
import ru.ifmo.galkin.gparse.Token;
import ru.ifmo.galkin.gparse.Tree;
import ru.ifmo.galkin.grammar.Terminal;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class Test {
    public static void main(String... args) throws ParseException {
        Parser parser = new Parser(new ByteArrayInputStream("16 + 32 * 44 + 55/5 - 14".getBytes()));
        Tree res = parser.parse();
        System.out.println(res.getValue());
    }
}
