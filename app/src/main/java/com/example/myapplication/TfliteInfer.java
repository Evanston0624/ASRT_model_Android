package com.example.myapplication;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class TfliteInfer {
    private Interpreter interpreter;
    private static final String TAG = "Tflite";

    public void loadModel(AssetManager assetManager, String modelPath) {
        try {
            // 加载模型文件
            AssetFileDescriptor assetFileDescriptor = assetManager.openFd(modelPath);
            FileInputStream inputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = assetFileDescriptor.getStartOffset();
            long declaredLength = assetFileDescriptor.getDeclaredLength();

            // 映射模型文件到内存
            MappedByteBuffer modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);

            // 创建解释器
            interpreter = new Interpreter(modelBuffer);
            Log.d("Tflite", "模型載入成功");
        } catch (IOException e) {
            Log.e("Tflite", "Error loading model: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public float[][] runInference(float[][] inputArray) {
        // 准备输入
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(inputArray.length * inputArray[0].length * Float.SIZE / Byte.SIZE);
        inputBuffer.order(ByteOrder.nativeOrder());
        for (float[] row : inputArray) {
            for (float value : row) {
                inputBuffer.putFloat(value);
            }
        }
        inputBuffer.rewind();
        // 准备输出
        int batchSize = 1;
        int outputShape = 200;
        int outputClass = 1431;
        ByteBuffer outputBuffer = ByteBuffer.allocateDirect(batchSize * outputShape*outputClass * Float.SIZE / Byte.SIZE);
        outputBuffer.order(ByteOrder.nativeOrder());

        // 进行推理
        interpreter.run(inputBuffer, outputBuffer);
        Log.d(TAG, "outputBuffer：" + outputBuffer.remaining());
        // 解析输出
        float[][] outputArray = new float[outputShape][outputClass];
        outputBuffer.rewind();
        for (int i = 0; i < outputShape; i++) {
            for (int j = 0; j < outputClass; j++) {
                outputArray[i][j] = outputBuffer.getFloat();
            }
        }
        return outputArray;
    }

    public static String greedyDecode(float[][] logits, String[] alphabet) {
        StringBuilder decoded = new StringBuilder();
        int prevIndex = -1;

        for (float[] logit : logits) {
            int maxIndex = 0;
            float maxValue = logit[0];

            for (int i = 1; i < logit.length; i++) {
                if (logit[i] > maxValue) {
                    maxValue = logit[i];
                    maxIndex = i;
                }
            }

            if (maxIndex != prevIndex && maxIndex < alphabet.length) {
                decoded.append(alphabet[maxIndex]);
            }
            prevIndex = maxIndex;
        }
        return decoded.toString();
    }
    public static String ensureSpaceAfterDigits(String input) {
        // 去除输入字符串的首尾空格
        input = input.trim();
        String result = input;
        // 使用正则表达式匹配数字后面不是空格的情况并插入空格
//        String result = input.replaceAll("(\\d)(?=\\S)", "$1 ");

        // 确保返回的字符串没有首尾空格
        return result.trim();
    }
}