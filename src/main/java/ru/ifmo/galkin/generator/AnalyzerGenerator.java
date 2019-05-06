package ru.ifmo.galkin.generator;

import ru.ifmo.galkin.utils.FormatUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class AnalyzerGenerator {

    private List<String> needGetterVars = new ArrayList<>();
    private final String wsAndStringTemplate = "%s%s";

    public void generateAnalyzer(HashMap<String, String> regexpToTerms, HashMap<String, String> valuesToTermNames, String path,
                                 String pkg, int innerLevel) {
        try (BufferedWriter analyzerWriter = new BufferedWriter(new FileWriter(String.format("%s/LexicalAnalyzer.java", path)))) {
            String ws = FormatUtils.getWhitespacesString(innerLevel);
            analyzerWriter.write(String.format("package %s;\n", pkg));
            analyzerWriter.write("import java.io.IOException ;\n" +
                    "import java.io.InputStream;\n" +
                    "import java.util.HashMap;\n" +
                    "import java.util.regex.Pattern;\n" +
                    "import java.text.ParseException;\n\n");
            analyzerWriter.write("public class LexicalAnalyzer{\n");
            analyzerWriter.write(buildAnalyzerBody(regexpToTerms, valuesToTermNames, innerLevel + 1));
            analyzerWriter.write(FormatUtils.getDefaultEnd(ws, false));

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public String buildAnalyzerBody(HashMap<String, String> regexpToTerms, HashMap<String, String> valuesToTerms, int innerLevel) {
        StringJoiner body = new StringJoiner("");
        body.add(getVarsString(innerLevel));
        body.add(buildConstructor(regexpToTerms, valuesToTerms, innerLevel));
        for (String varDecl : needGetterVars) {
            body.add(buildGetter(varDecl, innerLevel));
        }
        body.add(buildGetTokenByValue(valuesToTerms, innerLevel));
        body.add(buildIsBlank(innerLevel));
        body.add(buildNextChar(innerLevel));
        body.add(buildNextToken(valuesToTerms, innerLevel));
        return body.toString();
    }

    private String buildGetTokenByValue(HashMap<String, String> valuesToTerms, int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner method = new StringJoiner("");
        method.add(String.format(wsAndStringTemplate, ws, "private Token getTokenByValue(String value) {\n"));
        method.add(buildGetTokenByValueBody(valuesToTerms, innerLevel + 1));
        method.add(FormatUtils.getDefaultEnd(ws, true));
        return method.toString();
    }

    private String buildGetTokenByValueBody(HashMap<String, String> valuesToTerms, int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner body = new StringJoiner("");
        for (String key : valuesToTerms.keySet()) {
            String name = valuesToTerms.get(key);
            body.add(String.format(wsAndStringTemplate, ws, String.format("if (value.equals(\"%s\")) {\n",
                    key)));
            body.add(String.format(wsAndStringTemplate, ws + ws.substring(0, 4),
                    String.format("return Token.%s;\n", name)));
            body.add(FormatUtils.getDefaultEnd(ws, false));
        }
        body.add(String.format(wsAndStringTemplate, ws, "return null;\n"));
        return body.toString();
    }

    private String buildNextToken(HashMap<String, String> valuesToTerms, int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner method = new StringJoiner("");
        method.add(String.format(wsAndStringTemplate, ws, "public void nextToken() throws ParseException {\n"));
        method.add(buildWhitespacesSkipper(innerLevel + 1));
        method.add(buildTokenChecker(valuesToTerms,innerLevel + 1));
        method.add(FormatUtils.getDefaultEnd(ws, true));
        return method.toString();
    }


    private String buildTokenChecker(HashMap<String, String> valuesToTerms, int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner checker = new StringJoiner("");
        checker.add(String.format(wsAndStringTemplate, ws, "StringBuilder tokenString = new StringBuilder();\n\n"));
        checker.add(String.format(wsAndStringTemplate, ws, "String matchedRegexp = null;\n\n"));
        checker.add(String.format(wsAndStringTemplate, ws, "boolean tokenParsingFlag = true;;\n\n"));
        checker.add(String.format(wsAndStringTemplate, ws, "while(tokenParsingFlag) {\n"));
        checker.add(buildTokenCheckerBody(valuesToTerms, innerLevel + 1));
        checker.add(FormatUtils.getDefaultEnd(ws, false));
        return checker.toString();
    }


    private String buildTokenCheckerBody(HashMap<String, String> valuesToTerms, int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner body = new StringJoiner("");
        body.add(String.format(wsAndStringTemplate, ws, "if (curChar == -1) {curToken = Token.END; break;}\n"));
        body.add(String.format(wsAndStringTemplate, ws, "tokenString.append((char)curChar);\n"));
        body.add(String.format(wsAndStringTemplate, ws, "switch(tokenString.toString()) {\n"));
        for (String key : valuesToTerms.keySet()) {
            body.add(String.format(wsAndStringTemplate, ws + ws.substring(0, 4),
                    String.format("case \"%s\":\n", key)));
        }
        body.add(buildCommonTokenCheckerBody(innerLevel+2));
        body.add(String.format(wsAndStringTemplate, ws + ws.substring(0, 4), "default:\n"));
        body.add(buildRegexpTokenCheckerBody(innerLevel + 2));
        body.add(FormatUtils.getDefaultEnd(ws, false));
        return body.toString();
    }

    private String buildCommonTokenCheckerBody(int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner body = new StringJoiner("");
        body.add(String.format(wsAndStringTemplate, ws, "nextChar();\n"));
        body.add(String.format(wsAndStringTemplate, ws,
                "curToken = getTokenByValue(tokenString.toString());\n"));
        body.add(String.format(wsAndStringTemplate, ws,
                "curTokenString = tokenString.toString();\n"));
        body.add(String.format(wsAndStringTemplate, ws, "tokenParsingFlag = false;\n"));
        body.add(String.format(wsAndStringTemplate, ws, "break;\n"));
        return body.toString();
    }


    private String buildRegexpTokenCheckerBody(int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        int wsMultiplyer = 4;
        StringJoiner branch = new StringJoiner("");
        branch.add(String.format(wsAndStringTemplate, ws, "if (matchedRegexp == null) {\n"));
        branch.add(String.format(wsAndStringTemplate, FormatUtils.getModifiedWs(ws, wsMultiplyer),
                "for (String reg : regexpToTerms.keySet()) { \n"));
        branch.add(String.format(wsAndStringTemplate, FormatUtils.getModifiedWs(ws, wsMultiplyer * 2),
                "if (Pattern.matches(reg, tokenString.toString())) {\n"));
        branch.add(String.format(wsAndStringTemplate, FormatUtils.getModifiedWs(ws, wsMultiplyer * 3),
                "matchedRegexp = reg;\n"));
        branch.add(FormatUtils.getDefaultEnd(FormatUtils.getModifiedWs(ws, wsMultiplyer * 2), false));
        branch.add(FormatUtils.getDefaultEnd(FormatUtils.getModifiedWs(ws, wsMultiplyer), false));
        branch.add(FormatUtils.getDefaultEnd(ws, false));
        branch.add(String.format(wsAndStringTemplate, ws, "nextChar();\n"));
        branch.add(String.format(wsAndStringTemplate, ws, "if (matchedRegexp != null) {\n"));
        branch.add(String.format(wsAndStringTemplate, FormatUtils.getModifiedWs(ws, wsMultiplyer),
                "if (Pattern.matches(matchedRegexp, tokenString.toString()) && !Pattern.matches(matchedRegexp, tokenString.toString()+\"\"+(char)curChar)) {\n"));
        branch.add(String.format(wsAndStringTemplate, FormatUtils.getModifiedWs(ws, wsMultiplyer * 2),
                "curToken = getTokenByValue(matchedRegexp);\n"));
        branch.add(String.format(wsAndStringTemplate, FormatUtils.getModifiedWs(ws, wsMultiplyer * 2),
                "curTokenString = tokenString.toString();\n"));
        branch.add(String.format(wsAndStringTemplate, FormatUtils.getModifiedWs(ws, wsMultiplyer * 2), "tokenParsingFlag = false;\n"));
        branch.add(String.format(wsAndStringTemplate, FormatUtils.getModifiedWs(ws, wsMultiplyer * 2), "break;\n"));
        branch.add(FormatUtils.getDefaultEnd(FormatUtils.getModifiedWs(ws, wsMultiplyer), false));
        branch.add(FormatUtils.getDefaultEnd(ws, false));

        branch.add(String.format(wsAndStringTemplate, ws,
                "if (curChar == -1) throw new ParseException(\"Finished work with unfinished tokenString:\" + tokenString.toString(), curPos);\n"));
        return branch.toString();
    }


    private String buildWhitespacesSkipper(int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner skipper = new StringJoiner("");
        skipper.add(String.format(wsAndStringTemplate, ws, "while (isBlank(curChar)) {\n"));
        skipper.add(String.format(wsAndStringTemplate + "%s", ws, ws.substring(0, 4), "nextChar();\n"));
        skipper.add(FormatUtils.getDefaultEnd(ws, true));
        return skipper.toString();

    }

    private String buildGetter(String varDecl, int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner getter = new StringJoiner("");
        String[] declPart = varDecl.split("=")[0].split(" ");
        String type = declPart[declPart.length - 2];
        String varName = declPart[declPart.length - 1];
        String getterName = String.format(wsAndStringTemplate, "get",
                Character.toUpperCase(varName.charAt(0)) + varName.substring(1));
        getter.add(String.format("%s%s %s %s() {\n", ws, "public", type, getterName));
        getter.add(String.format("%s%s%s", ws, ws.substring(0, 4), String.format("return %s;\n", varName)));
        getter.add(FormatUtils.getDefaultEnd(ws, true));
        return getter.toString();
    }

    private String buildNextChar(int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner method = new StringJoiner("");
        method.add(String.format(wsAndStringTemplate, ws, "private void nextChar() throws ParseException {\n"));
        method.add(buildNextCharBody(innerLevel + 1));
        method.add(FormatUtils.getDefaultEnd(ws, true));
        return method.toString();
    }

    private String buildNextCharBody(int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner body = new StringJoiner("");
        body.add(String.format(wsAndStringTemplate, ws, "curPos++;\n"));
        body.add(String.format(wsAndStringTemplate, ws, "try {\n"));
        body.add(String.format("%s%s%s", ws, ws.substring(0, 4), "curChar = is.read();\n"));
        body.add(String.format(wsAndStringTemplate, ws, "} catch (IOException e) {\n"));
        body.add(String.format("%s%s%s", ws, ws.substring(0, 4), "throw new ParseException(e.getMessage(), curPos);\n"));
        body.add(String.format(wsAndStringTemplate, ws, "}\n"));
        return body.toString();
    }

    private String buildIsBlank(int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner method = new StringJoiner("");
        method.add(String.format(wsAndStringTemplate, ws, "private boolean isBlank(int c) {\n"));
        method.add(buildIsBlankBody(innerLevel + 1));
        method.add(FormatUtils.getDefaultEnd(ws, true));
        return method.toString();
    }

    private String buildIsBlankBody(int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner body = FormatUtils.getDefaultStringJoiner(ws, false);
        body.add("return c == ' ' || c == '\\r' || c == '\\n' || c == '\\t'");
        return body.toString();
    }

    private String getVarsString(int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner vars = FormatUtils.getDefaultStringJoiner(ws, true);
        vars.add("private InputStream is");
        needGetterVars.add("private int curChar");
        vars.add(needGetterVars.get(needGetterVars.size() - 1));
        vars.add("private HashMap<String,String> regexpToTerms");
        needGetterVars.add("private int curPos");
        vars.add(needGetterVars.get(needGetterVars.size() - 1));
        needGetterVars.add("private Token curToken");
        vars.add(needGetterVars.get(needGetterVars.size() - 1));
        needGetterVars.add("private String curTokenString");
        vars.add(needGetterVars.get(needGetterVars.size() - 1));
        return vars.toString();
    }

    private String buildConstructor(HashMap<String, String> regexpToTerms, HashMap<String, String> valuesToTerms, int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner constructor = new StringJoiner("");
        constructor.add(String.format(wsAndStringTemplate, ws, "public LexicalAnalyzer(InputStream is) throws ParseException {\n"));
        constructor.add(buildConstructorBody(regexpToTerms, valuesToTerms, innerLevel + 1));
        constructor.add(FormatUtils.getDefaultEnd(ws, true));
        return constructor.toString();
    }

    private String buildConstructorBody(HashMap<String, String> regexpToTerms, HashMap<String, String> valuesToTerms, int innerLevel) {
        String ws = FormatUtils.getWhitespacesString(innerLevel);
        StringJoiner body = FormatUtils.getDefaultStringJoiner(ws, false);
        body.add("this.is = is");
        body.add("curPos = 0");
        body.add("regexpToTerms = new HashMap()");
        for (String key : regexpToTerms.keySet()) {
            body.add(String.format("regexpToTerms.put(\"%s\",\"%s\")", key, regexpToTerms.get(key)));
        }
        body.add("nextChar()");
        return body.toString();
    }

}
