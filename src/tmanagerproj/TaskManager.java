package tmanagerproj;

import java.io.Closeable;
import java.io.IOException;

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
    private static String description = null;

    /**
     * TaskManager constructor to read in the database file, create a Ui, Storage & TaskList obj.
     *
     * @param filePath database file's path.
     * @see TaskManagerException
     */
    public TaskManager(String filePath) {   //constructor
        ui = new Ui();
        storage = new Storage(filePath);
        try {
            tasks = new TaskList(storage.load(filePath));
        } catch (TaskManagerException e) {
            ui.showToUser("Problem reading file...trying other alternatives...");
            try {
                ui.showToUser("Loading from a backup copy...");
                tasks = new TaskList(storage.load(storage.getBackupPath()));
            } catch (TaskManagerException err) {
                ui.showToUser("Problem reading from backup file encountered also...trying other alternatives...");
                ui.userPrompt("Pl specify another file path e.g. \"c:/data/filename.txt\" to load from [Press Enter to cancel option] : ");

                if (!ui.readUserCommand().equals("")) {
                    storage.setWorkFile(ui.readUserCommand());
                } else {
                    ui.showToUser("Starting with an empty task list.");
                    tasks = new TaskList();
                    ui.userPrompt("Pl enter your alternate work file path to use for this session [e.g. \"c:/data/filename.txt\"] ? : ");

                    if (!ui.readUserCommand().isEmpty()) {
                        storage.setWorkFile(ui.readUserCommand());
                        System.out.println(storage.getWorkFile());
                    }

                    ui.userPrompt("Pl enter your alternate back up file path to use for this session [e.g. \"c:/data/filename.txt\"] ? : ");

                    if (!ui.readUserCommand().equals("")) {
                        storage.setBackupPath(ui.readUserCommand());
                    }
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

    /**
     * This method updates task done status in tasks List, freshens up the work file data format integrity and currency
     *
     * @param line takes in the scanned text string from user input
     * @throws TaskManagerException on missing task number that should follow the CLI "done" command
     */

    private void updateTask(String line) throws TaskManagerException {
        description = Parser.getDesc(line);

        if (description.isEmpty()) {
            throw new TaskManagerException("Empty description for DONE");
        } else {
            int num = 0;

            try {
                num = Integer.parseInt(description.trim());

            } catch (NumberFormatException e) {
                ui.printError("TaskNo. Input Format Error <- " + e.getMessage());
            }

            int listSize = tasks.getSize();

            if (num > 0) {

                if (num <= listSize) {
                    tasks.getItem(num - 1).setDone(true);
                    ui.showToUser("Tasks in the list: " + taskCount);

                    flushToDisk(storage.getWorkFile());
                } else {
                    ui.printError("<Error>: TaskNo. exceeds total number in records of \"" + listSize + "\". Pl try again!" + System.lineSeparator());
                }

            } else {
                ui.printError("<Error>: TaskNo. value cannot be negative or 0 or a non-digit. Pl try again!" + System.lineSeparator());
            }
        }
    }

    private void delTask(String line) throws TaskManagerException {
        description = Parser.getDesc(line);
        int n = 0;
        if (description.isEmpty()) {
            throw new TaskManagerException("Empty description for DEL");
        } else {

            try {
                n = Integer.parseInt(description);

            } catch (NumberFormatException e) {
                ui.printError("TaskNo. input format error: " + e.getMessage());
            }
            int size = tasks.getSize();
            if (n > 0 && n <= size) {
                tasks.removeItem(n);
                taskCount--;
                ui.showToUser("Task record No. [" + n + "] successfully removed!");
                ui.showToUser("Tasks in the list: " + taskCount );
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

    private void addTodo(String line) throws TaskManagerException {
        flag = false;
        description = Parser.getDesc(line);

        if (description.isEmpty()) {
            throw new TaskManagerException("Empty description for TODO");
        } else {
            tasks.toArray().forEach((t) -> {    //exclude duplicates

                if (t instanceof Todo && t.getDesc().equalsIgnoreCase(description)) {
                    flag = true;
                    ui.printError("Task: << \"todo " + description + "\" >> already found in Register. Pl re-try!");
                }
            });

            if (!flag) {
                tasks.addTask(Parser.createTodo(description));
                ui.showToUser("Tasks in the list: " + ++taskCount);
                appendToFile();
            }
        }
    }

    private void appendToFile() {
        try {
            storage.appendFile(storage.getWorkFile(), taskCount - 1);
        } catch (TaskManagerException e) {
            ui.printError(e.getMessage());
        }
    }

    private void addDeadline(String line) throws TaskManagerException {
        flag = false;
        description = Parser.getDesc(line);

        if (description.isEmpty()) {
            throw new TaskManagerException("Empty description for DEADLINE");
        } else if (!description.contains("/by")) {
            ui.printError("CLI Syntax Error! Deadline input must use a \" /by \" as a delimiter between two text strings! Pl re-enter!");
        } else {

            String[] part = description.split(" /by ");

            for (Task t : tasks.toArray()) {   //exclude duplicates

                if (t instanceof Deadline && t.getDesc().equalsIgnoreCase(part[0]) && ((Deadline) t).getBy().equalsIgnoreCase(part[1])) {
                    flag = true;
                    ui.printError("Task: << \"todo " + description + "\"  >> already found in Register. Pl re-try!");
                }
            }

            if (!flag) {

                try {
                    //    tasks.add(new Deadline(part[0], part[1]));
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
        String arg0;
        String scanLine;

        do {
            ui.userPrompt("Your task? ");
            scanLine = ui.readUserCommand();
            arg0 = Parser.getCommandWord(scanLine);

            try {

                switch (arg0) {

                    case "":
                    case "exit":
                        toExit = true;
                        break;

                    case "todo":
                        addTodo(scanLine);
                        break;

                    case "deadline":
                        addDeadline(scanLine);
                        break;

                    case "done":
                        updateTask(scanLine);
                        break;
                    case "del": case "rm":
                        delTask(scanLine);
                        break;
                    case "print":
                        ui.printTask(tasks);

                        if (flag) {            // ensure data format & integrity in file with 1st print
                            flushToDisk(storage.getWorkFile());
                            flag = false;
                        }

                        break;

                    default:
                        ui.printError("Unknown command! please try again");
                        ui.printError("<< CLI Command to use - all lowercase only: e.g. print, todo, deadline, del, exit [or press Enter key], etc. >>");
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

    private void flushToDisk(String filePath) {
        try {
            storage.writeFile(tasks, filePath);    // preserve work file data format correctness & currency
        } catch (TaskManagerException e) {
            ui.printError(e.getMessage());
        }
    }

    public static void main(String[] args) {

        new TaskManager("/data/tasks.txt").run();

    }
}