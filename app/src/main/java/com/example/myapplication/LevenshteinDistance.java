package com.example.myapplication;

public class LevenshteinDistance {

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