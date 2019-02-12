package ru.ifmo.galkin.grammar;

public class RuleElem {
    private String name;

    public RuleElem() {

    }

    public RuleElem(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof RuleElem)
            return this.name.equals(((RuleElem) obj).getName());
        return false;
    }
}
