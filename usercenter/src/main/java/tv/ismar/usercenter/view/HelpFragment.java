package tv.ismar.usercenter.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tv.ismar.usercenter.HelpContract;
import tv.ismar.usercenter.databinding.FragmentHelpBinding;
import tv.ismar.usercenter.viewmodel.HelpViewModel;

/**
 * Created by huibin on 10/27/16.
 */

public class HelpFragment extends Fragment implements HelpContract.View {
    private static final String TAG = HelpFragment.class.getSimpleName();


    private HelpContract.Presenter mPresenter;
    private HelpViewModel mViewModel;

    public static HelpFragment newInstance() {
        return new HelpFragment();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
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

        FragmentHelpBinding helpBinding = FragmentHelpBinding.inflate(inflater, container, false);
//        helpBinding.setTasks();

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
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
}