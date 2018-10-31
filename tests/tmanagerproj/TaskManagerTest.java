package tmanagerproj;

import org.junit.Test;

import static org.junit.Assert.*;

public class TaskManagerTest {
    private TaskManager objTest =  new TaskManager();

    @Test
    public void showCal_exceptionThrown_test(){
        try {
            objTest.displayCal("cal ");
            fail();
        }catch(TaskManagerException e){
            assertEquals("Empty description for CAL. Check Legend for Command Syntax.", e.getMessage());

        }
    }

    @Test
    public void checkCommandSyntax_exceptionThrown_test() {

        try {
            objTest.checkCommandSyntax("fdel");
        } catch (TaskManagerException e) {
            assertEquals("Empty description for " + Parser.getCommandWord("fdel").toUpperCase() +
                    ". Enter print to check Legend for Command Syntax", e.getMessage());
        }

    }

    @Test
    public void rmDoneTask_taskManagerExceptionThrown_test(){
        try {
            objTest.rmDoneTask("fdel");
            fail();
        }catch(TaskManagerException err){
            assertEquals("Empty description for FDEL. Check Legend for Command Syntax.", err.getMessage());
        }
    }
}