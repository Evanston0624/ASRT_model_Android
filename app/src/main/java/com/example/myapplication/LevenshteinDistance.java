package com.example.myapplication;

import android.util.Log;

import java.util.Arrays;

public class LevenshteinDistance {
    private static final String TAG = "LevenshteinDistance";
    /**
     * 計算兩個字串之間的 Levenshtein 距離。
     * @param str1 第一個字串
     * @param str2 第二個字串
     * @return 兩個字串之间的 Levenshtein 距離
     */
    public static int computeLevenshteinDistance(String str1, String str2) {
        int len1 = str1.length();
        int len2 = str2.length();

        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }

        return dp[len1][len2];

    }
    public static int wordDifference(String str1, String str2) {
        String[] words1 = str1.trim().split("\\s+");
        String[] words2 = str2.trim().split("\\s+");

        return Math.abs(words1.length - words2.length);
    }
    /**
     * 計算兩個字串的相似度
     * @param str1 第一個字串
     * @param str2 第二個字串
     * @return 兩個字串的相似度（0.0 ~ 1.0)
     */
    public static double similarity(String str1, String str2) {
        int distance = computeLevenshteinDistance(str1, str2);
        int maxLength = Math.max(str1.length(), str2.length());
        return (maxLength - distance) / (double) maxLength;
    }
    /**
     * 找到兩個字串中最相似的段落
     * @param str1 第一個字串
     * @param str2 第二個字串
     * @return 最相似段落的相似度（0.0 ~ 1.0)
     */
    public static double findMostSimilarSegment(String str1, String str2) {
        // 如果任一字串為空，直接返回 0
        if (str1.isEmpty() || str2.isEmpty()) {
            return 0.0; // 或者根據你的需求返回適當值
        }

        // 如果兩個字串的長度相等，直接返回相似度
        int count = wordDifference(str1, str2);

        if (count == 0) {
            return similarity(str1, str2);
        }

        // 確保 str1 是較長的字串，str2 是較短的字串
        if (str1.length() < str2.length()) {
            String temp = str1;
            str1 = str2;
            str2 = temp;
        }

        String[] words1 = str1.trim().split("\\s+");
        String[] words2 = str2.trim().split("\\s+");
        if (words1.length == words2.length) {
            return similarity(str1, str2);
        }
        double minDistance = Double.MAX_VALUE;
        String bestSubStr1 = "";

        // 獲取較短字串的長度
        int targetLength = words2.length;

        // 檢查長字串的所有子串，子串長度等於較短字串的長度
        for (int start = 0; start <= words1.length - targetLength; start++) {
            // 取長字串的子串
            String subStr1 = String.join(" ", java.util.Arrays.copyOfRange(words1, start, start + targetLength));
//            Log.d(TAG, "main_log str check subStr1:" + subStr1);
            // 計算與短字串的編輯距離
            int distance = computeLevenshteinDistance(subStr1, str2);
//            Log.d(TAG, "main_log distance check:" + distance);
            // 增加調試輸出
            if (distance < minDistance) {
                minDistance = distance;
                bestSubStr1 = subStr1;
            }
        }
        // 檢查是否找到有效的子串
        if (minDistance == Double.MAX_VALUE) {
            return similarity(str1, str2);
        }

        // 根據找到的最優子串計算相似度
        return similarity(bestSubStr1, str2);
    }
    public static double[][] similarity_matrix(String[][] str1, String str2) {
        double[][] simi_matrix = new double[str1.length][str1[0].length];
        for (int i = 0; i < str1.length; i++) {
            for (int j = 0; j < str1[i].length; j++) {
                String element = str1[i][j];
                simi_matrix[i][j] = similarity(element, str2);
            }
        }
        return simi_matrix;
    }
}