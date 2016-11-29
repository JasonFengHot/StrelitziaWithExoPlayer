package tv.ismar.usercenter.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import tv.ismar.app.BaseFragment;
import tv.ismar.usercenter.HelpContract;
import tv.ismar.usercenter.databinding.FragmentHelpBinding;
import tv.ismar.usercenter.viewmodel.HelpViewModel;

/**
 * Created by huibin on 10/27/16.
 */

public class HelpFragment extends BaseFragment implements HelpContract.View, View.OnHoverListener ,UserCenterActivity.IndicatorItemHoverCallback{
    private static final String TAG = HelpFragment.class.getSimpleName();


    private HelpContract.Presenter mPresenter;
    private HelpViewModel mViewModel;

    public static HelpFragment newInstance() {
        return new HelpFragment();
    }

    FragmentHelpBinding helpBinding;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");

        ((UserCenterActivity)getActivity()).setIndicatorItemHoverCallback(this);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        helpBinding = FragmentHelpBinding.inflate(inflater, container, false);
        helpBinding.setTasks(mViewModel);
        helpBinding.setActionHandler(mPresenter);
        View root = helpBinding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        helpBinding.ismartvIcon.setOnHoverListener(this);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");


    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
    }

    public void setViewModel(HelpViewModel viewModel) {
        mViewModel = viewModel;
    }

    @Override
    public void setPresenter(HelpContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                v.requestFocus();
                v.requestFocusFromTouch();
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                helpBinding.tmp.requestFocus();
                helpBinding.tmp.requestFocusFromTouch();
                break;
        }
        ((UserCenterActivity)getActivity()).clearTheLastHoveredVewState();
        return true;
    }

    @Override
    public void onIndicatorItemHover() {
        helpBinding.tmp.requestFocus();
        helpBinding.tmp.requestFocusFromTouch();
    }
}
