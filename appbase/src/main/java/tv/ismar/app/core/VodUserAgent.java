package tv.ismar.app.core;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.net.NetworkInterface;
import java.util.UUID;

public class VodUserAgent {
	
	
//	public static final String deviceType = "A11";
//	public static final String deviceVersion = "2.0";
	private static String sn = "";
	/**
	 * getMACAddress == getSn
	 * 
	 * @return Sn
	 */
	public static  String getMACAddress(){
		String mac = "001122334455";
			try{
				byte addr[];
				addr=NetworkInterface.getByName("eth0").getHardwareAddress();
				mac="";
				for(int i=0; i<6; i++){
					mac+=String.format("%02X",addr[i]);
				}
			}catch(Exception e){
				return mac;
			}

		return mac;
	}
	/**
	 * getHttpUserAgent
	 * 
	 * @return UserAgent
	 */

	public static String getHttpUserAgent(){
		String userAgent = "";
		userAgent = VodUserAgent.getModelName() + "/" + SimpleRestClient.appVersion + " " + SimpleRestClient.sn_token;
		return userAgent;
	}

	public static String getModelName(){
		if(Build.PRODUCT.length() > 20){
			return Build.PRODUCT.replaceAll(" ", "_").toLowerCase().substring(0,19);
		}else {
			return Build.PRODUCT.replaceAll(" ", "_").toLowerCase();
		}
	}
	  public static String getSerialNumber(Context context){
		    TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
	        String macAddr = "";
	        try{
	        	
	            sn = tm.getSimSerialNumber();
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	        	if(sn == null) {
		        	sn = "";
		        }
	        	if(macAddr == null) {
	        		macAddr = "";
	        	}
	        	//UUID deviceUuid = new UUID(sn.hashCode(), macAddr.hashCode());
	        	String uuid = UUID.randomUUID().toString();
	            //sn = deviceUuid.toString().replaceAll("-","");
	           // sn = deviceUuid.toString();
	        	sn = uuid;
	        }
	        return sn;
	    }
	//String access_token
	public static String getAccessToken(String token) {
//		String userAgent = deviceType + "/"+ deviceVersion + " " + getMACAddress().toUpperCase();
		String access_token = "";
		return access_token;
	}
	
	/**
	 * 获取媒体IP
	 */
	public static String getMediaIp(String str){
		String ip = "";
		String tmp = str.substring(7, str.length());
		int index = tmp.indexOf("/");
	    ip = tmp.substring(0, index);
		return ip;
	}
	
	/**
	 * 获取sid
	 */
	public static String getSid(String str){
		String sid = "";
		int index = str.indexOf("sid");
		if(index==-1)
			return "";
		
		String sidstr = str.substring(index, str.length());		
		int sep = sidstr.indexOf("&");		
		sid = sidstr.substring(4,sep);
		return sid;
	}
}
