import org.junit.Assert;
import org.junit.Test;
import ru.ifmo.galkin.excaption.NotLL1GrammarException;
import ru.ifmo.galkin.generator.Generator;

public class RightBranchingTest {
    @Test
    public void generateLeftRecParenthesesParser() {
        try {
            new Generator(String.format("grammars/%s", "rightBranching.gr")).generate();
            Assert.fail();
        } catch (NotLL1GrammarException ex) {

        }
    }
}
