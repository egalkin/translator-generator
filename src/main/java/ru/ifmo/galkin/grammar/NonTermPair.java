package ru.ifmo.galkin.grammar;

import java.util.List;

public class NonTermPair extends RuleElem {
    private NonTerminal nonTerminal;
    private List<String> params;

    public NonTermPair(String name, NonTerminal nt, List<String> params) {
        super(name);
        this.nonTerminal = nt;
        this.params = params;
    }

    public NonTerminal getNonTerminal() {
        return this.nonTerminal;
    }

    public List<String> getParams() {
        return this.params;
    }

}
