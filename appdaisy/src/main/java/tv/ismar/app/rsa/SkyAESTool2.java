package tv.ismar.app.rsa;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 用于sky devicetoken 解密
 * 
 * @author lion
 *
 */
public class SkyAESTool2 {
	/**
	 * 解密
	 * 
	 * @param keyWord
	 * @param content
	 * @return
	 */
	public static String decrypt(String keyWord, byte[] content) {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			SecretKeySpec key = new SecretKeySpec(keyWord.getBytes(), "AES");
			IvParameterSpec iv = new IvParameterSpec(getPosByte(content, 0, 16));
			cipher.init(Cipher.DECRYPT_MODE, key, iv);
			byte[] plain = cipher.doFinal(getPosByte(content, 16, content.length));

			String s  = new String(plain);
//			System.out.println(s);

			// 去除填充字符
			byte[] plainTemp = getPosByte(plain, plain.length - 1, plain.length);
			byte[] plainTemp2 = getPosByte(plain, 0, plain.length - (int) plainTemp[0]);

			return new String(plainTemp2);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private static byte[] getPosByte(byte[] content, int start, int end) {
		byte[] newContent = new byte[end - start];

		int k = 0;
		for (int i = start; i < end; i++) {
			newContent[k] = content[i];
			k++;
		}
		return newContent;
	}
}