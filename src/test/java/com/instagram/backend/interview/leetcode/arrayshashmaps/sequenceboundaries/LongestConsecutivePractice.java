package com.instagram.backend.interview.leetcode.arrayshashmaps.sequenceboundaries;

import java.util.HashSet;
import java.util.Set;

/**
 * Editable practice program for Micro-Lesson 02: Sequence Boundaries.
 *
 * Write your implementation only inside longestConsecutive. Keep the main
 * method unchanged initially so the same examples test every attempt.
 */
public class LongestConsecutivePractice {

    public static void main(String[] args) {
        try {
            runChecks();
            System.out.println("All practice checks passed.");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Practice program is ready.");
            System.out.println("Implement longestConsecutive, then run this main method again.");
        }
    }

    static public int longestConsecutive(int[] nums) {
        Set<Integer> unique = new HashSet<Integer>();

        for(int num : nums){
            unique.add(num);
        }

        int longest = 0;


        for(Integer num:unique){

            boolean hasPredecessor = num!=Integer.MIN_VALUE && unique.contains(num-1);

            if(!hasPredecessor){

                int current = num;
                int length = 1;

                while(current!=Integer.MAX_VALUE && unique.contains(current+1)){
                    current++;
                    length++;
                }

                longest = Math.max(longest,length);
            }


        }

        return longest;
    }

    private static void runChecks() {
        assertEquals(4, longestConsecutive(new int[] {100, 4, 200, 1, 3, 2}),
                "unordered run");
        assertEquals(3, longestConsecutive(new int[] {1, 2, 0, 1}),
                "duplicates do not extend a run");
        assertEquals(0, longestConsecutive(new int[] {}),
                "empty input");
        assertEquals(1, longestConsecutive(new int[] {7}),
                "single value");
        assertEquals(3, longestConsecutive(new int[] {-1, -3, -2, 10}),
                "negative values");
        assertEquals(2,
                longestConsecutive(new int[] {Integer.MAX_VALUE - 1, Integer.MAX_VALUE}),
                "upper integer boundary");
        assertEquals(2,
                longestConsecutive(new int[] {Integer.MIN_VALUE, Integer.MIN_VALUE + 1}),
                "lower integer boundary");
        assertEquals(1,
                longestConsecutive(new int[] {Integer.MIN_VALUE, Integer.MAX_VALUE}),
                "integer extremes are not adjacent");
    }

    private static void assertEquals(int expected, int actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(
                    scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
