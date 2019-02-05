grammar ParserGrammar;

@header {
    import ru.ifmo.galkin.grammar.GrammarDescription;
    import ru.ifmo.galkin.grammar.Grammar;
    import ru.ifmo.galkin.grammar.Rule;
    import ru.ifmo.galkin.grammar.RuleElem;
    import ru.ifmo.galkin.grammar.NonTerminal;
    import ru.ifmo.galkin.grammar.Terminal;
    import ru.ifmo.galkin.grammar.CodeBlock;
    import ru.ifmo.galkin.grammar.NonTermPair;
    import java.util.StringJoiner;
    import java.util.List;
    import java.util.Arrays;
    import java.util.HashMap;
}

@members {
    HashMap<String, Terminal> nameToTerminal = new HashMap();
    HashMap<String, Terminal> valueToTerminal = new HashMap();
    HashMap<String, NonTerminal> nameToNonTerminal = new HashMap();
    Terminal eps  = new Terminal("EPS");
    int atNum = 1;
}

parseGrammar returns [GrammarDescription grammarDescription]
  : {$grammarDescription = new GrammarDescription(); }
     name {$grammarDescription.setGrammarName($name._name);}
     (headers {$grammarDescription.setImports($headers.imports);})?
     parse {$grammarDescription.setGrammar($parse.grammar);}
  ;

name returns [String _name]
   : GRAMMAR id=(NON_TERMINAL|TERMINAL) {$_name = $id.text;}
   ;

headers returns [List<String> imports]
   : {$imports = new ArrayList();} HEADER '{'  (imp {$imports.add($imp.s);})* '}'
   ;

imp returns [String s]
   : IMPORT id=IMPORT_STATEMENT {$s=$id.text;} SEMICOLON
   ;


parse returns [Grammar grammar]
  : {$grammar = new Grammar();}(parseRules {$grammar.addRule($parseRules.rule.getNonTerminal(), $parseRules.rule);}
   | parseTerminal {$grammar.addTerminal($parseTerminal.terminal);$grammar.setTermNameToValue($parseTerminal.terminal);})+
  ;

parseTerminal returns [Terminal terminal]
  : TERMINAL COLON id=VALID_TOKEN_VALUE SEMICOLON {
        if (!nameToTerminal.containsKey($TERMINAL.text)) {
            Terminal term = new Terminal($TERMINAL.text, $id.text);
            nameToTerminal.put($TERMINAL.text, term);
            valueToTerminal.put($id.text.substring(1, $id.text.length()-1), term);
            $terminal = term;
        } else {
            Terminal term = nameToTerminal.get($TERMINAL.text);
            term.setTerminalValue($id.text.substring(1, $id.text.length()-1));
            $terminal = term;
        }}
  | TERMINAL {
        if (!nameToTerminal.containsKey($TERMINAL.text)) {
            Terminal term = new Terminal($TERMINAL.text);
            nameToTerminal.put($TERMINAL.text, term);
            $terminal = term;
        } else {
            Terminal term = nameToTerminal.get($TERMINAL.text);
            $terminal = term;
        }
  } COLON (regexp[$terminal])+  SEMICOLON
  ;


regexp[Terminal givenTerminal]
   : LBRACK st=(DIGIT|TERMINAL|NON_TERMINAL)  DASH
                fin=(DIGIT|TERMINAL|NON_TERMINAL)  RBRACK (mulValue=REGEXP_MULTIPLYER)? {
                 givenTerminal.addRegexpValue($st.text, $fin.text,$mulValue.text);
             }
   ;

parseRules returns [Rule rule]
  : NON_TERMINAL {
        if (!nameToNonTerminal.containsKey($NON_TERMINAL.text)) {
            NonTerminal nonTerminal = new NonTerminal($NON_TERMINAL.text);
            nameToNonTerminal.put($NON_TERMINAL.text, nonTerminal);
            $rule = new Rule(nonTerminal);
        } else {
            NonTerminal nonTerminal = nameToNonTerminal.get($NON_TERMINAL.text);
            $rule = new Rule(nonTerminal);
        }
      }
      (LBRACK type=(TERMINAL|NON_TERMINAL) arg=(TERMINAL|NON_TERMINAL){$rule.addArg($type.text,$arg.text);}
            (',' type=(TERMINAL|NON_TERMINAL) arg=(TERMINAL|NON_TERMINAL){$rule.addArg($type.text,$arg.text);})* RBRACK )?
     (RETURNS LBRACK  retV=(TERMINAL|NON_TERMINAL) var=(TERMINAL|NON_TERMINAL) RBRACK
     {$rule.setReturnValue($retV.text);
      $rule.setVarName($var.text);})?
    COLON ruleBody {$rule.setProductions($ruleBody.productions);}SEMICOLON
  ;

ruleBody returns [List<List<RuleElem>> productions]
   :
   ruleDescription {$productions = new ArrayList<>();
   $productions.add($ruleDescription.rule);}
   (OR ruleDescription {$productions.add($ruleDescription.rule);})*
   ;

ruleDescription returns [List<RuleElem> rule]
  : {$rule = new ArrayList<>();}(TERMINAL {
        if (!nameToTerminal.containsKey($TERMINAL.text)) {
            Terminal terminal = new Terminal($TERMINAL.text);
            nameToTerminal.put($TERMINAL.text, terminal);
            $rule.add(terminal);
        } else {
            $rule.add(nameToTerminal.get($TERMINAL.text));
        }}
    | NON_TERMINAL {
        NonTerminal nonTerminal = null;
        if (!nameToNonTerminal.containsKey($NON_TERMINAL.text)) {
            nonTerminal = new NonTerminal($NON_TERMINAL.text);
            nameToNonTerminal.put($NON_TERMINAL.text, nonTerminal);
        } else {
            nonTerminal = nameToNonTerminal.get($NON_TERMINAL.text);
        }
       } {ArrayList<String> params = new ArrayList<>();}
            ( '<'(param = (TERMINAL|NON_TERMINAL|DIGIT) {params.add($param.text);} | QUOTED_STRING {params.add($QUOTED_STRING.text);})
                (','param=(TERMINAL|NON_TERMINAL|DIGIT) {params.add($param.text);}| QUOTED_STRING {params.add($QUOTED_STRING.text);})*'>')?
           {
           $rule.add(new NonTermPair(nonTerminal.getName(), nonTerminal, params));
           }
    | CODE_BLOCK {
        $rule.add(new CodeBlock(new ArrayList<>(Arrays.asList($CODE_BLOCK.text))));
    })+
    | {$rule = new ArrayList<>(); $rule.add(eps);}
  ;



QUOTED_STRING
   : '"' ~[\r\n]+ '"'
   ;

LPAREN
   : '('
   ;


RPAREN
   : ')'
  ;

DASH
  : '-'
;


OR
   : '|'
   ;

LBRACK
   : '['
   ;

RBRACK
  : ']'
  ;

SEMICOLON
   : ';'
   ;

COLON
   : ':'
   ;

DIGIT
  : '0'..'9'
  ;

REGEXP_MULTIPLYER
  : ('*'|'?'|'+')
  ;

INT
   : DIGIT+
   ;

IMPORT
   : 'import'
   ;

HEADER
   : '@header'
   ;

GRAMMAR
   : 'grammar'
   ;

RETURNS
   : 'returns'
   ;


VALID_TOKEN_VALUE: '\'' ~[\r\n]+ '\'';

NON_TERMINAL
   : VALID_NON_TERMINAL_START VALID_ID_CHAR*
   ;

TERMINAL
   : VALID_TERMINAL_START VALID_ID_CHAR*
   ;

CODE_BLOCK: '{' ~[\r\n]+ '}';

IMPORT_STATEMENT
   : (TERMINAL|NON_TERMINAL) ('.'(TERMINAL|NON_TERMINAL))*
   ;

fragment VALID_NON_TERMINAL_START
   : ('a' .. 'z')
   ;

fragment VALID_TERMINAL_START
   : ('A' .. 'Z')
   ;

fragment VALID_ID_START
   : ('a' .. 'z') | ('A' .. 'Z') | '_'
   ;

fragment VALID_ID_CHAR
   : VALID_ID_START | ('0' .. '9')
;

WS
   : [ \r\n\t] + -> skip
   ;

