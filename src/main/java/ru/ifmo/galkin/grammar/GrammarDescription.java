package ru.ifmo.galkin.grammar;

import java.util.ArrayList;
import java.util.List;

public class GrammarDescription {
    private String grammarName;
    private List<String> imports;
    private Grammar grammar;

    public GrammarDescription() {
        this.imports = new ArrayList<>();
    }

    public void setImports(List<String> imports) {
        this.imports = imports;
    }

    public void setGrammarName(String name) {
        this.grammarName = name;
    }

    public void setGrammar(Grammar grammar) {
        this.grammar = grammar;
    }

    public String getGrammarName() {
        return this.grammarName;
    }

    public List<String> getImports() {
        return this.imports;
    }

    public Grammar getGrammar() {
        return this.grammar;
    }


}
