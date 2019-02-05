package ru.ifmo.galkin.generator;

import ru.ifmo.galkin.grammar.Terminal;
import ru.ifmo.galkin.utils.FormatUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.StringJoiner;

public class TokensGenerator {

    public void generateTokens(Set<Terminal> terminals, String path, String pkg, int innerLevel) {
       try (BufferedWriter tokenWriter= new BufferedWriter(new FileWriter(String.format("%s/Token.java", path)))) {
            tokenWriter.write(String.format("package %s;\n", pkg));
            tokenWriter.write("public enum Token {\n");
            tokenWriter.write(generateTokensNamesString(terminals, innerLevel + 1));
            tokenWriter.write("}\n");
       } catch (IOException ex) {
           System.out.println(ex.getMessage());
       }
    }

    public String generateTokensNamesString(Set<Terminal> terminals, int innerLevel) {
        StringJoiner terminalsNames = new StringJoiner(",");
        for (Terminal terminal : terminals) {
            terminalsNames.add(terminal.getName());
        }
        terminalsNames.add("END");
        terminalsNames.add("EPS");
        return String.format("%s%s\n", FormatUtils.getWhitespacesString(innerLevel), terminalsNames.toString());
    }

}
