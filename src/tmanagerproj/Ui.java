package tmanagerproj;
import static tmanagerproj.TaskManager.taskCount;
import static java.lang.System.out;

import java.util.Scanner;


    class Ui {
    private Scanner in;


    Ui(){
        in = new Scanner(System.in);
    }

    private void print(String s) {
        out.println(s);
    }

    Scanner getScanSource(){
        return in;
    }

    void printWelcome(){
        print("");
        print("||     ------------------------------------     ||");
        print("   ||    * Welcome to TaskManage-level! *    ||   ");
        print("||     ------------------------------------     ||");

        print(System.lineSeparator() + "Instructions for CLI Usage: -");
        print("= = = = = = = = = = = = = =");
        print("");
        print("[i] For todo Task, enter: todo text...");
        print("[ii] For deadline Task, enter: deadline text... /by text...");
        print("[iii] For deletion of a task, enter: del or rm, followed by a TaskNo." + System.lineSeparator());
    }

    void showToUser(String str){
        print(str);
    }
    void userPrompt(String prompt){
        out.print(prompt);
    }

    String readUserCommand(){

        return in.nextLine().trim();
    }



    void printError(String err){
        showToUser("\u001B[31m" +  err + "\u001B[0m");
    }


    void printTask(TaskList tasks) {
        showToUser("Tasks:");

        for (int i = 0; i < taskCount; i++) {
            showToUser("[" + (i + 1) + "] " + tasks.getItem(i));
        }
    }

    void printShutDown(){
        print(System.lineSeparator() + "You've chosen Exit :) -->");
        print("Saving...");
        print("-----");
        print("--");

    }

    void printBye(){
        print("-");
        print("GoodBye :):):)!");
    }
}
