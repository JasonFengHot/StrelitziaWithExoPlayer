package tv.ismar.detailpage.view;

import android.os.Bundle;
import android.text.TextUtils;

import tv.ismar.app.BaseActivity;
import tv.ismar.detailpage.R;

import static tv.ismar.app.core.PageIntentInterface.EXTRA_MODEL;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_PK;

/**
 * Created by huibin on 8/18/16.
 */
public class DetailPageActivity extends BaseActivity {
    private static final String TAG = "DetailPageActivity";

    private int mItemPk;
    private String content_model;
    private DetailPageFragment detailPageFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailpage);
        content_model = getIntent().getStringExtra(EXTRA_MODEL);
        mItemPk = getIntent().getIntExtra(EXTRA_PK, -1);
        if (TextUtils.isEmpty(content_model) || mItemPk == -1) {
            finish();
            return;
        }

        detailPageFragment = DetailPageFragment.newInstance(mItemPk, content_model);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_detail_container, detailPageFragment)
                .commit();

    }

}
