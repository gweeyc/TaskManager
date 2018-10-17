package tmanagerproj;

import java.util.ArrayList;


    class TaskList {
    private ArrayList<Task> array;


    TaskList(ArrayList<Task> array){
        this.array = array;
    }
    TaskList(){

    }

    ArrayList<Task> toArray(){
        return array;
    }

    void addTask(Task t){
        array.add(t);

    }

    int getSize(){
        return array.size();
    }

    Task getItem(int n){
        return array.get(n);

    }

    void removeItem(int n){
        array.remove(n - 1);
    }

}
