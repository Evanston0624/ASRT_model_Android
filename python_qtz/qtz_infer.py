import os
import wave
import numpy as np
import tensorflow as tf
os.environ["CUDA_VISIBLE_DEVICES"] = ""
class data_preprocess():
    def __init__(self, framesamplerate=16000, timewindow=25, timeshift=10):
        self.time_window = timewindow
        self.target_length = int(framesamplerate/10)
        self.window_length = int(framesamplerate / 1000 * self.time_window)  # 计算窗长度的公式，目前全部为400固定值
        self.timeshift = timeshift

        self.x = np.linspace(0, 400 - 1, 400, dtype=np.int64)
        self.w = 0.54 - 0.46 * np.cos(2 * np.pi * (self.x) / (400 - 1))  # 汉明窗

    def read_wav_data(self, filename: str) -> tuple:
        wav = wave.open(filename,"rb") # 打开一个wav格式的声音文件流
        num_frame = wav.getnframes() # 获取帧数
        num_channel=wav.getnchannels() # 获取声道数
        framerate=wav.getframerate() # 获取帧速率
        num_sample_width=wav.getsampwidth() # 获取实例的比特宽度，即每一帧的字节数
        str_data = wav.readframes(num_frame) # 读取全部的帧
        wav.close() # 关闭流
        # wave_data = np.fromstring(str_data, dtype = np.short) # 将声音文件数据转换为数组矩阵形式
        wave_data = np.frombuffer(str_data, dtype = np.short)
        wave_data.shape = -1, num_channel # 按照声道数将数组整形，单声道时候是一列数组，双声道时候是两列的矩阵
        wave_data = wave_data.T # 将矩阵转置

        return wave_data, framerate

    def Spectrogram(self, wavsignal, fs=16000):
        from scipy.fftpack import fft
        if fs != 16000:
            raise ValueError(
                f"[Error] ASRT currently only supports wav audio files with a sampling rate of 16000 Hz, but this "
                f"audio is {fs} Hz.")

        # wav波形 加时间窗以及时移10ms
        window_length = int(fs / 1000 * self.time_window)  # 计算窗长度的公式，目前全部为400固定值
        # wav_arr = np.array(wavsignal)
        range0_end = int(len(wavsignal[0]) / fs * 1000 - self.time_window) // 10 + 1  # 计算循环终止的位置，也就是最终生成的窗数

        data_input = np.zeros((range0_end, window_length // 2), dtype=np.float64)  # 用于存放最终的频率特征数据
        data_line = np.zeros((1, window_length), dtype=np.float64)

        for i in range(0, range0_end):
            p_start = i * 160
            p_end = p_start + 400

            data_line = wavsignal[0, p_start:p_end]
            data_line = data_line * self.w  # 加窗
            data_line = np.abs(fft(data_line))

            data_input[i] = data_line[0: window_length // 2]  # 设置为400除以2的值（即200）是取一半数据，因为是对称的
        data_input = np.log(data_input + 1)
        return data_input

    def adaptive_padding(self, input_data):
        input_data = input_data.astype(np.float32)

        input_data = np.expand_dims(input_data, axis=0)  # 添加批量维度
        input_data = np.expand_dims(input_data, axis=-1)  # 添加通道维度
        # 计算需要填充的长度
        current_length = input_data.shape[1]
        padding_length = max(0, self.target_length - current_length)
        # 计算填充宽度
        left_padding = padding_length // 2
        right_padding = padding_length - left_padding
        pad_width = [(0, 0), (left_padding, right_padding), (0, 0), (0, 0)]

        # 进行填充
        return np.pad(input_data, pad_width, mode='constant').astype(np.float32)

from tensorflow.keras import backend as K
def ctc_decoder(model_output, phoneme_dict):
    # ctc decode
    in_len = tf.constant([200], dtype=tf.int32) # 输入序列的長度
    in_len = np.zeros((1,), dtype=np.int32)
    in_len[0] = 200
    in_len = [200]
    r = K.ctc_decode(model_output, in_len, greedy=True, beam_width=100, top_paths=1)

    #convert to phoneme sequence
    opt_str = ""
    for index in r[0][0][0] :
        if index != -1 :
            opt_str += phoneme_dict[index] + " "
        else :
            opt_str = opt_str[:-1]
            break
    return opt_str

def load_dict(dict_path):
    phoneme_dict = []
    with open(dict_path, 'r') as f :
        for row in f.readlines():
            phoneme_dict.append(row.split('\t')[0])
    return phoneme_dict

class onnx_tool() :
    def __init__(self, model_path):
        self.load_model(model_path)

    def load_model(self, model_path):
        import onnxruntime
        # 載入 ONNX 模型
        onnx_filename = model_path  # 模型的文件名
        self.session = onnxruntime.InferenceSession(onnx_filename)
        self.input_name = self.session.get_inputs()[0].name  # 取得模型輸入名稱
        self.output_name = self.session.get_outputs()[0].name  # 取得模型輸出名稱

    def infer(self, audio_features):
        input_feed = {self.input_name: audio_features}  # create input dict
        return self.session.run([self.output_name], input_feed)[0]

class tflite_tool() :
    def __init__(self, model_path):
        self.load_model(model_path)

    def load_model(self, model_path):
        # # load tf-lite model
        self.interpreter = tf.lite.Interpreter(model_path=model_path)
        self.interpreter.allocate_tensors()

    def infer(self, audio_features):
        # tf-lite infer
        self.interpreter.set_tensor(self.interpreter.get_input_details()[0]['index'], audio_features)
        # 運行推理
        self.interpreter.invoke()
        # 获取输出结果
        return self.interpreter.get_tensor(self.interpreter.get_output_details()[0]['index'])
    

if __name__ == '__main__':
    dict_path = 'dict.txt'
    phoneme_dict = load_dict(dict_path)
    
    # model_path = './save_models/model.tflite'
    # tft = tflite_tool(model_path)

    model_path = './save_models/model18.onnx'
    tft = onnx_tool(model_path)

    audio_path = 'test1.wav'

    # data prepro
    # from speech_features import Spectrogram
    dp = data_preprocess()
    wav_signal, sample_rate = dp.read_wav_data(audio_path)
    audio_features = dp.Spectrogram(wavsignal=wav_signal, fs=sample_rate)
    audio_features = dp.adaptive_padding(input_data=audio_features)
    import time
    val = time.time()
    base_pred = tft.infer(audio_features)

    print('infer time : ', time.time()-val, '========')
    val = time.time()
    print(ctc_decoder(base_pred, phoneme_dict))
    print('ctc time : ', time.time()-val, '========')
