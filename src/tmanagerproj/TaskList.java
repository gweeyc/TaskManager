package tmanagerproj;

import java.util.List;

class TaskList {
    private List<Task> array;

    TaskList(List<Task> array) {
        this.array = array;
    }

    TaskList() {

    }

    List<Task> toArray() {
        return array;
    }

    void addTask(Task t) {
        array.add(t);

    }

    int getSize() {
        return array.size();
    }

    Task getItem(int n) {
        return array.get(n);
    }

    void removeItem(int n) {
        array.remove(n - 1);
    }  // number from displayed List_number
}
