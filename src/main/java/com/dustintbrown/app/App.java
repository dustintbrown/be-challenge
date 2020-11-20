package com.dustintbrown.app;

import com.dustintbrown.app.util.OIDService;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Collection;
import java.util.Scanner;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class App {
    private final static Logger LOGGER = Logger.getLogger(App.class.getName());
    private static OIDService oidService = new OIDService();
    public static void main(String[] args) {
        //
        // Setup Logger
        //
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream input = classLoader.getResourceAsStream("logger.properties");
            LogManager.getLogManager().readConfiguration(input);
        }catch(IOException e){
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }

        //
        // Show Menu
        //
        showMenu();




//        LOGGER.info("Total OIDs Loaded: " + oidService.getOids().size());
//        LOGGER.info("Matches Found: ");
//        matches.forEach(t -> LOGGER.info(oidService.parseOID(t)));
//        LOGGER.info(matches.toString());
//        oidService.populateWithRandomOIDs(1);
//        oidService.getOids().forEach(t -> LOGGER.info(oidService.parseOID(t)));
//        oidService.encode();
//        LOGGER.info("Total execution time: " + duration.toHoursPart() + "h " + duration.toMinutesPart() + "m " + duration.toSecondsPart() + "s " + duration.toMillisPart() + "ms");
    }

    private static void showMenu(){
        System.out.println("Choose from these choices");
        System.out.println("-------------------------");
        System.out.println("1 - Load OIDs from file");
        System.out.println("2 - Load random OIDs");
        System.out.println("3 - Write loaded OIDs to a file");
        System.out.println("4 - Print loaded OIDs to the console");
        System.out.println("5 - Check if OID exists in loaded data");
        System.out.println("6 - Display OIDs in data matching a prefix");
        System.out.println("7- Show diff between loaded OIDs and a given file");
        System.out.println("8 - Quit");

        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                //
                // Import OIDs from a file
                //
                if(oidService.getOids().size()>0){
                    System.out.println("WARNING: This will clear existing loaded OIDs");
                }
                try {
                    System.out.println("Enter a filename");
                    Scanner scanner1= new Scanner(System.in);
                    String filename = scanner1.nextLine();
                    oidService.importOIDsFromFile(filename);
                } catch (Exception e) {
                    LOGGER.severe(e.getMessage());
                    e.printStackTrace();
                }
                break;
            case 2:
                //
                // Load from random generation
                //
                if(oidService.getOids().size()>0){
                    System.out.println("WARNING: This will clear existing loaded OIDs");
                }
                System.out.println("How many OIDs would you like to generate?");
                Scanner scanner2= new Scanner(System.in);
                oidService.populateWithRandomOIDs(scanner2.nextInt());
                break;
            case 3:
                //
                // Write OIDS to file
                //
                Scanner scanner3= new Scanner(System.in);
                String filename = scanner3.nextLine();
                try {
                    oidService.populateWithRandomOIDs(10);
                    oidService.writeOIDsToFile(filename);
                } catch (Exception e) {
                    LOGGER.severe(e.getMessage());
                    e.printStackTrace();
                }
                break;
            case 4:
                //
                // Print OIDs
                //
                OIDService.printOIDs(oidService.getOids());
                break;
            case 5:
                //
                // Does OID exist
                //
                if(oidService.getOids().size()<1){
                    System.out.println("You must first load OIDs.");
                    break;
                }
                System.out.println("Enter OID to find");
                Scanner scanner5= new Scanner(System.in);
                String oid = scanner5.nextLine();
                System.out.println("Does " + oid + " appear in the set: " + oidService.exists(oid));
                break;
            case 6:
                //
                // Return all OIDs with matching prefix
                //
                System.out.println("Enter prefix to find");
                Scanner scanner6= new Scanner(System.in);
                String prefix = scanner6.nextLine();
                Collection<int[]> matches = oidService.getOidsWithPrefix(prefix);
                if(matches.size()>0){
                    System.out.println(matches.size() + " found. Would you like to output them to the console? (y/n)");
                    Scanner scanner6a= new Scanner(System.in);
                    if(scanner6a.nextLine().startsWith("y")){
                        OIDService.printOIDs(matches);
                    }
                }else{
                    System.out.println("No matches found.");
                }
                break;
            case 7:
                // Generate diff
                break;
            case 8:
                // Perform "quit" case.
                System.exit(0);
            default:
                // The user input an unexpected choice.
                showMenu();
        }
        System.out.println("");
        showMenu();
    }
}
