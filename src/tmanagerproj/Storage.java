package tmanagerproj;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static tmanagerproj.TaskManager.taskCount;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

class Storage {
    private String workFilePath;
    private String backupPath;
    private String archivePath;

    Storage(String file) {                 //constructor
        workFilePath = file;
        backupPath = "data_backup/tasks_bk.txt";
        archivePath = "data_backup/archived.txt";
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

    String getArchivePath() {
        return archivePath;
    }

    void setWorkFile(String filePath) {
        workFilePath = filePath;
    }

    //load entire previously saved tasks List from the default work file to the TaskList object
    TaskList load(String file) throws TaskManagerException {
        TaskList tasks = new TaskList();
        String line;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            line = reader.readLine().trim();

            while (line != null) {

                if (!line.equals("")) {
                    tasks.addTask(createTask(line));
                    ++taskCount;
                }

                line = reader.readLine();
            }

        } catch (IOException e) {
            throw new TaskManagerException("Problems reading from work file path...");

        } finally {
            tasks.toArray().removeIf(Objects::isNull);

        }
        return tasks;
    }

    @Nullable
    private Task createTask(@NotNull String str) {
        String[] text = str.split(":");

        for (int i = 0; i < text.length; i++) {
            text[i] = text[i].trim();
        }

        switch (text[0]) {
            case "T":
                Task t = Parser.createTodo(text[2]);

                if (text[1].equals(("1"))) {
                    t.setDone(true);
                }

                return t;

            case "D":
                Task d = Parser.createDeadline(text[2], text[3]);

                if (text[1].equals(("1"))) {
                    d.setDone(true);
                }

                return d;

            default:
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

    void appendFile(TaskList tasks, String filePath, int index) throws TaskManagerException {

        try (FileWriter fw = new FileWriter(filePath, true)) {
            fw.write(tasks.getItem(index).toFileString() + System.lineSeparator());

        } catch (IOException err) {
            throw new TaskManagerException("File access has problems..." + err.getMessage());
        }
    }
}