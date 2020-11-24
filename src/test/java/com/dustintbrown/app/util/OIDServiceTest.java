package com.dustintbrown.app.util;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class OIDServiceTest extends TestCase {
    private OIDService oidService;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        oidService = new OIDService();
    }

    public void testDiffCollections() {
        HashSet<String> collectionA = new HashSet<>(Arrays.asList(
                "1.3.6.1.2.1.2.2.1.2",
                "2.3.243.243.1.7.0.1",
                "2.3.6.1.2.1.31.1.1.1.18",
                "0.2.840.10036.3.1.2.1.4",
                "1.3.6.1.4.1.9.9.13.1",
                "1.3.6.1.4.1.11.2.14.11.1.2",
                "1.3.6.1.2.1.25.2.3.1",
                "2.7.1.4297.1.0",
                "2.0.6.1.2.1.25.4.2.1",
                "1.3.6.1.4.1.311.21.20"
        ));
        HashSet<String> collectionB = new HashSet<>(Arrays.asList(
                "2.3.6.1.2.1.31.1.1.1.18",
                "0.8.0.276.1",
                "1.3.6.1.2.1.2.2.1.2",
                "0.1.5.4.2098.1.4.667",
                "1.3.6.1.2.1.25.2.3.1",
                "2.7.1.4297.1.0",
                "2.0.6.1.2.1.25.4.2.1",
                "1.2.856.10036.1.1.1.1",
                "2.3.243.243.1.7.0.1"
        ));

        Collection<String> results = oidService.diffCollections(collectionA,collectionB);

        HashSet<String> expectedResults = new HashSet<>(Arrays.asList(
                "1.3.6.1.2.1.2.2.1.2",
                "2.3.243.243.1.7.0.1",
                "2.3.6.1.2.1.31.1.1.1.18",
                "- 0.2.840.10036.3.1.2.1.4",
                "+ 0.8.0.276.1",
                "+ 0.1.5.4.2098.1.4.667",
                "- 1.3.6.1.4.1.9.9.13.1",
                "- 1.3.6.1.4.1.11.2.14.11.1.2",
                "1.3.6.1.2.1.25.2.3.1",
                "2.7.1.4297.1.0",
                "2.0.6.1.2.1.25.4.2.1",
                "+ 1.2.856.10036.1.1.1.1",
                "- 1.3.6.1.4.1.311.21.20"
        ));

        assertEquals(results.size(), expectedResults.size());
        assertTrue(results.containsAll(expectedResults));

    }

    public void testEncode() {
        HashSet<String> collectionA = new HashSet<>();

        Collections.addAll(collectionA,
                "1.3.6.1.4.1.311.21.20"
        );
        byte[] expectedEncodedOid = {
                (byte)0x06,
                (byte)0x09,
                (byte)0x2B,
                (byte)0x06,
                (byte)0x01,
                (byte)0x04,
                (byte)0x01,
                (byte)0x82,
                (byte)0x37,
                (byte)0x15,
                (byte)0x14
        };

        oidService.populateWithOIDs(collectionA);

        Collection<byte[]> encoded = oidService.encode();
        encoded.forEach(single -> assertTrue(Arrays.equals(expectedEncodedOid,single))); //just one in this list
    }
}