package tv.ismar.usercenter.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import tv.ismar.app.AppConstant;
import tv.ismar.app.BaseFragment;
import tv.ismar.app.core.PageIntent;
import tv.ismar.usercenter.HelpContract;
import tv.ismar.usercenter.R;
import tv.ismar.usercenter.databinding.FragmentHelpBinding;
import tv.ismar.usercenter.viewmodel.HelpViewModel;

/**
 * Created by huibin on 10/27/16.
 */

public class HelpFragment extends BaseFragment implements HelpContract.View, View.OnHoverListener,View.OnFocusChangeListener{
    private static final String TAG = HelpFragment.class.getSimpleName();


    private HelpContract.Presenter mPresenter;
    private HelpViewModel mViewModel;
    private ImageView ismartv_icon;

    public static HelpFragment newInstance() {
        return new HelpFragment();
    }

    FragmentHelpBinding helpBinding;

    private boolean fragmentIsPause = false;

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

        helpBinding = FragmentHelpBinding.inflate(inflater, container, false);
        helpBinding.setTasks(mViewModel);
        helpBinding.setActionHandler(mPresenter);
        View root = helpBinding.getRoot();
        ismartv_icon = (ImageView) root.findViewById(R.id.ismartv_icon);
        ismartv_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PageIntent pageIntent = new PageIntent();
                pageIntent.toHelpPage(HelpFragment.this.getContext());
            }
        });
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        helpBinding.ismartvIcon.setOnHoverListener(this);
        helpBinding.tmp.setNextFocusLeftId(R.id.usercenter_help);
        helpBinding.getRoot().setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                helpBinding.tmp.requestFocus();
                return false;
            }
        });
        helpBinding.ismartvIcon.setNextFocusLeftId(R.id.usercenter_help);
        helpBinding.ismartvIcon.setOnFocusChangeListener(this);

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
        AppConstant.purchase_page = "customer";
        fragmentIsPause = false;
        Log.d(TAG, "onResume");

    }

    @Override
    public void onPause() {
        fragmentIsPause = true;
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
                ((UserCenterActivity) getActivity()).clearTheLastHoveredVewState();
                v.requestFocus();
                v.requestFocusFromTouch();
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                if (!fragmentIsPause) {
                    helpBinding.tmp.requestFocus();
                    helpBinding.tmp.requestFocusFromTouch();
                }
                break;
        }

        return true;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        ((UserCenterActivity) getActivity()).clearTheLastHoveredVewState();
    }
}
