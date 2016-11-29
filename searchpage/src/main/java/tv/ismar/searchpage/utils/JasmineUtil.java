package tv.ismar.searchpage.utils;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.PopupWindow;

import java.util.HashMap;

import tv.ismar.app.core.client.NetworkUtils;
import tv.ismar.app.network.entity.EventProperty;
import tv.ismar.searchpage.R;

/**
 * Created by admin on 2016/1/13.
 */
public class JasmineUtil {

    public static void scaleOut(View view){

        Animator animator= AnimatorInflater.loadAnimator(view.getContext(), R.animator.scaleout_hotword);
        animator.setTarget(view);
        animator.start();
    }
    public static void scaleIn(View view){

        Animator animator= AnimatorInflater.loadAnimator(view.getContext(), R.animator.scalein_hotword);
        animator.setTarget(view);
        animator.start();
    }
    public static void scaleOut1(View view){

        Animator animator= AnimatorInflater.loadAnimator(view.getContext(), R.animator.scaleout_poster);
        animator.setTarget(view);
        animator.start();
    }
    public static void scaleIn1(View view){

        Animator animator= AnimatorInflater.loadAnimator(view.getContext(), R.animator.scalein_poster);
        animator.setTarget(view);
        animator.start();
    }
    public static void showKeyboard(Context context, final View view) {
        ObjectAnimator showAnimator = ObjectAnimator.ofFloat(view, "translationX", -context.getResources().getDimensionPixelOffset(R.dimen.keyboard_width), 0);
        showAnimator.setDuration(500);
        showAnimator.start();

    }

    public static void hideKeyboard(Context context, View view) {
        ObjectAnimator showAnimator = ObjectAnimator.ofFloat(view, "translationX", 0, -context.getResources().getDimensionPixelOffset(R.dimen.keyboard_width));
        showAnimator.setDuration(500);
        showAnimator.start();

    }


    /**
     * 打开搜索app日志上报
     */
    public static void app_start(String sn,String device,String size,String os_version,long sd_size,long sd_free_size,String userid,String province,String city,String isp,String source,String Mac,String title,String code,String version) {
        HashMap<String, Object> tempMap = new HashMap<>();
        tempMap.put(EventProperty.SN, sn);
        tempMap.put(EventProperty.DEVICE, device);
        tempMap.put(EventProperty.SIZE, size);
        tempMap.put(EventProperty.OS_VERSION, os_version);
        tempMap.put(EventProperty.SD_SIZE, sd_size);
        tempMap.put(EventProperty.SD_FREE_SIZE, sd_free_size);
        tempMap.put(EventProperty.USER_ID, userid);
        tempMap.put(EventProperty.PROVINCE, province);
        tempMap.put(EventProperty.CITY, city);
        tempMap.put(EventProperty.ISP, isp);
        tempMap.put(EventProperty.SOURCE, source);
        tempMap.put(EventProperty.MAC, Mac);
        tempMap.put(EventProperty.TITLE,title);
        tempMap.put(EventProperty.CODE,code);
        tempMap.put(EventProperty.VERSION, version);
        String eventName = NetworkUtils.APP_START;
        HashMap<String, Object> properties = tempMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }

    /**
     * 点击搜索结果日志上报
     */
    public static void video_search_arrive(String qWord,String content_type,int item,int subitem,String title){
        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.Q, qWord);
        tempMap.put(EventProperty.CONTENT_TYPE, content_type);
        tempMap.put(EventProperty.ITEM, item);
        tempMap.put(EventProperty.SUBITEM, subitem);
        tempMap.put(EventProperty.TITLE, title);
        String eventName = NetworkUtils.VIDEO_SEARCH_ARRIVE;
        HashMap<String, Object> properties = tempMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }

    /**
     * 搜索日志上报
     */
    public static void video_search(String content_type,String qWord){
        HashMap<String, Object> tempMap = new HashMap<>();
        tempMap.put(EventProperty.INTERFACE_TYPE, "text");
        tempMap.put(EventProperty.CONTENT_TYPE, content_type);
        tempMap.put(EventProperty.Q, qWord);
        String eventName = NetworkUtils.VIDEO_SEARCH;
        HashMap<String, Object> properties = tempMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }
}
