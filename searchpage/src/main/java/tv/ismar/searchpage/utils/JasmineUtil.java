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

}
