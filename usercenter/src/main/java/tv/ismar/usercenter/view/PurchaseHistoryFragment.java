package tv.ismar.usercenter.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import com.squareup.picasso.Picasso;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseFragment;
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

    private RecyclerViewTV mRecyclerView;

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

        HistoryAdapter adapter = new HistoryAdapter(getContext(), arrayList);
        mRecyclerView.setSelectedItemAtCentered(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(adapter);
        if (mPurchaseLoadCallback!= null){
            mPurchaseLoadCallback.onPurchaseLoadFinish();
        }

    }

    private class HistoryAdapter extends RecyclerView.Adapter<PurchaseHistoryFragment.HistoryViewHolder> implements View.OnHoverListener {
        private Context mContext;

        private List<AccountsOrdersEntity.OrderEntity> mOrderEntities;

        public HistoryAdapter(Context context, List<AccountsOrdersEntity.OrderEntity> orderEntities) {
            mContext = context;
            mOrderEntities = orderEntities;
        }

        @Override
        public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_purchase_history, parent, false);
            view.setOnHoverListener(this);
            HistoryViewHolder holder = new HistoryViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(HistoryViewHolder holder, int position) {

            AccountsOrdersEntity.OrderEntity item = mOrderEntities.get(position);


            holder.icon.setTag(position);


            String orderday = mContext.getResources().getString(R.string.personcenter_orderlist_item_orderday);
            String remainday = mContext.getResources().getString(R.string.personcenter_orderlist_item_remainday);
            String cost = mContext.getResources().getString(R.string.personcenter_orderlist_item_cost);
            String paySource = mContext.getResources().getString(R.string.personcenter_orderlist_item_paysource);
            holder.title.setText(item.getTitle());
            holder.buydate_txt.setText(String.format(orderday, item.getStart_date()));
            holder.orderlistitem_remainday.setText(String.format(remainday, remaindDay(item.getExpiry_date())));
            holder.totalfee.setText(String.format(cost, item.getTotal_fee()));
            holder.orderlistitem_paychannel.setText(String.format(paySource, getValueBySource(item.getSource())));
            if (!TextUtils.isEmpty(item.getThumb_url()))
                Picasso.with(mContext).load(item.getThumb_url()).into(holder.icon);
            if (!TextUtils.isEmpty(item.getInfo())) {
                String account = item.getInfo().split("@")[0];
                String mergedate = item.getInfo().split("@")[1];
                SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd");
                String mergeTime = time.format(Timestamp.valueOf(mergedate));

                if (item.type.equals("order_list")) {
                    holder.purchaseExtra.setText("( " + mergeTime + "合并至视云账户" + IsmartvActivator.getInstance().getUsername() + " )");
                } else if (item.type.equals("snorder_list")) {
                    holder.purchaseExtra.setText(mergeTime + "合并至视云账户" + account);
                }

                holder.purchaseExtra.setVisibility(View.VISIBLE);
                holder.mergeTxt.setVisibility(View.INVISIBLE);
            } else {
                holder.purchaseExtra.setVisibility(View.INVISIBLE);
                holder.mergeTxt.setVisibility(View.INVISIBLE);
            }

//            accountOrderListView.addView(convertView);
//            if (i == 0) {
//                icon.setId(R.id.purchase_history_list_first_id);
//                icon.setNextFocusUpId(icon.getId());
//            }
//            icon.setOnKeyListener(new View.OnKeyListener() {
//                @Override
//                public boolean onKey(View v, int keyCode, KeyEvent event) {
//                    int tag = (int) v.getTag();
//                    if (tag == 0 && keyCode == KeyEvent.KEYCODE_DPAD_UP) {
//                        return true;
//                    }
//                    return false;
//                }
//            });

            if (position != mOrderEntities.size() - 1) {
                ImageView imageView = new ImageView(mContext);
                imageView.setBackgroundResource(R.color.history_divider);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
                imageView.setLayoutParams(layoutParams);
//                accountOrderListView.addView(imageView);
            }
            if (position == mOrderEntities.size() - 1) {
                holder.itemView.setId(R.id.purchase_last_item_id);
                holder.itemView.setNextFocusDownId(R.id.purchase_last_item_id);
            }

        }

        @Override
        public int getItemCount() {
            return mOrderEntities.size();
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

        @Override
        public boolean onHover(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                case MotionEvent.ACTION_HOVER_MOVE:
                    if (!v.hasFocus()) {
                        v.requestFocus();
                        v.requestFocusFromTouch();
                    }
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    purchasehistoryBinding.mainupView.requestFocus();
                    purchasehistoryBinding.mainupView.requestFocusFromTouch();
                    break;

            }
            return false;
        }
    }

    private class HistoryViewHolder extends RecyclerView.ViewHolder {
        View itemView;

        TextView title;
        TextView buydate_txt;
        TextView totalfee;
        TextView orderlistitem_paychannel;
        TextView purchaseExtra;
        TextView orderlistitem_remainday;
        TextView mergeTxt;
        ImageView icon;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            title = (TextView) itemView.findViewById(R.id.orderlistitem_title);
            buydate_txt = (TextView) itemView.findViewById(R.id.orderlistitem_time);
            orderlistitem_remainday = (TextView) itemView.findViewById(R.id.orderlistitem_remainday);
            totalfee = (TextView) itemView.findViewById(R.id.orderlistitem_cost);
            icon = (ImageView) itemView.findViewById(R.id.orderlistitem_icon);
            orderlistitem_paychannel = (TextView) itemView.findViewById(R.id.orderlistitem_paychannel);
            purchaseExtra = (TextView) itemView.findViewById(R.id.purchase_extra);
            mergeTxt = (TextView) itemView.findViewById(R.id.orderlistitem_merge);
        }
    }

    public interface PurchaseLoadCallback {
        void onPurchaseLoadFinish();
    }

    PurchaseLoadCallback mPurchaseLoadCallback;

    public void setPurchaseLoadCallback(PurchaseLoadCallback purchaseLoadCallback) {
        mPurchaseLoadCallback = purchaseLoadCallback;
    }
}
