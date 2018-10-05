import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.List;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


public class TaskManager {

    protected static Scanner input = new Scanner(System.in);
    protected static List<Task> tasks = new ArrayList<>();
    protected static int taskCount = 0;

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

                    case "add":
                        addTask(line);
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
                }

            } catch (TaskManagerException e) {
                print("Error: " + e.getMessage());
            }
        } while (!toExit);

        toClose(input);
        print("Saving...");
        try {
            Files.copy(Paths.get("data/tasks.txt"), Paths.get("data_backup/tasks_bk.txt"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            print("Error copying file" + e.getMessage());
        } finally {
            print(System.lineSeparator() + "Bye!");
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
                writeToFile("data/tasks.txt");
            } else {
                print("Errors in input: Right Format = done TaskNo.");
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException err) {

            if (err instanceof ArrayIndexOutOfBoundsException) {
                print("Errors in input: No TaskNo. specified." + System.lineSeparator() + err.getMessage());
            } else {
                print("Error! Text entered instead of a valid TaskNo." + System.lineSeparator() + err.getMessage());
            }
        } catch (IndexOutOfBoundsException err) {
            print("Error! No such TaskNo. in records: Try again!" + System.lineSeparator() + err.getMessage());
        }
    }

    protected static void addTask(String line) throws TaskManagerException {
        String description = line.replace("add", "").trim();

        if (description.isEmpty()) {
            throw new TaskManagerException("Empty description for ADD");
        } else {
            tasks.add(new Task(description.replaceAll("\\s+", " ")));
            print("Tasks in the list: " + ++taskCount);
            writeToFile("data/tasks.txt");
        }
    }


    protected static void addTodo(String line) throws TaskManagerException {
        String description = line.substring("todo".length()).trim();

        if (description.isEmpty()) {
            throw new TaskManagerException("Empty description for TODO");
        }

        tasks.add(new Todo(description.replaceAll("\\s+", " ")));
        print("Tasks in the list: " + ++taskCount);
        writeToFile("data/tasks.txt");
    }

    protected static void addDeadline(String line) throws TaskManagerException {
        String description = line.substring("deadline".length()).trim();

        if (description.isEmpty()) {
            throw new TaskManagerException("Empty description for DEADLINE");
        }

        String[] deadlineInfo = line.substring("deadline".length()).trim().split(" /by ");
        try {
            tasks.add(new Deadline(deadlineInfo[0].replaceAll("\\s+", " "), deadlineInfo[1].replaceAll("\\s+", " ")));
            print("Tasks in the list: " + ++taskCount);
            writeToFile("data/tasks.txt");
        } catch (ArrayIndexOutOfBoundsException e) {
            print("Errors in input form encountered (e.g do include /by (without spacing) plus time, date, etc)!");
        }
    }

    private static void getTasksFromFile() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("data/tasks.txt"));
            String line = reader.readLine();

            while (line != null) {

                if (!(line.trim().isEmpty())) {
                    tasks.add(createTask(line));
                    ++taskCount;
                }

                line = reader.readLine();
            }

        } catch (IOException e) {
            print("Error accessing file object...file is not found!");
        } finally {
            toClose(reader);

            if (tasks != null && !tasks.isEmpty()) {  // remove nulls from ArrayList
                tasks.removeAll(Collections.singletonList(null));
            }
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
            taskCount--;
            return null;
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
        }finally{
            toClose(fw);
        }
    }
}
