package com.example.myapplication;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if audio file exists
        boolean fileExists = isFileExists(this, R.raw.test);
        if (fileExists) {
            // load audio to double list
            double[] audioData = DataLoader.readAsDouble(this, R.raw.test);

            if (audioData != null && audioData.length > 0) {
                Toast.makeText(this, "audioData length：" + audioData.length, Toast.LENGTH_SHORT).show();
                // Spectrogram
                Spectrogram spectrogram = new Spectrogram(16000, 25, 10);
                float[][] result = spectrogram.calculateSpectrogram(audioData);

                // padding
                float[][] mdl_input = DataLoader.prepareInput(result);

                AssetManager assetManager = getAssets();
                // 模型文件路径
                String modelPath = "model/model.tflite";

                // 检查模型文件是否存在
                try {
                    // 打开模型文件的输入流
                    InputStream inputStream = assetManager.open(modelPath);
                    Log.d(TAG, "模型文件存在");
                    // 关闭输入流
                    inputStream.close();
                } catch (IOException e) {
                    Log.d(TAG, "模型文件不存在");
                    e.printStackTrace();
                }

                TfliteInfer tfliteInfer = new TfliteInfer();
                tfliteInfer.loadModel(assetManager, modelPath);
                float [][] mdl_output = tfliteInfer.runInference(mdl_input);
                Log.d(TAG, "mdl_output");

                String[] firstElements = DataLoader.processFile(this, "dict/dict.txt");
                String ctc_output = tfliteInfer.greedyDecode(mdl_output, firstElements);
                ctc_output = LevenshteinDistance.RmLeadingAndTrailingSpaces(ctc_output);

//                String str2 = "kai1 qi3 tong1 zhi1 yan3 mian4";
//                String str2 = "a1 b1 c1 kai1 qi3 tong1 zhi1 yan3 mian4 d1 e1";
                String str2 = "tong1 kai1 qi3 tong1 zhi1 yan3 mian4 tong1";
                int distance = LevenshteinDistance.computeLevenshteinDistance(ctc_output, str2);
                double similarity = LevenshteinDistance.similarity(ctc_output, str2);
                int count_diff = LevenshteinDistance.wordDifference(ctc_output, str2);
                double fuzzy_simi = LevenshteinDistance.findMostSimilarSegment(ctc_output, str2);
                Log.d(TAG, "main_log tag:"+str2);
                Log.d(TAG, "main_log opt:"+ctc_output);
                Log.d(TAG, "main_log distance:"+distance);
                Log.d(TAG, "main_log similarity:"+similarity);
                Log.d(TAG, "main_log fuzzy_simi:"+fuzzy_simi);
                Log.d(TAG, "main_log count_diff:"+count_diff);

//                JsonReader.readJsonFromUrl(url, new JsonReader.JsonResponseCallback() {
//                    @Override
//                    public void onJsonResponse(String jsonResponse) {
//                        // Handle the JSON response here
//                        Log.d(TAG, "JSON response: " + jsonResponse);
//                        // You can also update UI elements here with the jsonResponse
//                        String [][] json_data = JsonReader.convertList(jsonResponse);
//                        double[][] simi_matrix = LevenshteinDistance.similarity_matrix(json_data, ctc_output);
//                    }
//                });
            } else {
                Toast.makeText(this, "audioData 回傳錯誤" , Toast.LENGTH_SHORT).show();
                // 處理讀取失敗的情況
            }
        } else {
            // 如果檔案不存在，執行相應的錯誤處理邏輯
            Toast.makeText(this, "test.wav 檔案不存在", Toast.LENGTH_SHORT).show();
        }
    }

    // 檢查檔案是否存在的方法
    public boolean isFileExists(Context context, int resourceId) {
        try {
            context.getResources().openRawResourceFd(resourceId).close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}