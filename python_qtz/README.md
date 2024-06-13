# The ASRT model runs on Android.
## Summary
This project is based on ASRT:  
https://github.com/nl8590687/ASRT_SpeechRecognition

to-do list:
[x] Convert
[x] Infer
[ ] Evaluation of model Quantization

This section uses Python to convert the ASRT model to ONNX, TensorFlow Lite, and Core ML formats. The performance results of quantization and other tests will be uploaded to this section after implementation.  


This project provides three converted models along with an ASRT model. You can use these three converted models for inference, or convert the ASRT model yourself. Of course, you can also convert your own models as needed.  

- For any questions beyond code or procedures, feel free to contact me via email: 
- g192e1654k@gmail.com.
- ✨Magic ✨

## Environment
1. python 3.8.10+
2. pip 22.0.4

If you are using Docker, you can use the image "nvcr.io/nvidia/tensorflow:22.03-tf1-py3".  


### Dependencies
This requirements.txt is not streamlined; it includes ASRT and other dependencies.  
```
pip install -r requirements.txt
```

### Convert the TensorFlow model to TensorFlow Lite.
If you want to convert the ASRT_model to ONNX, TensorFlow Lite, and Core ML, please refer to the readme.md at path ./python_qtz/.
The ASR model in this project is trained using the THCHS30, ST-CMDS, AIShell-1, Primewords, MagicData, and Mozilla Common Voice 14.0 (TW) datasets, encompassing 1430 phonemes.  


First, load the ASRT model:
```
import qtz_convert
# you need to replace it with your model path. such as:
# model_path = './ASRT/save_models/SpeechModel251bn/SpeechModel251bn.model.h5'
# You can also use the ASRT model trained in this project, which has 1430 pronunciation categories.
model_path = './ASRT/save_models/SpeechModel251bn_epoch40.model.h5'

trained_model, base_model = load_tf_model(model_path, OUTPUT_SIZE=1431)
```
OUTPUT_SIZE is the number of pronunciation categories output by ASRT. If you are using the default ASRT model, the size should be 1428.  

Next, You can convert the ASRT model to a TF-lite model using the following code:
```
save_path = './save_models/model.tflite'
# convert to tf-lite
convert_tf_lite(base_model, save_path=save_path)
```

You can convert the ASRT model to a onnx model using the following code:
```
opset = 18
save_path = f'./save_models/model{opset}.onnx'
# convert to onnx
convert_tf_onnx(base_model, save_path, opset=opset)
```

If you need the complete code or more examples, you can refer to qtz_convert.py and qtz_infer.py.  
```
if __name__ == '__main__':
...
```

All parameters of this project are based on the configurations used during ASRT training. If you have questions about the parameters or need to adjust them, you should make the corresponding adjustments when training the ASRT model.  
Speech data format: sampling rate = 16000, pcm-16bit (value range from -32768 to 32767)  


### Running inference with ONNX and TF Lite models
If you want to perform inference, you'll need to use the pronunciation list of ASRT (./ASRT/dict.txt). Please note that the phoneme list provided in this project differs from the original ASRT, and our model training data also varies from the original.  

Next, you'll also need a test audio file.   

The following code can be found in qtz_infer.py We will explain step by step what it does.  

load ONNX model:  
```
import qtz_infer
model_path = './save_models/model.tflite'
qtz_mdl = onnx_tool(model_path)
```

or load tflite model:  
```
import qtz_infer
model_path = './save_models/model.tflite'
qtz_mdl = tflite_tool(model_path)
```

The data preprocessing in this project utilizes the code from [ASRT](https://github.com/nl8590687/ASRT_SpeechRecognition), with all rights belonging to them.
  
Data preprocessing:  
```
dp = data_preprocess()
wav_signal, sample_rate = dp.read_wav_data(audio_path)
audio_features = dp.Spectrogram(wavsignal=wav_signal, fs=sample_rate)
audio_features = dp.adaptive_padding(input_data=audio_features)
```

The speech features are fed into the model to obtain an output vector, which is then processed by the CTC decoder to obtain the final predicted pronunciation.  
Inference:  
```
dict_path = 'dict.txt'
phoneme_dict = load_dict(dict_path)
base_pred = qtz_mdl.infer(audio_features)
print(ctc_decoder(base_pred, phoneme_dict))
```