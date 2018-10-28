package tmanagerproj;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Will first initialize by creating a new ArrayList in memory, then loads a work file "/data/data.txt", containing a List
 * of previously saved tasks, into it. It does validate user CLI command inputs to add todo and deadline tasks into the records;
 * it also does task done status updates, deletion and edit & paginated display of the current tasks List; options also
 * exist that allow for specific task type to list only via the console CLI command mode.
 *
 * @author Gwee Yeu Chai
 * @version 5.9
 * @since 2018-08-28
 */

public class TaskManager {
    Storage storage;
    TaskList tasks;
    Ui ui;
    boolean flag = true;
    static int taskCount;
    static final int YEAR = 2018;
    static String description;
    static String arg0;
    static Map<Integer, Integer> map = new LinkedHashMap<>();  // map submenu List numbers to tasks' index

    /**
     * TaskManager constructor to read in the database file, create a Ui, Storage & TaskList obj.
     *
     * @param filePath database file's path.
     * @see TaskManagerException
     */
    protected TaskManager(String filePath) {   //constructor
        ui = new Ui();
        storage = new Storage(filePath);

        try {
            tasks = new TaskList(storage.load(filePath));
        } catch (TaskManagerException e) {
            ui.showToUser("");
            ui.showToUser("\u001b[33;1m" + "[Message]:" + "\u001b[0m");
            ui.printError(e.getMessage() + " ...trying other alternatives...");

            try {
                ui.showToUser("\033[1;92m" + "...Loading from a backup copy..." + "\033[0m");
                ui.showToUser("");
                ui.showToUser("\033[1;96m" + "Work will be saved to backup file data_backup/tasks._bk.txt for this session..." + "\033[0m");
                ui.showToUser("\033[1;92m" + "kindly Contact Administrator to re-instate work file path access after program exit." + "\033[0m" + System.lineSeparator());
                String tmp = storage.getBackupPath();
                tasks = new TaskList(storage.load(tmp));
                storage.setWorkFile(tmp);
            } catch (TaskManagerException err) {
                ui.printError(err.getMessage() + " ...trying other alternatives..." + System.lineSeparator());
                ui.showToUser("Starting with an empty task List...");
                tasks = new TaskList();
                assert tasks.toArray().isEmpty() : "Task List not empty";  //assert test1
                ui.userPrompt("Enter an alternative work file path, e.g. new.txt, for this session : ");
                String path = ui.readUserCommand();
                File file = new File(path);
                assert file.isDirectory() : "invalid file path input!"; //assert test2


                try {

                    if (file.createNewFile()) {
                        ui.showToUser("Temp work file: " + path + " successfully created!");
                        storage.setWorkFile(path);
                        ui.userPrompt("Enter an alternative backup file path, e.g. bk.txt : ");
                        path = ui.readUserCommand();
                        file = new File(path);

                        if (file.createNewFile()) {
                            ui.showToUser("Temp backup file: " + path + " successfully created!");
                            storage.setBackupPath(path);
                        } else {
                            ui.showToUser("File already exist");
                        }

                    } else {
                        ui.showToUser("File already exist");
                    }

                } catch (Exception ex) {
                    e.printStackTrace();
                    ui.showToUser("\033[1;95m" + "Please Contact Administrator!" + "\033[0m");
                }
            }
        }
    }

    protected TaskManager(){

    }

    void showCal(String line) throws TaskManagerException {
        description = Parser.getTaskDesc(line);

        if (description.isEmpty()) {
            throw new TaskManagerException("Empty description for CAL. Check Legend for Command Syntax.");
        } else {
            int n = 0;

            try {
                n = Integer.parseInt(description);

            } catch (NumberFormatException e) {
                ui.printError("TaskNo. input format error: " + e.getMessage());
            }

            ui.showToUser("\033[1;96m");
            assert n > 0 && n <= 12 : "Invalid month input";
            ui.calMonthDisplay(YEAR, n);

            ui.showToUser("\033[0m");
        }
    }

    /**
     * This method updates task done status in tasks List, freshens up the work file data format integrity and currency
     *
     * @param line takes in the scanned text string from user input
     * @throws TaskManagerException on missing task number that should follow the CLI "done" command
     */

    protected void updateTask(String line) throws TaskManagerException {
        description = Parser.getTaskDesc(line);

        if (description.isEmpty()) {
            throw new TaskManagerException("Empty description for DONE. Check Legend for Command Syntax.");
        } else {
            int n = 0;

            try {
                n = Integer.parseInt(description);

            } catch (NumberFormatException e) {
                ui.printError("TaskNo. Input Format Error <- " + e.getMessage());
            }

            int listSize = tasks.getSize();

            if (n > 0) {

                if (n <= listSize) {
                    tasks.getItem(n - 1).setDone(true);
                    ui.showToUser("Tasks in the list: " + taskCount);

                    flushToDisk(storage.getWorkFile());
                } else {
                    ui.printError("Error: TaskNo. exceeds total number in records of \"" + listSize + "\". Pl try again!" + System.lineSeparator());
                }

            } else {
                ui.printError("Error: TaskNo. value cannot be negative or 0 or a non-digit. Pl try again!" + System.lineSeparator());
            }
        }
    }

    protected void delTask(String line) throws TaskManagerException {
        description = Parser.getTaskDesc(line);

        if (description.isEmpty()) {
            throw new TaskManagerException("Empty description for MDEL. Check Legend for Command Syntax");
        } else {
            int n = 0;

            try {
                n = Integer.parseInt(description);

            } catch (NumberFormatException e) {
                ui.printError("TaskNo. input format error: " + e.getMessage());
            }

            int size = tasks.getSize();

            if (n > 0 && n <= size) {
                String holder = tasks.getItem(n - 1).toString();
                tasks.removeItem(n);
                taskCount--;
                ui.showToUser(System.lineSeparator() + "\033[1;95m" + "Message: " + "\033[0m" + "\u231C" + "\033[1;93m" + " @ " + "\033[0m" +
                        "\u231D --> Task " + "\033[1;95m" + holder + "\033[0m" + " has been successfully removed! --> \u231E"
                        + "\033[1;95m" + " :) " + "\033[0m" + "\u231F");
                ui.showToUser(System.lineSeparator() + "Tasks in the list: " + taskCount);
                flushToDisk(storage.getWorkFile());
            } else {
                ui.printError("TaskNo. value must be between 1 and " + size + " (total no. of records). Pl retry!!");
            }
        }
    }

    /**
     * This method checks for empty task description, ignores duplicates, inserts a new Todo Task into the tasks List
     *
     * @param line pass in the scanned text string from user input
     * @see TaskManagerException
     */

    protected void addTodo(String line) throws TaskManagerException {
        flag = false;
        description = Parser.getTaskDesc(line);

        if (description.isEmpty())
            throw new TaskManagerException("Empty description for TODO. Check Legend for Command Syntax.");
        else {
            if (!tasks.toArray().isEmpty()) {
                tasks.toArray().forEach((t) -> {    //exclude duplicates

                    if (t instanceof Todo && t.getDesc().equalsIgnoreCase(description)) {
                        flag = true;
                        ui.printError("Task: \"todo " + description + "\" already found in Register. Pl re-try!");
                    }
                });
            }

            if (!flag) {
                tasks.addTask(Parser.createTodo(description));
                ui.showToUser("Tasks in the list: " + ++taskCount);
                appendToFile();
            }
        }
    }

    protected void addDeadline(String line) throws TaskManagerException {
        flag = false;
        description = Parser.getTaskDesc(line);

        if (description.isEmpty()) {
            throw new TaskManagerException("Empty description for DEADLINE. Check Legend for Command Syntax.");
        } else if (!description.contains("/by")) {
            ui.printError("CLI Syntax Error! Deadline input must use a \" /by \" as a delimiter between two text strings! Pl re-enter!");
        } else {

            String[] part = description.split(" /by ");

            for (Task t : tasks.toArray()) {   //exclude duplicates

                if (t instanceof Deadline && t.getDesc().equalsIgnoreCase(part[0]) && ((Deadline) t).getBy().equalsIgnoreCase(part[1])) {
                    flag = true;
                    ui.printError("Task: \"todo " + description + "\"  already found in Register. Pl re-try!");
                }
            }

            if (!flag) {

                try {
                    tasks.addTask(Parser.createDeadline(part[0], part[1]));

                } catch (ArrayIndexOutOfBoundsException e) {
                    ui.printError("Errors in input encountered (Exact Format: deadline task_text /by ...other details)");
                }

                ui.showToUser("Tasks in the list: " + ++taskCount);
                appendToFile();
            }
        }
    }

    private void run() {
        ui.printWelcome();
        boolean toExit = false;
        String scanLine;

        do {

            ui.userPrompt("Your task? ");
            scanLine = ui.readUserCommand().trim();
            arg0 = Parser.getCommandWord(scanLine);

            try {

                switch (arg0) {

                    case "":
                    case "exit":
                        toExit = true;
                        break;

                    case "todo":
                        ui.printWelcome();
                        addTodo(scanLine);
                        ui.printTask(tasks);
                        break;

                    case "deadline":
                        ui.printWelcome();
                        addDeadline(scanLine);
                        ui.printTask(tasks);
                        break;

                    case "done":
                        ui.printWelcome();
                        updateTask(scanLine);
                        ui.printTask(tasks);
                        break;

                    case "del":
                        ui.printWelcome();
                        delTask(scanLine);
                        ui.printTask(tasks);
                        break;

                    case "tshow":
                        ui.printWelcome();
                        showTodo(tasks);
                        break;

                    case "tdel":
                        ui.printWelcome();
                        rmTodo(scanLine);
                        break;

                    case "tdone":
                        updateTodo(scanLine);
                        showTodo(tasks);
                        break;

                    case "dshow":
                        ui.printWelcome();
                        showDeadline(tasks);
                        break;

                    case "ddel":
                        ui.printWelcome();
                        rmDeadline(scanLine);
                        break;

                    case "ddone":
                        updateDeadline(scanLine);
                        showDeadline(tasks);
                        break;

                    case "fshow":
                        ui.printWelcome();
                        showDoneTasks(tasks);
                        break;

                    case "fdel":
                        ui.printWelcome();
                        rmDoneTask(scanLine);
                        break;

                    case "fa":
                        ui.printWelcome();
                        archiveDoneTasks(tasks);
                        ui.printTask(tasks);
                        flushToDisk(storage.getWorkFile());
                        break;

                    case "cal":
                        showCal(scanLine);
                        break;

                    case "print":
                    case "show":
                        ui.printWelcome();
                        ui.printTask(tasks);

                        if (flag) {// ensure data format & integrity in file with 1st print
                            flushToDisk(storage.getWorkFile());
                            flag = false;
                        }

                        break;

                    default:
                        ui.printError("Unknown command! please try again");
                        ui.printError("CLI Command to use (all lowercase only): See Main Menu Legend for the correct Commands Syntax");
                        ui.printWelcome();
                }

            } catch (TaskManagerException e) {
                ui.printError("Error: " + e.getMessage());
            }
        } while (!toExit);

        toClose(ui.getScanSource());
        ui.printShutDown();
        flushToDisk(storage.getBackupPath());
        ui.printBye();
    }

    void showDoneTasks(TaskList tasks) {
        map.clear();
        ui.showToUser(System.lineSeparator() + "\033[1;95m" + "[SubMenu]:" + "\033[0m" + " Done Tasks");
        ui.showToUser("----------");

        for (int i = 0, j = 1; i < taskCount; i++) {

            if (tasks.getItem(i).isDone) {
                map.put(j, i);
                ui.showToUser("[" + (j) + "] " + tasks.getItem(i));
                j++;

            }
        }

        if (map.isEmpty()) {
            ui.showToUser("Done Tasks List is currently empty!");
        }

    }

    private static int countTab = 0;

    protected void archiveDoneTasks(TaskList tasks) {
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            fw = new FileWriter(storage.getArchivePath(), true);
            bw = new BufferedWriter(fw);

            for (int i = 0; i < taskCount; i++) {

                if (tasks.getItem(i).isDone) {
                    bw.write("[" + countTab++ + "] " + tasks.getItem(i).toString() + System.lineSeparator());
                    tasks.removeItem(i + 1);
                    i--;
                    taskCount--;
                }
            }

            ui.showToUser("Archived all completed Tasks successfully!" + System.lineSeparator());
        } catch (IOException e) {
            ui.printError("File IO error! Contact Admin");
        } finally {

            toClose(bw);
            toClose(fw);
        }
    }

    protected void rmDoneTask(String line) throws TaskManagerException {
        description = Parser.getTaskDesc(line);

        if (description.isEmpty()) {
            throw new TaskManagerException("Empty description for FDEL. Check Legend for Command Syntax.");
        } else {
            int n = 0;

            try {
                n = Integer.parseInt(description);

            } catch (NumberFormatException e) {
                ui.printError("TaskNo. input format error: " + e.getMessage());
            }

            if (n <= 0 || n > map.size()) {
                ui.showToUser("List number is invalid. Pl re-try!");
            } else {

                try {
                    delTask("del " + (1 + map.get(n)));
                    int tab = map.size() - 1;
                    ui.showToUser("Done Tasks remaining in List: " + tab);

                    if (tab != 0) {
                        showDoneTasks(tasks);
                    }

                } catch (TaskManagerException e) {
                    ui.printError("Array access error");
                }
            }
        }
    }

    void showTodo(TaskList tasks) {
        map.clear();
        ui.showToUser(System.lineSeparator() + "\033[1;95m" + "[SubMenu]:" + "\033[0m" + " Todo Tasks");
        ui.showToUser("----------");

        for (int i = 0, j = 1; i < taskCount; i++) {

            if (!(tasks.getItem(i) instanceof Deadline)) {
                map.put(j, i);
                ui.showToUser("[" + (j) + "] " + tasks.getItem(i));
                j++;
            }
        }

        if (map.isEmpty()) {
            ui.showToUser("Todo Tasks List is currently empty!");
        }
    }

    protected void rmTodo(String line) throws TaskManagerException, NumberFormatException {
        description = Parser.getTaskDesc(line);

        if (description.isEmpty()) {
            throw new TaskManagerException("Empty description for TDEL. Check Legend for Command Syntax.");
        } else {
            int n = 0;

            try {
                n = Integer.parseInt(description);

            } catch (NumberFormatException e) {
                ui.printError("TaskNo. input format error: " + e.getMessage());
                throw e;
            }

            if (n <= 0 || n > map.size()) {
                ui.showToUser("List number is invalid. Pl re-try!");
            } else {

                try {
                    delTask("del " + (1 + map.get(n)));
                    int tab = map.size() - 1;
                    ui.showToUser("Todo Tasks remaining in List: " + tab);

                    if (tab != 0) {
                        showTodo(tasks);
                    }

                } catch (TaskManagerException e) {
                    ui.printError("Array access error");
                }
            }
        }
    }

    protected void updateTodo(String line) throws TaskManagerException {
        updateDeadline(line);
        ui.showToUser("Todo Tasks in List: " + map.size());

    }

    void showDeadline(TaskList tasks) {
        map.clear();
        ui.showToUser(System.lineSeparator() + "\033[1;95m" + "[SubMenu]:" + "\033[0m" + " Deadline Tasks");
        ui.showToUser("----------");

        for (int i = 0, j = 0; i < taskCount; i++) {
            if (tasks.getItem(i) instanceof Deadline) {
                map.put(++j, i);
                ui.showToUser("[" + (j) + "] " + tasks.getItem(i));
            }
        }
        if (map.isEmpty()) {
            ui.showToUser("Deadline Tasks List is currently empty!");
        }
    }

    protected void updateDeadline(String line) throws TaskManagerException {
        description = Parser.getTaskDesc(line);

        if (description.isEmpty()) {
            throw new TaskManagerException("Empty description for DDONE. Check Legend for Command Syntax.");
        } else {
            int n = 0;

            try {
                n = Integer.parseInt(description);

            } catch (NumberFormatException e) {
                ui.printError("TaskNo. input format error: " + e.getMessage());
            }

            if(n > 0 && n <= map.size()) {
                updateTask("done " + (map.get(n) + 1));
            }
        }
    }

    protected void rmDeadline(String line) throws TaskManagerException {
        description = Parser.getTaskDesc(line);

        if (description.isEmpty()) {
            throw new TaskManagerException("Empty description for DDEL. Check Legend for Command Syntax.");
        } else {
            int n = 0;

            try {
                n = Integer.parseInt(description);

            } catch (NumberFormatException e) {
                ui.printError("TaskNo. input format error: " + e.getMessage());
            }

            if (n <= 0 || n > map.size()) {
                ui.showToUser("List number is invalid. Pl re-try!");
            } else {

                try {
                    delTask("del " + (1 + map.get(n)));
                    int tab = map.size() - 1;
                    ui.showToUser("Deadline Tasks remaining in List: " + tab);

                    if (tab != 0) {
                        showDeadline(tasks);
                    }
                } catch (TaskManagerException e) {
                    ui.printError("Array access error");
                }
            }
        }
    }

    private void toClose(Closeable obj) {   //for possible closure technical glitch

        if (obj != null) {

            try {
                obj.close();

            } catch (IOException ex) {
                ui.printError("Possible disc error / file system full!" + ex.getMessage());
            }
        }
    }

    private void flushToDisk(String filePath) {
        try {
            storage.writeFile(tasks, filePath);    // preserve work file data format correctness & currency
        } catch (TaskManagerException e) {
            ui.printError(e.getMessage());
        }
    }

    private void appendToFile() {
        try {
            storage.appendFile(storage.getWorkFile(), taskCount - 1);
        } catch (TaskManagerException e) {
            ui.printError(e.getMessage());
        }
    }

    public static void main(String[] args) {
        new TaskManager("data/tasks.txt").run();

    }
}