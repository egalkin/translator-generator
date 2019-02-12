package ru.ifmo.galkin.grammar;

import ru.ifmo.galkin.grammar.exception.BrokenRegexpException;

import java.util.*;

public class Grammar {
    private Set<Terminal> terminals;
    private Set<NonTerminal> nonTerminals;
    private HashMap<NonTerminal, Rule> rules;
    private HashMap<String, String> termNameToValue;
    private HashMap<String, String> valueToTermName;
    private HashMap<String, String> regexpToTermName;
    private HashMap<String, String> brokenTerminals;


    public Grammar() {
        terminals = new LinkedHashSet<>();
        nonTerminals = new LinkedHashSet<>();
        rules = new HashMap<>();
        termNameToValue = new HashMap<>();
        valueToTermName = new HashMap<>();
        regexpToTermName = new HashMap<>();
        brokenTerminals = new HashMap<>();
    }

    public void addTerminal(Terminal terminal) {
        this.terminals.add(terminal);
    }

    public void addNonTerminal(NonTerminal nonTerminal) {
        this.nonTerminals.add(nonTerminal);
    }

    public void setTermNameToValue(Terminal terminal) {
        if (!terminal.getTerminalValue().equals("Empty")) {
            termNameToValue.put(terminal.getName(), terminal.getTerminalValue());
            valueToTermName.put(terminal.getTerminalValue(), terminal.getName());
        }
        if (!terminal.getRegexp().equals("Empty")) {
            if (!terminal.isBrokenRegexp()) {
                termNameToValue.put(terminal.getName(), terminal.getRegexp());
                valueToTermName.put(terminal.getRegexp(), terminal.getName());
                regexpToTermName.put(terminal.getRegexp(), terminal.getName());
            } else {
                brokenTerminals.put(terminal.getName(), terminal.getRegexp());
            }
        }
    }

    public void addRule(NonTerminal source, Rule rule) {
        nonTerminals.add(source);
        rules.put(source, rule);
    }

    public Set<Terminal> getTerminals() {
        return this.terminals;
    }

    public HashMap<String, String> getBrokenTerminals() {
        return brokenTerminals;
    }


    public Set<NonTerminal> getNonTerminals() {
        return this.nonTerminals;
    }

    public HashMap<NonTerminal, Rule> getRules() {
        return this.rules;
    }

    public HashMap<String,String> getTermNameToValue() {
        return this.termNameToValue;
    }

    public HashMap<String, String> getRegexpToTermName() {
        return regexpToTermName;
    }


    public HashMap<String,String> getValueToTermName() {return this.valueToTermName;}
}
