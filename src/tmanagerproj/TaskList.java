package tmanagerproj;

import java.util.List;

class TaskList {
    private List<Task> array;

    TaskList(List<Task> array) {
        this.array = array;
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
    } // n: index in arrayList

    void removeItem(int n) {
        array.remove(n - 1);
    }  // n: number from displayed List_number
}
