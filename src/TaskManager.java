import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.System.out;


public class TaskManager {

    private final static String FILE = "data/tasks.txt";
    private final static String FILE_BACKUP = "data_backup/tasks_bk.txt";
    private static boolean flag = true;
    protected static Scanner input = new Scanner(System.in);
    protected static List<Task> tasks = new ArrayList<>();
    protected static int taskCount;
    protected static  StringBuilder l = new StringBuilder(25);   // lesser generation of immutable strings called

    public static void main(String[] args) {
        getTasksFromFile();

        print("");
        print( "||     ------------------------------------     ||");
        print( "   ||    * Welcome to TaskManage-level! *    ||   ");
        print( "||     ------------------------------------     ||");


        print(System.lineSeparator() + "For todo Task enter: todo text...");
        print("For deadline Task enter: deadline text... /by text..." + System.lineSeparator());

        Boolean toExit = false;
        String arg0;

        do {
            l.setLength(0);   //clear buffer before next use
            out.print("Your task? ");
            l.insert(0, input.nextLine().trim());
            strBuilderTrim();

            if (l.length() == 0) {         //line parsed will never be null, at most ""
                arg0 = "";
            } else {

                if(l.indexOf(" ") > 0) {
                    arg0 = l.substring(0, l.indexOf(" "));  // get the first word
                }
                else {
                    arg0 = l.toString();    // allow for command print and exit
                }
            }

            try {

                switch (arg0) {

                    case "": case "exit":
                        toExit = true;
                        break;

                    case "todo":
                        addTodo(l.toString());
                        break;

                    case "deadline":
                        addDeadline(l.toString());
                        break;

                    case "done":
                        updateTask(l.toString());
                        break;

                    case "print":
                        printTask();

                        if (flag) {            // ensure data format & integrity in file with 1st print
                            writeToFile(FILE);
                            flag = false;
                        }

                        break;

                    default:
                        print("Unknown command! please try again");
                        print("Command starts with deadline or todo (lowercase only), a space, then text...!");
                }

            } catch (TaskManagerException e) {
                print("Error: " + e.getMessage());
            }
        } while (!toExit);

        toClose(input);
        print(System.lineSeparator() + "You've chosen Exit :) -->");
        print("Saving...");
        print("-----");
        print("--");
        writeToFile(FILE_BACKUP);  // make a copy of tasks file
        print("-");
        print("GoodBye :):):)!");
    }

    public static void toClose(Closeable obj) {   //for possible closure technical glitch

        if (obj != null) {

            try {
                obj.close();

            } catch (IOException ex) {
                print("Possible disc error / file system full!" + ex.getMessage());
            }
        }
    }

    protected static void print(String s) {
        System.out.println(s);
    }

    protected static void printTask() {
        print("Tasks:");

        for (int i = 0; i < taskCount; i++) {
            print("[" + (i + 1) + "] " + tasks.get(i));
        }
    }

    protected static void strBuilderTrim(){
        if(l.length() < 16 || l.length() > 25 && l.length() < 52 || l.length() > 52 && l.length() < 106){
            l.trimToSize();   // for limited RAM case
        }
    }

    protected static void updateTask(String line) {
        l.setLength(0);
        l.insert(0, line);
        String s;
        if(l.indexOf(" ") != -1) {
            l.delete(0, l.indexOf(" "));
            s = l.toString().trim();
        }
        else{
            s = l.toString();   // no need to generate new a string here: only "done" stored here
        }

        int num = 0;

        try {
            num = Integer.parseInt(s);
        }catch(NumberFormatException e){
            print("TaskNo.(omitted) = Null " + e.getMessage());
        }

        int listSize = tasks.size();

            if (num > 0 && num <= listSize) {

                tasks.get(num - 1).setDone(true);
                print("Tasks in the list: " + taskCount);
                writeToFile(FILE);    // efficiently preserve format and data integrity
            } else {
                if(num > listSize){
                    print("<Error3 : TaskNo. cannot be greater than " + listSize + " Pl try again!>" + System.lineSeparator());
                }
                else if(num <= 0){
                    print("<Error2 : TaskNo. cannot be omitted, need to be at least 1. Pl try again!>" + System.lineSeparator());
                }
                else {
                    print("<Error1 : Right Format = done TaskNo.>" + System.lineSeparator());
                }
            }
    }

    protected static void addTodo(String line) throws TaskManagerException {
        flag = false;
        l.setLength(0);
        l.insert(0, line);
        String description;
        if(l.indexOf(" ") != -1) {
            l.delete(0, l.indexOf(" "));
            description = l.toString().trim().replaceAll("\\s+", " ");
        }
        else{
            description = "";   // no need to generate new a string here: only "todo" entered here
        }

        strBuilderTrim();

        if (description.isEmpty()) {
            throw new TaskManagerException("Empty description for TODO");
        } else {

            tasks.forEach((t) -> {    //exclude duplicates

                if (t instanceof Todo && t.getDesc().equalsIgnoreCase(description)) {
                    flag = true;
                    print("Task: < \"todo " + description + "\" > already found in record. Pl re-enter!");
                }
            });

            if (!flag) {
                tasks.add(new Todo(description));
                print("Tasks in the list: " + ++taskCount);
                appendToFile(FILE, taskCount - 1);
            }
        }
    }

    protected static void addDeadline(String line) throws TaskManagerException {
        flag = false;
        l.setLength(0);
        l.insert(0, line);
        String description;
        if(l.indexOf(" ") != -1) {
            l.delete(0, l.indexOf(" "));
            description = l.toString().trim().replaceAll("\\s+", " ");
        }
        else{
            description = "";   // no need to generate new a string here: only "deadline" entered here
        }

        strBuilderTrim();

        if (description.isEmpty()) {
            throw new TaskManagerException("Empty description for DEADLINE");
        } else {
            String[] part = description.split(" /by ");
            for (Task t : tasks) {   //exclude duplicates

                if (t instanceof Deadline && t.getDesc().equalsIgnoreCase(part[0]) && ((Deadline) t).getBy().equalsIgnoreCase(part[1])) {
                    flag = true;
                    print("Task: < \"todo " + description + "\" > already found in record. Pl re-enter!");
                }
            }
            if (!flag) {

                try {
                    tasks.add(new Deadline(part[0], part[1]));

                } catch (ArrayIndexOutOfBoundsException e) {
                    print("Errors in input encountered (Exact Format: deadline task_text /by ...other details)");
                }

                print("Tasks in the list: " + ++taskCount);
                appendToFile(FILE, taskCount - 1);
            }
        }
    }

    private static void getTasksFromFile() {  //load from file
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(FILE));
            String line = reader.readLine();

            while (line != null) {

                if (!line.trim().equals("")) {
                    tasks.add(createTask(line.replace("\\s+", " ")));
                    ++taskCount;
                }

                line = reader.readLine();
            }

        } catch (IOException e) {
            print("Error accessing file object...Exit and contact Administrator!");

        } finally {
            toClose(reader);
            tasks.removeIf(Objects::isNull);
        }
    }

    private static Task createTask(String str) {
        String[] text = str.trim().split(":");

        for (int i = 0; i < text.length; i++) {
            text[i] = text[i].trim();
        }

        if (text[0].equals("T")) {
            Task t = new Todo(text[2]);

            if (text[1].equals(("1"))) {
                t.setDone(true);
            }

            return t;

        } else if (text[0].equals("D")) {
            Task d = new Deadline(text[2], text[3]);

            if (text[1].equals(("1"))) {
                d.setDone(true);
            }

            return d;

        } else {
            taskCount--;  // to compensate for getTaskFromFile taskCount++
            return null;
        }
    }

    private static void appendToFile(String filePath, int index) {
        FileWriter fw = null;

        try {

            fw = new FileWriter(filePath, true);
            fw.write(tasks.get(index).toFileString() + System.lineSeparator());

        } catch (IOException e) {
            print("File access has problems... " + e.getMessage());

        } finally {
            toClose(fw);
        }
    }

    private static void writeToFile(String filePath) {
        FileWriter fw = null;

        try {
            fw = new FileWriter(filePath);

            for (int i = 0; i < taskCount; i++) {
                fw.write(tasks.get(i).toFileString() + System.lineSeparator());
            }

        } catch (IOException e) {
            print("File access has problems... " + e.getMessage());

        } finally {
            toClose(fw);
        }
    }
}


