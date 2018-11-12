package tmanagerproj;

import org.junit.Test;

import static org.junit.Assert.*;

public class TaskManagerExceptionTest {
    private TaskManager testObj =  new TaskManager();

    @Test
    public void test_addTodo(){

        try{
            testObj.addTodo("todo");

        }catch (Exception e) {
            assertEquals("Empty description for TODO. Please see Legend for the right Command Syntax", e.getMessage());
        }
    }
}