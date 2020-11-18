package com.dustintbrown.app.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
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
        LOGGER.log(Level.INFO, "Starting generation of {0} oids.", num);
        oids.clear();
        while (oids.size() < num) {
            oids.add(generateSingleOID());
        }
        LOGGER.log(Level.INFO, "Finished generation of oids.");
    }

    private int[] generateSingleOID() {
        // First 4 sections are 1-15
        // 5-10 total sections
        // each section after  is a random number from 1-Integer.MAX_VALUE
        int length = rand.nextInt(6) + 4;
        int[] returnMe = new int[length];
        for (int i = 0; i < length; i++) {
            if (i < 5) {
                returnMe[i] = (rand.nextInt(15) + 1);
            } else {
                returnMe[i] = (rand.nextInt(Integer.MAX_VALUE) + 1);
            }
        }
        return returnMe;
    }

    public void importOIDsFromFile(String filename) throws FileNotFoundException, IOException {
        LOGGER.log(Level.INFO, "Importing OIDs from file: {0}", filename);
        oids.clear();
        final long startTime = System.currentTimeMillis();
        int record_count = 0;
        try (final BufferedReader myReader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8))) {
            String line;
            while ((line = myReader.readLine()) != null) {
                oids.add(parseOIDString(line));
                record_count++;
                if (record_count % 1000000 == 0) {
                    LOGGER.log(Level.INFO, "Read {0} records.", record_count);
                }
            }
        }
        final long endTime = System.currentTimeMillis();
        Object[] params = {oids.size(), endTime - startTime};
        LOGGER.log(Level.INFO, "Read {0} OIDs from file. Operation completed in {1} milliseconds.", params);
    }

    public int[] parseOIDString(String oid) {
        String[] parts = oid.split("\\.");
        int[] returnMe = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            returnMe[i] = Integer.parseInt(parts[i]);
        }
        return returnMe;
    }

    public String parseOID(int[] oid) {
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
    public Collection<String> getOidsWithPrefix(String prefix) {
        int[] start = parseOIDString(prefix);
        int[] end = Arrays.copyOf(start, start.length + 1);
        end[end.length - 1] = Integer.MAX_VALUE;
        LOGGER.log(Level.INFO, "Searching for {0}", prefix);
        final long startTime = System.currentTimeMillis();
        Set<int[]> subset = oids.subSet(start, end);
        Set<String> returnMe = subset.stream().map(this::parseOID).collect(Collectors.toSet());
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
                encodeComplexInt(data, oid[i]);
            } else {
                int step1 = oid[i] / 262144;
                int txMg = 0x8 + step1;
                int base = 262144 * step1;
                int step2 = oid[i] - base;
                int txMi = step2 / 16384;

                String hexVal = "0x" + Integer.toString(txMg) + Integer.toString(txMi);
                data.add((byte) Integer.decode(hexVal).intValue());

                base = base + (txMi * 16384);
                int step3 = oid[i] - base;
                encodeComplexInt(data, step3);
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

    private void encodeComplexInt(ArrayList<Byte> data, int i) {
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