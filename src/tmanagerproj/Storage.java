package tmanagerproj;

import static tmanagerproj.TaskManager.taskCount;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

class Storage {
    private String workFilePath;
    private String backupPath;
    private ArrayList<Task> tasks = new ArrayList<>();

    Storage(String file) {  //constructor
        workFilePath = file;
        backupPath = "data_backup/tasks_bk.txt";
    }

    String getBackupPath() {
        return backupPath;
    }
    void setBackupPath(String filePath) {
        backupPath = filePath;
    }

    String getWorkFile() {
        return workFilePath;
    }

    void setWorkFile(String filePath) {
        workFilePath = filePath;
    }

    ArrayList<Task> load(String file) throws TaskManagerException {
        //load from file
        String line = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            line = reader.readLine().trim();

            while (line != null) {

                if (!line.equals("")) {
                    tasks.add(createTask(line));
                    ++taskCount;
                }

                line = reader.readLine();
            }

        } catch (IOException e) {
            throw new TaskManagerException("");

        } finally {
            tasks.removeIf(Objects::isNull);

        }
        return tasks;
    }

    private Task createTask(String str) {
        String[] text = str.split(":");

        for (int i = 0; i < text.length; i++) {
            text[i] = text[i].trim();
        }

        if (text[0].equals("T")) {
            Task t = Parser.createTodo(text[2]);

            if (text[1].equals(("1"))) {
                t.setDone(true);
            }

            return t;

        } else if (text[0].equals("D")) {
            Task d = Parser.createDeadline(text[2], text[3]);

            if (text[1].equals(("1"))) {
                d.setDone(true);
            }

            return d;

        } else {
            taskCount--;  // to compensate for getTaskFromFile taskCount++
            return null;
        }
    }

    void writeFile(TaskList tasks, String filePath) throws TaskManagerException {

        try (FileWriter fw = new FileWriter(filePath)) {

            for (int i = 0; i < taskCount; i++) {
                fw.write(tasks.getItem(i).toFileString() + System.lineSeparator());
            }

        } catch (IOException err) {
            throw new TaskManagerException("File access has problems..." + err.getMessage());
        }
    }


    void appendFile(String filePath, int index) throws TaskManagerException {

        try (FileWriter fw = new FileWriter(filePath, true)) {
            fw.write(tasks.get(index).toFileString() + System.lineSeparator());

        } catch (IOException err) {
            throw new TaskManagerException("File access has problems..." + err.getMessage());
        }
    }

}