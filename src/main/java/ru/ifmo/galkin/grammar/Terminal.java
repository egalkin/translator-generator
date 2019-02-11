package ru.ifmo.galkin.grammar;

import java.util.StringJoiner;

public class Terminal extends RuleElem {
    private String terminalValue;
    private StringJoiner regexp;
    private boolean isBrokenRegexp = false;

    public Terminal(String name) {
        this(name, "Empty");
    }

    public Terminal(String name, String value) {
        super(name);
        this.terminalValue = value;
        this.regexp = null;
    }


    public String getRegexp() {
        return regexp == null ? "" : this.regexp.toString();
    }

    public boolean isBrokenRegexp() {
        return isBrokenRegexp;
    }

    public void addRegexpValue(String start, String finish, String mulValue) {
        if (this.regexp == null)
            this.regexp = new StringJoiner("");
        if (start.length() != 1 || finish.length() != 1)
            isBrokenRegexp = true;
        if (start.compareTo(finish) >= 0)
            isBrokenRegexp = true;
        regexp.add(String.format("[%s-%s]%s", start, finish, (mulValue == null ? "" : mulValue)));
    }

    public String getTerminalValue() {
        return this.terminalValue;
    }

    public void setTerminalValue(String value) {
        if (this.regexp == null) {
            this.regexp = new StringJoiner("");
            this.regexp.add("Empty");
        }
        this.terminalValue = value;
    }

}