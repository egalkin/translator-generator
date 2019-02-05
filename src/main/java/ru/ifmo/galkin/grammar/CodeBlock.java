package ru.ifmo.galkin.grammar;

import java.util.ArrayList;
import java.util.List;

public class CodeBlock extends RuleElem{
    private List<String> codeLines;

    public CodeBlock(List<String> codeLines) {
        super("codeBlock");
        this.codeLines = codeLines;
    }

    public List<String> getCodeLines() {
        return codeLines;
    }

}
