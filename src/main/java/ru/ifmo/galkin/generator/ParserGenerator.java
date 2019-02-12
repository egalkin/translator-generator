package ru.ifmo.galkin.generator;

import ru.ifmo.galkin.excaption.NotLL1GrammarException;
import ru.ifmo.galkin.grammar.*;
import ru.ifmo.galkin.utils.FormatUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ParserGenerator {
    private final int WS_MULTIPLIER = 4;

    private Set<NonTerminal> nonTerminals;
    private List<String> imports;
    private HashMap<NonTerminal, Rule> rules;
    private HashMap<String, HashSet<Terminal>> first;
    private HashMap<String, HashSet<Terminal>> follow;
    private Terminal eps;


    public ParserGenerator(Set<NonTerminal> nonTerminals, HashMap<NonTerminal, Rule> rules, List<String> imports) {
        this.imports = imports;
        this.nonTerminals = nonTerminals;
        this.rules = rules;
        this.first = new HashMap<>();
        this.follow = new HashMap<>();
        this.eps = null;
        for (NonTerminal nt : nonTerminals) {
            first.put(nt.getName(), new HashSet<>());
            follow.put(nt.getName(), new HashSet<>());
        }
    }

    public void generateParser(String path, String pkg, int innerLevel) throws NotLL1GrammarException {
        countFirst();
        countFollow();
        checkLL1();
        try (BufferedWriter parserWriter = new BufferedWriter(new FileWriter(String.format("%s/Parser.java", path)))) {
            String ws = FormatUtils.getWhitespacesString(innerLevel);
            parserWriter.write(String.format("package %s;\n", pkg));
            parserWriter.write(buildImports());
            parserWriter.write("public class Parser {\n");
            parserWriter.write(buildParserBody(innerLevel + 1));
            parserWriter.write(FormatUtils.getDefaultEnd(ws, false));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void buildFirst() {
        countFirst();
        for (String name : first.keySet()) {
            System.out.print(name + " first : ");
            for (Terminal term : first.get(name)) {
                System.out.print(term + " ");
            }
            System.out.println();
        }

    }

    private void buildFollow() {
        countFollow();
        for (String name : follow.keySet()) {
            System.out.print(name + " folow : ");
            for (Terminal term : follow.get(name)) {
                System.out.print(term + " ");
            }
            System.out.println();
        }
    }


    private String buildImports() {
        StringJoiner imports = new StringJoiner("");
        this.imports.addAll(Arrays.asList("java.io.InputStream",
                "java.util.Collections",
                "java.util.ArrayList",
                "java.util.Arrays",
                "java.util.List",
                "java.util.HashMap",
                "java.util.HashSet",
                "java.text.ParseException"));
        Set<String> impsSet = new HashSet<>(this.imports);
        for (String s : impsSet)
            imports.add("import " + s + ";\n");
        imports.add("\n");
        return imports.toString();
    }

    private String buildParserBody(int innerLevel) {
        StringJoiner body = new StringJoiner("");
        body.add(getVarsString(innerLevel));
        body.add(buildConstructor(innerLevel));
        body.add(buildIsFinished());
        for (NonTerminal nt : nonTerminals) {
            body.add(buildRule(rules.get(nt), innerLevel));
        }
        return body.toString();
    }


    private String buildIsFinished() {
        String method =
                "    public boolean isFinished() {\n" +
                        "        return this.curToken == Token.END;\n" +
                        "    }\n\n";
        return method;
    }

    private String buildRule(Rule rule, int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner method = new StringJoiner("");
        StringJoiner argsString = new StringJoiner(",", "", "");
        List<Rule.TypePair> args = rule.getArgs();
        for (Rule.TypePair pair : args) {
            argsString.add(pair.toString());
        }
        method.add(String.format("%spublic Tree %s(%s) throws ParseException{\n", ws, rule.getNonTerminal().getName(), argsString.toString()));
        method.add(buildRuleBody(rule, innerLevel + 1));
        method.add(FormatUtils.getDefaultEnd(ws, true));
        return method.toString();
    }

    private String buildRuleBody(Rule rule, int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner body = new StringJoiner("");
        body.add(String.format("%s%s", ws, "curToken = this.lex.getCurToken();\n"));
        body.add(String.format("%s%s", ws, "List<Tree> trees = new ArrayList<>();\n"));
        if (rule.getReturnValue() != null)
            body.add(String.format("%s%s", ws, String.format("%s %s = %s;\n", rule.getReturnValue(), rule.getVarName(), null)));
        else
            body.add(String.format("%s%s", ws, "Object value = null;\n"));
        body.add(String.format("%s%s", ws, String.format("if (this.first.get(\"%s\").contains(curToken)) {\n",
                rule.getNonTerminal().getName())));
        body.add(buildRuleIfBranchBody(rule, innerLevel + 1));
        body.add(FormatUtils.getDefaultEnd(ws, false));
        body.add(String.format("%s%s", ws, String.format("if (this.follow.get(\"%s\").contains(curToken)) return new Tree<>(\"%s\",Arrays.asList(new Tree(\"eps\")), %s);\n",
                rule.getNonTerminal().getName(), rule.getNonTerminal().getName(), rule.getVarName())));
        body.add(String.format("%s%s", ws,
                "else throw new ParseException(\"Unexpected symbol \" + (char) lex.getCurChar() + \" at position: \" + lex.getCurPos(), lex.getCurPos());\n"));
        return body.toString();
    }


    private String buildTerminalCaseBaseBody(String ws, Terminal terminal, Set<String> namesInContext) {
        StringJoiner body = new StringJoiner("");
        body.add(String.format("%sif (curToken == Token.%s) {\n", ws, terminal.getName()));
        if (!namesInContext.contains(terminal.getName())) {
            body.add(String.format("%sTree<String> %s = new Tree<>(this.lex.getCurTokenString(), Collections.emptyList(), this.lex.getCurTokenString());\n",
                    FormatUtils.getModifiedWs(ws, WS_MULTIPLIER), terminal.getName()));
            namesInContext.add(terminal.getName());
        } else {
            body.add(String.format("%s%s = new Tree<>(this.lex.getCurTokenString(), Collections.emptyList(), this.lex.getCurTokenString());\n",
                    FormatUtils.getModifiedWs(ws, WS_MULTIPLIER), terminal.getName()));
        }
        body.add(String.format("%strees.add(%s);\n",
                FormatUtils.getModifiedWs(ws, WS_MULTIPLIER), terminal.getName()));
        body.add(String.format("%slex.nextToken();\n",
                FormatUtils.getModifiedWs(ws, WS_MULTIPLIER)));
        return body.toString();
    }

    private String buildNonTermPairCaseBaseBody(String ws, NonTermPair pair, Set<String> namesInContext, boolean addIfBranch) {
        StringJoiner body = new StringJoiner("");
        NonTerminal nonTerminal = pair.getNonTerminal();
        List<String> params = pair.getParams();
        StringJoiner paramString = new StringJoiner(",");
        for (String param : params)
            paramString.add(param);
        if (addIfBranch)
            body.add(String.format("%s%s", ws, String.format("if (this.first.get(\"%s\").contains(curToken)) {\n",
                    nonTerminal.getName())));
        String type = rules.get(nonTerminal).getReturnValue();
        String genericType = type == null ? "Object" : type;
        if (!namesInContext.contains(nonTerminal.getName())) {
            body.add(String.format("%s%s", FormatUtils.getModifiedWs(ws, WS_MULTIPLIER),
                    String.format("Tree<%s> %s = %s(%s);\n", genericType,
                            nonTerminal.getName(), nonTerminal.getName(), paramString.toString())));
            namesInContext.add(nonTerminal.getName());
        } else {
            body.add(String.format("%s%s", FormatUtils.getModifiedWs(ws, WS_MULTIPLIER),
                    String.format("%s = %s(%s);\n", nonTerminal.getName(), nonTerminal.getName(), paramString.toString())));
            namesInContext.add(nonTerminal.getName());
        }
        body.add(String.format("%strees.add(%s);\n", FormatUtils.getModifiedWs(ws, WS_MULTIPLIER), nonTerminal.getName()));
        return body.toString();
    }

    private String buildRuleIfBranchBody(Rule rule, int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner body = new StringJoiner("");
        for (List<RuleElem> oneProd : rule.getProductions()) {
            Set<String> namesInContext = new HashSet<>();
            if (oneProd.size() == 1 && oneProd.get(0).getName().equals("EPS"))
                continue;
            for (int i = 0; i < oneProd.size(); ++i) {
                RuleElem elem = oneProd.get(i);
                if (i == 0) {
                    if (elem instanceof Terminal) {
                        Terminal terminal = (Terminal) elem;
                        body.add(buildTerminalCaseBaseBody(ws, terminal, namesInContext));
                    } else if (elem instanceof NonTermPair) {
                        NonTermPair pair = (NonTermPair) elem;
                        body.add(buildNonTermPairCaseBaseBody(ws, pair, namesInContext, true));
                    } else if (elem instanceof CodeBlock) {
                        CodeBlock codeBlock = (CodeBlock) elem;
                        for (String line : codeBlock.getCodeLines()) {
                            body.add(String.format("%s%s\n", ws, line.substring(1, line.length() - 1).trim()));
                        }
                    }
                } else {
                    if (elem instanceof Terminal) {
                        Terminal terminal = (Terminal) elem;
                        body.add(buildTerminalCaseBaseBody(FormatUtils.getModifiedWs(ws, WS_MULTIPLIER), terminal, namesInContext));
                        body.add(FormatUtils.getDefaultEnd(FormatUtils.getModifiedWs(ws, WS_MULTIPLIER), false));
                        body.add(String.format("%s%s", FormatUtils.getModifiedWs(ws, WS_MULTIPLIER),
                                "else throw new ParseException(\"Unexpected symbol \" + (char) lex.getCurChar() + \" at position: \" + lex.getCurPos(), lex.getCurPos());\n"));

                    } else if (elem instanceof NonTermPair) {
                        NonTermPair pair = (NonTermPair) elem;
                        body.add(buildNonTermPairCaseBaseBody(ws, pair, namesInContext, false));
                    } else if (elem instanceof CodeBlock) {
                        CodeBlock codeBlock = (CodeBlock) elem;
                        for (String line : codeBlock.getCodeLines()) {
                            body.add(String.format("%s%s\n", FormatUtils.getModifiedWs(ws, WS_MULTIPLIER), line.substring(1, line.length() - 1).trim()));
                        }
                    }
                }
            }
            body.add(FormatUtils.getDefaultEnd(ws, false));
        }
        body.add(String.format("%sreturn new Tree<>(\"%s\", trees,%s);\n", ws, rule.getNonTerminal().getName(), rule.getVarName()));
        return body.toString();
    }

    private String getVarsString(int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner vars = FormatUtils.getDefaultStringJoiner(ws, true);
        vars.add("private LexicalAnalyzer lex");
        vars.add("private HashMap<String, HashSet<Token>> first");
        vars.add("private HashMap<String, HashSet<Token>> follow");
        vars.add("private Token curToken");
        return vars.toString();
    }

    private String buildConstructor(int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner constructor = new StringJoiner("");
        constructor.add(String.format("%s%s", ws, "public Parser(InputStream is) throws ParseException {\n"));
        constructor.add(getConstructorBody(innerLevel + 1));
        constructor.add(FormatUtils.getDefaultEnd(ws, true));
        return constructor.toString();
    }

    private String getConstructorBody(int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner body = FormatUtils.getDefaultStringJoiner(ws, false);
        body.add("this.lex = new LexicalAnalyzer(is)");
        body.add("this.first = new HashMap()");
        fillMap(body, first, "first");
        body.add("this.follow = new HashMap()");
        fillMap(body, follow, "follow");
        body.add("this.lex.nextToken()");
        return body.toString();
    }


    private void fillMap(StringJoiner body, HashMap<String, HashSet<Terminal>> mp, String name) {
        for (String nt : mp.keySet()) {
            int i = 0;
            for (Terminal terminal : mp.get(nt)) {
                if (i == 0) {
                    body.add(String.format("this.%s.put(\"%s\", new HashSet())", name, nt));
                    if (name.equals("follow"))
                        body.add(String.format("this.%s.get(\"%s\").add(Token.END)", name, nt));

                }
                body.add(String.format("this.%s.get(\"%s\").add(Token.%s)", name, nt, terminal.getName()));
                i++;
            }
        }
    }


    private void countFirst() {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (NonTerminal nt : nonTerminals) {
                Rule ntRule = rules.get(nt);
                List<List<RuleElem>> productions = ntRule.getProductions();
                int firstSize = this.first.get(nt.getName()).size();
                for (List<RuleElem> prod : productions) {
                    List<RuleElem> oneProd = prod.stream().filter((it) -> !(it instanceof CodeBlock)).collect(Collectors.toList());
                    for (int i = 0; i < oneProd.size(); ++i) {
                        RuleElem elem = oneProd.get(i);
                        if (elem instanceof Terminal) {
                            Terminal terminal = (Terminal) elem;
                            if (terminal.getName().equals("EPS")) {
                                if (eps == null)
                                    eps = terminal;
                            }
                            this.first.get(nt.getName()).add(terminal);
                            break;
                        } else if (elem instanceof NonTermPair) {
                            if (i == 0)
                                this.first.get(nt.getName()).addAll(this.first.get(elem.getName()));
                            else {
                                RuleElem previous = oneProd.get(i - 1);
                                if (previous instanceof NonTermPair)
                                    if (this.first.get(previous.getName()).contains(eps))
                                        this.first.get(nt.getName()).addAll(this.first.get(previous.getName()));
                            }
                        }
                    }
                }
                changed |= (firstSize != this.first.get(nt.getName()).size());
            }
        }
    }

    private void countFollow() {
        boolean changed = true;
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
                            int followSize = this.follow.get(elem.getName()).size();
                            if (i == oneProd.size() - 1) {
                                this.follow.get(elem.getName()).addAll(this.follow.get(nt.getName()));
                            }
                            for (int j = i + 1; j < oneProd.size(); ++j) {
                                RuleElem next = oneProd.get(j);
                                if (next instanceof Terminal) {
                                    Terminal terminal = (Terminal) next;
                                    if (next.getName().equals("EPS"))
                                        this.follow.get(elem.getName()).addAll(this.follow.get(nt.getName()));
                                    else
                                        this.follow.get(elem.getName()).add(terminal);
                                    break;
                                } else if (next instanceof NonTermPair) {
                                    NonTerminal nonTerminal = ((NonTermPair) next).getNonTerminal();
                                    this.follow.get(elem.getName()).addAll(this.first.get(nonTerminal.getName()));
                                    this.follow.get(elem.getName()).remove(eps);
                                    if (this.first.get(nonTerminal.getName()).contains(eps))
                                        this.follow.get(elem.getName()).addAll(this.follow.get(nt.getName()));
                                    else {
                                        break;
                                    }
                                }
                            }
                            changed |= (followSize != this.follow.get(elem.getName()).size());
                        }
                    }
                }
            }
        }
    }

    public void checkLL1() throws NotLL1GrammarException {
        for (NonTerminal nt : nonTerminals) {
            Rule ntRule = rules.get(nt);
            List<List<RuleElem>> productions = ntRule.getProductions();
            for (List<RuleElem> prod1 : productions)
                for (List<RuleElem> prod2 : productions) {
                    if (prod1 == prod2)
                        continue;
                    List<RuleElem> firstProd = prod1.stream().filter((it) -> !(it instanceof CodeBlock)).collect(Collectors.toList());
                    List<RuleElem> secondProd = prod2.stream().filter((it) -> !(it instanceof CodeBlock)).collect(Collectors.toList());
                    Set<Terminal> first1 = getElemsFirst(firstProd.get(0));
                    Set<Terminal> first2 = getElemsFirst(secondProd.get(0));
                    if (first1.contains(eps)) {
                        Set<Terminal> follow = new HashSet<>(this.follow.get(nt.getName()));
                        follow.retainAll(first2);
                        if (follow.size() != 0)
                            throw new NotLL1GrammarException("Given grammar is not LL1");
                    } else {
                        first1.retainAll(first2);
                        if (first1.size() != 0)
                            throw new NotLL1GrammarException("Given grammar is not LL1");
                    }
                }
        }
    }

    public Set<Terminal> getElemsFirst(RuleElem elem) {
        if (elem instanceof NonTermPair) {
            NonTerminal nonTerminal = ((NonTermPair) elem).getNonTerminal();
            return new HashSet<>(this.first.get(nonTerminal.getName()));
        } else if (elem instanceof Terminal) {
            Terminal terminal = (Terminal) elem;
            return new HashSet<>(Collections.singletonList(terminal));
        }
        return null;
    }
}
