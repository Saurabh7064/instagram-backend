package com.instagram.backend.interview.leetcode.twopointers.validpalindrome;

/**
 * Runnable reference solution for Two Pointers Micro-Lesson 01.
 */
public class ValidPalindromeSolution {

    public static void main(String[] args) {
        assertEquals(true, isPalindrome("A man, a plan, a canal: Panama"),
                "phrase with punctuation");
        assertEquals(false, isPalindrome("race a car"), "mismatch");
        assertEquals(true, isPalindrome(" "), "no alphanumeric characters");
        assertEquals(false, isPalindrome("0P"), "digit and letter mismatch");
        assertEquals(true, isPalindrome("ab_a"), "ignored separator");

        System.out.println("All reference-solution checks passed.");
    }

    static boolean isPalindrome(String text) {
        int left = 0;
        int right = text.length() - 1;

        while (left < right) {
            while (left < right && !Character.isLetterOrDigit(text.charAt(left))) {
                left++;
            }
            while (left < right && !Character.isLetterOrDigit(text.charAt(right))) {
                right--;
            }

            char leftCharacter = Character.toLowerCase(text.charAt(left));
            char rightCharacter = Character.toLowerCase(text.charAt(right));
            if (leftCharacter != rightCharacter) {
                return false;
            }

            left++;
            right--;
        }

        return true;
    }

    private static void assertEquals(boolean expected, boolean actual, String scenario) {
        if (expected != actual) {
            throw new AssertionError(
                    scenario + ": expected " + expected + " but received " + actual);
        }
    }
}
