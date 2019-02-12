package ru.ifmo.galkin.grammar;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Rule {
    private NonTerminal nonTerminal;
    private String returnValue;
    private String varName;
    private List<List<RuleElem>> productions;
    private List<TypePair> args;

    public class TypePair {
        public String type;
        public String name;

        public TypePair(String type, String name) {
            this.type = type;
            this.name = name;
        }

        @Override
        public String toString() {
            return this.type + " " +this.name;
        }
    }

    public Rule(NonTerminal nonTerminal) {
        this.nonTerminal = nonTerminal;
        this.args = new ArrayList<>();
        this.returnValue = null;
        this.varName = null;
    }

    public void addArg(String type, String name) {
        this.args.add(new TypePair(type, name));
    }

    public List<TypePair> getArgs() {
        return this.args;
    }

    public void setReturnValue(String returnValue) {
        this.returnValue = returnValue;
    }

    public void setVarName(String name) {
        this.varName = name;
    }

    public void setProductions(List<List<RuleElem>> productions) {
        this.productions = productions;
    }

    public NonTerminal getNonTerminal() {
        return this.nonTerminal;
    }

    public String getReturnValue() {
        return this.returnValue;
    }

    public String getVarName() {
        return this.varName;
    }

    public List<List<RuleElem>> getProductions() {
        return this.productions;
    }

    @Override
    public String toString() {
        StringJoiner rule = new StringJoiner("");
        for (List<RuleElem> rules : productions) {
            for (RuleElem elem : rules) {
                rule.add(elem + " ");
            }
            rule.add("|");
        }
        return rule.toString();
    }
}
