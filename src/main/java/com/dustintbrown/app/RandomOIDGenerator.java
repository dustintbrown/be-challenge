package com.dustintbrown.app;

import com.dustintbrown.app.util.OIDService;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class RandomOIDGenerator {

    private final static Logger LOGGER = Logger.getLogger(RandomOIDGenerator.class.getName());

    public static void main(String[] args){
        if(args==null || args.length!=2){
            System.out.println("Invalid Arguments. Must specify number to create and output filename");
            System.out.println("Usage: RandomOIDGenerator 1000 ./1000-oids.txt");
            System.exit(1);
        }

        OIDService oidService = new OIDService();

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

        final int num = Integer.parseInt(args[0]);
        final String filename = args[1];

        try {
            oidService.populateWithRandomOIDs(num);
            oidService.writeOIDsToFile(filename);
        }catch (IOException e){
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            e.printStackTrace();
        }
    }
}
