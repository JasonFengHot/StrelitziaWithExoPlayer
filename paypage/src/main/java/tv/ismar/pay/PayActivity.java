package tv.ismar.pay;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.foregroundimageview.ForegroundImageView;
import com.squareup.picasso.Picasso;

import cn.ismartv.tvhorizontalscrollview.TvHorizontalScrollView;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.PageIntentInterface.PaymentInfo;
import tv.ismar.app.core.PageIntentInterface.ProductCategory;
import tv.ismar.app.network.entity.PayLayerEntity;

import static tv.ismar.app.core.PageIntentInterface.FromPage.unknown;
import static tv.ismar.app.core.PageIntentInterface.PAYMENT;
import static tv.ismar.app.core.PageIntentInterface.PAYVIP;
import static tv.ismar.pay.PaymentActivity.PAYMENT_REQUEST_CODE;

/**
 * Created by huaijie on 4/11/16.
 */
public class PayActivity extends BaseActivity implements View.OnHoverListener, View.OnFocusChangeListener {
    private ImageView leftArrow;
    private ImageView rightArrow;
    private LinearLayout scrollViewLayout;
    private TvHorizontalScrollView mTvHorizontalScrollView;
    private ImageView tmp;
    private int mItemId;
    private Subscription paylayerSub;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_newvip_pay);
        initViews();
        Intent intent = getIntent();
        mItemId = intent.getIntExtra("item_id", -1);
        payLayer(String.valueOf(mItemId));
//        payLayer("675300");
    }

    private void initViews() {
        leftArrow = (ImageView) findViewById(R.id.left_arrow);
        rightArrow = (ImageView) findViewById(R.id.right_arrow);
        tmp = (ImageView) findViewById(R.id.tmp);
        scrollViewLayout = (LinearLayout) findViewById(R.id.pay_scrollview);
        mTvHorizontalScrollView = (TvHorizontalScrollView) findViewById(R.id.tvhorizontalscrollview);
        mTvHorizontalScrollView.setLeftArrow(leftArrow);
        mTvHorizontalScrollView.setRightArrow(rightArrow);
    }

    //675305
    //675302
    //675300
    //675322
    public void payLayer(String itemId) {
        paylayerSub = mSkyService.apiPaylayer(itemId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<PayLayerEntity>() {
                    @Override
                    public void onCompleted() {

                    }


                    @Override
                    public void onNext(PayLayerEntity payLayerEntity) {
                        fillLayout(payLayerEntity);

                    }
                });
    }

    private void fillLayout(final PayLayerEntity payLayerEntity) {
        scrollViewLayout.removeAllViews();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = (int) getResources().getDimension(R.dimen.newip_payitem_margin);
        layoutParams.setMargins(margin, 0, margin, 0);
        PayLayerEntity.Vip vip = payLayerEntity.getVip();
        if (vip != null) {
            RelativeLayout vipItem;
            if (payLayerEntity.getCpname().startsWith("ismar")) {
                vipItem = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.item_daisy_vip_pay, null);
            } else {
                vipItem = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.item_newvip_pay, null);
            }
            ForegroundImageView imageView = (ForegroundImageView) vipItem.findViewById(R.id.item_newvip_pay_img);
            TextView title = (TextView) vipItem.findViewById(R.id.title);
            title.setText(vip.getTitle());
            TextView price = (TextView) vipItem.findViewById(R.id.price);
            price.setText(vip.getPrice() + "元/" + vip.getDuration() + "天");
            if (TextUtils.isEmpty(vip.getVertical_url())) {
                Picasso.with(this).load(R.drawable.error_ver).fit().into(imageView);
            } else {

                Picasso.with(this).load(vip.getVertical_url()).error(R.drawable.error_ver).fit().into(imageView);
            }
            vipItem.setOnHoverListener(this);
            vipItem.setOnFocusChangeListener(this);
            vipItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PaymentInfo paymentInfo = new PaymentInfo(mItemId, PAYVIP, payLayerEntity.getCpid());
                    new PageIntent().toPaymentForResult(PayActivity.this, unknown.toString(), paymentInfo);
                }
            });
            scrollViewLayout.addView(vipItem, layoutParams);
        }

        final PayLayerEntity.Expense_item expenseItem = payLayerEntity.getExpense_item();
        if (expenseItem != null) {
            RelativeLayout item = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.item_newvip_pay, null);
            ImageView imageView = (ImageView) item.findViewById(R.id.item_newvip_pay_img);
            if (TextUtils.isEmpty(expenseItem.getVertical_url())) {
                Picasso.with(this).load(R.drawable.error_ver).fit().fit().into(imageView);
            } else {

                Picasso.with(this).load(expenseItem.getVertical_url()).error(R.drawable.error_ver).fit().into(imageView);
            }
            TextView title = (TextView) item.findViewById(R.id.title);
            title.setText(expenseItem.getTitle());
            TextView price = (TextView) item.findViewById(R.id.price);
            price.setText(expenseItem.getPrice() + "元/" + expenseItem.getDuration() + "天");
            item.setOnHoverListener(this);
            item.setOnFocusChangeListener(this);
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buyVideo(expenseItem.getPk(), expenseItem.getType(), Float.parseFloat(expenseItem.getPrice()), expenseItem.getDuration(), expenseItem.getTitle());
                }
            });
            scrollViewLayout.addView(item, layoutParams);
        }

        final PayLayerEntity.Package newVipPackage = payLayerEntity.getPkage();
        if (newVipPackage != null) {
            RelativeLayout item = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.item_newvip_pay, null);
            ImageView imageView = (ImageView) item.findViewById(R.id.item_newvip_pay_img);
            if (TextUtils.isEmpty(newVipPackage.getVertical_url())) {
                Picasso.with(this).load(R.drawable.error_ver).fit().into(imageView);
            } else {
                Picasso.with(this).load(newVipPackage.getVertical_url()).error(R.drawable.error_ver).fit().into(imageView);
            }
            TextView title = (TextView) item.findViewById(R.id.title);
            title.setText(newVipPackage.getTitle());
            TextView price = (TextView) item.findViewById(R.id.price);
            price.setText(newVipPackage.getPrice() + "元/" + newVipPackage.getDuration() + "天");
            item.setOnHoverListener(this);
            item.setOnFocusChangeListener(this);
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra("package_id", newVipPackage.getPackage_pk());
                    intent.putExtra("movie_id", mItemId);
                    intent.setClass(PayActivity.this, PayLayerPackageActivity.class);
                    startActivityForResult(intent, PAYMENT_REQUEST_CODE);
                }
            });
            scrollViewLayout.addView(item, layoutParams);
        }
        if (scrollViewLayout.getChildAt(0) != null)
            scrollViewLayout.getChildAt(0).requestFocus();
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

    private void buyVideo(int pk, String type, float price, String duration, String title) {
        PaymentInfo paymentInfo = new PaymentInfo(ProductCategory.item, pk, PAYMENT);
        new PageIntent().toPaymentForResult(this, unknown.toString(), paymentInfo);
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
        if (paylayerSub != null && paylayerSub.isUnsubscribed()) {
            paylayerSub.unsubscribe();
        }
        super.onStop();
    }
}
