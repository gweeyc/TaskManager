package tmanagerproj;

public class Todo extends Task {

    Todo(String s) {
        super(s);
    }   // constructor

    @Override
    public String toString() {
        return "description: " + super.toString() + System.lineSeparator() + "    is done? " + (isDone ? "Yes" : "No");
    }

    @Override
    public String toFileString() {
        return "T" + " : " + (isDone ? "1" : "0") + " : " + super.toFileString();
    }
}
