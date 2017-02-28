package tv.ismar.pay;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.foregroundimageview.ForegroundImageView;
import com.squareup.picasso.Picasso;

import cn.ismartv.tvhorizontalscrollview.TvHorizontalScrollView;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.PageIntentInterface;
import tv.ismar.app.core.PlayCheckManager;
import tv.ismar.app.core.Source;
import tv.ismar.app.core.VipMark;
import tv.ismar.app.network.entity.PayLayerPackageEntity;
import tv.ismar.statistics.DetailPageStatistics;

import static tv.ismar.pay.PaymentActivity.PAYMENT_REQUEST_CODE;

/**
 * Created by huaijie on 4/12/16.
 */
public class PayLayerPackageActivity extends BaseActivity implements View.OnHoverListener, OnFocusChangeListener, View.OnClickListener {
    private static final String TAG = "PayLayerPackageActivity";

    private ImageView tmp;
    private TvHorizontalScrollView mTvHorizontalScrollView;
    private LinearLayout scrollViewLayout;
    private ImageView leftArrow;
    private ImageView rightArrow;
    private TextView title;
    private TextView price;
    private TextView duration;
    private TextView decription;

    private PayLayerPackageEntity entity;
    private Button purchaseBtn;
    private boolean listLayoutItemNextFocusUpIsSelf = false;
    private DetailPageStatistics mPageStatistics;
    private int packageId;
    private Subscription paylayerPackageSub;

    private int movieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paylayerpackage);
        mPageStatistics = new DetailPageStatistics();
        initViews();
        Intent intent = getIntent();
        packageId = intent.getIntExtra("package_id", -1);
        movieId = intent.getIntExtra("movie_id", -1);
        payLayerPackage(String.valueOf(packageId));
        orderCheck(String.valueOf(packageId));
    }

    @Override
    protected void onResume() {
        mPageStatistics.packageDetailIn(packageId + "", "detail");
        super.onResume();
    }

    private void initViews() {
        mTvHorizontalScrollView = (TvHorizontalScrollView) findViewById(R.id.scroll_view);
        scrollViewLayout = (LinearLayout) findViewById(R.id.scroll_layout);
        leftArrow = (ImageView) findViewById(R.id.left_arrow);
        rightArrow = (ImageView) findViewById(R.id.right_arrow);
        mTvHorizontalScrollView.setLeftArrow(leftArrow);
        mTvHorizontalScrollView.setRightArrow(rightArrow);
        mTvHorizontalScrollView.setCoverOffset(20);
        tmp = (ImageView) findViewById(R.id.tmp);

        title = (TextView) findViewById(R.id.title);
        price = (TextView) findViewById(R.id.price);
        duration = (TextView) findViewById(R.id.duration);
        decription = (TextView) findViewById(R.id.description);
        purchaseBtn = (Button) findViewById(R.id.paylayerpkg_purchase);
        purchaseBtn.setOnClickListener(this);
        purchaseBtn.setOnHoverListener(this);
        purchaseBtn.setOnFocusChangeListener(this);
        purchaseBtn.requestFocus();

        leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvHorizontalScrollView.pageScroll(View.FOCUS_LEFT);
            }
        });

        rightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvHorizontalScrollView.pageScroll(View.FOCUS_RIGHT);
            }
        });

        leftArrow.setOnHoverListener(this);
        rightArrow.setOnHoverListener(this);
    }

    private void payLayerPackage(String packageId) {
        paylayerPackageSub = mSkyService.apiPaylayerPackage(packageId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<PayLayerPackageEntity>() {
                    @Override
                    public void onCompleted() {

                    }


                    @Override
                    public void onNext(PayLayerPackageEntity payLayerPackageEntity) {
                        entity = payLayerPackageEntity;
                        title.setText("名称 : " + entity.getTitle());
                        price.setText("金额 : " + entity.getPrice() + "元");
                        duration.setText("有效期 : " + entity.getDuration() + "天");
                        decription.setText("说明 : " + entity.getDescription());
                        fillLayout(entity);
                    }
                });
    }


    private void fillLayout(final PayLayerPackageEntity packageEntity) {
        scrollViewLayout.removeAllViews();
        int margin = (int) getResources().getDimension(R.dimen.newvip_paylayervip_margin);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(margin, 0, margin, 0);
        for (int i = 0; i < packageEntity.getItem_list().size(); i++) {
            final PayLayerPackageEntity.Item_list itemList = packageEntity.getItem_list().get(i);
            RelativeLayout itemView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.item_paylayerpackage, null);
            ImageView imageView = (ImageView) itemView.findViewById(R.id.image);
            TextView itemTitle = (TextView) itemView.findViewById(R.id.title);
            itemTitle.setText(itemList.getTitle());
            ImageView expense_txt = (ImageView) itemView.findViewById(R.id.expense_txt);
            if (!TextUtils.isEmpty(itemList.getCptitle())) {
                String imageUrl = VipMark.getInstance().getImage(this, itemList.getPay_type(), itemList.getCpid());
                Picasso.with(this).load(imageUrl).into(expense_txt);

            }

            if (TextUtils.isEmpty(itemList.getVertical_url())) {
                Picasso.with(this).load(R.drawable.error_ver).into(imageView);
            } else {
                Picasso.with(this).load(itemList.getVertical_url()).error(R.drawable.error_ver).into(imageView);
            }

            itemView.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    View itemContainer = v.findViewById(R.id.item_container);
                    View itemTitle = v.findViewById(R.id.title);
                    if (hasFocus) {
                        itemContainer.setSelected(true);
                        itemTitle.setSelected(true);
                    } else {
                        itemContainer.setSelected(false);
                        itemTitle.setSelected(false);
                    }

                }
            });
            itemView.setOnHoverListener(this);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new PageIntent().toDetailPage(PayLayerPackageActivity.this, Source.UNKNOWN.getValue(), itemList.getItem_id());
                }
            });
            if (listLayoutItemNextFocusUpIsSelf == true) {
                itemView.setNextFocusUpId(itemView.getId());
            } else {
                itemView.setNextFocusUpId(purchaseBtn.getId());
            }
            if (i == packageEntity.getItem_list().size() - 1) {
                itemView.setNextFocusRightId(R.id.pay_layer_item);
            }
            scrollViewLayout.addView(itemView, layoutParams);
        }
        if (!purchaseBtn.isFocusable()) {
            scrollViewLayout.getChildAt(0).findViewById(R.id.pay_layer_item).requestFocus();
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                v.requestFocusFromTouch();
                v.requestFocus();
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                tmp.requestFocus();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

//        if (hasFocus) {
//            ViewScaleUtil.zoomin_1_15(v);
//        } else {
//            ViewScaleUtil.zoomout_1_15(v);
//        }
    }

    private void buyVideo(int pk, String type, float price) {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.pay.payment");
        intent.putExtra(PageIntent.EXTRA_PK, pk);
        intent.putExtra("model", "package");
        intent.putExtra("movie_id", movieId);
        intent.putExtra(PageIntentInterface.EXTRA_PRODUCT_CATEGORY, PageIntentInterface.ProductCategory.Package.toString());
        startActivityForResult(intent, PAYMENT_REQUEST_CODE);
    }

    public void buyPackage() {
        buyVideo(entity.getPk(), entity.getType(), Float.parseFloat(entity.getPrice()));
    }

    private void orderCheck(String pkg) {
        PlayCheckManager.getInstance(mSkyService).checkPkg(pkg, new PlayCheckManager.Callback() {
            @Override
            public void onSuccess(boolean isBuy, int remainDay, String user) {
                if (isBuy) {
                    purchaseBtn.setFocusable(false);
                    purchaseBtn.setText("已购买");
                    purchaseBtn.setEnabled(false);
                    changeListItemNextFocusUp(true);
                    listLayoutItemNextFocusUpIsSelf = true;
                } else {
                    purchaseBtn.setFocusable(true);
                    purchaseBtn.setText("购买");
                    purchaseBtn.setEnabled(true);
                    changeListItemNextFocusUp(false);
                    listLayoutItemNextFocusUpIsSelf = false;
                }
            }

            @Override
            public void onFailure() {

            }
        });
    }

    private void changeListItemNextFocusUp(boolean isSelf) {
        for (int i = 0; i < scrollViewLayout.getChildCount(); i++) {
            if (isSelf)
                scrollViewLayout.getChildAt(i).setNextFocusUpId(scrollViewLayout.getChildAt(i).getId());
            else
                scrollViewLayout.getChildAt(i).setNextFocusUpId(purchaseBtn.getId());
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.paylayerpkg_purchase) {
            buyPackage();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == PaymentActivity.PAYMENT_SUCCESS_CODE) {
            setResult(PaymentActivity.PAYMENT_SUCCESS_CODE, data);
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        setResult(PaymentActivity.PAYMENT_FAILURE_CODE);
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        if (paylayerPackageSub != null && paylayerPackageSub.isUnsubscribed()) {
            paylayerPackageSub.unsubscribe();
        }
        super.onStop();
    }
}
