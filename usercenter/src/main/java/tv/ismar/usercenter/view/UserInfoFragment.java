package tv.ismar.usercenter.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseFragment;
import tv.ismar.app.network.entity.AccountBalanceEntity;
import tv.ismar.app.network.entity.AccountPlayAuthEntity;
import tv.ismar.app.ui.MessageDialogFragment;
import tv.ismar.usercenter.R;
import tv.ismar.usercenter.UserInfoContract;
import tv.ismar.usercenter.databinding.FragmentUserinfoBinding;
import tv.ismar.usercenter.viewmodel.UserInfoViewModel;

/**
 * Created by huibin on 10/27/16.
 */

public class UserInfoFragment extends BaseFragment implements UserInfoContract.View,IsmartvActivator.AccountChangeCallback {
    private static final String TAG = UserInfoFragment.class.getSimpleName();
    private UserInfoViewModel mViewModel;
    private UserInfoContract.Presenter mPresenter;


    public static UserInfoFragment newInstance() {
        return new UserInfoFragment();
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
        IsmartvActivator.getInstance().addAccountChangeListener(this);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        final FragmentUserinfoBinding userinfoBinding = FragmentUserinfoBinding.inflate(inflater, container, false);
        userinfoBinding.setTasks(mViewModel);
        userinfoBinding.setActionHandler(mPresenter);
        userinfoBinding.fragmentContainer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (userinfoBinding.exitAccount.getVisibility() == View.VISIBLE){
                    userinfoBinding.exitAccount.requestFocus();
                }
            }
        });
        View root = userinfoBinding.getRoot();
        return root;
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
        mPresenter.start();

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
        IsmartvActivator.getInstance().removeAccountChangeListener(this);
        super.onDestroy();
        Log.d(TAG, "onDestroy");

    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
    }

    public void setViewModel(UserInfoViewModel viewModel) {
        mViewModel = viewModel;
    }

    @Override
    public void setPresenter(UserInfoContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void loadPrivilege(AccountPlayAuthEntity entity) {

    }

    @Override
    public void loadBalance(AccountBalanceEntity entity) {
        mViewModel.refresh();
    }


    public void showExitAccountConfirmPop() {
        final MessageDialogFragment dialog = new MessageDialogFragment(getContext(), getString(R.string.confirm_exit_account_text), null);
        dialog.showAtLocation(getView(), Gravity.CENTER, new MessageDialogFragment.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        dialog.dismiss();
                        IsmartvActivator.getInstance().removeUserInfo();

                    }
                },
                new MessageDialogFragment.CancelListener() {
                    @Override
                    public void cancelClick(View view) {
                        dialog.dismiss();
                    }
                }

        );


    }

    private void showExitAccountMessagePop() {
        final MessageDialogFragment dialog = new MessageDialogFragment(getContext(), getString(R.string.exit_account_message_text), null);
        dialog.showAtLocation(getView(), Gravity.CENTER, new MessageDialogFragment.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        dialog.dismiss();

                    }
                },
                null
        );
    }

    @Override
    public void onLogout() {
        showExitAccountMessagePop();
        mPresenter.fetchBalance();
        mPresenter.fetchPrivilege();
        mViewModel.refresh();
    }
}
