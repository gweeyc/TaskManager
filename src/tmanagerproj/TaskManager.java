package tmanagerproj;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static java.lang.System.out;

/**
 * <h1>TaskManager</h1>&emsp;&emsp;&emsp;&emsp;Is a command line program used to quickly create a personal todo or
 * deadline task list. It will first initialize by loading a full list of previously saved tasks, read in from a default
 * work file "/data/data.txt", into an in-memory Tasks ArrayList newly created for the user when the program starts, and
 * will continue to accept new user inputs from then on. It parses and validates user inputs to store new tasks into the
 * ArrayList, updates the done status of tasks, delete tasks, does resets of deadlines, removal and archiving of Done
 * tasks to file; a mode to do 10-lines pagination of the entire Tasks list is also available.
 *
 * <p>&emsp;&emsp;&emsp;&emsp;In addition, commands options exist that can display a Tasks list under a Main Menu, Todo
 * subMenu, Deadline subMenu or Done task subMenu, with an accompanying suite of specifically crafted commands that will
 * ensure continued proper command syntax and contextual usage under each specific view - for the user convenience; also
 * all possible errors caused by user command(s) mix-up will be blocked. Real-time verification and validation will be
 * carried out at runtime, to ensure error-free and non-corruption compliance throughout program use.
 *
 * <p>&emsp;&emsp;&emsp;&emsp;Emergencies situations have also been taken care of in the form of immediate user prompts
 * given the user - once the original default ones are no longer available for some technical reasons: to get the user
 * inputs for both work file and backup file paths for that exceptional session set up only. It is expected user will
 * arrange for everything correctly so that all the files will be created successfully. After the emergency session has
 * ended, measures should be taken to restore the application proper, or the Administrator should be contacted to restore
 * the TaskManager program to its original state as soon as possible, before the program's next run.
 *
 *
 * @author Gwee Yeu Chai
 * @version Level 12
 * @since 2018-08-28
 */

public class TaskManager {
    private Storage storage;
    private TaskList tasks;
    private Ui ui;

    private boolean flag = true;     // pure boolean flag use
    private static boolean isMainMenu;       // boolean to track which Menu User is currently using or in
    private static boolean isTodoMenu;
    private static boolean isDeadlineMenu;
    private static boolean isDoneMenu;
    static int taskCount;            // Store of the current total number of Tasks in in-memory TaskList
    private static String description;     //  Task description without the commandWord
    private static Map<Integer, Integer> map = new LinkedHashMap<>(); // Map subMenu List No. to task index in ArrayList
    private List<String> pageDisplay = new ArrayList<>();     // For Pagination Listing in-memory storage
    private static final int PAGESIZE = 10;
    private static final int YEAR = LocalDate.now().getYear();    // For Calender Display (current year use)

    /**
     * The TaskManager constructor reads in the default database text file, create an Ui Object <em>ui</em> ,
     * a Storage Object <em>storage</em> and a TaskList Object <em>tasks</em>.
     * If file read is unsuccessful, programmed emergency measures will activate next, and a live user session can still
     * go online for the User to work from, starting out with a provided empty Task List instead.
     *
     * @param filePath database file's path.
     * @see TaskManagerException
     */
    protected TaskManager(String filePath) {          // overload constructor
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
                ui.showToUser("\033[1;96m" + "Default backup path will be set to \"data_backup/tasks_bk.txt\""
                        + " if available....." + "\033[0m" + System.lineSeparator());
                ui.showToUser("\033[1;92m" + "Kindly Contact Administrator to re-instate work file path access"
                        + " after program exit." + "\033[0m" + System.lineSeparator());
                String tmp = storage.getBackupPath();
                tasks = storage.load(tmp);
                storage.setWorkFile(tmp);
            } catch (TaskManagerException err) {
                ui.printError("Problems reading the backup file path also...trying other alternatives..."
                        + System.lineSeparator());

                try {
                    ui.userPrompt("Enter the session's work file path, e.g. E:/temp/new.txt"
                            + " (or a relative path if applicable): ");
                    String newWorkFile = createFileAsPerUserInput();
                    storage.setWorkFile(newWorkFile);
                    ui.showToUser("...Setting up a work file " + newWorkFile + " for this session...successful!"
                            + System.lineSeparator());

                    ui.userPrompt("Enter the session's backup file path, e.g. C:/temp/new.txt"
                            + " (or a relative path if applicable): ");
                    String backupFile = createFileAsPerUserInput();
                    storage.setBackupPath(backupFile);
                    ui.showToUser("...Setting up a backup file " + backupFile + " for this session...successful!"
                            + System.lineSeparator());

                    ui.userPrompt("Enter the session's archived file path for Done tasks, e.g. E:/temp/archived.txt"
                            + " (or a relative path if applicable): ");
                    String archivedFile = createFileAsPerUserInput();
                    storage.setWorkFile(archivedFile);
                    ui.showToUser("...Setting up an archived file for Done tasks " + archivedFile + " for this session...successful!"
                            + System.lineSeparator());

                    tasks = new TaskList();
                    ui.showToUser("Starting with an empty Task List created for current session only...");

                    assert tasks.getSize() == 0 : "Task List not empty";  //assert statement

                } catch (IOException e1) {
                    ui.showToUser("");
                    e1.printStackTrace();
                    ui.showToUser("");
                    ui.showToUser("\033[1;95m" + "Please Contact your Administrator!" + "\033[0m");
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

        assert (n > 0 && n <= 12) : "Invalid month input";  // assert statement

        ui.calMonthDisplay(YEAR, n);
        ui.showToUser("\033[0m");
    }

    /**
     * TaskManager Main Menu Tasks Listing.
     *
     * @param tasks the TaskList object in memory.
     */
    private void printTask(TaskList tasks) {
        menuType(true, false, false, false);


        ui.showToUser("Tasks:");

        for (int i = 0; i < taskCount; i++) {
            ui.showToUser("[" + (i + 1) + "] " + tasks.getItem(i));
        }

    }

    // set maximum Page lines for each screen to display
    private List<String> getPageLines(List<String> source, int pageNo, int pageLines) {

        if (pageLines <= 0 || pageNo <= 0) {
            throw new IllegalArgumentException("invalid page size specified: " + pageLines);
        }

        int startIndex = (pageNo - 1) * pageLines;

        if (source == null || source.size() < startIndex) {
            return Collections.emptyList();
        }

        // toIndex exclusive
        return source.subList(startIndex, Math.min(startIndex + pageLines, source.size()));
    }

    // Next-page-display Pagination method for a text Console
    private void displayPagination() {
        int n = 1;
        List<String> temp;
        String cmd = "";
        pageDisplay.clear();    // temp storage for pagination display of items

        for (int i = 0; i < taskCount; i++) {
            pageDisplay.add("[" + (i + 1) + "] " + tasks.getItem(i));
        }

        assert !pageDisplay.isEmpty() : "There is no page to Display";   // assert statement

        do {
            temp = getPageLines(pageDisplay, n, PAGESIZE);
            ui.showToUser("");

            if (temp.isEmpty()) {
                ui.showToUser("==== *** End of Tasks List Reached! *** ====" + System.lineSeparator());
                break;
            } else {

                for (String s : temp) {
                    ui.showToUser(s);
                }
            }

            ui.userPrompt(System.lineSeparator() + "\033[1;32m"
                    + "Press ENTER for EDIT MODE. Enter N or n for NEXT PAGE: " + "\033[0m");
            cmd = ui.readUserCommand();

            if (cmd.equalsIgnoreCase("page"))
                break;

            if (cmd.equalsIgnoreCase("N")) {
                n++;
            } else {
                ui.showToUser(System.lineSeparator() + "* - Invalid Command entered! Pl retry again! - *"
                        + System.lineSeparator());
                ui.showToUser("===== [Exiting Pagination Mode] =====" + System.lineSeparator());
                break;
            }
        } while (!temp.isEmpty());

        if (cmd.equalsIgnoreCase("page"))
            displayPagination();
    }

    // work & backup files creation (emergency session)
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

    private void toClose(Closeable obj) {     //for possible closure technical glitch or error

        if (obj != null) {

            try {
                obj.close();

            } catch (IOException ex) {
                ui.printError("Possible disc error / file system full!" + ex.getMessage());
            }
        }
    }

    // to preserve work file data format correctness & currency
    private void flushToDisk(String filePath) {
        try {
            storage.writeFile(tasks, filePath);
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
     * This method checks for void task description, ignores duplicates, inserts a new Todo Task into tasks List.
     *
     * @param line pass in the scanned text string from user input.
     * @throws TaskManagerException on missing Todo task text description.
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

            assert description != null : "No Task description string set!";   // assert statement

            Task todo = Parser.createTodo(description);
            tasks.addTask(todo);
            ui.showToUser("Tasks in the list: " + ++taskCount);
            appendToFile();
        }
    }

    /**
     * This method checks for void task description, ignores duplicates, inserts a new Deadline Task into tasks List.
     *
     * @param line pass in the scanned text string from user input.
     * @throws TaskManagerException on missing Deadline task text description.
     * @see TaskManagerException
     */
    private void addDeadline(String line) throws TaskManagerException {
        flag = false;
        checkCommandSyntax(line);

        if (!description.contains("/by")) {
            ui.printError("CLI Syntax Error! Deadline input must use a \" /by \" as a delimiter"
                    + " between two text strings! Pl re-enter!");
        } else {

            String[] part = description.split(" /by ");

            for (Task t : tasks.toArray()) {                                  //exclude duplicates

                if (t instanceof Deadline && t.getDesc().equalsIgnoreCase(part[0])
                        && ((Deadline) t).getBy().equalsIgnoreCase(part[1])) {
                    flag = true;
                    ui.printError("Task: \"todo " + description + "\"  already found in Register. Pl re-try!");
                }
            }

            if (!flag) {

                assert part[0] != null : "No Task description string set!";   // assert statement
                assert part[1] != null : "No Task description string set!";   // assert statement

                try {
                    Task deadline = Parser.createDeadline(part[0], part[1]);
                    tasks.addTask(deadline);

                } catch (ArrayIndexOutOfBoundsException e) {
                    ui.printError("Input Errors encountered! (Exact Format: deadline task_text /by ...other details)");
                }

                ui.showToUser("Tasks in the list: " + ++taskCount);
                appendToFile();
            }
        }
    }

    /**
     * This optimized method updates the task done status in tasks List, freshens up the database work file data format:
     * ensures format integrity and data currency at the same time.
     *
     * @param line takes in the scanned text string from user input.
     * @throws TaskManagerException on missing task number that should follow the CLI "done" command.
     */

    private void updateTask(String line) throws TaskManagerException {
        checkCommandSyntax(line);
        int n = getListedNumber();
        int listSize = tasks.getSize();

        assert (listSize > 0) : "List has no element !";     // assert  statement

        if (n > 0) {

            if (n <= listSize) {
                tasks.getItem(n - 1).setDone(true);
                ui.showToUser("Tasks in the list: " + taskCount);

                flushToDisk(storage.getWorkFile());         // update the work file
            } else {
                ui.printError("Error: TaskNo. greater than the total number in records of \"" + listSize
                        + "\". Pl try again!" + System.lineSeparator());
            }

        } else {
            ui.printError("Error: TaskNo. value cannot be negative or 0 or a non-digit. Pl try again!"
                    + System.lineSeparator());
        }
    }

    /**
     * delTask deletes a task entree from a Main Menu Listing.
     *
     * @param line takes in the scanned text string from user input.
     * @throws TaskManagerException on missing task description (after the command's first word)
     * @see TaskManagerException
     */
    private void delTask(String line) throws TaskManagerException {
        checkCommandSyntax(line);
        int n = getListedNumber();
        int listSize = tasks.getSize();

        assert (listSize > 0) : "List has no element !";     // assert  statement

        if (n > 0 && n <= listSize) {
            String holder = tasks.getItem(n - 1).toString();
            tasks.removeItem(n);
            taskCount--;
            ui.showToUser(System.lineSeparator() + "\033[1;93m" + "Message:- " + "\033[0m"
                    + "\033[1;95m" + ":)" + "\033[0m" + "\033[1;31m" + " --> " + "\033[0m"
                    + "Task " + "\033[1;96m" + holder + "\033[0m" + " has been successfully removed! "
                    + "\033[1;31m" + " --> "
                    + "\033[0m" + "\033[1;95m" + ";)" + "\033[0m");

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ui.showToUser(System.lineSeparator() + "Tasks in the list: " + taskCount);
            flushToDisk(storage.getWorkFile());             // update the work file
        } else {
            ui.printError("TaskNo. value must be between 1 and " + listSize + " (= total no. of records). Pl retry!!");
        }

    }

    /**
     * This method resets a Deadline by value under Main Menu.
     *
     * @param line takes in the scanned text string from user input.
     * @throws TaskManagerException on missing task description (after the command's first word)
     * @see TaskManagerException
     */
    private void resetMainMenuBy(String line) throws TaskManagerException {
        checkCommandSyntax(line);
        int n = getListedNumber();

        if (n > 0 && n <= taskCount) {
            String newByDate = getNewByValue("Enter a new by : ");
            Task getTask = tasks.getItem(n - 1);
            resetSubMenuBy(newByDate, getTask);
        } else {
            ui.showToUser("List number is invalid. Pl re-try!");
        }
    }

    /**
     * Helper method to prompt for User input of a new By value to reset in the selected Deadline Task.
     *
     * @param s The prompt for a new Deadline by value.
     * @return The User input string containing the new Deadline by value to set.
     */
    private String getNewByValue(String s) {
        ui.userPrompt(System.lineSeparator() + s);
        return ui.readUserCommand();
    }

    /**
     * Helper method for resetting Deadline by value used in Main Menu and Deadline SubMenu.
     *
     * @param newByDate String stores the User new deadline schedule entry.
     * @param getTask   A Task object whose by value is to reset.
     */

    private void resetSubMenuBy(String newByDate, Task getTask) {   // helper function for resetting Deadline by value
        if (!(getTask instanceof Deadline)) {
            ui.showToUser("\033[1;96m" + "Sorry! You selected a Task That's NOT a Deadline Task!"
                    + "\033[0m" + System.lineSeparator());

            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } else {
            Deadline setTask = (Deadline) getTask;
            setTask.setBy(newByDate);
            flushToDisk(storage.getWorkFile());
        }
    }

    private boolean compareWithMany(String first, String... rest) {     // varargs used in this helper function
        for (String aRest : rest) {
            if (first.equals(aRest))
                return true;
        }
        return false;
    }

    /**
     * This method guards against CLI commands mix-up under the different menus views.
     *
     * @param arg0 first command word of user input.
     * @return boolean value to filter off the wrong commands and pass the right ones only.
     */
    private boolean check_userCliContext(String arg0) {   // optimize compareWithMany method efficiency

        if (isTodoMenu) {

            if (compareWithMany(arg0, "done", "ddone", "del", "ddel", "fdel", "reset", "dreset")) {
                ui.showToUser("Warning! Your command is not for Todo SubMenu use.");
                ui.showToUser("Pl re-enter: e.g. legend for Commands Legend");
                return true;
            }
        } else if (isMainMenu) {

            if (compareWithMany(arg0, "tdone", "ddone", "tdel", "ddel", "fdel", "dreset")) {
                ui.showToUser("Warning! This command is not for TaskManager Main SubMenu use.");
                ui.showToUser("Pl re-enter: e.g. legend for Commands Legend");
                return true;
            }
        } else if (isDeadlineMenu) {

            if (compareWithMany(arg0, "done", "tdone", "del", "tdel", "fdel", "reset")) {
                ui.showToUser("Warning! Your command is not for Deadline SubMenu use.");
                ui.showToUser("Pl re-enter: e.g. legend for Commands Legend");
                return true;
            }
        } else if (isDoneMenu) {

            if (compareWithMany(arg0, "done", "ddone", "tdone", "del", "tdel", "ddel", "reset", "dreset")) {
                ui.showToUser("Warning! Your command is not for Done SubMenu use.");
                ui.showToUser("Pl re-enter: e.g. legend for Commands Legend");
                return true;
            }
        }

        return false;
    }

    /**
     * This method is the Welcome Screen when program starts
     */
    private void runOnceCalTime() {
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        ui.printWelcome();
        out.print("\033[1;93m");
        ui.calMonthDisplay(year, month);
        out.print("\033[0m");
        out.print("\033[1;96m");
        ui.dayTimeDisplay();
        out.print("\033[0m");
    }

    /**
     * This method will track which Menu View the user is currently using or under
     *
     * @param main     the boolean value for the Main menu view
     * @param todo     the boolean value for the Todo menu view
     * @param deadline the boolean value for the Deadline menu view
     * @param done     the boolean value for the Done menu view
     */
    private void menuType(boolean main, boolean todo, boolean deadline, boolean done) {
        isMainMenu = main;
        isTodoMenu = todo;
        isDeadlineMenu = deadline;
        isDoneMenu = done;
    }

    /**
     * This method starts the main TaskManager Program with a Welcome Screen with the Commands Legend Menu, activates
     * the right operations as per the valid commands parsed and passed. Upon exit, a Bye screen will be displayed.
     * The entire Tasks List is flushed to disk and secured on a default backup file. All used resources are closed.
     */
    private void run() {
        menuType(true, false, false, false);
        runOnceCalTime();
        boolean toExit = false;
        String scanLine, arg0;

        do {

            ui.userPrompt("Your task? ");
            scanLine = ui.readUserCommand().trim().toLowerCase();
            arg0 = Parser.getCommandWord(scanLine);

            assert arg0 != null : "No First word command: null!";       // assert statement

            // guard against user possible Menu & SubMenu commands mix-ups
            if (check_userCliContext(arg0))
                continue;
            try {

                switch (arg0) {

                    case "":
                    case "exit":
                        toExit = true;
                        break;

                    case "page":
                        displayPagination();
                        break;

                    case "print":
                        ui.printWelcome();
                        printTask(tasks);

                        if (flag) {        // ensure data integrity & format written to work file, with 1st print
                            flushToDisk(storage.getWorkFile());
                            flag = false;
                        }

                        break;

                    case "legend":
                        ui.printWelcome();
                        break;

                    case "tshow":
                        ui.printWelcome();
                        showTodo(tasks);
                        break;

                    case "dshow":
                        ui.printWelcome();
                        showDeadline(tasks);
                        break;

                    case "fshow":
                        ui.printWelcome();
                        showDoneTasks(tasks);
                        break;

                    case "todo":
                        ui.printWelcome();
                        addTodo(scanLine);
                        printTask(tasks);
                        break;

                    case "deadline":
                        ui.printWelcome();
                        addDeadline(scanLine);
                        printTask(tasks);
                        break;

                    case "done":
                        ui.printWelcome();
                        updateTask(scanLine);
                        printTask(tasks);
                        break;

                    case "del":
                        ui.printWelcome();
                        delTask(scanLine);
                        printTask(tasks);
                        break;

                    case "reset":
                        resetMainMenuBy(scanLine);
                        printTask(tasks);
                        break;

                    case "tdel":
                        ui.printWelcome();
                        rmTodo(scanLine);
                        break;

                    case "tdone":
                        ui.printWelcome();
                        updateTodo(scanLine);
                        showTodo(tasks);
                        break;

                    case "ddel":
                        ui.printWelcome();
                        rmDeadline(scanLine);
                        break;

                    case "ddone":
                        ui.printWelcome();
                        updateDeadline(scanLine);
                        showDeadline(tasks);
                        break;

                    case "dreset":
                        modDeadlineBy(scanLine);
                        showDeadline(tasks);
                        break;

                    case "fdel":
                        rmDoneTask(scanLine);
                        break;

                    case "farchive":
                        ui.printWelcome();
                        archiveDoneTasks(tasks);
                        printTask(tasks);
                        flushToDisk(storage.getWorkFile());
                        break;

                    case "cal":
                        ui.printWelcome();
                        displayCal(scanLine);
                        break;

                    default:
                        ui.printError("Unknown command! please try again");
                        ui.printError("CLI Command to use (all lowercase only): Enter legend"
                                + " to check the Commands Syntax Legend, or print to list the Task records.");
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

    /**
     * This is a helper method to allow update to the task Done status of a task when under a Todo SubMenu or Deadline.
     * SubMenu View.
     *
     * @param line Stores the User input of CLI command and the task number.
     * @param s    contains the total number of Todo or Deadline or Done tasks in tasks List.
     * @throws TaskManagerException on missing task description (after the command's first word)
     */
    private void updateEntree(String line, String s) throws TaskManagerException {  // update done status in ArrayList
        checkCommandSyntax(line);
        int n = getListedNumber();

        assert (map.size()) > 0 : "map Set is empty!";  //assert statement

        if (n > 0 && n <= map.size()) {
            updateTask("done " + (map.get(n) + 1));
        } else {
            ui.showToUser("List number is invalid. Pl re-try!");
        }
        ui.showToUser(s + map.size());
    }

    /**
     * This method displays all the numbered Done tasks under a Done SubMenu View.
     *
     * @param tasks in-memory tasks List object.
     */
    private void showDoneTasks(TaskList tasks) {
        menuType(false, false, false, true);

        map.clear();
        ui.showToUser(System.lineSeparator() + "\033[1;95m" + "[SubMenu]:" + "\033[0m" + " Done Tasks");
        ui.showToUser("----------");

        assert (map.isEmpty()) : "map Set has unknown elements in it!";     // assert statement

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

    /**
     * This method is a housekeeping function that archives and removes all Done Tasks from the tasks List and writes
     * to disk: to the default backup file set by Admin.
     *
     * @param tasks the in-memory tasks List object.
     */
    private void archiveDoneTasks(TaskList tasks) {
        BufferedWriter bw = null;
        FileWriter fw = null;
        int countTab = 0;

        try {
            fw = new FileWriter(storage.getArchivePath(), true);
            bw = new BufferedWriter(fw);

            for (int i = 0; i < taskCount; i++) {

                if (tasks.getItem(i).isDone) {
                    bw.write("[" + ++countTab + "] " + tasks.getItem(i).toString() + System.lineSeparator());
                    tasks.removeItem(i + 1);
                    i--;
                    taskCount--;
                }
            }

            ui.showToUser("All completed Tasks archived successfully!" + System.lineSeparator());
        } catch (IOException e) {
            ui.printError("File IO error! Please Contact Admin!");
        } finally {

            toClose(bw);
            toClose(fw);
        }
    }

    /**
     * This method deletes a Done Task from the database when under a Done Task SubMenu.
     *
     * @param line contains the User input of CLI command and the task number.
     * @throws TaskManagerException on missing task description (after the command's first word)
     */
    void rmDoneTask(String line) throws TaskManagerException {
        checkCommandSyntax(line);
        int n = getListedNumber();

        assert (map.size()) > 0 : "map Set has no element in it!";       // assert statement

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

    /**
     * This method displays a tasks List under a Todo SubMenu View.
     *
     * @param tasks the in-memory Tasks List object.
     */
    private void showTodo(TaskList tasks) {
        menuType(false, true, false, false);

        map.clear();
        ui.showToUser(System.lineSeparator() + "\033[1;95m" + "[SubMenu]:" + "\033[0m" + " Todo Tasks");
        ui.showToUser("----------");

        assert (map.isEmpty()) : "map Set has unknown elements in it!";   //assert statement

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

    /**
     * This method deletes a Todo Task from the database under a Todo SubMenu.
     *
     * @param line Stores the User input of CLI command and the number of the task.
     * @throws TaskManagerException  on missing Todo task description.
     * @throws NumberFormatException on User text input instead of a task number.
     */
    private void rmTodo(String line) throws TaskManagerException, NumberFormatException {
        checkCommandSyntax(line);
        int n = getListedNumber();

        assert (map.size() > 0) : "map Set is empty!";  //assert statement

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

    /**
     * This method updates the task Done status of a Todo Task under a Todo SubMenu.
     *
     * @param line stores the User input of CLI command and the task number.
     * @throws TaskManagerException on missing task description (after the command's first word)
     */
    private void updateTodo(String line) throws TaskManagerException {
        updateEntree(line, "Todo Tasks in List: ");

    }

    /**
     * This method displays the Deadline SubMenu View listing.
     *
     * @param tasks the in-memory Tasks List.
     */
    private void showDeadline(TaskList tasks) {
        menuType(false, false, true, false);

        map.clear();
        ui.showToUser(System.lineSeparator() + "\033[1;95m" + "[SubMenu]:" + "\033[0m" + " Deadline Tasks");
        ui.showToUser("----------");

        assert (map.isEmpty()) : "map Set has unknown elements in it!";   //assert statement

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

    /**
     * This method updates the task Done status of a Deadline Task under a Deadline SubMenu.
     *
     * @param line stores the User input of CLI command and the task number.
     * @throws TaskManagerException on missing task description (after the command's first word)
     */
    private void updateDeadline(String line) throws TaskManagerException {
        updateEntree(line, "Deadline Tasks in List: ");
    }

    private void rmDeadline(String line) throws TaskManagerException {
        checkCommandSyntax(line);
        int n = getListedNumber();

        assert (map.size()) > 0 : "map Set is empty!";  //assert statement

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

    /**
     * This method changes a Deadline Task "by" value under a Deadline SubMenu View.
     *
     * @param line Stores the User input of CLI command and the task number.
     * @throws TaskManagerException on missing task description (after the command's first word)
     */
    private void modDeadlineBy(String line) throws TaskManagerException {
        checkCommandSyntax(line);
        int n = getListedNumber();

        assert (map.size()) > 0 : "map Set is empty!";  //assert statement

        if (n > 0 && n <= map.size()) {
            String newByDate = getNewByValue("Enter a new by: ");
            Task getTask = tasks.getItem(map.get(n));

            assert getTask instanceof Deadline : "User selected Task is NOT a Deadline Task";   // assert statement

            resetSubMenuBy(newByDate, getTask);
            ui.showToUser("Deadline Tasks in List: " + map.size());

        } else {
            ui.showToUser("List number is invalid. Pl re-try!");
        }
    }

    /**
     * The main function of TaskManager Class file.
     *
     * @param args Command line arguments for the main method.
     */
    public static void main(String[] args) {
        new TaskManager("data/tasks.txt").run();

    }
}