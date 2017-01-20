package tv.ismar.usercenter.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.open.androidtvwidget.leanback.recycle.LinearLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseFragment;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.PageIntentInterface;
import tv.ismar.app.core.Util;
import tv.ismar.app.network.entity.AccountBalanceEntity;
import tv.ismar.app.network.entity.AccountPlayAuthEntity;
import tv.ismar.app.ui.MessageDialogFragment;
import tv.ismar.usercenter.R;
import tv.ismar.usercenter.UserInfoContract;
import tv.ismar.usercenter.databinding.FragmentUserinfoBinding;
import tv.ismar.usercenter.viewmodel.UserInfoViewModel;

import static tv.ismar.app.core.PageIntentInterface.EXTRA_PRODUCT_CATEGORY;
import static tv.ismar.app.network.entity.AccountPlayAuthEntity.PlayAuth;

/**
 * Created by huibin on 10/27/16.
 */

public class UserInfoFragment extends BaseFragment implements UserInfoContract.View,
        IsmartvActivator.AccountChangeCallback, View.OnHoverListener,
        OnFocusChangeListener, LinearLayoutManagerTV.FocusSearchFailedListener {
    private static final String TAG = UserInfoFragment.class.getSimpleName();
    private UserInfoViewModel mViewModel;
    private UserInfoContract.Presenter mPresenter;

    public static final int REQUEST_CHARGE = 0xAC45;


    private RecyclerViewTV privilegeRecyclerView;
    private boolean showChargeSuccessPop = false;


    public static UserInfoFragment newInstance() {
        return new UserInfoFragment();
    }

    private FragmentUserinfoBinding userinfoBinding;

    private List<View> privilegeView;

    private boolean framgentIsPause = false;

    private UserCenterActivity mUserCenterActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mUserCenterActivity = (UserCenterActivity) activity;
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
        userinfoBinding = FragmentUserinfoBinding.inflate(inflater, container, false);
        userinfoBinding.setTasks(mViewModel);
        userinfoBinding.setActionHandler(mPresenter);
//        userinfoBinding.fragmentContainer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (userinfoBinding.exitAccount.getVisibility() == View.VISIBLE) {
//                    userinfoBinding.exitAccount.requestFocus();
//                }
//            }
//        });

        privilegeRecyclerView = userinfoBinding.privilegeRecycler;
        privilegeRecyclerView.setSelectedItemAtCentered(false);
        privilegeRecyclerView.addItemDecoration(new SpacesItemDecoration(getResources().getDimensionPixelSize(R.dimen.privilege_item_margin_bottom)));
        View root = userinfoBinding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        userinfoBinding.exitAccount.setOnHoverListener(this);
        userinfoBinding.chargeMoney.setOnHoverListener(this);

        userinfoBinding.exitAccount.setNextFocusDownId(R.id.exit_account);
        userinfoBinding.exitAccount.setNextFocusLeftId(R.id.charge_money);

        userinfoBinding.chargeMoney.setNextFocusUpId(R.id.exit_account);
        userinfoBinding.chargeMoney.setNextFocusRightId(R.id.exit_account);
        userinfoBinding.chargeMoney.setNextFocusDownId(R.id.btn);
        userinfoBinding.chargeMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("tv.ismar.pay.payment");
                intent.putExtra(EXTRA_PRODUCT_CATEGORY, PageIntentInterface.ProductCategory.charge.name());
                startActivityForResult(intent, REQUEST_CHARGE);
            }
        });

        userinfoBinding.tmp.setNextFocusLeftId(R.id.usercenter_userinfo);
        mPresenter.start();
        userinfoBinding.exitAccount.setOnFocusChangeListener(this);
        userinfoBinding.chargeMoney.setOnFocusChangeListener(this);

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
        framgentIsPause = false;
        Log.d(TAG, "onResume");
        privilegeRecyclerView.setVisibility(View.INVISIBLE);
        mUserCenterActivity.changeUserInfoSelectStatus();
        mPresenter.fetchPrivilege();
        mPresenter.fetchBalance();


    }

    @Override
    public void onPause() {
        framgentIsPause = true;
        mPresenter.stop();
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
        privilegeRecyclerView.setVisibility(View.VISIBLE);
        mViewModel.refresh();
        ArrayList<AccountPlayAuthEntity.PlayAuth> playAuths = new ArrayList<>();

        playAuths.addAll(entity.getSn_playauth_list());
        playAuths.addAll(entity.getPlayauth_list());
        LinearLayoutManagerTV linearLayoutManagerTV = new LinearLayoutManagerTV(getContext());
        linearLayoutManagerTV.setFocusSearchFailedListener(this);
        privilegeRecyclerView.setLayoutManager(linearLayoutManagerTV);
        boolean flag = false;
        for (AccountPlayAuthEntity.PlayAuth element : playAuths) {
            if (element.getAction() == AccountPlayAuthEntity.Action.watch || element.getAction() == AccountPlayAuthEntity.Action.repeat_buy) {
                flag = true;
                break;
            }
        }
        if (!flag)
            userinfoBinding.chargeMoney.setNextFocusDownId(R.id.charge_money);
        PrivilegeAdapter privilegeAdapter = new PrivilegeAdapter(getContext(), playAuths);
        privilegeRecyclerView.setAdapter(privilegeAdapter);



    }

    @Override
    public void loadBalance(AccountBalanceEntity entity) {
//        UserCenterActivity userCenterActivity = (UserCenterActivity) getActivity();
        View userInfo = mUserCenterActivity.findViewById(R.id.usercenter_userinfo);

        if (entity.getBalance().add(entity.getSn_balance()).setScale(2).equals(new BigDecimal(0).setScale(2))) {
            if (IsmartvActivator.getInstance().isLogin()) {
                userInfo.setNextFocusRightId(R.id.exit_account);
            } else {
                userInfo.setNextFocusRightId(userInfo.getId());
            }
            userinfoBinding.exitAccount.setNextFocusDownId(R.id.btn);
            userinfoBinding.exitAccount.setNextFocusLeftId(R.id.usercenter_userinfo);
            if (privilegeView != null) {
                for (View v : privilegeView) {
                    v.setNextFocusLeftId(View.NO_ID);
                }
            }
        } else {
            userInfo.setNextFocusRightId(R.id.charge_money);
            userinfoBinding.exitAccount.setNextFocusDownId(R.id.charge_money);
            if (privilegeView != null) {
                for (View v : privilegeView) {
                    v.setNextFocusLeftId(R.id.usercenter_userinfo);
                }
            }
        }
        mViewModel.refresh();
        userinfoBinding.chargeMoney.setNextFocusLeftId(R.id.usercenter_userinfo);

        if (showChargeSuccessPop) {
            showChargeSuccessPop = false;
            showChargeSuccessPop(entity.getBalance().add(entity.getSn_balance()).setScale(2));
        }
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
        if (mUserCenterActivity.isExpireAccessToken) {
            mUserCenterActivity.isExpireAccessToken = false;
        } else {
            showExitAccountMessagePop();
        }

        mPresenter.fetchBalance();
        mPresenter.fetchPrivilege();
        mViewModel.refresh();
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                if (!v.hasFocus()) {
                    ((UserCenterActivity) getActivity()).clearTheLastHoveredVewState();
                    v.requestFocus();
                    v.requestFocusFromTouch();
                }
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                if (!framgentIsPause) {
                    userinfoBinding.tmp.requestFocus();
                    userinfoBinding.tmp.requestFocusFromTouch();
                }
                break;
        }

        return true;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        ((UserCenterActivity) getActivity()).clearTheLastHoveredVewState();
    }

    @Override
    public View onFocusSearchFailed(View view, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (focusDirection == View.FOCUS_DOWN) {
            privilegeRecyclerView.smoothScrollBy(0, getResources().getDimensionPixelSize(R.dimen.privilege_item_scroll));
        }
        if (focusDirection == View.FOCUS_UP) {
            if (!privilegeRecyclerView.canScrollVertically(-1)){
                return null;
            }
            privilegeRecyclerView.smoothScrollBy(0, getResources().getDimensionPixelSize(R.dimen.privilege_item_scroll) * -1);
            if (privilegeRecyclerView.getChildAt(0).findViewById(R.id.btn) == view&&!privilegeRecyclerView.canScrollVertically(-1)) {
                return null;
            }
        }
        return view;
    }

    public void showChargeSuccessPop(BigDecimal balance) {
        final MessageDialogFragment dialog = new MessageDialogFragment(getContext(), "当前账户余额" + balance + "元", null);
        dialog.showAtLocation(getView(), Gravity.CENTER, new MessageDialogFragment.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        dialog.dismiss();
                    }
                },
                null
        );
    }

    public void setShowChargeSuccessPop(boolean showChargeSuccessPop) {
        this.showChargeSuccessPop = showChargeSuccessPop;
    }


    private class PrivilegeAdapter extends RecyclerView.Adapter<PrivilegeViewHolder> implements View.OnClickListener {
        private Context mContext;

        private List<AccountPlayAuthEntity.PlayAuth> mPlayAuths;

        public PrivilegeAdapter(Context context, List<AccountPlayAuthEntity.PlayAuth> playAuths) {
            mContext = context;
            mPlayAuths = playAuths;
            privilegeView = new ArrayList<>();
        }

        @Override
        public PrivilegeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.privilege_listview_item, parent, false);
            PrivilegeViewHolder holder = new PrivilegeViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(PrivilegeViewHolder holder, int position) {
            PlayAuth playAuth = mPlayAuths.get(position);
            String remainday = mContext.getResources().getString(R.string.personcenter_orderlist_item_remainday);
            holder.date.setText(String.format(remainday, remaindDay(playAuth.getExpiry_date())));
            holder.title.setText(playAuth.getTitle());
            if (playAuth.getAction() == null) {
                holder.mButton.setVisibility(View.INVISIBLE);
            } else if (playAuth.getAction() == AccountPlayAuthEntity.Action.watch) {
                holder.mButton.setText("详情");
                holder.mButton.setVisibility(View.VISIBLE);
            } else if (playAuth.getAction() == AccountPlayAuthEntity.Action.repeat_buy) {
                holder.mButton.setText("续费");
                holder.mButton.setVisibility(View.VISIBLE);
            } else {
                holder.mButton.setVisibility(View.INVISIBLE);
            }
            if (holder.mButton.getVisibility() == View.VISIBLE) {
                holder.mButton.setId(R.id.btn);
                if (position == mPlayAuths.size() - 1) {
                    holder.mButton.setNextFocusDownId(holder.mButton.getId());
                }
            } else {
                holder.mButton.setId(View.NO_ID);
            }
            holder.mButton.setNextFocusLeftId(R.id.usercenter_userinfo);
            holder.mButton.setNextFocusRightId(holder.mButton.getId());
            holder.mButton.setTag(playAuth);
            holder.mButton.setOnHoverListener(UserInfoFragment.this);
            holder.mButton.setOnClickListener(this);
            privilegeView.add(holder.mButton);
        }

        @Override
        public int getItemCount() {
            return mPlayAuths.size();
        }

        private int remaindDay(String exprieTime) {
            try {
                return Util.daysBetween(Util.getTime(), exprieTime) + 1;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        public void onClick(View v) {
            PlayAuth playAuth = (PlayAuth) v.getTag();
            List<String> pathSegments = Uri.parse(playAuth.getUrl()).getPathSegments();
            String pk = pathSegments.get(pathSegments.size() - 1);
            String type = pathSegments.get(pathSegments.size() - 2);
            PageIntent pageIntent = new PageIntent();
            if (playAuth.getAction() == null) {
                //
            } else if (playAuth.getAction() == AccountPlayAuthEntity.Action.watch) {
                switch (type) {
                    case "package":
                        pageIntent.toPackageDetail(mContext, "privilege", Integer.parseInt(pk));
                        break;
                    case "item":
                        pageIntent.toDetailPage(mContext, "privilege", Integer.parseInt(pk));
                        break;
                    default:
                        Toast.makeText(mContext, "因版权限制，此产品无法在当前设备查看", Toast.LENGTH_SHORT).show();
                }
            } else if (playAuth.getAction() == AccountPlayAuthEntity.Action.repeat_buy) {
                switch (type) {
                    case "package":
                        pageIntent.toPackageDetail(mContext, "privilege", Integer.parseInt(pk));
                        break;
                    case "item":
                        pageIntent.toDetailPage(mContext, "privilege", Integer.parseInt(pk));
                        break;
                    default:
                        Toast.makeText(mContext, "因版权限制，此产品无法在当前设备查看", Toast.LENGTH_SHORT).show();
                }
            } else {
                //other type
            }
        }
    }

    private class PrivilegeViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private TextView date;
        private Button mButton;


        public PrivilegeViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title_txt);
            date = (TextView) itemView.findViewById(R.id.buydate_txt);
            mButton = (Button) itemView.findViewById(R.id.btn);
            mButton.setNextFocusRightId(R.id.charge_money);
        }
    }


    private class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {

            // Add top margin only for the first item to avoid double space between items
            int position = parent.getChildAdapterPosition(view);
            if (position != parent.getAdapter().getItemCount() - 1) {
                outRect.bottom = space;
            }
        }
    }
}
