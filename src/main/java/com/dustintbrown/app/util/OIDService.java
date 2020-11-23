package com.dustintbrown.app.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class OIDService {
    private static SplittableRandom rand = new SplittableRandom();
    private final static Logger LOGGER = Logger.getLogger(OIDService.class.getName());

    private TreeSet<int[]> oids = new TreeSet<>(new OIDComp());

    public TreeSet<int[]> getOids() {
        return oids;
    }

    // replaces set with generated data
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

    private int[] generateSingleOID() {
        // First 2 sections are 1-15
        // 3-10 total sections
        // each section after  is a random number from 1-Integer.MAX_VALUE
        int length = rand.nextInt(6) + 4;
        int[] returnMe = new int[length];
        for (int i = 0; i < length; i++) {
            if (i < 2) {
                returnMe[i] = (rand.nextInt(15) + 1);
            } else if(i==length-1) {
                //last item should have a larger scope to be more realistic
                returnMe[i] = generateBiasedIntGTZero(65000,4096);
            }else{
                //middle numbers should be biased toward 0 give the way these numbers appear in the wild.
                returnMe[i] = generateBiasedIntGTZero(255 * i,128);
            }
        }
        return returnMe;
    }

    private int generateBiasedIntGTZero(int max, int bias_threshold){
        int cnt = 1;
        int next = 0;
        while(cnt<6){ //basically a poor man's weighting toward numbers lower than start_max
            next = rand.nextInt(max) + 1;
            if(next<(bias_threshold*cnt)){
                break;
            }
            cnt++;
        }
        return next;
    }

    public static void printOIDs(Collection<int[]> oidsToPrint){
        oidsToPrint.forEach(oid -> {
            System.out.println(OIDService.parseOID(oid));
        });
    }

    public void importOIDsFromFile(String filename) throws IOException {
        loadOIDsFromFile(oids, filename, false);
    }

    private Collection<String> loadOIDsFromFileAsStrings(String filename) throws IOException{
        ArrayList<String> returnMe = new ArrayList<>();
        loadOIDsFromFile(returnMe, filename, true);
        return returnMe;
    }

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

    public Collection<String> diffFiles(String filename1, String filename2) throws IOException {
        ArrayList<String> returnMe = new ArrayList<>();
        ArrayList<String> setA = new ArrayList<>();
        ArrayList<String> setB = new ArrayList<>();
        loadOIDsFromFile(setA, filename1,true);
        loadOIDsFromFile(setB, filename2,true);

        setA.forEach(oid ->{
            if(setB.contains(oid)){
                returnMe.add("  " + oid);
            }else{
                returnMe.add("- " + oid);
            }
        });
        setB.removeAll(setA); //leaves setB with only additions
        setB.forEach(oid -> returnMe.add( "+ " + oid));
        return returnMe;

    }

    public void writeOIDsToFile(String filename) throws IOException {
        FileWriter fileWriter = new FileWriter(filename);
        try(PrintWriter printWriter = new PrintWriter(fileWriter)){
            oids.forEach(oid -> printWriter.print(parseOID(oid)));
        }
    }

    public static int[] parseOIDString(String oid) {
        String[] parts = oid.split("\\.");
        int[] returnMe = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            returnMe[i] = Integer.parseInt(parts[i]);
        }
        return returnMe;
    }

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

    /*
        Solution for Question #2
     */
    public boolean exists(String oid) {
        return oids.contains(parseOIDString(oid));
    }

    /*
        Solution for Question #3
     */
    public Collection<int[]> getOidsWithPrefix(String prefix) {
        int[] start = parseOIDString(prefix);
        int[] end = Arrays.copyOf(start, start.length + 1);
        end[end.length - 1] = Integer.MAX_VALUE;
        LOGGER.log(Level.INFO, "Searching for {0}", prefix);
        final long startTime = System.currentTimeMillis();
        Set<int[]> returnMe = oids.subSet(start, end);
//        Set<String> returnMe = subset.stream().map(OIDService::parseOID).collect(Collectors.toSet());
        final long endTime = System.currentTimeMillis();
        Object[] params = {returnMe.size(), (endTime - startTime)};
        LOGGER.log(Level.INFO, "Found {0} results in {1} milliseconds", params);
        return returnMe;
    }

    /*
        Solution for Question #4
     */
    public Collection<byte[]> encode() {
        LOGGER.log(Level.INFO, "Encoding {0} OIDs", oids.size());
        Collection<byte[]> returnMe = oids.stream().map(this::encodeOID).collect(Collectors.toSet());
        LOGGER.info("Finished Encoding");
        return returnMe;

    }

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

    public static void printEncodedOIDsInHex(Collection<byte[]> data){
        data.forEach(t -> {
            for (int i = 0; i < t.length; i++) {
                System.out.println(Integer.toHexString((0x000000FF & t[i])));
            }
        });

    }

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