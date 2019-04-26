package ru.ifmo.galkin.grammar;

import java.util.ArrayList;
import java.util.List;

public class CodeBlock extends RuleElem{
    private String codeLine;

    public CodeBlock(String codeLine) {
        super("codeBlock");
        this.codeLine = codeLine;
    }

    public String getCodeLine() {
        return codeLine;
    }

}
