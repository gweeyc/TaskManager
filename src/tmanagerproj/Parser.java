package tmanagerproj;

class Parser {
    private static StringBuilder l = new StringBuilder(25);

    private static void strBuilderTrim() {
        if (l.length() > 25 && l.length() < 52 || l.length() > 52 && l.length() < 106) {
            l.trimToSize();   // for limited RAM case
        }
    }

    // get the first word of User command line input
    static String getCommandWord(String fullCommand) {
        l.setLength(0);
        String firstWord = null;
        String strTrim = fullCommand.trim();
        l.insert(0, strTrim);
        strBuilderTrim();

        if (l.indexOf(" ") > 0) {
            firstWord = l.substring(0, l.indexOf(" "));
            return firstWord;
        } else {
            return strTrim;    // allow for single command print, exit, etc
        }
    }

    // The task description minus the first CLI command word
    static String getTaskDesc(String str) {
        l.setLength(0);
        l.insert(0, str);
        strBuilderTrim();

        if (l.indexOf(" ") > 0) {
            l.delete(0, l.indexOf(" "));
            return l.toString().trim().replaceAll("\\s+", " ");
        } else {
            return "";
        }

    }

    static Task createTodo(String line) {
        return new Todo(line);
    }

    static Task createDeadline(String desc, String by) {
        return new Deadline(desc, by);
    }
}






