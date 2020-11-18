package com.dustintbrown.app.util;

import java.util.Comparator;

public class OIDComp implements Comparator<int[]> {
    @Override
    public int compare(int[] left, int[] right) {
        for (int i = 0, j = 0; i < left.length && j < right.length; i++, j++) {
            int a = left[i];
            int b = right[j];
            if (a != b) {
                return a - b;
            }
        }
        return left.length - right.length;
    }
}
