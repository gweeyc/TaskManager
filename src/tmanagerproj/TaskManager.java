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
    private Storage storage;
    private TaskList tasks;
    private Ui ui;
    private boolean flag = true;
    static int taskCount;
    private static final int YEAR = 2018;
    private static String description;
    private static Map<Integer, Integer> map = new LinkedHashMap<>();  // map submenu List numbers to tasks' index

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
            tasks = storage.load(filePath);
        } catch (TaskManagerException e) {
            ui.showToUser("");
            ui.showToUser("\033[4;33m" + "[ MESSAGE ]:-" + "\033[0m");
            ui.printError(e.getMessage() + "trying other alternatives...");

            try {
                ui.showToUser("\033[1;92m" + "..Loading from a backup copy.." + "\033[0m");
                ui.showToUser("\033[1;96m" + "Default backup path will be set to \"data_backup/tasks_bk.txt\" if available....."
                        + "\033[0m" + System.lineSeparator());
                ui.showToUser("\033[1;92m" + "Kindly Contact Administrator to re-instate work file path access after program exit."
                        + "\033[0m" + System.lineSeparator());
                String tmp = storage.getBackupPath();
                tasks = storage.load(tmp);
                storage.setWorkFile(tmp);
            } catch (TaskManagerException err) {
                ui.printError("Problems reading the backup file path also...trying other alternatives..." + System.lineSeparator());

                try {
                    ui.userPrompt("Enter a work file path for this session, e.g. new.txt (without a drive letter) : ");
                    String newWorkFile = createFileAsPerUserInput();
                    storage.setWorkFile(newWorkFile);
                    storage.setBackupPath("bak.txt");
                    ui.showToUser("...Setting up a backup file \"bak.txt\" for this session...successful!" + System.lineSeparator());
                    ui.showToUser("Starting with an empty Task List created for current session only...");
                    tasks = new TaskList();

                    assert tasks.getSize() == 0 : "Task List not empty";  //assert statement 01

                } catch (IOException e1) {
                    ui.showToUser( "");
                    e1.printStackTrace();
                    ui.showToUser("");
                    ui.showToUser( "\033[1;95m" + "Please Contact Administrator!" + "\033[0m");
                    System.exit(-99);
                }
            }
        }
    }

    protected TaskManager() {  // default constructor

    }

    void displayCal(String line) throws TaskManagerException {
        checkCommandSyntax(line);
        int n = getListedNumber();
        ui.showToUser("\033[1;96m");

        assert (n > 0 && n <= 12) : "Invalid month input";  // assert statement 02

        ui.calMonthDisplay(YEAR, n);
        ui.showToUser("\033[0m");
    }

    private String createFileAsPerUserInput() throws IOException {
        String path = ui.readUserCommand();
        File file = new File(path);
        boolean isFileNew = file.createNewFile();

        if (!isFileNew) {
            ui.showToUser("File already exist!");
        }

        return path;
    }

    void checkCommandSyntax(String line) throws TaskManagerException {
        description = Parser.getTaskDesc(line);

        if (description.isEmpty()) {
            throw new TaskManagerException("Empty description for " + Parser.getCommandWord(line).toUpperCase() +
                    ". Enter print to check Legend for Command Syntax");
        }
    }

    private int getListedNumber() {
        int n = 0;

        try {
            n = Integer.parseInt(description);

        } catch (NumberFormatException e) {
            ui.printError("Input Format Error -> Task No. entered is not a digit number!");
        }

        return n;
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
            storage.appendFile(tasks, storage.getWorkFile(), taskCount - 1);
        } catch (TaskManagerException e) {
            ui.printError(e.getMessage());
        }
    }

    /**
     * This method updates task done status in tasks List, freshens up the work file data format integrity and currency
     *
     * @param line takes in the scanned text string from user input
     * @throws TaskManagerException on missing task number that should follow the CLI "done" command
     */

    private void updateTask(String line) throws TaskManagerException {
        checkCommandSyntax(line);
        int n = getListedNumber();
        int listSize = tasks.getSize();

        assert (listSize > 0) : "List has no element !";     // assert  statement 05

        if (n > 0) {

            if (n <= listSize) {
                tasks.getItem(n - 1).setDone(true);
                ui.showToUser("Tasks in the list: " + taskCount);

                flushToDisk(storage.getWorkFile());  // update the work file
            } else {
                ui.printError("Error: TaskNo. greater than the total number in records of \"" + listSize + "\". Pl try again!" + System.lineSeparator());
            }

        } else {
            ui.printError("Error: TaskNo. value cannot be negative or 0 or a non-digit. Pl try again!" + System.lineSeparator());
        }
    }

    private void delTask(String line) throws TaskManagerException {
        checkCommandSyntax(line);
        int n = getListedNumber();
        int listSize = tasks.getSize();

        assert (listSize > 0) : "List has no element !";     // assert  statement 06

        if (n > 0 && n <= listSize) {
            String holder = tasks.getItem(n - 1).toString();
            tasks.removeItem(n);
            taskCount--;
            ui.showToUser(System.lineSeparator() + "\033[1;93m" + "Message:- " + "\033[0m"
                    + "\033[1;95m" + ":)" + "\033[0m" + "\033[1;31m" + " --> " + "\033[0m"
                    + "Task " + "\033[1;96m" + holder + "\033[0m" + " has been successfully removed! " + "\033[1;31m" + " --> "
                    + "\033[0m" + "\033[1;95m" + ";)" + "\033[0m");
            ui.showToUser(System.lineSeparator() + "Tasks in the list: " + taskCount);
            flushToDisk(storage.getWorkFile());   // update the work file
        } else {
            ui.printError("TaskNo. value must be between 1 and " + listSize + " (= total no. of records). Pl retry!!");
        }

    }

    /**
     * This method checks for empty task description, ignores duplicates, inserts a new Todo Task into the tasks List
     *
     * @param line pass in the scanned text string from user input
     * @see TaskManagerException
     */

    void addTodo(String line) throws TaskManagerException {
        flag = false;
        checkCommandSyntax(line);

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

    private void addDeadline(String line) throws TaskManagerException {
        flag = false;
        checkCommandSyntax(line);

        if (!description.contains("/by")) {
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

    private void resetByDate(String line) throws TaskManagerException {
        checkCommandSyntax(line);
        int n = getListedNumber();

        if (n > 0 && n <= taskCount) {
            ui.userPrompt(System.lineSeparator() + "Enter a new by : ");
            String newByDate = ui.readUserCommand();
            Task getTask = tasks.getItem(n - 1);
            Deadline setTask = (Deadline) getTask;
            setTask.setBy(newByDate);
            flushToDisk(storage.getWorkFile());
        } else {
            ui.showToUser("List number is invalid. Pl re-try!");
        }
    }

    private void run() {
        ui.printWelcome();
        boolean toExit = false;
        String scanLine, arg0;

        do {

            ui.userPrompt("Your task? ");
            scanLine = ui.readUserCommand().trim().toLowerCase();
            arg0 = Parser.getCommandWord(scanLine);

            if (arg0.equals("todo") || arg0.equals("deadline") || arg0.equals("done")    // minimize possible user commands mix-up errors under various menus scenarios
                    || arg0.equals("del") || arg0.equals("reset"))
                arg0 = "m".concat(arg0);

            try {

                switch (arg0) {

                    case "":
                    case "exit":
                        toExit = true;
                        break;

                    case "mtodo":
                        ui.printWelcome();
                        addTodo(scanLine);
                        ui.printTask(tasks);
                        break;

                    case "mdeadline":
                        ui.printWelcome();
                        addDeadline(scanLine);
                        ui.printTask(tasks);
                        break;

                    case "mdone":
                        ui.printWelcome();
                        updateTask(scanLine);
                        ui.printTask(tasks);
                        break;

                    case "mdel":
                        ui.printWelcome();
                        delTask(scanLine);
                        ui.printTask(tasks);
                        break;

                    case "mreset":
                        resetByDate(scanLine);
                        ui.printTask(tasks);
                        break;

                    case "tshow":
                        ui.printWelcome();
                        showTodo(tasks);
                        break;

                    case "tdel":
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
                        rmDoneTask(scanLine);
                        break;

                    case "fa":
                        ui.printWelcome();
                        archiveDoneTasks(tasks);
                        ui.printTask(tasks);
                        flushToDisk(storage.getWorkFile());
                        break;

                    case "cal":
                        displayCal(scanLine);
                        break;

                    case "dreset":
                        modDeadlineBy(scanLine);
                        showDeadline(tasks);
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
                        ui.printError("CLI Command to use (all lowercase only): Enter print or show to see Commands Syntax Legend.");
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

    private void traceLocAndUpdate(String line, String s) throws TaskManagerException {  // for done status updates in ArrayList
        checkCommandSyntax(line);
        int n = getListedNumber();

        assert (map.size()) > 0 : "map Set is empty!";  //assert statement 13

        if (n > 0 && n <= map.size()) {
            updateTask("done " + (map.get(n) + 1));
        } else {
            ui.showToUser("List number is invalid. Pl re-try!");
        }
        ui.showToUser(s + map.size());
    }

    private void showDoneTasks(TaskList tasks) {
        map.clear();
        ui.showToUser(System.lineSeparator() + "\033[1;95m" + "[SubMenu]:" + "\033[0m" + " Done Tasks");
        ui.showToUser("----------");

        assert (map.isEmpty()) : "map Set has unknown elements in it!";     // assert statement 07

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

    private void archiveDoneTasks(TaskList tasks) {
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
            ui.printError("File IO error! Please Contact Admin!");
        } finally {

            toClose(bw);
            toClose(fw);
        }
    }

    void rmDoneTask(String line) throws TaskManagerException {
        checkCommandSyntax(line);
        int n = getListedNumber();

        assert (map.size()) > 0 : "map Set has no element in it!";       // assert statement 08

        if (n <= 0 || n > map.size()) {
            ui.showToUser("List number is invalid. Pl re-try!");
        } else {

            try {
                delTask("del " + (1 + map.get(n)));
                int tab = map.size() - 1;
                ui.showToUser("Done Tasks remaining in List: " + tab);
                showDoneTasks(tasks);

            } catch (TaskManagerException e) {
                ui.printError("Array access error");
            }
        }
    }

    private void showTodo(TaskList tasks) {
        map.clear();
        ui.showToUser(System.lineSeparator() + "\033[1;95m" + "[SubMenu]:" + "\033[0m" + " Todo Tasks");
        ui.showToUser("----------");

        assert (map.isEmpty()) : "map Set has unknown elements in it!";   //assert statement 09

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

    private void rmTodo(String line) throws TaskManagerException, NumberFormatException {
        checkCommandSyntax(line);
        int n = getListedNumber();

        assert (map.size() > 0) : "map Set is empty!";  //assert statement 10

        if (n <= 0 || n > map.size()) {
            ui.showToUser("List number is invalid. Pl re-try!");
        } else {

            try {
                delTask("del " + (1 + map.get(n)));
                int tab = map.size() - 1;
                ui.showToUser("Todo Tasks remaining in List: " + tab);
                showTodo(tasks);

            } catch (TaskManagerException e) {
                ui.printError("Array access error");
            }
        }
    }

    private void updateTodo(String line) throws TaskManagerException {
        traceLocAndUpdate(line, "Todo Tasks in List: ");

    }

    private void showDeadline(TaskList tasks) {
        map.clear();
        ui.showToUser(System.lineSeparator() + "\033[1;95m" + "[SubMenu]:" + "\033[0m" + " Deadline Tasks");
        ui.showToUser("----------");

        assert (map.isEmpty()) : "map Set has unknown elements in it!";   //assert statement 12

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

    private void modDeadlineBy(String line) throws TaskManagerException {
        checkCommandSyntax(line);
        int n = getListedNumber();

        assert (map.size()) > 0 : "map Set is empty!";  //assert statement 13

        if (n > 0 && n <= map.size()) {
            ui.userPrompt(System.lineSeparator() + "Enter a new by: ");
            String newByDate = ui.readUserCommand();
            Task getTask = tasks.getItem(map.get(n));
            Deadline setTask = (Deadline) getTask;
            setTask.setBy(newByDate);
            ui.showToUser("Deadline Tasks in List: " + map.size());
            flushToDisk(storage.getWorkFile());

        } else {
            ui.showToUser("List number is invalid. Pl re-try!");
        }
    }

    private void updateDeadline(String line) throws TaskManagerException {
        traceLocAndUpdate(line, "Deadline Tasks in List: ");
    }

    private void rmDeadline(String line) throws TaskManagerException {
        checkCommandSyntax(line);
        int n = getListedNumber();

        assert (map.size()) > 0 : "map Set is empty!";  //assert statement 14

        if (n <= 0 || n > map.size()) {
            ui.showToUser("List number is invalid. Pl re-try!");
        } else {

            try {
                delTask("del " + (1 + map.get(n)));
                int tab = map.size() - 1;
                ui.showToUser("Deadline Tasks remaining in List: " + tab);
                showDeadline(tasks);

            } catch (TaskManagerException e) {
                ui.printError("Array access error");
            }
        }
    }

    public static void main(String[] args) {
        new TaskManager("data/tasks.txt").run();

    }
}