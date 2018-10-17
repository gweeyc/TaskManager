package tmanagerproj;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class ParserTest {

        @Test
        public void getCommandWord(){
            assertEquals("todo", Parser.getCommandWord("todo read book"));
            assertEquals("deadline", Parser.getCommandWord("deadline return book /by next Friday"));
            assertEquals("exit", Parser.getCommandWord("exit"));
            assertEquals("xyz", Parser.getCommandWord("   xyz   ")); // leading and trailing spaces
            // ...
        }

        @Test
        public void createTodo() throws TaskManagerException {
            Todo actual = (Todo) Parser.createTodo("todo read book");
            Todo expected = new Todo("read book");
            assertEquals(expected.toString(), actual.toString());
        }

 /*   @Test
    public void test_TaskManagerExceptionThrown() {
        try {
            addTodo("");
        } catch (Exception e) {
            Assert.assertEquals("Empty description for TODO", e.getMessage());
        }
    }  */
}
