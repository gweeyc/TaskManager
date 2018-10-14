import org.junit.Test;

import static org.junit.Assert.*;

public class DeadlineTest {

    @Test
    public void test_to_file_stringConversion() {
        TaskManager.tasks.add(new Deadline("remind Boss meeting", "Sat 4pm"));
        assertEquals("D : 0 : remind Boss meeting : Sat 4pm", TaskManager.tasks.get(0).toFileString());
    }
}