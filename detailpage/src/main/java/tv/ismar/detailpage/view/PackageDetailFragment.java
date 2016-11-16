package tv.ismar.detailpage.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseFragment;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.widget.LoadingDialog;
import tv.ismar.detailpage.R;

import static tv.ismar.app.core.PageIntentInterface.EXTRA_ITEM_JSON;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_SOURCE;

/**
 * Created by huibin on 11/14/16.
 */

public class PackageDetailFragment extends BaseFragment {
    private TextView vod_payment_pacakge_title;
    private TextView vod_payment_packageDescribe_content;
    private LoadingDialog mLoadingDialog;
    private ItemEntity mItem;
    private LinearLayout mRelatedVideoContainer;
    private ImageView vod_payment_poster;
    private TextView vod_payment_price;
    private TextView vod_payment_duration;
    private Button vod_payment_item_more;
    private Button vod_payment_buyButton;
    private RelativeLayout detail_left_container;
    private LinearLayout detail_right_container;
    private Dialog dialog = null;
    private DialogInterface.OnClickListener mPositiveListener;
    private DialogInterface.OnClickListener mNegativeListener;
    private int remainDay = -1;
    private ImageView isbuy_label;
    private ImageView mDetailQualityLabel;

    private RecyclerViewTV vod_payment_item_of_package_container;

    private View rootView;


    public static PackageDetailFragment newInstance(String fromPage, String itemJson) {
        PackageDetailFragment fragment = new PackageDetailFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_SOURCE, fromPage);
        args.putString(EXTRA_ITEM_JSON, itemJson);
        fragment.setArguments(args);
        return fragment;
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
        initView();
    }

    private void initView() {
        isbuy_label = (ImageView) rootView.findViewById(R.id.isbuy_label);
        detail_left_container = (RelativeLayout) rootView.findViewById(R.id.detail_left_container);
        detail_right_container = (LinearLayout) rootView.findViewById(R.id.detail_right_container);
//        mLoadingDialog = new LoadingDialog(this, getResources().getString(R.string.loading));
//        mLoadingDialog.setOnCancelListener();
        vod_payment_pacakge_title = (TextView) rootView.findViewById(R.id.vod_payment_pacakge_title);
        vod_payment_packageDescribe_content = (TextView) rootView.findViewById(R.id.vod_payment_packageDescribe_content);
        mRelatedVideoContainer = (LinearLayout) rootView.findViewById(R.id.related_video_container);
        vod_payment_item_of_package_container = (RecyclerViewTV) rootView.findViewById(R.id.vod_payment_item_of_package_container);
        vod_payment_poster = (ImageView) rootView.findViewById(R.id.vod_payment_poster);
        vod_payment_price = (TextView) rootView.findViewById(R.id.vod_payment_price);
        vod_payment_duration = (TextView) rootView.findViewById(R.id.vod_payment_duration);
        vod_payment_buyButton = (Button) rootView.findViewById(R.id.vod_payment_buyButton);
        vod_payment_item_more = (Button) rootView.findViewById(R.id.vod_payment_item_more);
//        vod_payment_buyButton.setOnHoverListener(onHoverListener);
//        vod_payment_item_more.setOnHoverListener(onHoverListener);
        vod_payment_buyButton.setFocusable(true);
        vod_payment_buyButton.requestFocus();
        vod_payment_buyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                buyVideo();
            }
        });
        vod_payment_item_more.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                if (mItem != null) {
//                    Intent intent = new Intent();
//                    intent.setAction("tv.ismar.daisy.packagelist");
//                    intent.putExtra("pk", mItem.pk);
//                    startActivity(intent);
//                }
            }
        });
    }


//    private void loadView() {
//        if (mItem != null) {
//            vod_payment_pacakge_title.setText(mItem.title);
//            vod_payment_packageDescribe_content.setText(mItem.description);
//            if (mItem.items.isEmpty()) {
//                vod_payment_item_of_package_container.setVisibility(View.INVISIBLE);
//                vod_payment_item_more.setEnabled(false);
//                vod_payment_item_more.setFocusable(false);
//            } else {
//                vod_payment_item_of_package_container.setVisibility(View.VISIBLE);
//                vod_payment_item_more.setEnabled(true);
//                vod_payment_item_more.setFocusable(true);
//                ItemAdapter adaptet = new ItemAdapter(PackageDetailActivity.this, mItem.items);
//                vod_payment_item_of_package_container.setAdapter(adaptet);
//            }
//            if (mItem.expense != null) {
//                //收费
//                isbuy();
//            }
//            vod_payment_poster.setUrl(mItem.adlet_url);
//            fetchRelated(String.valueOf(mItem.pk));
//        }
//    }

//    private void fetchRelated(long pk) {
//
//        .packageRelate(pk, deviceToken, accessToken)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<ItemEntity[]>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.e("LH/", "fetchRelated get data error");
//                    }
//
//                    @Override
//                    public void onNext(ItemEntity[] itemEntities) {
////                        mRelatedItem = itemEntities;
////
////                        if (mRelatedItem != null && mRelatedItem.length > 0) {
////                            buildRelatedList();
////                        }
//                        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
//                            mLoadingDialog.dismiss();
//                            detail_left_container.setVisibility(View.VISIBLE);
//                            detail_right_container.setVisibility(View.VISIBLE);
//                        }
//                    }
//                });
//
//    }
}
