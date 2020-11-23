package com.dustintbrown.app;

import com.dustintbrown.app.util.OIDService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.logging.Level;
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
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }

        //
        // Show Menu
        //
        showMenu();
    }

    private static void showMenu() {
        System.out.println("-------------------------");
        System.out.println(" Choose an OID operation ");
        System.out.println("-------------------------");
        System.out.println("1 - Load OIDs from file");
        System.out.println("2 - Load random OIDs");
        System.out.println("3 - Write loaded OIDs to a file");
        System.out.println("4 - Print loaded OIDs to the console");
        System.out.println("5 - Check if OID exists in loaded data");
        System.out.println("6 - Find OIDs in data matching a prefix");
        System.out.println("7 - Encode loaded OIDs");
        System.out.println("8 - Show diff between loaded OIDs and a given file");
        System.out.println("9 - Quit");

        Scanner scanner = new Scanner(System.in);
        try {
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    //
                    // Import OIDs from a file
                    //
                    if (oidService.getOids().size() > 0) {
                        System.out.println("WARNING: This will clear existing loaded OIDs");
                    }
                    try {
                        System.out.println("Enter a filename");
                        Scanner scanner1 = new Scanner(System.in);
                        String filename = scanner1.nextLine();
                        oidService.importOIDsFromFile(filename);
                    } catch (FileNotFoundException e) {
                        System.out.println("The specified file was not found.");
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                    }
                    break;
                case 2:
                    //
                    // Load from random generation
                    //
                    if (oidService.getOids().size() > 0) {
                        System.out.println("WARNING: This will clear existing loaded OIDs");
                    }
                    System.out.println("How many OIDs would you like to generate?");
                    Scanner scanner2 = new Scanner(System.in);
                    oidService.populateWithRandomOIDs(scanner2.nextInt());
                    break;
                case 3:
                    //
                    // Write OIDS to file
                    //
                    Scanner scanner3 = new Scanner(System.in);
                    try {
                        String filename = scanner3.nextLine();
                        oidService.populateWithRandomOIDs(10);
                        oidService.writeOIDsToFile(filename);
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
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
                    if (oidService.getOids().size() < 1) {
                        System.out.println("You must first load OIDs.");
                        break;
                    }
                    System.out.println("Enter OID to find");
                    Scanner scanner5 = new Scanner(System.in);
                    String oid = scanner5.nextLine();
                    System.out.println("Does " + oid + " appear in the set: " + oidService.exists(oid));
                    break;
                case 6:
                    //
                    // Return all OIDs with matching prefix
                    //
                    System.out.println("Enter prefix to find");
                    Scanner scanner6 = new Scanner(System.in);
                    String prefix = scanner6.nextLine();
                    Collection<int[]> matches = oidService.getOidsWithPrefix(prefix);
                    if (matches.size() > 0) {
                        System.out.println(matches.size() + " matches found for '" + prefix + "'. Would you like to output them to the console? (y/n)");
                        Scanner scanner6a = new Scanner(System.in);
                        if (scanner6a.nextLine().startsWith("y")) {
                            OIDService.printOIDs(matches);
                        }
                    } else {
                        System.out.println("No matches found.");
                    }
                    break;
                case 7:
                    //
                    // Encode Loaded OIDs
                    //
                    Collection<byte[]> encoded = oidService.encode();
                    System.out.println(encoded.size() + "Would you like to output them to the console in Hex? (y/n)");
                    Scanner scanner6a = new Scanner(System.in);
                    if (scanner6a.nextLine().startsWith("y")) {
                        OIDService.printEncodedOIDsInHex(encoded);
                    }
                    break;
                case 8:
                    //
                    // Generate diff
                    //
                    System.out.println("Enter filename A to load and diff");
                    Scanner scanner7 = new Scanner(System.in);

                    try {
                        String filenameA = scanner7.nextLine();
                        System.out.println("Enter filename B to load and diff");
                        String filenameB = scanner7.nextLine();


                        oidService.diffFiles(filenameA, filenameB).forEach(System.out::println);
                    } catch (FileNotFoundException e) {
                        System.out.println("The specified file was not found.");
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                    }

                    break;
                case 9:
                    // Perform "quit" case.
                    System.exit(0);
                default:
                    // The user input an unexpected choice.
                    System.out.println("Invalid choice. Please try again.");
                    showMenu();
            }
        } catch(InputMismatchException ime){
            System.out.println("Invalid choice. Please try again.");
        }
        System.out.println("");
        showMenu();
    }
}
