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
tflite_model = converter.convert()

# Save TensorFlow Lite model
with open("save_path", 'wb') as f:
    f.write(tflite_model)
```

### Load the audio file.
### Spectrogram convert.
### Run inference using the TensorFlow Lite model.
