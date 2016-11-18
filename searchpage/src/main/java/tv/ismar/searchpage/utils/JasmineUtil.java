package tv.ismar.searchpage.utils;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.PopupWindow;

import tv.ismar.searchpage.R;

/**
 * Created by admin on 2016/1/13.
 */
public class JasmineUtil {

    public static String HOTWORDS_URL="/api/tv/hotwords/";
    public static String VODSEARCH_URL="/api/tv/vodsearch/";
    public static String SUGGEST_URL="/api/tv/suggest/";
    public static String RECOMMEND_URL="/api/tv/homepage/sharphotwords/8/";

    public static PopupWindow popupWindow;
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
        ObjectAnimator showAnimator = ObjectAnimator.ofFloat(view, "translationX", -601, 0);
        showAnimator.setDuration(500);
        showAnimator.start();

    }

    public static void hideKeyboard(Context context, View view) {
        ObjectAnimator showAnimator = ObjectAnimator.ofFloat(view, "translationX", 0, -601);
        showAnimator.setDuration(500);
        showAnimator.start();

    }



    /**
     * 判断是否有网络连接
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 判断wifi是否连接
     * @param context
     * @return
     */
    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }

}
