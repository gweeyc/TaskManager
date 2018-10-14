import org.junit.Test;

import static org.junit.Assert.*;

public class TodoTest {

    @Test
    public void testStringConversion() {
        TaskManager.tasks.add(new Todo("read a textbook"));
        assertEquals("description: read a textbook" + System.lineSeparator() + "    is done? No", TaskManager.tasks.get(0).toString());
    }
}