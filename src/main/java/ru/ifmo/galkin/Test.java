package ru.ifmo.galkin;

import ru.ifmo.galkin.gparse.Parser;
import ru.ifmo.galkin.gparse.Tree;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;

public class Test {
    public static void main(String... args) throws ParseException, IOException {
        Parser parser = new Parser(new ByteArrayInputStream("2^2^2^2*5".getBytes()));
        Tree res = parser.parse();
        System.out.println(res.getValue());
        TreeWriter.treeToDotCode(res);
    }
}
