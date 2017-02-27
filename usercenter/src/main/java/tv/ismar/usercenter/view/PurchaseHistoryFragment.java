package tv.ismar.usercenter.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.AppConstant;
import tv.ismar.app.BaseFragment;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.Util;
import tv.ismar.app.network.entity.AccountsOrdersEntity;
import tv.ismar.usercenter.PurchaseHistoryContract;
import tv.ismar.usercenter.R;
import tv.ismar.usercenter.databinding.FragmentPurchasehistoryBinding;
import tv.ismar.usercenter.viewmodel.PurchaseHistoryViewModel;

/**
 * Created by huibin on 10/27/16.
 */

public class PurchaseHistoryFragment extends BaseFragment implements PurchaseHistoryContract.View {
    private static final String TAG = PurchaseHistoryFragment.class.getSimpleName();
    private PurchaseHistoryViewModel mViewModel;
    private PurchaseHistoryContract.Presenter mPresenter;


    public static PurchaseHistoryFragment newInstance() {
        return new PurchaseHistoryFragment();
    }

    private FragmentPurchasehistoryBinding purchasehistoryBinding;

    private RelativeLayout mRecyclerView;

    private boolean fragmentIsPause = false;


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
        View purchaseHistoryIndicator = mUserCenterActivity.findViewById(R.id.usercenter_purchase_history);
        purchaseHistoryIndicator.setNextFocusRightId(purchaseHistoryIndicator.getId());
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
        purchasehistoryBinding = FragmentPurchasehistoryBinding.inflate(inflater, container, false);
        purchasehistoryBinding.setTasks(mViewModel);
        purchasehistoryBinding.setActionHandler(mPresenter);

        mRecyclerView = purchasehistoryBinding.recyclerview;
        View root = purchasehistoryBinding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        mPresenter.start();

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
        AppConstant.purchase_page = "history";
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

    public void setViewModel(PurchaseHistoryViewModel viewModel) {
        mViewModel = viewModel;
    }

    @Override
    public void setPresenter(PurchaseHistoryContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void loadAccountOrders(AccountsOrdersEntity accountsOrdersEntity) {


        ArrayList<AccountsOrdersEntity.OrderEntity> arrayList = new ArrayList<AccountsOrdersEntity.OrderEntity>();
        if (!TextUtils.isEmpty(IsmartvActivator.getInstance().getAuthToken()) && !TextUtils.isEmpty(IsmartvActivator.getInstance().getUsername())) {


            for (AccountsOrdersEntity.OrderEntity entity : accountsOrdersEntity.getOrder_list()) {
                entity.type = "order_list";
                arrayList.add(entity);
            }
            for (AccountsOrdersEntity.OrderEntity entity : accountsOrdersEntity.getSn_order_list()) {
                entity.type = "snorder_list";
                arrayList.add(entity);
            }

        } else {
            for (AccountsOrdersEntity.OrderEntity entity : accountsOrdersEntity.getSn_order_list()) {
                entity.type = "snorder_list";
                arrayList.add(entity);
            }
        }
        View purchaseHistoryIndicator = mUserCenterActivity.findViewById(R.id.usercenter_purchase_history);

        if (arrayList.isEmpty()) {
            purchaseHistoryIndicator.setNextFocusRightId(purchaseHistoryIndicator.getId());
        } else {
            purchaseHistoryIndicator.setNextFocusRightId(View.NO_ID);
        }
        createHistoryListView(arrayList);
    }


    private void createHistoryListView(ArrayList<AccountsOrdersEntity.OrderEntity> orderEntities) {
        mRecyclerView.removeAllViews();
        if (mUserCenterActivity == null) {
            return;
        }
//            if(!orderEntities.isEmpty()){
//                purchaseHasData = true;
//            }

        for (int i = 0; i < orderEntities.size(); i++) {
            View convertView = LayoutInflater.from(mUserCenterActivity).inflate(R.layout.item_purchase_history, null);

            AccountsOrdersEntity.OrderEntity item = orderEntities.get(i);

            TextView title = (TextView) convertView.findViewById(R.id.orderlistitem_title);
            TextView buydate_txt = (TextView) convertView.findViewById(R.id.orderlistitem_time);
            TextView orderlistitem_remainday = (TextView) convertView.findViewById(R.id.orderlistitem_remainday);
            TextView totalfee = (TextView) convertView.findViewById(R.id.orderlistitem_cost);
            ImageView icon = (ImageView) convertView.findViewById(R.id.orderlistitem_icon);
            TextView orderlistitem_paychannel = (TextView) convertView.findViewById(R.id.orderlistitem_paychannel);
            TextView purchaseExtra = (TextView) convertView.findViewById(R.id.purchase_extra);
            TextView mergeTxt = (TextView) convertView.findViewById(R.id.orderlistitem_merge);

            icon.setTag(i);


            String orderday = mUserCenterActivity.getResources().getString(R.string.personcenter_orderlist_item_orderday);
            String remainday = mUserCenterActivity.getResources().getString(R.string.personcenter_orderlist_item_remainday);
            String cost = mUserCenterActivity.getResources().getString(R.string.personcenter_orderlist_item_cost);
            String paySource = mUserCenterActivity.getResources().getString(R.string.personcenter_orderlist_item_paysource);
            title.setText(item.getTitle());
            buydate_txt.setText(String.format(orderday, item.getStart_date()));
            orderlistitem_remainday.setText(String.format(remainday, remaindDay(item.getExpiry_date())));
            Log.d(TAG, "remainday: " + remaindDay(item.getExpiry_date()));
            totalfee.setText(String.format(cost, item.getTotal_fee()));
            orderlistitem_paychannel.setText(String.format(paySource, getValueBySource(item.getSource())));
            if (!TextUtils.isEmpty(item.getThumb_url()))
                Picasso.with(mUserCenterActivity).load(item.getThumb_url()).memoryPolicy(MemoryPolicy.NO_STORE).config(Bitmap.Config.RGB_565).into(icon);
            if (!TextUtils.isEmpty(item.getInfo())) {
                String account = item.getInfo().split("@")[0];
                String mergedate = item.getInfo().split("@")[1];
                SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd");
                String mergeTime = time.format(Timestamp.valueOf(mergedate));

                if (item.type.equals("order_list")) {
                    purchaseExtra.setText("( " + mergeTime + "合并至视云账户" + IsmartvActivator.getInstance().getUsername() + " )");
                } else if (item.type.equals("snorder_list")) {
                    purchaseExtra.setText(mergeTime + "合并至视云账户" + account);
                }

                purchaseExtra.setVisibility(View.VISIBLE);
                mergeTxt.setVisibility(View.INVISIBLE);
            } else {
                purchaseExtra.setVisibility(View.INVISIBLE);
                mergeTxt.setVisibility(View.INVISIBLE);
            }
            convertView.setTag(item);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i == 0) {
                convertView.setId(R.id.balance_pay);
            } else {
                convertView.setId(R.id.balance_pay + i * 5);
                params.addRule(RelativeLayout.BELOW, R.id.feedback_time + 5 * (i - 1));
            }
            mRecyclerView.addView(convertView,params);
            ImageView imageView = new ImageView(mUserCenterActivity);
            imageView.setBackgroundResource(R.color.history_divider);
            imageView.setId(R.id.feedback_time+i * 5);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 1);
            layoutParams.bottomMargin = 4;
            layoutParams.topMargin =4;
            layoutParams.addRule(RelativeLayout.BELOW,convertView.getId());
            imageView.setLayoutParams(layoutParams);
            mRecyclerView.addView(imageView);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AccountsOrdersEntity.OrderEntity orderEntity = (AccountsOrdersEntity.OrderEntity) v.getTag();
                    if (TextUtils.isEmpty(orderEntity.getUrl())) {
                        Toast.makeText(mUserCenterActivity, "url is empty!!!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<String> pathSegments = Uri.parse(orderEntity.getUrl()).getPathSegments();
                    String pk = pathSegments.get(pathSegments.size() - 1);
                    String type = pathSegments.get(pathSegments.size() - 2);
                    PageIntent pageIntent = new PageIntent();
                    switch (type) {
                        case "package":
                            pageIntent.toPackageDetail(mUserCenterActivity, "history", Integer.parseInt(pk));
                            break;
                        case "item":
                            pageIntent.toDetailPage(mUserCenterActivity, "history", Integer.parseInt(pk));
                            break;
                        default:
                            throw new IllegalArgumentException(orderEntity.getUrl() + " type not support!!!");
                    }
                }
            });

            convertView.setOnHoverListener(new View.OnHoverListener() {
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
                            Log.d(TAG, "MotionEvent.ACTION_HOVER_EXIT");
                            if (!fragmentIsPause) {
                                purchasehistoryBinding.mainupView.requestFocus();
                                purchasehistoryBinding.mainupView.requestFocusFromTouch();
                            }
                            break;
                    }
                    return false;
                }
            });
            if (i == 0) {
//                convertView.setId(R.id.purchase_last_item_id);
                convertView.setNextFocusUpId(convertView.getId());
            }
            if(i == orderEntities.size() -1){
                convertView.setNextFocusDownId(convertView.getId());
            }
            convertView.setNextFocusLeftId(R.id.usercenter_purchase_history);
            icon.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    int tag = (int) v.getTag();
                    if (tag == 0 && keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        return true;
                    }
                    return false;
                }
            });

//            if (i != orderEntities.size() - 1) {
//                ImageView imageView = new ImageView(mUserCenterActivity);
//                imageView.setBackgroundResource(R.color.history_divider);
//                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
//                imageView.setLayoutParams(layoutParams);
//                mRecyclerView.addView(imageView);
//            }
        }

    }


    private String getValueBySource(String source) {
        if (source.equals("weixin")) {
            return "微信";
        } else if (source.equals("alipay")) {
            return "支付宝";
        } else if (source.equals("balance")) {
            return "余额";
        } else if (source.equals("card")) {
            return "卡";
        } else {
            return source;
        }
    }

    private int remaindDay(String exprieTime) {
        try {
            return Util.daysBetween(Util.getTime(), exprieTime) + 1;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }


}
