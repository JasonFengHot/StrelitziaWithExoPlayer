package tv.ismar.searchpage;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.KeyEvent;

import tv.ismar.searchpage.data.http.SemantichObjectEntity;


/**
 * Created by huaijie on 1/18/16.
 */
public class BaseActivity extends FragmentActivity {
    private static final String TAG = "BaseActivity";
    private float densityRate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        densityRate = DensityRate.getDensityRate(this);
    }


    public float getDensityRate() {
        return densityRate;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    protected void onVodItemClick(SemantichObjectEntity objectEntity) {
        String url = objectEntity.getUrl();
        String contentModel = objectEntity.getContent_model();
        Long pk = Long.parseLong(objectEntity.getPk());
        String title = objectEntity.getTitle();

        String verticalUrl = objectEntity.getVertical_url();
        String horizontalUrl = objectEntity.getPoster_url();

        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        switch (contentModel) {
            case "person":
                intent.putExtra("pk", pk);
                intent.putExtra("title", title);
                intent.setAction("cn.ismartv.voice.filmstar");
                startActivity(intent);

                break;
            default:
                intent.putExtra("url", url);
                if (!TextUtils.isEmpty(verticalUrl)) {
                    intent.setAction("tv.ismar.daisy.PFileItem");
                } else {
                    intent.setAction("tv.ismar.daisy.Item");
                }
        }
        startActivity(intent);
    }

}
