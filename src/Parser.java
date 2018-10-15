

public class Parser {
    private static StringBuilder l = new StringBuilder(25);

    public static void strBuilderTrim() {
        if (l.length() > 25 && l.length() < 52 || l.length() > 52 && l.length() < 106) {
            l.trimToSize();   // for limited RAM case
        }
    }

    public static String getCommandWord(String fullCommand) {
        l.setLength(0);
        l.insert(0, fullCommand);
        strBuilderTrim();

        if (l.length() == 0) {         //line parsed will never be null, at most ""
            return "";
        } else {

            if (l.indexOf(" ") > 0) {
                return l.substring(0, l.indexOf(" "));  // get the first word
            } else {
                return fullCommand;    // allow for single command print, exit, etc
            }
        }
    }

    public static String getDesc(String str) {
        l.setLength(0);
        l.insert(0, str);
        strBuilderTrim();

        if (l.indexOf(" ") != -1) {
            l.delete(0, l.indexOf(" "));
            return l.toString().trim().replaceAll("\\s+", " ");
        }else{
            return "";
        }

    }
}






