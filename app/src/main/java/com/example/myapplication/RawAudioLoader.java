package com.example.myapplication;
import android.content.Context;
import android.util.Log;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class RawAudioLoader {
    // 用來存儲文件名和資源ID的類
    public static class AudioFile {
        public int id;
        public String fileName;

        public AudioFile(int id, String fileName) {
            this.id = id;
            this.fileName = fileName;
        }
    }

    // 返回文件名中包含 'tts' 的資源文件信息
    public static List<AudioFile> getRawAudioFileIdsWithTTS(Context context) {
        List<AudioFile> rawAudioFiles = new ArrayList<>();
        Field[] fields = R.raw.class.getFields();  // 反射获取 R.raw 中的所有资源

        for (Field field : fields) {
            try {
                String fileName = field.getName();  // 获取资源文件的名称
                int resourceId = field.getInt(null);  // 获取资源ID
                rawAudioFiles.add(new AudioFile(resourceId, fileName));  // 将ID和文件名添加到列表
            } catch (IllegalAccessException e) {
                Log.e("RawAudioLoader", "Error accessing raw resource ID", e);
            }
        }

        return rawAudioFiles;
    }
}
