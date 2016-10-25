package tv.ismar.app.util;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class SystemFileUtil {


    public static String LogPath="";
    public static String appPath="";
    static{
		String LOGLOCALPATH = "vodlog.txt";
	    String LOCALLOGDIR = "/tv/ismar/daisy";
		//File sdCardDir = Environment.getExternalStorageDirectory();
	    //LogPath = sdCardDir+File.separator+LOCALLOGDIR+File.separator+LOGLOCALPATH;
    };
	public static void readFile(String filePath, Context context) {

		FileInputStream istream;
		try {
			int len = -1;
			istream = context.openFileInput(filePath);
			byte[] buffer = new byte[1024];
			ByteArrayOutputStream ostream = new ByteArrayOutputStream();
			while ((len = istream.read(buffer)) != -1) {
				ostream.write(buffer, 0, len);
			}
			istream.close();

			ostream.close();

		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void writeFile(String content, String filePath,
			Context context) {

		FileOutputStream fos;
		try {
			fos = context.openFileOutput(filePath, Context.MODE_PRIVATE);
			fos.write(content.getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void writeSDCardFile(String content, String filePath) {

		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {

			File sdCardDir = Environment.getExternalStorageDirectory();

			File saveFile = new File(sdCardDir, filePath);

			FileOutputStream outStream;
			try {
				outStream = new FileOutputStream(saveFile);
				outStream.write("Hi,I’m ChaoYu".getBytes());
				outStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}
public static boolean isCanWriteSD(){
	if (Environment.getExternalStorageState().equals(
			Environment.MEDIA_MOUNTED)) {
	   	return true;
	}
	else{
		return false;
	}
}

	public static long getSdCardTotal(final Context context) {
		try{
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getBlockCount();
			long totalSize = totalBlocks * blockSize;
			return totalSize/1048576;
		}else{
			return 0;
		}
		}catch (IllegalArgumentException e) {
			return 0;
		}
	}

	public static long getSdCardAvalible(final Context context) {
		try {
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				File path = Environment.getExternalStorageDirectory();
				StatFs stat = new StatFs(path.getPath());
				long blockSize = stat.getBlockSize();
				long availableBlocks = stat.getAvailableBlocks();
				long availSize = availableBlocks * blockSize;
				return availSize / 1048576;
			} else {
				return 0;
			}
		} catch (IllegalArgumentException e) {
			return 0;
		}

	}

	public static synchronized void writeLogToLocal(String content) {
			String LOGLOCALPATH = "vodlog.txt";
		    LogPath = appPath+"/"+LOGLOCALPATH;
			File Dir = new File(appPath);
			if(!Dir.exists())
				Dir.mkdirs();
			
			File saveFile = new File(LogPath);
			if (!saveFile.exists()) {
				try {
					saveFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} 
				PrintWriter streamWriter = null;
				try {
					streamWriter = new PrintWriter(new FileOutputStream(
							saveFile, true));
					streamWriter.write(content + "\r\n");

					streamWriter.close();
				} catch (IOException EX) {
					System.out.println(EX.toString());
				} finally {
					if(streamWriter!=null)
					   streamWriter.close();
				}				
	}
    public static void delete(){
	   File f = new File(LogPath);
	   if(f.exists()){
		   f.delete();
	   }
   }
}
