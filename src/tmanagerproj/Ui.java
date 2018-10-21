package tmanagerproj;

import static tmanagerproj.TaskManager.taskCount;
import static java.lang.System.out;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        print("");
        print("               ||       *+++++++++++++++++++++++++++++++++++++       ||");
        print("                  ||--- |  Welcome to TaskManage Main Menu!  | ---|| ");
        print("               ||       *+++++++++++++++++++++++++++++++++++++       ||");
        print("");
        print("");
        print("Instructions for All CLI Commands Syntax Usage - (Use lowercase only):");
        print("= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =");
        print("                                        LEGEND:");
        print("                                      ++++++++++ ");
        print("-------------------------------------------------------------------------------------");
        print("[] todo a Task Syntax                         : todo text...");
        print("[] deadline a Task Syntax                     : deadline text... /by text...");
        print("[] update a Task Done Status                  : done List_Number");
        print("[] exit or quit                               : exit, or just press Enter Key");
        print("-------------------------------------------------------------------------------------");
        print("[] to display the whole Tasks List            : print");
        print("[] to display Todo Tasks SubMenu              : showt");
        print("[] to display Deadline Tasks SubMenu          : showd");
        print("[] to display Completed Tasks SubMenu         : showf");
        print("[] to display a Specific Month's Calendar     : cal Digit_No. (1 - 12 for Jan - Dec)");
        print("-------------------------------------------------------------------------------------");
        print("[] delete any task in TaskManager Menu        : rmm List_No.");
        print("[] delete a Todo task in Todo SubMenu         : rmt List_No.");
        print("[] delete a Deadline task in Deadline SubMenu : rmd List_No.");
        print("-------------------------------------------------------------------------------------");
        print("");
        calMonthDisplay(year, month);
        dayTimeDisplay();
    }

    private void dayTimeDisplay(){
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

        if(cal.isLeapYear(year)){
            print("The current year is a leap year" + System.lineSeparator());
        }else{
            print("The current year is not a leap year");
            print("");
        }

    }

    void calMonthDisplay(int year, int month){

         if (month > 1 && month < 12 || year > 1900 )
            printMonth(year, month);
        print("");
    }

    private void printMonth(int year, int month){
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
            case 1: monthName = "January"; break;
            case 2: monthName = "February"; break;
            case 3: monthName = "March"; break;
            case 4: monthName = "April"; break;
            case 5: monthName = "May"; break;
            case 6: monthName = "June"; break;
            case 7: monthName = "July"; break;
            case 8: monthName = "August"; break;
            case 9: monthName = "September"; break;
            case 10: monthName = "October"; break;
            case 11: monthName = "November"; break;
            case 12: monthName = "December";
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
            out.print("    ");
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
    private int getStartDay(int year, int month) {

        //Get total number of days since 1/1/1800
        int startDay1900 = 1;  // For Jan 1900 is 1 Mon, 1752 or 1800 is 3 = Wed) <- (weekday no. 0 - 6 = Sun - Sat)
        int totalNumberOfDays = getTotalNumberOfDays(year, month);

        //Return the start day
        return (totalNumberOfDays + startDay1900) % 7;
    }
    private int getTotalNumberOfDays(int year, int month) {   //total number of days between 1,1,1900 to present year and month
        LocalDate ref = LocalDate.of(1900, Month.JANUARY, 1);  //no time zone SG
        LocalDate now = ref.withYear(year).withMonth(month);
        return (int) ChronoUnit.DAYS.between(ref, now);
    }

    private int getNumberOfDaysInMonth(int year, int month) {
        YearMonth ref = YearMonth.of(year, month);
        return ref.lengthOfMonth();
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