package ru.ifmo.galkin.generator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class TreeGenerator {

    public static void generateTree(String path, String pkg) {
        try (BufferedWriter treeWriter = new BufferedWriter(new FileWriter(String.format("%s/Tree.java", path)))) {
            treeWriter.write(String.format("package %s;\n", pkg));
            treeWriter.write("import java.util.List;\n" +
                    "\n" +
                    "public class Tree<T> {\n" +
                    "    public String node;\n" +
                    "    public List<Tree> children;\n" +
                    "    private T value;\n" +
                    "\n" +
                    "    public Tree(String node, List<Tree> children, T value) {\n" +
                    "        this.node = node;\n" +
                    "        this.children = children;\n" +
                    "        this.value = value;\n" +
                    "    }\n" +
                    "\n" +
                    "    public T getValue() {\n" +
                    "        return this.value;\n" +
                    "    }\n" +
                    "    \n" +
                    "    public Tree(String node) {\n" +
                    "        this.node = node;\n" +
                    "    }\n" +
                    "}");
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

    }

}

