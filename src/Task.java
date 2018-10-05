public class Task {
    private String description;
    protected boolean isDone;

    protected Task(String s) {
        description = s;
    }

    protected void setDone(boolean b) {
        isDone = b;
    }

    @Override
    public String toString() {
        return description;
    }   // description to print to console output

    public String toFileString() {
        return description;
    }   // description to write to file

}
