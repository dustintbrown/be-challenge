package com.dustintbrown.app.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Service to manage OIDs in an internal TreeSet
 *
 * @author  Dustin Brown
 */
public class OIDService {
    private static SplittableRandom rand = new SplittableRandom();
    private final static Logger LOGGER = Logger.getLogger(OIDService.class.getName());

    private TreeSet<int[]> oids = new TreeSet<>(new OIDComp());

    public TreeSet<int[]> getOids() {
        return oids;
    }

    /**
     * Set internal collection to given OIDs as Strings
     *
     * @param collection
     */
    public void populateWithOIDs(Collection<String> collection){
        oids.clear();
        collection.forEach(oid ->{
            oids.add(parseOIDString(oid));
        });
    }

    /**
     *
     * @param num
     */
    public void populateWithRandomOIDs(int num) {
        long startTime = System.currentTimeMillis();
        LOGGER.log(Level.INFO, "Starting generation of {0} oids.", num);
        oids.clear();
        while (oids.size() < num) {
            oids.add(generateSingleOID());
        }
        long endTme = System.currentTimeMillis();
        LOGGER.log(Level.INFO, "Finished generation of oids in {0}ms.",(endTme-startTime));
    }

    /**
     * generates a single OID between the length 2 and 10 32-bit integers. Each segment of the OID is
     * stored in an int[].
     *
     * @return                  int array containing 2-10 items representing an OID
     */
    private int[] generateSingleOID() {
        // First 2 sections are 0-15
        // 3-10 total sections
        // each section after  is a random number from 0-Integer.MAX_VALUE
        int length = rand.nextInt(8) + 2;
        int[] returnMe = new int[length];
        for (int i = 0; i < length; i++) {
            if (i < 2) {
                returnMe[i] = (rand.nextInt(15));
            } else if(i==length-1) {
                //last item should have a larger scope to be more realistic
                returnMe[i] = generateBiasedPositiveInt(65000,4096);
            }else{
                //middle numbers should be biased toward 0 give the way these numbers appear in the wild.
                returnMe[i] = generateBiasedPositiveInt(255 * i,128);
            }
        }
        return returnMe;
    }

    /**
     * Returns an int below the max value that is biased toward lower numbers.
     *
     * @param max               the largest value returned
     * @param bias_threshold    the int to base the threshold calculation
     * @return                  int less than max biased toward lower numbers
     */
    private int generateBiasedPositiveInt(int max, int bias_threshold){
        if(max<0){
            return 0;
        }else if(bias_threshold > max){
            bias_threshold = max;
        }
        int cnt = 1;
        int next = 0;
        while(cnt<6){ //basically a poor man's weighting toward numbers lower than start_max
            next = rand.nextInt(max);
            if(next<(bias_threshold*cnt)){
                break;
            }
            cnt++;
        }
        return next;
    }

    /**
     * Helper function to print the collection of OIDs represented as int[] to the console
     *
     * @param oidsToPrint       Collection of int[] represented OIDs
     */
    public static void printOIDs(Collection<int[]> oidsToPrint){
        oidsToPrint.forEach(oid -> {
            System.out.println(OIDService.parseOID(oid));
        });
    }

    /**
     * Imports the given file name to the oids data structure in this class. File should contain
     * one OID per line represented as x.y.z.a.b.c where each segment between the periods is a 32-bit integer.
     *
     * @param filename          text filename representing a location on the local disk
     * @throws IOException      throws an exception if there are issues with the given file
     */
    public void importOIDsFromFile(String filename) throws IOException {
        loadOIDsFromFile(oids, filename, false);
    }

    /**
     * Reads a given file name and inserts each OID into the given collection as either String or int[].
     * File should contain one OID per line represented as x.y.z.a.b.c where each segment between the periods
     * is a 32-bit integer.
     *
     * @param c                 collection to add found OIDs from the file
     * @param filename          local file to read OIDs from
     * @param asString          if true the OIDs are added as a String. Otherwise they are added as int[]
     * @throws IOException      throws an exception if there are issues with the given file
     */
    private void loadOIDsFromFile(Collection c, String filename, boolean asString) throws IOException{
        LOGGER.log(Level.INFO, "Importing OIDs from file: {0}", filename);
        final long startTime = System.currentTimeMillis();
        int record_count = 0;
        try (final BufferedReader myReader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8))) {
            String line;
            while ((line = myReader.readLine()) != null) {
                try {
                    if(asString){
                        c.add(line);
                    }else {
                        c.add(parseOIDString(line));
                    }
                    record_count++;
                    if (record_count % 1000000 == 0) {
                        LOGGER.log(Level.FINE, "Read {0} records.", record_count);
                    }
                } catch (Exception e){
                    // Fails if a line can't be read
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        }
        final long endTime = System.currentTimeMillis();
        Object[] params = {c.size(), endTime - startTime};
        LOGGER.log(Level.INFO, "Read {0} OIDs from file. Operation completed in {1} milliseconds.", params);
    }

    /**
     * Loads OIDs from two different files and compares them. Files should contain
     * one OID per line represented as x.y.z.a.b.c where each segment between the periods is a 32-bit integer.
     * Filename1 is considered the base file and the diff results are returned as a +/- to that collection.
     *
     * @param filename1         local file to read OIDs from
     * @param filename2         local file to read OIDs from
     * @return                  Collection of Strings representing the diff
     * @throws IOException      throws an exception if there are issues with the given file
     */
    public Collection<String> diffFiles(String filename1, String filename2) throws IOException {
        ArrayList<String> returnMe = new ArrayList<>();
        HashSet<String> setA = new HashSet<>();
        HashSet<String> setB = new HashSet<>();
        loadOIDsFromFile(setA, filename1,true);
        loadOIDsFromFile(setB, filename2,true);

        return diffCollections(setA, setB);
    }

    /**
     * Loads OIDs from two different collections and compares them. Collections should contain
     * OIDs represented as x.y.z.a.b.c Strings where each segment between the periods is a 32-bit integer.
     * CollectionA is considered the base data and the diff results are returned as a +/- to that collection.
     *
     * @param collectionA
     * @param collectionB
     * @return
     */
    public Collection<String> diffCollections(Collection<String> collectionA, Collection<String> collectionB) {
        ArrayList<String> returnMe = new ArrayList<>();
        collectionA.forEach(oid ->{
            if(collectionB.contains(oid)){
                returnMe.add(oid);
            }else{
                returnMe.add("- " + oid);
            }
        });
        collectionB.removeAll(collectionA); //leaves collectionB with only additions
        collectionB.forEach(oid -> returnMe.add( "+ " + oid));
        return returnMe;
    }

    /**
     * Utility function used to write oids as Strings to a file.
     *
     * @param filename          local file to write OIDs
     * @throws IOException      throws an exception if there are issues with the given file
     */
    public void writeOIDsToFile(String filename) throws IOException {
        FileWriter fileWriter = new FileWriter(filename);
        try(PrintWriter printWriter = new PrintWriter(fileWriter)){
            oids.forEach(oid -> printWriter.print(parseOID(oid)));
        }
    }

    /**
     * Utility function to convert OIDs as a String to int[]
     *
     * @param oid               OID formatted String
     * @return                  OID formatted as int[]
     */
    public static int[] parseOIDString(String oid) {
        String[] parts = oid.split("\\.");
        int[] returnMe = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            returnMe[i] = Integer.parseInt(parts[i]);
        }
        return returnMe;
    }

    /**
     * Utility function to convert OIDs as int[] to String
     *
     * @param oid               OID formatted as int[]
     * @return                  OID formatted String
     */
    public static String parseOID(int[] oid) {
        String returnMe = "";
        for (int i = 0; i < oid.length; i++) {
            returnMe += oid[i];
            if (i < oid.length - 1) {
                returnMe += ".";
            }
        }
        return returnMe;
    }

    /**
     * Checks internal data structure to see if a give OID as a String exists in the collection
     *
     * @param oid               OID formatted as String
     * @return                  true/false if given item exists in the collection
     */
    public boolean exists(String oid) {
        return oids.contains(parseOIDString(oid));
    }

    /**
     * Function to return all OIDs in the internal data structure that match a given prefix.
     *
     * @param prefix            String prefix to compare OIDs in the collection
     * @return                  Collection of matching OIDs as int[]
     */
    public Collection<int[]> getOidsWithPrefix(String prefix) {
        int[] start = parseOIDString(prefix);
        int[] end = Arrays.copyOf(start, start.length + 1);
        end[end.length - 1] = Integer.MAX_VALUE;
        LOGGER.log(Level.INFO, "Searching for {0}", prefix);
        final long startTime = System.currentTimeMillis();
        Set<int[]> returnMe = oids.subSet(start, end);
        final long endTime = System.currentTimeMillis();
        Object[] params = {returnMe.size(), (endTime - startTime)};
        LOGGER.log(Level.INFO, "Found {0} results in {1} milliseconds", params);
        return returnMe;
    }

    /**
     * Encodes the loaded OIDs and returns them as a Collection of byte[].
     * @see #encodeOID(int[])
     *
     * @return
     */
    public Collection<byte[]> encode() {
        LOGGER.log(Level.INFO, "Encoding {0} OIDs", oids.size());
        Collection<byte[]> returnMe = oids.stream().map(this::encodeOID).collect(Collectors.toSet());
        LOGGER.info("Finished Encoding");
        return returnMe;

    }

    /**
     * Encodes an int[] formatted OID to binary based on the below method
     * First Byte 0x06
     * Second Byte length of value
     * Next Bytes are the encoded value based on https://docs.microsoft.com/en-us/windows/win32/seccertenroll/about-object-identifier?redirectedfrom=MSDN
     *
     * @param oid               int[] formatted OID
     * @return                  byte[] encded OID
     */
    private byte[] encodeOID(int[] oid) {
        // First Byte 0x06
        // Second Byte length of value
        // Next Bytes are the encoded value based on https://docs.microsoft.com/en-us/windows/win32/seccertenroll/about-object-identifier?redirectedfrom=MSDN
        ArrayList<Byte> data = new ArrayList<Byte>();
        data.add((byte) 0x06);
        data.add((byte) 0x00); //Placeholder for size of data

        data.add((byte) (oid[0] * 40 + oid[1]));

        for (int i = 2; i < oid.length; i++) {
            //encode each int
            if (oid[i] <= 128) {
                //single byte encoding
                data.add((byte) oid[i]);
            } else if (oid[i] <= 16383) {
                encodeTwoByteInt(data, oid[i]);
            } else {
                encodeFourByteInt(data, oid[i]);
            }
        }
        data.set(1, (byte) (data.size() - 2));

        //This is stupid. Need a better way.
        byte[] returnMe = new byte[data.size()];
        for (int i = 0; i < data.size(); i++) {
            returnMe[i] = data.get(i);
            LOGGER.finest(Integer.toHexString((0x000000FF & returnMe[i])));
        }

        return returnMe;
    }

    /**
     * Utility function to print a given encoded collection of OIDs as Hex
     *
     * @param data
     */
    public static void printEncodedOIDsInHex(Collection<byte[]> data){
        data.forEach(t -> {
            for (int i = 0; i < t.length; i++) {
                System.out.println(Integer.toHexString((0x000000FF & t[i])));
            }
        });

    }

    /**
     * Private utility function to aide in encoding OIDs
     *
     * @param data
     * @param i
     */
    private void encodeFourByteInt(ArrayList<Byte> data, int i) {
        int step1 = i / 262144;
        int txMg = 0x8 + step1;
        int base = 262144 * step1;
        int step2 = i - base;
        int txMi = step2 / 16384;

        String hexVal = "0x" + Integer.toString(txMg) + Integer.toString(txMi);
        data.add((byte) Integer.decode(hexVal).intValue());

        base = base + (txMi * 16384);
        int step3 = i - base;
        encodeTwoByteInt(data, step3);
    }

    /**
     * Private utility function to aide in encoding OIDs
     *
     * @param data
     * @param i
     */
    private void encodeTwoByteInt(ArrayList<Byte> data, int i) {
        int step1 = i / 2048;
        int txMg = 0x8 + Integer.decode("0x" + step1).intValue();
        int base = 2048 * step1;
        int step2 = i - base;
        int txMi = step2 / 128;
        base = base + (txMi * 128);
        int step4 = i - base;

        String hexVal = "0x" + txMg + Integer.toString(txMi, 16);
        data.add((byte) Integer.decode(hexVal).intValue());
        data.add((byte) step4);
    }
}