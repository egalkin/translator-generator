import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.ifmo.galkin.excaption.NotLL1GrammarException;
import ru.ifmo.galkin.generator.Generator;
import ru.ifmo.galkin.gparse.Parser;

import java.io.ByteArrayInputStream;

import java.text.ParseException;

public class ParenthesesTest {

    @Test
    public void testSimpleExpression() {
        try {
            Parser parser = new Parser(new ByteArrayInputStream("()()".getBytes()));
            parser.parse();
        } catch (ParseException ex) {
            Assert.fail();
        }

    }

    @Test()
    public void testInvalidExpressionOne() {
        try {
            Parser parser = new Parser(new ByteArrayInputStream("(()".getBytes()));
            parser.parse();
            Assert.fail();
        } catch (ParseException ex) {

        }
    }

    @Test()
    public void testInvalidExpressionTwo() {
        try {
            Parser parser = new Parser(new ByteArrayInputStream("))()".getBytes()));
            parser.parse();
            Assert.fail();
        } catch (ParseException ex) {

        }
    }

}
