import os
from model_zoo.speech_model.keras_backend import SpeechModel251BN
import numpy as np
import tensorflow as tf

def load_tf_model(model_path, OUTPUT_SIZE=1431):
    # def load_tf_model(model_path):
    AUDIO_LENGTH = 1600
    AUDIO_FEATURE_LENGTH = 200
    CHANNELS = 1
    # 默认输出的拼音的表示大小是1428，即1427个拼音+1个空白块
    # OUTPUT_SIZE = 1431
    sm251bn = SpeechModel251BN(
        input_shape=(AUDIO_LENGTH, AUDIO_FEATURE_LENGTH, CHANNELS),
        output_size=OUTPUT_SIZE
    )
    sm251bn.load_weights('./save_models/SpeechModel251bn_cv/SpeechModel251bn_epoch40.model.h5')
    trained_model, base_model = sm251bn.get_model()
    return trained_model, base_model

def convert_tf_onnx(tf_model, save_path, opset):
    import tf2onnx
    trained_model = tf_model 

    # 轉換為 ONNX 格式
    onnx_model, _ = tf2onnx.convert.from_keras(trained_model, opset=opset)

    # 保存 ONNX 模型
    with open(save_path, 'wb') as f:
        f.write(onnx_model.SerializeToString())
    return os.path.isfile(save_path)

def convert_tf_coreML(tf_model, save_path):
    import coremltools as ct

    # 将TensorFlow模型转换为CoreML模型
    coreml_model = ct.convert(tf_model)

    # 保存CoreML模型
    coreml_model.save(save_path)

def convert_tf_lite(tf_model, save_path):
    # 轉換为 TensorFlow Lite 模型
    converter = tf.lite.TFLiteConverter.from_keras_model(tf_model)
    tflite_model = converter.convert()

    # 保存 TensorFlow Lite 模型
    with open(save_path, 'wb') as f:
        f.write(tflite_model)

def quantize_onnx(model_path, quant_type):
    import onnx
    from onnxruntime.quantization import quantize_dynamic, QuantType

    model_quant = '.'+model_path.split('.')[1]+'_q.'+model_path.split('.')[2]
    quantized_model = quantize_dynamic(model_path, model_quant,weight_type=QuantType.QUInt8)

def preprocess_onnx(model_path):
    output_path = '.'+model_path.split('.')[1]+'_p.'+model_path.split('.')[2]
    os.system(f'python3 -m onnxruntime.quantization.preprocess --input {model_path} --output {output_path}')

def load_onnx_infer(model_path):
    import onnxruntime
    import numpy as np
    # 載入 ONNX 模型
    onnx_filename = model_path  # 模型的文件名
    session = onnxruntime.InferenceSession(onnx_filename)
    return session

def load_coreML_infer(model_path):
    import coremltools as ct
    return ct.models.MLModel(model_path)

def load_tflite_infer(model_path):
    # # load tf-lite model
    interpreter = tf.lite.Interpreter(model_path=tflite_path)
    interpreter.allocate_tensors()
    return interpreter

if __name__ == '__main__':
    model_path = './save_models/SpeechModel251bn_cv/SpeechModel251bn_epoch40.model.h5'
    # load tf model
    trained_model, base_model = load_tf_model(model_path, OUTPUT_SIZE=1431)

    # opset = 18
    # save_path = f'./save_models/model{opset}.onnx'
    # # convert to onnx
    # convert_tf_onnx(base_model, save_path, opset=opset)

    # 
    # preprocess_onnx(onnx_path)
    # onnx_path = f'./save_models/model{opset}_p.onnx'
    # quantize_onnx(onnx_path, 'int8')

    # save_path = './save_models/model.mlmodel'
    # # convert to coreML
    # convert_tf_coreML(base_model, save_path=save_path)

    save_path = './save_models/model.tflite'
    # convert to tf-lite
    convert_tf_lite(base_model, save_path=save_path)