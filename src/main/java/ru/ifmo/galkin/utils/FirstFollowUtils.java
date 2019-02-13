package ru.ifmo.galkin.utils;

import ru.ifmo.galkin.exception.NotLL1GrammarException;
import ru.ifmo.galkin.grammar.*;

import java.util.*;
import java.util.stream.Collectors;

public class FirstFollowUtils {


    public static HashMap<String, HashSet<Terminal>> countFirst(Set<NonTerminal> nonTerminals,
                                                                HashMap<NonTerminal, Rule> rules) {
        HashMap<String, HashSet<Terminal>> first = new HashMap<>();
        for (NonTerminal nt : nonTerminals)
            first.put(nt.getName(), new HashSet<>());
        Terminal eps = new Terminal("EPS");
        boolean changed = true;
        while (changed) {
            changed = false;
            for (NonTerminal nt : nonTerminals) {
                Rule ntRule = rules.get(nt);
                List<List<RuleElem>> productions = ntRule.getProductions();
                int firstSize = first.get(nt.getName()).size();
                for (List<RuleElem> prod : productions) {
                    List<RuleElem> oneProd = prod.stream().filter((it) -> !(it instanceof CodeBlock)).collect(Collectors.toList());
                    for (int i = 0; i < oneProd.size(); ++i) {
                        RuleElem elem = oneProd.get(i);
                        if (elem instanceof Terminal) {
                            Terminal terminal = (Terminal) elem;
                            first.get(nt.getName()).add(terminal);
                            break;
                        } else if (elem instanceof NonTermPair) {
                            if (i == 0)
                                first.get(nt.getName()).addAll(first.get(elem.getName()));
                            else {
                                RuleElem previous = oneProd.get(i - 1);
                                if (previous instanceof NonTermPair)
                                    if (first.get(previous.getName()).contains(eps))
                                        first.get(nt.getName()).addAll(first.get(previous.getName()));
                            }
                        }
                    }
                }
                changed |= (firstSize != first.get(nt.getName()).size());
            }
        }
        return first;
    }

    public static HashMap<String, HashSet<Terminal>> countFollow(Set<NonTerminal> nonTerminals,
                                                                 HashMap<NonTerminal, Rule> rules,
                                                                 HashMap<String, HashSet<Terminal>> first) {

        HashMap<String, HashSet<Terminal>> follow = new HashMap<>();
        for (NonTerminal nt : nonTerminals)
            follow.put(nt.getName(), new HashSet<>());
        Terminal eps = new Terminal("EPS");
        Terminal end = new Terminal("END");
        boolean changed = true;
        follow.get(nonTerminals.iterator().next().getName()).add(end);
        while (changed) {
            changed = false;
            for (NonTerminal nt : nonTerminals) {
                Rule ntRule = rules.get(nt);
                List<List<RuleElem>> productions = ntRule.getProductions();
                for (List<RuleElem> prod : productions) {
                    List<RuleElem> oneProd = prod.stream().filter((it) -> !(it instanceof CodeBlock)).collect(Collectors.toList());
                    for (int i = 0; i < oneProd.size(); ++i) {
                        RuleElem elem = oneProd.get(i);
                        if (elem instanceof NonTermPair) {
                            int followSize = follow.get(elem.getName()).size();
                            if (i == oneProd.size() - 1) {
                                follow.get(elem.getName()).addAll(follow.get(nt.getName()));
                            }
                            for (int j = i + 1; j < oneProd.size(); ++j) {
                                RuleElem next = oneProd.get(j);
                                if (next instanceof Terminal) {
                                    Terminal terminal = (Terminal) next;
                                    if (next.getName().equals("EPS"))
                                        follow.get(elem.getName()).addAll(follow.get(nt.getName()));
                                    else
                                        follow.get(elem.getName()).add(terminal);
                                    break;
                                } else if (next instanceof NonTermPair) {
                                    NonTerminal nonTerminal = ((NonTermPair) next).getNonTerminal();
                                    follow.get(elem.getName()).addAll(first.get(nonTerminal.getName()));
                                    follow.get(elem.getName()).remove(eps);
                                    if (first.get(nonTerminal.getName()).contains(eps))
                                        follow.get(elem.getName()).addAll(follow.get(nt.getName()));
                                    else {
                                        break;
                                    }
                                }
                            }
                            changed |= (followSize != follow.get(elem.getName()).size());
                        }
                    }
                }
            }
        }
        return follow;
    }

    public static void checkLL1(Set<NonTerminal> nonTerminals,
                                 HashMap<NonTerminal, Rule> rules,
                                 HashMap<String, HashSet<Terminal>> first,
                                 HashMap<String, HashSet<Terminal>> follow) throws NotLL1GrammarException {
        Terminal eps = new Terminal("EPS");
        for (NonTerminal nt : nonTerminals) {
            Rule ntRule = rules.get(nt);
            List<List<RuleElem>> productions = ntRule.getProductions();
            for (List<RuleElem> prod1 : productions)
                for (List<RuleElem> prod2 : productions) {
                    if (prod1 == prod2)
                        continue;
                    List<RuleElem> firstProd = prod1.stream().filter((it) -> !(it instanceof CodeBlock)).collect(Collectors.toList());
                    List<RuleElem> secondProd = prod2.stream().filter((it) -> !(it instanceof CodeBlock)).collect(Collectors.toList());
                    Set<Terminal> first1 = getElemsFirst(firstProd.get(0), first);
                    Set<Terminal> first2 = getElemsFirst(secondProd.get(0), first);
                    if (first1.contains(eps)) {
                        Set<Terminal> curFollow = new HashSet<>(follow.get(nt.getName()));
                        curFollow.retainAll(first2);
                        if (curFollow.size() != 0)
                            throw new NotLL1GrammarException("Given grammar is not LL1");
                    } else {
                        first1.retainAll(first2);
                        if (first1.size() != 0)
                            throw new NotLL1GrammarException("Given grammar is not LL1");
                    }
                }
        }
    }

    private static Set<Terminal> getElemsFirst(RuleElem elem, HashMap<String, HashSet<Terminal>> first) {
        if (elem instanceof NonTermPair) {
            NonTerminal nonTerminal = ((NonTermPair) elem).getNonTerminal();
            return new HashSet<>(first.get(nonTerminal.getName()));
        } else if (elem instanceof Terminal) {
            Terminal terminal = (Terminal) elem;
            return new HashSet<>(Collections.singletonList(terminal));
        }
        return null;
    }
}
