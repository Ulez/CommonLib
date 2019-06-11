package com.ulez.bdxflibrary.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Ulez on 2019/5/8.
 * Email：1104128773@qq.com
 */
public class FileUtil {

    public static final String TAG = "FileUtil";

    /**
     * Sdcard是否存在
     */
    public static boolean isExitsSdcard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }


    /**
     * 获取录音输出地址。
     *
     * @param context
     * @param name
     * @return
     */
    public static String makeFilePath(Context context, String name) {
        long timeMillis = System.currentTimeMillis();
//        Log.e(TAG,"外部存储私有目录="+BASE_RECORD_PATH);
//        return getExternalFilesPath(context) + "/" + name + timeMillis / 1000 + ".mp3";
        return getExternalFilesPath(context) + "/" + name + ".mp3";
    }

    //    public static final String WAKEUP_SCENE_PCM_SAVE_PATH = TEST_DIR + "wakeupPCM/";
//    public static final String ASR_PCM_SAVE_PATH = TEST_DIR + "asrPCM/";

    public static String WAKEUP_SCENE_PCM_SAVE_PATH(Context context) {
        return getExternalFilesPath(context) + "/wakeupPCM/";
    }

    public static String ASR_PCM_SAVE_PATH(Context context) {
        return getExternalFilesPath(context) + "/asrPCM/";
    }

    /**
     * 获取外部存储私有路径：/storage/sdcard0/Android/data/com.ec.myrecord/files/
     *
     * @param context
     * @return
     */
    public static String getExternalFilesPath(Context context) {
        return context.getExternalFilesDir(null).getAbsolutePath();
    }

    /**
     * 读取文件内容为二进制数组
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public static byte[] read2ByteArray(String filePath) throws IOException {
//        FileInputStream in = new FileInputStream(filePath);
//        byte[] data = inputStreamToByteArray(in);
        InputStream in = new FileInputStream(filePath);
        byte[] data = inputStream2ByteArray(in);
        in.close();
        return data;
    }

    /**
     * 流转二进制数组
     *
     * @param in
     * @return
     * @throws IOException
     */
    private static byte[] inputStream2ByteArray(InputStream in) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];
        int n = 0;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }

    /**
     * 流转二进制数组
     *
     * @param in
     * @return
     * @throws IOException
     */
    private static byte[] inputStreamToByteArray(FileInputStream in) throws IOException {
        byte[] bArray = null;
        BufferedInputStream bufferedInputStream = null;
        DataInputStream dataInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        DataOutputStream dataOutputStream = null;
        try {
            bufferedInputStream = new BufferedInputStream(in);
            dataInputStream = new DataInputStream(bufferedInputStream);
            byteArrayOutputStream = new ByteArrayOutputStream();
            dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            byte[] buffer = new byte[1024];
            int n = 0;
            while ((n = dataInputStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, n);
            }
            bArray = byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) {
                in.close();
            }
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            if (dataInputStream != null) {
                dataInputStream.close();
            }
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
        }
        return bArray;
    }

    /**
     * 保存唤醒音频文件
     *
     * @param path
     */
    public static void createWakeupFile(String path, Context context) {
        // 创建目录
        File dstDir = new File(WAKEUP_SCENE_PCM_SAVE_PATH(context));
        if (!dstDir.exists()) {
            dstDir.mkdirs();
        }
        File file = new File(path);
        if (!file.exists()) {
            file = new File(path);
        }
    }

    /**
     * 保存识别音频文件
     *
     * @param path
     */
    public static void createASRFile(String path, Context context) {
        // 创建目录
        File dstDir = new File(ASR_PCM_SAVE_PATH(context));
        if (!dstDir.exists()) {
            dstDir.mkdirs();
        }
        File file = new File(path);
        if (!file.exists()) {
            file = new File(path);
        }
    }

    /**
     * 获取文件夹下的所有文件。
     * @param path
     * @return
     */
    public static ArrayList<String> getFiles(String path) {
        ArrayList<String> files = new ArrayList<String>();
        File file = new File(path);
        File[] tempList = file.listFiles();

        if (tempList==null||tempList.length==0)return files;
        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
//              System.out.println("文     件：" + tempList[i]);
                files.add(tempList[i].toString());
            }
            if (tempList[i].isDirectory()) {
//              System.out.println("文件夹：" + tempList[i]);
            }
        }
        return files;
    }

    /**
     * PCM文件转WAV文件
     *
     * @param inPcmFilePath  输入PCM文件路径
     * @param outWavFilePath 输出WAV文件路径
     * @param sampleRate     采样率，例如44100
     * @param channels       声道数 单声道：1或双声道：2
     * @param bitNum         采样位数，8或16
     */
    public static void convertPcm2Wav(String inPcmFilePath, String outWavFilePath, int sampleRate,
                                      int channels, int bitNum) {
        FileInputStream in = null;
        FileOutputStream out = null;
        byte[] data = new byte[1024];

        try {
            //采样字节byte率
            long byteRate = sampleRate * channels * bitNum / 8;

            in = new FileInputStream(inPcmFilePath);
            out = new FileOutputStream(outWavFilePath);

            //PCM文件大小
            long totalAudioLen = in.getChannel().size();

            //总大小，由于不包括RIFF和WAV，所以是44 - 8 = 36，在加上PCM文件大小
            long totalDataLen = totalAudioLen + 36;

            writeWaveFileHeader(out, totalAudioLen, totalDataLen, sampleRate, channels, byteRate);

            int length = 0;
            while ((length = in.read(data)) > 0) {
                out.write(data, 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 输出WAV文件
     *
     * @param out           WAV输出文件流
     * @param totalAudioLen 整个音频PCM数据大小
     * @param totalDataLen  整个数据大小
     * @param sampleRate    采样率
     * @param channels      声道数
     * @param byteRate      采样字节byte率
     * @throws IOException
     */
    private static void writeWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                            long totalDataLen, int sampleRate, int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);//数据大小
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //FMT Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';//过渡字节
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (byte) (channels * 16 / 8);
        header[33] = 0;
        //每个样本的数据位数
        header[34] = 16;
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

    public static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) file.delete();
    }

    /**
     * 输入文件流，获取文件byte数组
     * @param is
     * @return
     * @throws IOException
     */
    private byte[] InputStreamToByte(InputStream is) throws IOException {
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        int ch;
        while ((ch = is.read()) != -1) {
            bytestream.write(ch);
        }
        byte imgdata[] = bytestream.toByteArray();
        bytestream.close();
        return imgdata;
    }

    /**
     * 检查结果文件中是否存在pcm格式的音频文件，存在则转化为wav格式文件
     * @param context
     */
    private void checkPcmToWav(Context context) {
        ArrayList<String> asrFiles = FileUtil.getFiles(FileUtil.ASR_PCM_SAVE_PATH(context));
        for (String s : asrFiles) {
            pcm2wav(s);
        }
        ArrayList<String> wakeUps = FileUtil.getFiles(FileUtil.WAKEUP_SCENE_PCM_SAVE_PATH(context));
        for (String s : wakeUps) {
            pcm2wav(s);
        }
    }

    /**
     * 将pcm数据转化为wav数据
     * @param path
     */
    public static void pcm2wav(String path) {
//        Log.e(TAG,"pcm path="+ path);
        if (TextUtils.isEmpty(path)) return;
        if (path.contains(".pcm")) {
            String wavPath = path.replace(".pcm", ".wav");
            FileUtil.deleteFile(wavPath);
//            Log.e(TAG,"wavPath="+wavPath);
            FileUtil.convertPcm2Wav(path, wavPath, 16000, 1, 16);
            FileUtil.deleteFile(path);
        }
    }



    // 创建一个临时目录，用于复制临时文件，如assets目录下的离线资源文件
    public static String createTmpDir(Context context,String sampleDir) {
        String tmpDir = Environment.getExternalStorageDirectory().toString() + "/" + sampleDir;
        if (!FileUtil.makeDir(tmpDir)) {
            tmpDir = context.getExternalFilesDir(sampleDir).getAbsolutePath();
            if (!FileUtil.makeDir(sampleDir)) {
                throw new RuntimeException("create model resources dir failed :" + tmpDir);
            }
        }
        return tmpDir;
    }

    public static boolean fileCanRead(String filename) {
        File f = new File(filename);
        return f.canRead();
    }

    public static boolean makeDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            return file.mkdirs();
        } else {
            return true;
        }
    }

    public static void copyFromAssets(AssetManager assets, String source, String dest, boolean isCover)
            throws IOException {
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = assets.open(source);
                String path = dest;
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                }
            }
        }
    }
}
