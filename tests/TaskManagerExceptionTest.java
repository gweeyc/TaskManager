import org.junit.Test;
import static org.junit.Assert.*;

public class TaskManagerExceptionTest {
    @Test
    public void test_TaskManagerExceptionThrown() {
        try {
            TaskManager.addTodo("");
        } catch (Exception e) {
            assertEquals("Empty description for TODO", e.getMessage());
        }
    }
}