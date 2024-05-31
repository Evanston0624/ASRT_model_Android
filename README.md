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
The ASR model in this project is trained using the THCHS30, ST-CMDS, AIShell-1, Primewords, MagicData, and Mozilla Common Voice 14.0 (TW) datasets, encompassing 1430 phonemes.  

If you wish to train an ASR model, please refer to ASRT:  
https://github.com/nl8590687/ASRT_SpeechRecognition  

You can convert the ASRT model to a TF-lite model using the following code:
```
import tensorflow as tf
# Please refer to the ASRT project for this loading class.  
from model_zoo.speech_model.keras_backend import SpeechModel251BN

# import ASRT model
AUDIO_LENGTH = 1600
AUDIO_FEATURE_LENGTH = 200
CHANNELS = 1
OUTPUT_SIZE = your_phoneme_num{int}
sm251bn = SpeechModel251BN(
    input_shape=(AUDIO_LENGTH, AUDIO_FEATURE_LENGTH, CHANNELS),
    output_size=OUTPUT_SIZE
)
sm251bn.load_weights("your_asrt_model_save_file")
trained_model, base_model = sm251bn.get_model()

# Convert to TensorFlow Lite model
converter = tf.lite.TFLiteConverter.from_keras_model(trained_model)
tflite_model = converter.convert
# Save TensorFlow Lite model
with open("save_path", 'wb') as f:
    f.write(tflite_model)
```

All parameters of this project are based on the configurations used during ASRT training. If you have questions about the parameters or need to adjust them, you should make the corresponding adjustments when training the ASRT model.  
Speech data format: sampling rate = 16000, pcm-16bit (value range from -32768 to 32767)  

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

### 字串比較
This project has implemented a LevenshteinDistance class to calculate the difference between two strings.  
```
int distance = LevenshteinDistance.computeLevenshteinDistance(ctc_output, str2);
double similarity = LevenshteinDistance.similarity(ctc_output, str2);
```

### to-do list
1. Convert the pronunciation string to a sentence.  
2. word error rate.  
