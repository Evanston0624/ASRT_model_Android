package com.example.myapplication;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class DataLoader {
    private static final String TAG = "DataLoader";

    public static double[] readAsInt(Context context, int resourceId) {
        MediaExtractor extractor = new MediaExtractor();
        try {
            AssetFileDescriptor assetFileDescriptor = context.getResources().openRawResourceFd(resourceId);
            extractor.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());

            assetFileDescriptor.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        float numSamples = getNumSamples(extractor);
        ByteBuffer audioData = readData(extractor, numSamples);
        Log.d(TAG, "音訊數據的剩餘樣本數量：" + audioData.remaining()/2);

        return convertToIntegerArray(audioData);
    }

    private static int getNumSamples(MediaExtractor extractor) {
        MediaFormat format = extractor.getTrackFormat(0);
        int sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        int durationUs = (int) format.getLong(MediaFormat.KEY_DURATION);
        Log.d(TAG, "Sample rate: " + sampleRate);
        Log.d(TAG, "Duration (us): " + durationUs);
        durationUs = durationUs/1000;
        sampleRate = sampleRate/1000;
        return durationUs*sampleRate;
    }

    private static ByteBuffer readData(MediaExtractor extractor, float numSamples) {
        ByteBuffer buffer = ByteBuffer.allocate((int) numSamples * 2); // 16-bit PCM, 2 bytes per sample

        extractor.selectTrack(0);

        while (true) {
            int bytesRead = extractor.readSampleData(buffer, buffer.position());
            if (bytesRead < 0) {
                break;
            }
            buffer.position(buffer.position() + bytesRead);
            extractor.advance();
        }
        // Reset buffer position to the beginning
        buffer.rewind();
        return buffer;
    }

    private static double[] convertToIntegerArray(ByteBuffer byteBuffer) {
        byteBuffer.rewind();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN); // assuming little-endian data
        double[] intData = new double[byteBuffer.remaining() / 2];
        Log.d(TAG, "intData-length：" + intData.length);
        for (int i = 0; i < intData.length; i++) {
            double value = (double)byteBuffer.getShort();
            intData[i] = value; // 不進行無符號轉換
        }
        Log.d(TAG, "intData-length：" + intData.length);
        return intData;
    }
    public static float[][][][] prepareInput(float[][] inputData) {
        int batchSize = 1;
        int height = 1600;
        int width = 200;
        int depth = 1;

        float[][][][] inputArray = new float[batchSize][height][width][depth];
        int padding_length = height - inputData.length;
        int left_padding = padding_length / 2;

        int count = 0;
        for (int i = 0; i < height; i++) {
            if (i >= left_padding && i < left_padding+inputData.length) {
                float[] row = inputData[count];
                for (int j = 0; j < width; j++) {
                    inputArray[0][i][j][0] = row[j];
                }
                count += 1;
            }else {
                for (int j = 0; j < width; j++) {
                    inputArray[0][i][j][0] = 0.0f;
                }
            }
        }
        return inputArray;
    }
    public static String[] processFile(Context context, String fileName) {
        List<String> firstElements = new ArrayList<>();

        try (InputStream is = context.getAssets().open(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] elements = line.split("\t");
                if (elements.length > 0) {
                    firstElements.add(elements[0]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        firstElements.add(" ");
        return firstElements.toArray(new String[0]);
    }
}