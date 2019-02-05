package ru.ifmo.galkin;

import guru.nidi.graphviz.engine.*;
import ru.ifmo.galkin.gparse.Tree;

import java.io.File;
import java.io.IOException;
import java.util.StringJoiner;


public class TreeWriter {


    public static String writeCodeToFile(String code) throws IOException {
        Graphviz.fromString(code).render(Format.PNG).toFile(new File("plots/plot.png"));
        return code;
    }

    public static String treeToDotCode(Tree<?> root) throws IOException {
        StringJoiner code = new StringJoiner("");
        code.add("digraph G {\n");
        code.add(0 + " [label=\"" + root.node + "\"];\n");
        treeToDotCode(root, code, 0, 1);
        code.add("}\n");
        return writeCodeToFile(code.toString());
    }

    public static int treeToDotCode(Tree<?> root, StringJoiner code, int parent, int newVerNum) {
        int curVertNum = newVerNum;
        if (root.children != null) {
            for (Tree<?> son : root.children) {
                if (son == null)
                    continue;
                code.add(curVertNum + " [label=\"" + son.node + "\"];\n");
                code.add(parent + "->" + curVertNum + ";\n");
                curVertNum = treeToDotCode(son, code, curVertNum, curVertNum + 1);
            }
        }
        return curVertNum;
    }

}