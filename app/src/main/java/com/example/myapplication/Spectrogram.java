package com.example.myapplication;

import org.jtransforms.fft.DoubleFFT_1D;

public class Spectrogram {
    private final int frameSampleRate;
    private final int timeWindow;
    private final int timeShift;

    public Spectrogram(int frameSampleRate, int timeWindow, int timeShift) {
        this.frameSampleRate = frameSampleRate;
        this.timeWindow = timeWindow;
        this.timeShift = timeShift;
    }

    public float[][] calculateSpectrogram(double[] wavSignal) {
        // 計算窗函數長度
        int windowLength = (frameSampleRate * timeWindow) / 1000;
        System.out.println("windowLength:"+windowLength);
        // 計算Hamming窗
        double[] hammingWindow = calculateHammingWindow(windowLength);

        // 计算 spectrogram
        // int numFrames = (signalLength - windowLength) / timeShift + 1;
        double numFrames_f = (((double)(wavSignal.length)/ frameSampleRate * 1000) - timeWindow);
        int numFrames = (int) Math.floor(numFrames_f/10)+1;
        System.out.println("numFrames:"+numFrames);
        float[][] spectrogram = new float[numFrames][windowLength / 2];

        for (int i = 0; i < numFrames; i++) {
            // get now frame data
            double[] frameSignal = new double[windowLength];
            System.arraycopy(wavSignal, i * timeShift*frameSampleRate/1000, frameSignal, 0, windowLength);
//            System.out.println("arraycopy_frameSignal.length:"+frameSignal.length);
            // windowing
            for (int j = 0; j < windowLength; j++) {
                frameSignal[j] *= hammingWindow[j];
            }
//            if (i==100) {
//                System.out.println("frameSignal: " + Arrays.toString(frameSignal));
//            }
            // FFT
            DoubleFFT_1D fft = new DoubleFFT_1D(windowLength);
            fft.realForward(frameSignal);
//            if (i==100) {
//                System.out.println("frameSignal: " + Arrays.toString(frameSignal));
//            }
            // Calculate power spectrum
            for (int j = 0; j < windowLength / 2; j++) {
                double real = frameSignal[2 * j];
                double imaginary = frameSignal[2 * j + 1];
                spectrogram[i][j] = (float)Math.sqrt(real * real + imaginary * imaginary);
            }
        }
        for (int i = 0; i < spectrogram.length; i++) {
            for (int j = 0; j < spectrogram[i].length; j++) {
                // 將元素加1，然後取對數
                spectrogram[i][j] = (float)Math.log(spectrogram[i][j] + 1);
            }
        }
        return spectrogram;
    }

    private double[] calculateHammingWindow(int windowLength) {
        double[] hammingWindow = new double[windowLength];
        for (int i = 0; i < windowLength; i++) {
            hammingWindow[i] = 0.54 - 0.46 * Math.cos(2 * Math.PI * i / (windowLength - 1));
        }
        return hammingWindow;
    }
}