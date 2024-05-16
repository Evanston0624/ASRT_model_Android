package com.example.myapplication;

public class LevenshteinDistance {

    /**
     * 计算两个字符串之间的 Levenshtein 距离。
     * @param str1 第一个字符串
     * @param str2 第二个字符串
     * @return 两个字符串之间的 Levenshtein 距离
     */
    public static int computeLevenshteinDistance(String str1, String str2) {
        int len1 = str1.length();
        int len2 = str2.length();

        // 创建二维数组保存所有子问题的解
        int[][] dp = new int[len1 + 1][len2 + 1];

        // 初始化dp数组的边界条件
        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        // 填充dp数组
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }

        // dp[len1][len2]包含了从str1转换到str2所需要的最少编辑操作次数
        return dp[len1][len2];
    }

    /**
     * 计算两个字符串的相似度。
     * @param str1 第一个字符串
     * @param str2 第二个字符串
     * @return 两个字符串的相似度（0.0 到 1.0 之间）
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
                // 對每個元素執行所需的操作
                simi_matrix[i][j] = similarity(element, str2);
            }
        }
        return simi_matrix;
    }
}