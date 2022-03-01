/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utility;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 *Simple Utility class for storing login attempt information
 * @author Shadab Mustafa
 */
public class Logger {
    private static final String FILENAME = "login_activity.txt";

    public Logger() {}

    /**
     * Records every log in attempt with a ISO timestamp, the attempted username, and whether or not the attempt succeeded.
     * @param LoginUserName , logged username in to the login_activity.txt file.
     * @param success records status of the login attempt
     */
    public static void logLoginAttempt(String LoginUserName, boolean success) {
        final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        final String time = formatter.format(OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS));

        try {
            final FileWriter fw = new FileWriter("login_activity.txt", true);
            final BufferedWriter bw = new BufferedWriter(fw);
            bw.write("time: " + time + "\t");
            bw.write("username: " + LoginUserName + "\t");
            bw.write("Status: " + (success ? " Success" : " Failure") + "\t");
            bw.newLine();
            bw.close();
        } catch (IOException ex) {
            System.out.println("Failed to log invalid login attempt:");
            System.out.println(ex.getMessage());
        }
    }
}