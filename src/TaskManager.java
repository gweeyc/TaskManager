import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


public class TaskManager {

    private final static String FILE = "data/tasks.txt";
    private final static String FILE_BACKUP = "data_backup/tasks_bk.txt";
    protected static Scanner input = new Scanner(System.in);
    protected static List<Task> tasks = new ArrayList<>();
    protected static int taskCount;

    public static void main(String[] args) {
        getTasksFromFile();

        print("Welcome to TaskManage-level1!");

        Boolean toExit = false;
        String line, arg0;

        do {
            System.out.print("Your task? ");
            line = input.nextLine();

            if (line.trim().isEmpty()) {         //line parsed will never be null, at most ""
                arg0 = "";
            } else {
                arg0 = line.split("\\s+")[0];  // get the first word
            }

            try {

                switch (arg0) {

                    case "":
                    case "exit":
                        toExit = true;
                        break;

                    case "todo":
                        addTodo(line);
                        break;

                    case "deadline":
                        addDeadline(line);
                        break;

                    case "done":
                        updateTask(line);
                        break;

                    case "print":
                        printTask();
                        break;

                    default:
                        print("Unknown command! please try again");
                        print("Command begins with deadline or todo (lowercase only)!");
                }

            } catch (TaskManagerException e) {
                print("Error: " + e.getMessage());
            }
        } while (!toExit);

        toClose(input);
        print("You've chosen Exit :) -->");
        print("Saving...");
        writeToFile(FILE);

        try {
            Files.copy(Paths.get(FILE), Paths.get(FILE_BACKUP), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            print("Error copying file" + e.getMessage());
        } finally {
            print(System.lineSeparator() + "..GoodBye :):)!");
        }
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

    protected static void updateTask(String line) {
        String[] text = line.trim().split("\\s+");
        try {

            if (text.length <= 2 && Integer.parseInt(text[1].trim()) > 0) {
                int num = Integer.parseInt(text[1]);
                tasks.get(num - 1).setDone(true);
                print("Tasks in the list: " + taskCount);
                writeToFile(FILE);
            } else {
                print("Errors in input: Right Format = done TaskNo.");
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException err) {

            if (err instanceof ArrayIndexOutOfBoundsException) {
                print("Errors in input: Please enter a Task No." + System.lineSeparator() + err.getMessage());
            } else {
                print("Error! Text entered instead of a valid TaskNo." + System.lineSeparator() + err.getMessage());
            }
        } catch (IndexOutOfBoundsException err) {
            print("Error! Task No. not found in record: Try again!" + System.lineSeparator() + err.getMessage());
        }
    }

    protected static void addTodo(String line) throws TaskManagerException {
        String description = line.substring("todo".length()).trim().replaceAll("\\s+", " ");

        if (description.isEmpty()) {
            throw new TaskManagerException("Empty description for TODO");
        }

        tasks.add(new Todo(description));
        print("Tasks in the list: " + ++taskCount);
        appendToFile(FILE, taskCount - 1);
    }

    protected static void addDeadline(String line) throws TaskManagerException {
        String description = line.substring("deadline".length()).trim().replaceAll("\\s+", " ");

        if (description.isEmpty()) {
            throw new TaskManagerException("Empty description for DEADLINE");
        }

        String[] deadlineInfo = line.substring("deadline".length()).trim().split(" /by ");
        try {
            tasks.add(new Deadline(deadlineInfo[0], deadlineInfo[1]));
            print("Tasks in the list: " + ++taskCount);
            appendToFile(FILE, taskCount - 1);
        } catch (ArrayIndexOutOfBoundsException e) {
            print("Errors in input encountered (Exact Format: deadline task_text /by ...other details)");
        }
    }

    private static void getTasksFromFile() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(FILE));
            String line = reader.readLine();

            while (line != null) {

                if (!line.trim().equals("")){
                    tasks.add(createTask(line.replace("\\s+", " ")));
                    ++taskCount;
                }

                line = reader.readLine();
            }

        } catch (IOException e) {
            print("Error accessing file object...file is not found!");
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
            print("File access has encountered problems..." + e.getMessage());
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
            print("File access has encountered problems..." + e.getMessage());
        } finally {
            toClose(fw);
        }
    }
}
