package tv.ismar.detailpage.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import cn.ismartv.truetime.TrueTime;
import okhttp3.ResponseBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.BaseFragment;
import tv.ismar.app.core.DaisyUtils;
import tv.ismar.app.core.InitializeProcess;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.PageIntentInterface;
import tv.ismar.app.core.PageIntentInterface.PaymentInfo;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.VodUserAgent;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.network.entity.PlayCheckEntity;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.app.util.DeviceUtils;
import tv.ismar.app.util.SPUtils;
import tv.ismar.app.util.SystemFileUtil;
import tv.ismar.app.util.Utils;
import tv.ismar.detailpage.R;
import tv.ismar.pay.PaymentActivity;
import tv.ismar.statistics.DetailPageStatistics;
import tv.ismar.statistics.PurchaseStatistics;

import static tv.ismar.app.core.PageIntentInterface.EXTRA_ITEM_JSON;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_SOURCE;
import static tv.ismar.app.core.PageIntentInterface.PAYMENT;
import static tv.ismar.app.core.PageIntentInterface.PAYMENT_FAILURE_CODE;
import static tv.ismar.app.core.PageIntentInterface.PAYMENT_REQUEST_CODE;
import static tv.ismar.app.core.PageIntentInterface.PAYMENT_SUCCESS_CODE;
import static tv.ismar.app.core.PageIntentInterface.POSITION;
import static tv.ismar.app.core.PageIntentInterface.ProductCategory.Package;

/**
 * Created by huibin on 11/14/16.
 */

public class PackageDetailFragment extends BaseFragment {
    private TextView vod_payment_pacakge_title;
    private TextView vod_payment_packageDescribe_content;
    private LinearLayout mRelatedVideoContainer;
    private ImageView vod_payment_poster;
    private TextView vod_payment_price;
    private TextView vod_payment_duration;
    private Button vod_payment_item_more;
    private Button vod_payment_buyButton;
    private RelativeLayout detail_left_container;
    private LinearLayout detail_right_container;
    private ImageView isbuy_label;
    private ImageView mDetailQualityLabel;

    private RecyclerViewTV vod_payment_item_of_package_container;

    private View rootView;
    private SkyService mSkyService;

    private ItemEntity mItemEntity;
    private String source;
    private int position;

    private ItemEntity[] relatedItems;

    private DetailPageActivity mActivity;

    private DetailPageStatistics mPageStatistics;

    private List<ItemEntity> itemEntities;

    private boolean firstIn=false;
    private String frompage;

    public static PackageDetailFragment newInstance(String fromPage, String itemJson) {
        PackageDetailFragment fragment = new PackageDetailFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_SOURCE, fromPage);
        args.putString(EXTRA_ITEM_JSON, itemJson);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (DetailPageActivity) getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageStatistics = new DetailPageStatistics();
        Bundle bundle = getArguments();
        mItemEntity = new Gson().fromJson(bundle.getString(EXTRA_ITEM_JSON), ItemEntity.class);
        source = bundle.getString(EXTRA_SOURCE);
        position = bundle.getInt(POSITION,-1);
        frompage = getActivity().getIntent().getStringExtra("fromPage");
        if(frompage !=null&& frompage.equals("launcher")){
            ((BaseActivity)getActivity()).baseSection="";
            ((BaseActivity)getActivity()).baseChannel="";
            DaisyUtils.tempInitStaticVariable(getActivity());
            CallaPlay callaPlay = new CallaPlay();
            callaPlay.launcher_vod_click("item",mItemEntity.getPk(),mItemEntity.getTitle(),position);

            String province = (String) SPUtils.getValue(InitializeProcess.PROVINCE_PY, "");
            String city = (String) SPUtils.getValue(InitializeProcess.CITY, "");
            String isp = (String) SPUtils.getValue(InitializeProcess.ISP, "");
            callaPlay.app_start(IsmartvActivator.getInstance().getSnToken(),
                        VodUserAgent.getModelName(), DeviceUtils.getScreenInch(getActivity()),
                        android.os.Build.VERSION.RELEASE,
                        SimpleRestClient.appVersion,
                        SystemFileUtil.getSdCardTotal(getActivity().getApplicationContext()),
                        SystemFileUtil.getSdCardAvalible(getActivity().getApplicationContext()),
                        IsmartvActivator.getInstance().getUsername(), province, city, isp, frompage,
                        DeviceUtils.getLocalMacAddress(getActivity().getApplicationContext()),
                        SimpleRestClient.app, getActivity().getPackageName()
                );
        }
        mSkyService = SkyService.ServiceManager.getService();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_packagedetail, null);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firstIn=true;
        initView();
    }

    private void initView() {
        isbuy_label = (ImageView) rootView.findViewById(R.id.isbuy_label);
        detail_left_container = (RelativeLayout) rootView.findViewById(R.id.detail_left_container);
        detail_right_container = (LinearLayout) rootView.findViewById(R.id.detail_right_container);
        vod_payment_pacakge_title = (TextView) rootView.findViewById(R.id.vod_payment_pacakge_title);
        vod_payment_packageDescribe_content = (TextView) rootView.findViewById(R.id.vod_payment_packageDescribe_content);
        mRelatedVideoContainer = (LinearLayout) rootView.findViewById(R.id.related_video_container);
        vod_payment_item_of_package_container = (RecyclerViewTV) rootView.findViewById(R.id.vod_payment_item_of_package_container);
        vod_payment_poster = (ImageView) rootView.findViewById(R.id.vod_payment_poster);
        vod_payment_price = (TextView) rootView.findViewById(R.id.vod_payment_price);
        vod_payment_duration = (TextView) rootView.findViewById(R.id.vod_payment_duration);
        vod_payment_buyButton = (Button) rootView.findViewById(R.id.vod_payment_buyButton);
        vod_payment_item_more = (Button) rootView.findViewById(R.id.vod_payment_item_more);
        vod_payment_buyButton.setOnHoverListener(onHoverListener);
        vod_payment_item_more.setOnHoverListener(onHoverListener);
        vod_payment_buyButton.setFocusable(true);
        vod_payment_buyButton.requestFocus();
        vod_payment_buyButton.requestFocusFromTouch();
        vod_payment_buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PageIntent pageIntent = new PageIntent();
                PaymentInfo paymentInfo = new PaymentInfo(Package, mItemEntity.getPk(), PAYMENT);
                pageIntent.toPaymentForResult(getActivity(), "package", paymentInfo);
            }
        });
        vod_payment_item_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PageIntent pageIntent = new PageIntent();
                pageIntent.toPackageList(getContext(), "package", mItemEntity.getPk());
            }
        });

        loadView();
    }

    @Override
    public void onResume() {
        super.onResume();
        requestPlayCheck(String.valueOf(mItemEntity.getPk()));
        mPageStatistics.packageDetailIn(mItemEntity.getPk()+"", source==null?frompage:source);
        new PurchaseStatistics().expensePacketDetail(
                mItemEntity.getPk(),
                mItemEntity.getTitle(),
                mItemEntity.getExpense().getPrice(),
                "enter",
                TrueTime.now().getTime());

    }

    private void loadView() {
        if (mItemEntity != null) {
            vod_payment_pacakge_title.setText(mItemEntity.getTitle());
            vod_payment_packageDescribe_content.setText(mItemEntity.getDescription());
            if (mItemEntity.getItems().isEmpty()) {
                vod_payment_item_of_package_container.setVisibility(View.INVISIBLE);
                vod_payment_item_more.setEnabled(false);
                vod_payment_item_more.setFocusable(false);
            } else {
                vod_payment_item_of_package_container.setVisibility(View.VISIBLE);
                vod_payment_item_more.setEnabled(true);
                vod_payment_item_more.setFocusable(true);

                vod_payment_item_of_package_container.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                if (mItemEntity.getItems().size() > 3) {
                    itemEntities = mItemEntity.getItems().subList(0, 3);
                } else {
                    itemEntities = mItemEntity.getItems();
                }
                vod_payment_item_of_package_container.addItemDecoration(new SpacesItemDecoration(getResources().getDimensionPixelSize(R.dimen.package_detail_list_item_margin_left)));

                vod_payment_item_of_package_container.setOnItemClickListener(PackageItemClickListener);
            }

            Picasso.with(getContext())
                    .load(mItemEntity.getAdletUrl())
                    .into(vod_payment_poster);

            fetchRelated(mItemEntity.getPk());
            if (itemEntities != null && !itemEntities.isEmpty()) {
                vod_payment_item_of_package_container.setAdapter(new PackageItemAdapter(getContext(), itemEntities));
            }
        }
    }

    private void fetchRelated(long pk) {
        mSkyService.packageRelate(pk)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mActivity.new BaseObserver<ItemEntity[]>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(ItemEntity[] itemEntities) {
                        relatedItems = itemEntities;
                        if (relatedItems != null && relatedItems.length > 0) {
                            buildRelatedList();
                        }
                        detail_left_container.setVisibility(View.VISIBLE);
                        detail_right_container.setVisibility(View.VISIBLE);

                        if (((DetailPageActivity) getActivity()).mLoadingDialog != null &&
                                ((DetailPageActivity) getActivity()).mLoadingDialog.isShowing()) {
                            ((DetailPageActivity) getActivity()).mLoadingDialog.dismiss();
                        }
                    }
                });
    }


    private void buildRelatedList() {
        mRelatedVideoContainer.removeAllViews();
        List<ItemEntity> relatedList = Arrays.asList(relatedItems);
        relatedList = relatedList.subList(0, 4);

        for (ItemEntity relatedItem : relatedList) {
            RelativeLayout relatedHolder = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.item_related_layout, null);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    getResources().getDimensionPixelSize(R.dimen.item_detail_related_W),
                    getResources().getDimensionPixelSize(R.dimen.item_detail_related_H));
            relatedHolder.setLayoutParams(layoutParams);
            TextView titleView = (TextView) relatedHolder.findViewById(R.id.related_title);
            ImageView imgView = (ImageView) relatedHolder.findViewById(R.id.related_preview_img);
            TextView focusView = (TextView) relatedHolder.findViewById(R.id.related_focus);
            ImageView qualityLabel = (ImageView) relatedHolder.findViewById(R.id.related_quality_label);
            TextView related_price_txt = (TextView) relatedHolder.findViewById(R.id.related_price_txt);
            if (relatedItem.getExpense() != null) {
                related_price_txt.setVisibility(View.VISIBLE);
                related_price_txt.setText("￥" + relatedItem.getExpense().getPrice());
            }
            if (relatedItem.getQuality() == 3) {
                qualityLabel.setImageResource(R.drawable.label_hd_small);
            } else if (relatedItem.getQuality() == 4 || relatedItem.getQuality() == 5) {
                qualityLabel.setImageResource(R.drawable.label_uhd_small);
            }
            Picasso.with(getContext()).load(relatedItem.getAdletUrl()).into(imgView);
            titleView.setText(relatedItem.getTitle());
            focusView.setText(relatedItem.getFocus());
            relatedHolder.setTag(relatedItem);
            mRelatedVideoContainer.addView(relatedHolder);
            relatedHolder.setOnClickListener(mRelatedClickListener);
            relatedHolder.setOnHoverListener(onHoverListener);
        }
    }

    private class PackageItemAdapter extends RecyclerView.Adapter<PackageItemViewHolder> {
        private Context mContext;

        private List<ItemEntity> mObjects;


        public PackageItemAdapter(Context context, List<ItemEntity> objects) {
            mContext = context;
            mObjects = objects;
        }

        @Override

        public PackageItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_package_list, viewGroup, false);
            view.setTag(mObjects.get(i));
            PackageItemViewHolder holder = new PackageItemViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(PackageItemViewHolder holder, int position) {
            ItemEntity item = mObjects.get(position);
            holder.mTextView.setText(item.getTitle());
            Picasso.with(mContext).load(item.getAdletUrl()).error(R.drawable.error_hor).into(holder.mImageView);
        }

        @Override
        public int getItemCount() {
            return mObjects.size();
        }
    }

    private class PackageItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;
        private TextView mTextView;

        public PackageItemViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.ItemImage);
            final ImageView ItemdefaultImage = (ImageView) itemView.findViewById(R.id.ItemdefaultImage);
            mTextView = (TextView) itemView.findViewById(R.id.ItemText);
            ItemdefaultImage.setTag(itemView.getTag());
            ItemdefaultImage.setOnHoverListener(new View.OnHoverListener() {
                @Override
                public boolean onHover(View view, MotionEvent event) {
                    View v=mTextView;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_HOVER_ENTER:
                        case MotionEvent.ACTION_HOVER_MOVE:
                            v.setFocusable(true);
                            v.setFocusableInTouchMode(true);
                            v.requestFocus();
                            v.requestFocusFromTouch();
                            view.setBackgroundResource(R.drawable.vod_img_selector);
                            break;
                        case MotionEvent.ACTION_HOVER_EXIT:
                            view.setBackgroundColor(Color.TRANSPARENT);
                            break;
                    }
                    return false;
                }
            });
            mTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus){
                        ItemdefaultImage.setBackgroundResource(R.drawable.vod_img_selector);
                    }else{
                        ItemdefaultImage.setBackgroundColor(Color.TRANSPARENT);
                    }
                }
            });
            mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((View)v.getParent()).callOnClick();
                }
            });
        }
    }

    private View.OnClickListener mRelatedClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ItemEntity itemEntity = (ItemEntity) v.getTag();
            mPageStatistics.videoRelateClick(mItemEntity.getPk(), itemEntity);
            PageIntent pageIntent = new PageIntent();
            pageIntent.toPackageDetail(getContext(), "package", itemEntity.getPk());

        }
    };


    private RecyclerViewTV.OnItemClickListener PackageItemClickListener = new RecyclerViewTV.OnItemClickListener() {
        @Override
        public void onItemClick(RecyclerViewTV recyclerViewTV, View view, int i) {
            ItemEntity itemEntity = itemEntities.get(i);
            PageIntent pageIntent = new PageIntent();
            pageIntent.toDetailPage(getContext(), "package", itemEntity.getPk());
        }
    };


    public void requestPlayCheck(String itemPk) {
        mSkyService.apiPlayCheck(null, itemPk, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mActivity.new BaseObserver<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            PlayCheckEntity playCheckEntity = calculateRemainDay(responseBody.string());
                            refreshPayInfo(playCheckEntity);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    private void refreshPayInfo(PlayCheckEntity playCheckEntity) {
        int remainDay = playCheckEntity.getRemainDay();
        if (remainDay > 0) {
            vod_payment_duration.setText("剩余" + remainDay + "天");
            vod_payment_price.setText("已付费");
            vod_payment_duration.setBackgroundResource(R.drawable.vod_detail_already_payment_duration);
            vod_payment_price.setBackgroundResource(R.drawable.vod_detail_already_payment_price);
            if (mItemEntity.isRepeat_buy()) {
                vod_payment_buyButton.setVisibility(View.VISIBLE);
                vod_payment_buyButton.setEnabled(true);
                vod_payment_buyButton.setFocusable(true);
                vod_payment_buyButton.setText("再次购买");
                if(firstIn) {
                vod_payment_buyButton.requestFocus();
                vod_payment_buyButton.requestFocusFromTouch();
                }
            } else {
                vod_payment_buyButton.setEnabled(false);
                vod_payment_buyButton.setFocusable(false);
                vod_payment_buyButton.setText("已购买");
            }
        } else {
            vod_payment_buyButton.setVisibility(View.VISIBLE);
            vod_payment_buyButton.setText("购买");
            vod_payment_duration.setText("有效期" + mItemEntity.getExpense().getDuration() + "天");
            vod_payment_price.setText("￥" + mItemEntity.getExpense().getPrice() + "元");
            vod_payment_duration.setBackgroundResource(R.drawable.vod_detail_unpayment_duration);
            vod_payment_price.setBackgroundResource(R.drawable.vod_detail_unpayment_price);
            if(firstIn) {
            vod_payment_buyButton.requestFocus();
            vod_payment_buyButton.requestFocusFromTouch();
            }
        }
        vod_payment_buyButton.setVisibility(View.VISIBLE);
    }


    private PlayCheckEntity calculateRemainDay(String info) {
        PlayCheckEntity playCheckEntity;
        switch (info) {
            case "0":
                playCheckEntity = new PlayCheckEntity();
                playCheckEntity.setRemainDay(0);
                break;
            default:
                playCheckEntity = new Gson().fromJson(info, PlayCheckEntity.class);
                int remainDay;
                try {
                    remainDay = Utils.daysBetween(Utils.getTime(), playCheckEntity.getExpiry_date())+1 ;
                } catch (ParseException e) {
                    remainDay = 0;
                }
                playCheckEntity.setRemainDay(remainDay);
                break;
        }
        return playCheckEntity;
    }

    private class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            int position = parent.indexOfChild(view);
            if (position != 0) {
                // Add top margin only for the first item to avoid double space between items
                outRect.left = space;
            }
        }
    }

    @Override
    public void onPause() {
        firstIn=false;
        super.onPause();
        new PurchaseStatistics().expensePacketDetail(
                mItemEntity.getPk(),
                mItemEntity.getTitle(),
                mItemEntity.getExpense().getPrice(),
                "cancel",
                TrueTime.now().getTime());
    }

    private View.OnHoverListener onHoverListener = new View.OnHoverListener() {
        @Override
        public boolean onHover(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                case MotionEvent.ACTION_HOVER_MOVE:
                    v.setFocusable(true);
                    v.setFocusableInTouchMode(true);
                    v.requestFocus();
                    v.requestFocusFromTouch();
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    break;
            }
            return false;
        }
    };



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case PAYMENT_REQUEST_CODE:
                switch (resultCode){
                    case PAYMENT_SUCCESS_CODE:
//                        expensePacketExit();
                        break;
                    case PAYMENT_FAILURE_CODE:
                        break;
                }
                break;
        }
    }
}
