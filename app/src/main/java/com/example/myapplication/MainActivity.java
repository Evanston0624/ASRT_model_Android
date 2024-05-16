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

        // 檢查 test.wav 檔案是否存在
        boolean fileExists = isFileExists(this, R.raw.test);

        if (fileExists) {
            // 如果檔案存在，執行您的相應邏輯
            Toast.makeText(this, "test.wav 檔案存在", Toast.LENGTH_SHORT).show();

            // 讀取並處理音訊檔案
            double[] audioData = DataLoader.readAsInt(this, R.raw.test);

            if (audioData != null && audioData.length > 0) {
                Toast.makeText(this, "audioData 長度：" + audioData.length, Toast.LENGTH_SHORT).show();
                // 判斷數值
                double min = audioData[0]; // 將第一個元素設置為最小值
                double max = audioData[0]; // 將第一個元素設置為最大值

                for (int i = 1; i < audioData.length; i++) {
                    if (audioData[i] < min) {
                        min = audioData[i];
                    }
                    if (audioData[i] > max) {
                        max = audioData[i];
                    }
                }
                Log.d(TAG, "intData-min：" + min);
                Log.d(TAG, "intData-max：" + max);
                // 處理音訊數據
                Spectrogram spectrogram = new Spectrogram(16000, 25, 10);
                float[][] result = spectrogram.calculateSpectrogram(audioData);
                // 查找最大和最小值
                min = Double.MAX_VALUE;
                max = Double.MIN_VALUE;
                // 遍历结果数组并查找最大和最小值
                for (float[] frame : result) {
                    for (double value : frame) {
                        if (value < min) {
                            min = value;
                        }
                        if (value > max) {
                            max = value;
                        }
                    }
                }
                Log.d(TAG, "spectrogram-min：" + min);
                Log.d(TAG, "spectrogram-max：" + max);

                // padding
                float[][][][] mdl_input = DataLoader.prepareInput(result);

                // check model
                // 创建 AssetManager 实例
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
                Log.d(TAG, "ctc_output"+ctc_output);

                String str2 = " kai1 qi3 tong1 zhi1 yan3 mian4 ";
                int distance = LevenshteinDistance.computeLevenshteinDistance(ctc_output, str2);
                double similarity = LevenshteinDistance.similarity(ctc_output, str2);
//                Log.d(TAG, "distance"+distance);
//                Log.d(TAG, "similarity"+similarity);


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