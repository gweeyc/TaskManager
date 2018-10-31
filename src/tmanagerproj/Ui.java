package tmanagerproj;

import static tmanagerproj.TaskManager.taskCount;
import static java.lang.System.out;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Scanner;


class Ui {
    private Scanner in;

    Ui() {
        in = new Scanner(System.in);
    }

    private void print(String s) {
        out.println(s);
    }

    Scanner getScanSource() {
        return in;
    }

    void printWelcome() {


        print("");
        print("\033[1;96m" + "               ||       *++++++++++++++++++++++++++++++++++++*       ||" + "\033[0m");
        print("                  " + "\033[1;91m" + "||--- " + "\033[0m" + "\033[0;105m" + "\033[1;97m" + "|  Welcome to TaskManager Main Menu! |" + "\033[0m" + "\033[1;91m" + " ---||" + "\033[0m");
        print("\033[1;96m" + "               ||       ++++++++++++++++++++++++++++++++++++++       ||" + "\033[0m");
        print("");
        out.print("\033[1;31m");
        print("Instructions for all CLI Commands Syntax Usage - (Use lowercase only):");
        print("= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =");
        out.print("\033[0m");
        out.print("\033[0;32m");
        print("                                        LEGEND:");
        print("                                      ++++++++++ ");
        print("-------------------------------------------------------------------------------------");
        print("[] add a todo Task in Main Menu                : todo text...");
        print("[] add a deadline Task in Main Menu            : deadline text... /by text...");
        print("[] update Task Done Status in Main Menu        : done List_Number");
        print("[] update Task Done Status in Deadline SubMenu : ddone List_Number");
        print("[] update Task Done Status in Todo SubMenu)    : tdone List_Number");
        print("[] exit or quit                                : exit, or just press Enter Key");
        print("-------------------------------------------------------------------------------------");
        print("[] display the whole Tasks List               : print or show");
        print("[] display Todo Tasks SubMenu                 : tshow");
        print("[] display Deadline Tasks SubMenu             : dshow");
        print("[] display Done Tasks SubMenu                 : fshow");
        print("[] display a Specific Month's Calendar        : cal Digit_No. (1 - 12 for Jan - Dec)");
        print("-------------------------------------------------------------------------------------");
        print("[] delete any task in TaskManager Main Menu   : del List_No.");
        print("[] delete a Todo task in Todo SubMenu         : tdel List_No.");
        print("[] delete a Deadline task in Deadline SubMenu : ddel List_No.");
        print("[] delete a Done task in Done SubMenu         : fdel List_No.");
        print("-------------------------------------------------------------------------------------");
        print("[] reset a Deadline by schedule in Main Menu        : reset List_Number");
        print("[] reset a Deadline by schedule in Deadline SubMenu : dreset List_Number");
        print("-------------------------------------------------------------------------------------");
        print("[] remove & archive all Done Tasks to file    : fa");
        print("-------------------------------------------------------------------------------------");
        print("\033[0m");
        print("[ NB ]: - All SubMenu commands must begin with the 1st. character of task type:");
        print("t for todo task e.g. tshow, d for deadline task e.g. ddel, f for done task e.g. fshow." + System.lineSeparator());


    }

    void dayTimeDisplay() {
        GregorianCalendar cal = new GregorianCalendar();
        int year;
        String months[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep",
                "Oct", "Nov", "Dec"};
        out.print("Date: ");
        out.print(months[cal.get(Calendar.MONTH)]);
        out.print(" " + cal.get(Calendar.DATE) + " ");
        out.println(year = cal.get(Calendar.YEAR));
        out.print("Time: " + cal.get(Calendar.HOUR) + ":" + cal.get(Calendar.MINUTE));
        print(":" + cal.get(Calendar.SECOND));

        if (cal.isLeapYear(year)) {
            print("The current year is a leap year" + System.lineSeparator());
        } else {
            print("The current year is not a leap year");
            print("");
        }

    }

    void calMonthDisplay(int year, int month) {

        if (month > 1 && month < 12 || year > 1900)
            printMonth(year, month);
        print("");
    }

    private void printMonth(int year, int month) {
        //Print the headings of the calendar
        printMonthTitle(year, month);

        //Print the body of the calendar
        printMonthBody(year, month);
    }

    private void printMonthTitle(int year, int month) {

        print("         " + getMonthName(month) + " " + year);
        print("=============================");
        print(" Sun Mon Tue Wed Thu Fri Sat");
        print("=============================");
    }

    private String getMonthName(int month) {
        String monthName = null;
        switch (month) {
            case 1:
                monthName = "January";
                break;
            case 2:
                monthName = "February";
                break;
            case 3:
                monthName = "March";
                break;
            case 4:
                monthName = "April";
                break;
            case 5:
                monthName = "May";
                break;
            case 6:
                monthName = "June";
                break;
            case 7:
                monthName = "July";
                break;
            case 8:
                monthName = "August";
                break;
            case 9:
                monthName = "September";
                break;
            case 10:
                monthName = "October";
                break;
            case 11:
                monthName = "November";
                break;
            case 12:
                monthName = "December";
        }
        return monthName;
    }

    private void printMonthBody(int year, int month) {

        // Get start day of the week for the first date in the month to pad spaces for
        int startDay = getStartDay(year, month);

        // Get number of days in the month
        int daysInMonth = getNumberOfDaysInMonth(year, month);

        // Pad space before the first day of the month
        int i = 0;
        for (i = 0; i < startDay; i++)
            out.print("    ");  // 4 spaces: e.g. _SUN_MON_TUE...
        for (i = 1; i <= daysInMonth; i++) {
            if (i < 10)
                out.print("   " + i);
            else
                out.print("  " + i);
            if ((i + startDay) % 7 == 0)
                print("");
        }
        print("");
        print("=============================");
    }

    private int getStartDay(int year, int month) {   //Get total number of days since 1,1,1900.

        // For Jan 1900 is startDay of the Week is 1 Mon (for 1752 or 1800, use 3 for Wed).
        int startDay1900 = 1;               //  Weekday 0 - 6 corresponds to Sun - Sat.
        int totalNumberOfDays = getTotalNumberOfDays(year, month);

        //Return the start day
        return (totalNumberOfDays + startDay1900) % 7;
    }

    private int getTotalNumberOfDays(int year, int month) {   //total number of days between 1,1,1900 to current year, month
        LocalDate ref = LocalDate.of(1900, Month.JANUARY, 1);  //no time zone in SG
        LocalDate now = ref.withYear(year).withMonth(month);
        return (int) ChronoUnit.DAYS.between(ref, now);
    }

    private int getNumberOfDaysInMonth(int year, int month) {
        YearMonth ref = YearMonth.of(year, month);
        return ref.lengthOfMonth();
    }

    void showToUser(String str) {
        print(str);
    }

    void userPrompt(String prompt) {
        out.print(prompt);
    }

    String readUserCommand() {

        return in.nextLine().trim();
    }

    void printError(String err) {
        showToUser("\u001B[31m" + err + "\u001B[0m");
    }

    void printTask(TaskList tasks) {
        showToUser("Tasks:");

        for (int i = 0; i < taskCount; i++) {
            showToUser("[" + (i + 1) + "] " + tasks.getItem(i));
        }

    }

    void printShutDown() {
        print(System.lineSeparator() + "You've chosen Exit :) -->");
        print("Saving...");
        print("-----");
        print("--");

    }

    void printBye() {
        print("-");
        print("GoodBye :):):)!");
    }
}
