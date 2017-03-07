package tv.ismar.pay;

import android.media.Image;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.network.entity.AgreementEntity;

/**
 * Created by huibin on 17-2-15.
 */

public class RenewalAgreementActivity extends BaseActivity
        implements View.OnClickListener,OnHoverListener {

    private Subscription agreementSub;
    private LinearLayout agreementLayout;

    private Button agreementBtn;
    private ImageView tmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_renewalagreement);
        agreementLayout = (LinearLayout) findViewById(R.id.agreement_layout);
        agreementBtn = (Button) findViewById(R.id.agreement_back);
        agreementBtn.setOnClickListener(this);
        fetchRenewalAgreement("renew");
        agreementBtn.setOnHoverListener(this);
        tmp = (ImageView)findViewById(R.id.tmp);
    }

    @Override
    protected void onPause() {

        if (agreementSub != null && agreementSub.isUnsubscribed()) {
            agreementSub.unsubscribe();
        }

        super.onPause();
    }

    private void fetchRenewalAgreement(String type) {
        agreementSub = mSkyService.agreement(type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<AgreementEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(AgreementEntity agreementEntity) {
                        layoutView(agreementEntity);
                    }
                });
    }

    private void layoutView(AgreementEntity entity) {
        for (AgreementEntity.Info info : entity.getInfo()) {
            TextView titleTextView = (TextView) LayoutInflater.from(this).inflate(R.layout.renewal_text, null);
            TextView contentTextView = (TextView) LayoutInflater.from(this).inflate(R.layout.renewal_text, null);

//            titleTextView.setTextSize(getResources().getDimensionPixelSize(R.dimen.text_size_30sp));
            titleTextView.setGravity(Gravity.CENTER);
//            contentTextView.setTextSize(getResources().getDimensionPixelSize(R.dimen.text_size_30sp));

            titleTextView.setText(info.getTitle());
            contentTextView.setText(info.getContent());
//            android:lineSpacingExtra="@dimen/usercenter_line_spacing"

            LinearLayout.LayoutParams titleTextViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            titleTextViewParams.gravity = Gravity.CENTER_HORIZONTAL;
            titleTextViewParams.setMargins(0, getResources().getDimensionPixelSize(R.dimen.agreement_title_mt), 0, getResources().getDimensionPixelSize(R.dimen.agreement_title_mb));

            LinearLayout.LayoutParams contentTextViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);


            agreementLayout.addView(titleTextView, titleTextViewParams);
            agreementLayout.addView(contentTextView, contentTextViewParams);
        }
    }

    @Override
    public void onClick(View view) {
        finish();
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                v.requestFocus();
                v.requestFocusFromTouch();
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                tmp.requestFocus();
                break;
        }
        return false;
    }
}
