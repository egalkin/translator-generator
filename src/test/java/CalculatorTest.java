import org.junit.Assert;
import org.junit.Test;
import ru.ifmo.galkin.gparse.Parser;

import java.io.ByteArrayInputStream;

import java.text.ParseException;

import static org.junit.Assert.assertEquals;

public class CalculatorTest {

    @Test
    public void testSimpleExpression() {
        try {
            Parser parser = new Parser(new ByteArrayInputStream("(2 * 3 + 15) / 7 + 1".getBytes()));
            assertEquals(4, parser.parse().getValue());
        } catch (ParseException ex) {
            Assert.fail();
        }

    }

    @Test
    public void testUnaryExp() {
        try {
            Parser parser = new Parser(new ByteArrayInputStream("-5".getBytes()));
            assertEquals(-5, parser.parse().getValue());
        } catch (ParseException ex) {
            Assert.fail();
        }
    }


    @Test
    public void testFactorialExp() {
        try {
            Parser parser = new Parser(new ByteArrayInputStream("(2+3)!".getBytes()));
            assertEquals(120, parser.parse().getValue());
        } catch (ParseException ex) {
            Assert.fail();
        }
    }

    @Test
    public void testPowExp() {
        try {
            Parser parser = new Parser(new ByteArrayInputStream("2 ^ 4 ^ 2".getBytes()));
            assertEquals(65536, parser.parse().getValue());
        } catch (ParseException ex) {
            Assert.fail();
        }
    }

}
