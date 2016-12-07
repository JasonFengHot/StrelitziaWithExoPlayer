package tv.ismar.app.util;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;

/**
 * Created by longhai on 16-10-26.
 */

public class FileUtils {

    public static String getFileByUrl(String httpUrl) {
        try {
            URL url = new URL(httpUrl);
            String file = url.getFile();
            File localFile = new File(file);
            String fileName = localFile.getName();
            if (fileName.contains("?")) {
                int index = fileName.indexOf("?");
                fileName = fileName.substring(0, index);
            }
            return fileName;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getMd5ByFile(File file) {
        String value;
        FileInputStream in;
        try {
            in = new FileInputStream(file);
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024 * 1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                messageDigest.update(buffer, 0, length);
            }
            BigInteger bi = new BigInteger(1, messageDigest.digest());
            value = bi.toString(16);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        int offset = 32 - value.length();
        if (offset > 0) {
            String data = new String();
            for (int i = 0; i < offset; i++) {
                data = data + "0";
            }
            value = data + value;
        }
        return value;
    }

    public boolean isFileExist(String filePath) {
        File f = new File(filePath);
        return f.exists();
    }

    public File createDir(String dirPath) throws IOException {
        File d = new File(dirPath);
        if (!d.isDirectory()) {
            d.mkdir();
        }
        return d;
    }

    public static boolean deleteFile(String filename) throws IOException {
        File f = new File(filename);
        return f.delete();
    }

    /**
     * 删除目录（文件夹）下的所有文件
     *
     * @param sPath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDir(String sPath) throws IOException {
        //如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        //如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        //删除文件夹下的所有文件(包括子目录)
        File[] files = dirFile.listFiles();
        if(files == null || files.length == 0){
            Log.i("LH/", "deleteDir empty");
            return false;
        }
        for (int i = 0; i < files.length; i++) {
            //删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag)
                    break;
            } //删除子目录
            else {
                flag = deleteDir(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag)
            return false;

        return true;
    }

}
