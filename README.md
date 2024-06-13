# The ASRT model runs on Android.
## Summary
This project is based on ASRT:  
https://github.com/nl8590687/ASRT_SpeechRecognition

Convert the ASRT trained model to TensorFlow Lite and perform inference on Android.  

This project replicates the following components of ASRT on Android:  
[x] Spectrogram.  
[x] ASR inference.  
[x] CTC decoder(There are some differences, but it can correctly generate phoneme).  
[ ] Convert phoneme to words.  

- For any questions beyond code or procedures, feel free to contact me via email: 
- g192e1654k@gmail.com.
- ✨Magic ✨

### Convert the TensorFlow model to TensorFlow Lite.

If you want to convert the ASRT_model to ONNX, TensorFlow Lite, and Core ML, please refer to the readme.md at url : [Model_Quantization](https://github.com/Evanston0624/ASRT_model_Android/tree/main/python_qtz)
 

### android studio project setting
The packages used in this project need to be configured in build.gradle.kts:  
```
dependencies {
    implementation ("org.tensorflow:tensorflow-lite:2.8.0") //tf-lite
    implementation("com.github.wendykierp:JTransforms:3.1") // JTransforms
}
```
in AndroidManifest.xml :
```
<manifest ...
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    ...
```

## Run
The following code is included in MainActivity.  
### Load the audio file.
This project obtains speech data by reading audio files. If you acquire data through recording, please ensure that the recorded speech data format is PCM-16bit (value range from -32768 to 32767) and save it as a double list.  
```
// Check if audio file exists
boolean fileExists = isFileExists(this, R.raw.test);
if (fileExists) {
    // load audio to double list
    double[] audioData = DataLoader.readAsDouble(this, R.raw.test);
```
### Spectrogram convert
The Spectrogram method in this project refers to the relevant settings of ASRT. The method can be called as follows:  
```
// Spectrogram
Spectrogram spectrogram = new Spectrogram(16000, 25, 10);
float[][] result = spectrogram.calculateSpectrogram(audioData);
```
### Run inference
Perform paddging:
```
float[][] mdl_input = DataLoader.prepareInput(result);
```

#### TensorFlow Lite model
ASR infer:
```
float [][] mdl_output = tfliteInfer.runInference(mdl_input);
```

#### ctc decoder
The ctc_decoder in this project needs to use the dict.txt file used during training of ASRT. If you are using your own trained ASRT model for inference, you also need to use the dict.txt file used during training of that model.  
run ctc_decoder:  
```
String[] firstElements = DataLoader.processFile(this, "dict/dict.txt");
String ctc_output = tfliteInfer.greedyDecode(mdl_output, firstElements);
```

The ctc_output is a pronunciation string separated by spaces, such as:  
" kai1 qi3 tong1 zhi1 yan3 mian4 "  

### String comparison
This project has implemented a LevenshteinDistance class to calculate the difference between two strings.  
```
int distance = LevenshteinDistance.computeLevenshteinDistance(ctc_output, str2);
double similarity = LevenshteinDistance.similarity(ctc_output, str2);
```

### to-do list
1. Convert the pronunciation string to a sentence.  
2. word error rate.  
