package tmanagerproj;

public class Task {
    private String description;
    boolean isDone;

    Task(String s) {
        description = s;
    }

    void setDone(boolean b) {
        isDone = b;
    }

    String getDesc() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }   // description to print to console output

    public String toFileString() {
        return description;
    }   // description to write to file
}
