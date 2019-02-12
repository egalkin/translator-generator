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


    public ParserGenerator(Set<NonTerminal> nonTerminals,
                           HashMap<NonTerminal, Rule> rules,
                           List<String> imports,
                           HashMap<String, HashSet<Terminal>> first,
                           HashMap<String, HashSet<Terminal>> follow) {
        this.imports = imports;
        this.nonTerminals = nonTerminals;
        this.rules = rules;
        this.first = new HashMap<>();
        this.follow = new HashMap<>();
        this.eps = new Terminal("EPS");
        this.first = first;
        this.follow = follow;
    }

    public void generateParser(String path, String pkg, int innerLevel)  {
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
        body.add(buildParseMethod(innerLevel));
        for (NonTerminal nt : nonTerminals) {
            body.add(buildRule(rules.get(nt), innerLevel));
        }
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
        body.add("this.lex.nextToken()");
        return body.toString();
    }

    private String buildParseMethod(int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner method = new StringJoiner("");
        method.add(String.format("%s%s", ws, "public Tree parse() throws ParseException {\n"));
        method.add(buildParseMethodBody(innerLevel + 1));
        method.add(FormatUtils.getDefaultEnd(ws, true));
        return method.toString();
    }

    private String buildParseMethodBody(int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner body = new StringJoiner("");
        NonTerminal start = nonTerminals.iterator().next();
        body.add(String.format("%s%s", ws, String.format("Tree result = %s();\n", start.getName())));
        body.add(String.format("%s%s", ws, "if (this.curToken == Token.END) return result;\n"));
        body.add(String.format("%s%s", ws, "else throw new ParseException(\"Expect end of input, found: \" + curToken, lex.getCurPos());\n"));
        return body.toString();
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
        body.add(String.format("%s%s", ws, "switch (curToken) {\n"));
        body.add(buildRuleIfBranchBody(rule, innerLevel + 1));
        if (this.first.get(rule.getNonTerminal().getName()).contains(eps)) {
            body.add(buildEpsBranchBody(rule, innerLevel + 1));
        }
        body.add(buildExceptionBranchBody(innerLevel + 1));
        body.add(FormatUtils.getDefaultEnd(ws, false));
        return body.toString();
    }

    private String buildRuleIfBranchBody(Rule rule, int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner body = new StringJoiner("");
        Set<String> namesInContext = new HashSet<>();
        for (List<RuleElem> oneProd : rule.getProductions()) {
            if (oneProd.size() == 1 && oneProd.get(0).getName().equals("EPS"))
                continue;
            for (int i = 0; i < oneProd.size(); ++i) {
                RuleElem elem = oneProd.get(i);
                if (i == 0) {
                    if (elem instanceof Terminal) {
                        Terminal terminal = (Terminal) elem;
                        body.add(buildTerminalCaseBaseBody(ws, terminal, namesInContext, true));
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
                        body.add(buildTerminalCaseBaseBody(FormatUtils.getModifiedWs(ws, WS_MULTIPLIER), terminal, namesInContext, false));
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
            body.add(String.format("%sreturn new Tree<>(\"%s\", trees,%s);\n", FormatUtils.getModifiedWs(ws, WS_MULTIPLIER), rule.getNonTerminal().getName(), rule.getVarName()));
        }
        return body.toString();
    }

    private String buildTerminalCaseBaseBody(String ws, Terminal terminal, Set<String> namesInContext, boolean isCase) {
        StringJoiner body = new StringJoiner("");
        if (isCase)
            body.add(String.format("%scase %s:\n", ws, terminal.getName()));
        else
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

    private String buildNonTermPairCaseBaseBody(String ws, NonTermPair pair, Set<String> namesInContext, boolean isCase) {
        StringJoiner body = new StringJoiner("");
        NonTerminal nonTerminal = pair.getNonTerminal();
        List<String> params = pair.getParams();
        StringJoiner paramString = new StringJoiner(",");
        for (String param : params)
            paramString.add(param);
        if (isCase) {
            Set<Terminal> first = this.first.get(nonTerminal.getName());
            for (Terminal terminal : first) {
                body.add(String.format("%s%s", ws, String.format("case %s:\n", terminal.getName())));
            }
        }
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


    private String buildExceptionBranchBody(int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner body = new StringJoiner("");
        body.add(String.format("%s%s", ws, "default:\n"));
        body.add(String.format("%s%s", FormatUtils.getModifiedWs(ws, WS_MULTIPLIER),
                "throw new ParseException(\"Unexpected symbol \" + (char) lex.getCurChar() + \" at position: \" + lex.getCurPos(), lex.getCurPos());\n"));
        return body.toString();
    }


    private String buildEpsBranchBody(Rule rule, int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner body = new StringJoiner("");
        Set<Terminal> follow = this.follow.get(rule.getNonTerminal().getName());
        for (Terminal terminal : follow) {
            body.add(String.format("%s%s", ws, String.format("case %s:\n", terminal.getName())));
        }
        body.add(String.format("%s%s", FormatUtils.getModifiedWs(ws, WS_MULTIPLIER),
                String.format("return new Tree<>(\"%s\",Arrays.asList(new Tree(\"eps\")), %s);\n", rule.getNonTerminal().getName(), rule.getVarName())));
        return body.toString();
    }

}
