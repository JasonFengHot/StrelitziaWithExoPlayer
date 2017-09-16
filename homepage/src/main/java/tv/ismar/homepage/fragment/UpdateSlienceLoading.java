package tv.ismar.homepage.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import tv.ismar.homepage.R;

/** Created by huibin on 22/12/2016. */
public class UpdateSlienceLoading extends Fragment {
    private ImageView updateLoadingImg;

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_update_slience, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateLoadingImg = (ImageView) view.findViewById(R.id.update_loading_img);
        Animation operatingAnim = AnimationUtils.loadAnimation(getContext(), R.anim.qpp_update_tip);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        updateLoadingImg.startAnimation(operatingAnim);
    }

    @Override
    public void onPause() {
        updateLoadingImg.clearAnimation();
        super.onPause();
    }
}
