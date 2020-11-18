package com.dustintbrown.app;

import com.dustintbrown.app.util.OIDService;

import java.time.Duration;
import java.util.logging.Logger;

public class App {
//    private final static Logger LOGGER = Logger.getLogger(App.class.getName());

    private final static Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        OIDService oidService = new OIDService();
        final long startTime = System.currentTimeMillis();

        //
        // Print OIDS
        //

//        System.out.println(Arrays.toString(oids.toArray()));

        //
        // Write OIDS to file
        //

//        try {
//            oidService.populateWithRandomOIDs(10);
//            oidService.writeOIDsToFile("/Users/dustintbrown/Desktop/10_oids.txt");
//        } catch (Exception e) {
//            LOGGER.severe(e.getMessage());
//            e.printStackTrace();
//        }

        //
        // Import OIDs from a file
        //

        try {
            oidService.importOIDsFromFile("/Users/dustintbrown/Desktop/1-million_oids.txt");
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }

        //
        // Does 1.6.7.4.5.44626 exist in set?
        //

//        LOGGER.info("exists (6.5.4.9.5.14403.12885.19823.26314): " + oidService.exists("6.5.4.9.5.14403.12885.19823.26314"));

        //
        // Find Prefixes
        //

//        oidService.getOidsWithPrefix("9.9");
//        oidService.getOidsWithPrefix("1.2.3");
//        Collection<String> matches = oidService.getOidsWithPrefix("6.5.4.9");


        final long endTime = System.currentTimeMillis();
        Duration duration = Duration.ofMillis(endTime - startTime);

//        LOGGER.info("Total OIDs Loaded: " + oidService.getOids().size());
//        LOGGER.info("Matches Found: ");
//        matches.forEach(t -> LOGGER.info(oidService.parseOID(t)));
//        LOGGER.info(matches.toString());
//        oidService.populateWithRandomOIDs(1);
//        oidService.getOids().forEach(t -> LOGGER.info(oidService.parseOID(t)));
        oidService.encode();
        LOGGER.info("Total execution time: " + duration.toHoursPart() + "h " + duration.toMinutesPart() + "m " + duration.toSecondsPart() + "s " + duration.toMillisPart() + "ms");
    }
}
