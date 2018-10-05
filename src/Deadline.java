public class Deadline extends Todo {
    private String by;

    protected Deadline(String s, String by) {
        super(s);
        this.by = by;
    }

    protected void setBy(String by) {
        this.by = by;
    }

    protected String getBy() {
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

