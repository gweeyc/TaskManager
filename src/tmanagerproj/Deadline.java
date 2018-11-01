package tmanagerproj;

public class Deadline extends Todo {
    private String by;

    Deadline(String s, String by) {   //constructor
        super(s);
        this.by = by;
    }

    void setBy(String by) {
        this.by = by;
    }

    String getBy() {
        return by;
    }

    @Override
    public String toString() {
        return super.toString() + System.lineSeparator() + "    do by: " + by;
    }

    @Override
    public String toFileString() {
        return super.toFileString().replaceFirst("T", "D") + " : " + by;
    }
}

