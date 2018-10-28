package tmanagerproj;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class ParserTest {

        @Test
        public void getCommandWord(){
            assertEquals("todo", Parser.getCommandWord(" todo read book"));
            assertEquals("deadline", Parser.getCommandWord("deadline return book /by next Friday"));
            assertEquals("exit", Parser.getCommandWord("exit"));
            assertEquals("xyz", Parser.getCommandWord("   xyz   ")); // leading and trailing spaces

        }

        @Test
        public void createTodo(){
            Todo actual = (Todo) Parser.createTodo(Parser.getTaskDesc("todo read book"));
            Todo expected = new Todo("read book");
            assertEquals(expected.getDesc(), actual.getDesc());
        }

        @Test
        public void createDeadline(){
            String [] taskDescription = Parser.getTaskDesc("deadline join sports club /by June 6th").split(" /by ");
            Deadline actual = (Deadline) Parser.createDeadline(taskDescription[0], taskDescription[1]);
            Deadline expected = new Deadline("join sports club", "June 6th");
            assertEquals(expected.getDesc(), actual.getDesc());
            assertEquals(expected.getBy(), actual.getBy());
        }
}
