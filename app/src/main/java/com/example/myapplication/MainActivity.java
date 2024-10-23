package com.example.myapplication;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        ArrayList<String> targets = new ArrayList<>();
        ArrayList<String> inputs = new ArrayList<>();
//        ArrayList<String> targets_name = new ArrayList<>();
        ArrayList<String> inputs_name = new ArrayList<>();
//         模型文件路径
        String modelPath = "model/model.tflite";

        // 检查模型文件是否存在
        AssetManager assetManager = getAssets();
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

        // load audio id
        List<RawAudioLoader.AudioFile> ttsAudioFiles = RawAudioLoader.getRawAudioFileIdsWithTTS(this);

        for (RawAudioLoader.AudioFile audioFile : ttsAudioFiles) {
            double[] audioData = null;
            if (isFileExists(this, audioFile.id)) {
                audioData = DataLoader.readAsDouble(this, audioFile.id);
            }

            if (audioData != null && audioData.length > 0) {
                Toast.makeText(this, "audioData length：" + audioData.length, Toast.LENGTH_SHORT).show();
                // Spectrogram
                Spectrogram spectrogram = new Spectrogram(16000, 25, 10);
                float[][] result = spectrogram.calculateSpectrogram(audioData);

                // padding
                float[][] mdl_input = DataLoader.prepareInput(result);

                TfliteInfer tfliteInfer = new TfliteInfer();
                tfliteInfer.loadModel(assetManager, modelPath);
                float[][] mdl_output = tfliteInfer.runInference(mdl_input);

                String[] firstElements = DataLoader.processFile(this, "dict/dict.txt");
                String ctc_output = tfliteInfer.greedyDecode(mdl_output, firstElements);
//                Log.d(TAG, "main_log greedyDecode:" + ctc_output);
                ctc_output = tfliteInfer.ensureSpaceAfterDigits(ctc_output);
//                Log.d(TAG, "main_log ensureSpaceAfterDigits:" + ctc_output);

//                if (audioFile.fileName.contains("tts")) {
//                    targets.add(ctc_output);
//                    targets_name.add(audioFile.fileName);
//                } else {
//                    inputs.add(ctc_output);
//                    inputs_name.add(audioFile.fileName);
//                }
                inputs.add(ctc_output);
                inputs_name.add(audioFile.fileName);
            } else {
                Toast.makeText(this, "audioData 回傳錯誤", Toast.LENGTH_SHORT).show();
                // 處理讀取失敗的情況
            }
        }
            // 遍歷 targets
//            for (int i = 0; i < targets.size(); i++) {
//                String target = targets.get(i);
//                String targetName = targets_name.get(i);
        String target = "gong1 si1 yi3 rang4 shi4 jie4 geng4 you3 chang4 yi4wei4 shi3 ming4 mian4 xiang4 qian2 qiu2 hai3 liang4 xin1 sheng1 dai4 yong4 hu4 ti2 gong1 jian3 dan1 gao1 xiao4 de5 shu4 zi4 chang4 yi1ruan3 jian4 qiao2 liu2 shi2 shang4 de5 chang4 ni3 zi1 yuan2 he2 feng1 fu4 duo1 yuan3 de5 shen1 tai4 hua4 fu2 wu4";
        // 遍歷 inputs
        for (int j = 0; j < inputs.size(); j++) {
            String ctc_output = inputs.get(j);
            String inputsName = inputs_name.get(j);

//            // 測試只念一半，
//            int middle = ctc_output.length() / 2;
//            ctc_output = ctc_output.substring(0, middle);

            // 計算 Levenshtein 距離和相似性
//            int distance = LevenshteinDistance.computeLevenshteinDistance(ctc_output, target);
            double similarity = LevenshteinDistance.similarity(ctc_output, target);
            int count_diff = LevenshteinDistance.wordDifference(ctc_output, target);
            double fuzzy_simi = LevenshteinDistance.findMostSimilarSegment(ctc_output, target);

            // 日誌輸出
//            Log.d(TAG, "main_log target_name:" + targetName);
            Log.d(TAG, "main_log input _name:" + inputsName);
//            Log.d(TAG, "main_log target: " + target);
//            Log.d(TAG, "main_log input: " + ctc_output);
//            Log.d(TAG, "main_log distance: " + distance);
            Log.d(TAG, "main_log similarity: " + similarity);
            Log.d(TAG, "main_log fuzzy_simi: " + fuzzy_simi);
            Log.d(TAG, "main_log count_diff: " + count_diff);

//            }
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