import org.junit.Assert;
import org.junit.Test;
import ru.ifmo.galkin.exception.NotLL1GrammarException;
import ru.ifmo.galkin.generator.Generator;

public class LeftRecursionTest {

    @Test
    public void generateLeftRecParenthesesParser() {
        try {
            new Generator(String.format("grammars/%s", "leftRecParentheses.gr")).generate();
            Assert.fail();
        } catch (NotLL1GrammarException ex) {

        }
    }

    @Test
    public void generateLeftRecCalculatorParser() {
        try {
            new Generator(String.format("grammars/%s", "leftRecCalculator.gr")).generate();
            Assert.fail();
        } catch (NotLL1GrammarException ex) {

        }
    }
}
