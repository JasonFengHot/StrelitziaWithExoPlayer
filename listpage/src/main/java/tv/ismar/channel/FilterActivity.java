package tv.ismar.channel;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import tv.ismar.app.AppConstant;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.DaisyUtils;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.ui.HeadFragment;
import tv.ismar.app.util.BitmapDecoder;
import tv.ismar.app.widget.LaunchHeaderLayout;
import tv.ismar.listpage.R;
import tv.ismar.view.BackHandledFragment;
import tv.ismar.view.BackHandledInterface;
import tv.ismar.view.FilterFragment;


/**
 * Created by zhangjiqiang on 15-6-18.
 */
public class FilterActivity extends BaseActivity implements BackHandledInterface {
    private String mChannel;
    private SimpleRestClient mRestClient;
    private BackHandledFragment mBackHandedFragment;
    private HeadFragment headFragment;
    private LaunchHeaderLayout weatherFragment;
    private BitmapDecoder bitmapDecoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_layout);
//       final View vv = findViewById(R.id.large_layout);
//        bitmapDecoder = new BitmapDecoder();
//        bitmapDecoder.decode(this, R.drawable.main_bg, new BitmapDecoder.Callback() {
//            @Override
//            public void onSuccess(BitmapDrawable bitmapDrawable) {
//                vv.setBackgroundDrawable(bitmapDrawable);
//            }
//        });
      //  DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(), this);
        mRestClient = new SimpleRestClient();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppConstant.purchase_referer = "channel";
        AppConstant.purchase_page = "result";
    }

    private void initView() {
        String mTitle = getIntent().getStringExtra("title");
        mChannel = getIntent().getStringExtra("channel");

        weatherFragment = (LaunchHeaderLayout) findViewById(R.id.top_column_layout);
        weatherFragment.setTitle(mTitle);
        weatherFragment.hideSubTiltle();
        weatherFragment.hideIndicatorTable();
        FilterFragment filterfragment = new FilterFragment();
        filterfragment.mChannel = mChannel;
        filterfragment.isPortrait = getIntent().getBooleanExtra("isPortrait", false);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.filter_fragment_container, filterfragment);
        fragmentTransaction.addToBackStack("tag");
        fragmentTransaction.commit();
        //  doFilterRequest();
        headFragment = new HeadFragment();
        Bundle bundle = new Bundle();
        bundle.putString("type", HeadFragment.HEADER_FILTER);
        bundle.putString("channel_name", mTitle);
        headFragment = new HeadFragment();
        headFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.fitle_head, headFragment).commit();
    }

    private void doFilterRequest() {
        String s = mChannel;
        // String url = "http://cordadmintest.tvxio.com/api/tv/retrieval/"+mChannel+"/";
        // String url = "http://v2.sky.tvxio.com/v2_0/SKY/dto/api/tv/retrieval/" + mChannel + "/";

        String url = "http://cord.tvxio.com/v2_0/A21/dto/api/topic/8/";
        mRestClient.doTopicRequest(url, "get", "", new SimpleRestClient.HttpPostRequestInterface() {

            @Override
            public void onPrepare() {
                Toast.makeText(FilterActivity.this, "11", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(String info) {
                try {
                    JSONObject jsonObject = new JSONObject(info);
                    JSONObject attributes = jsonObject.getJSONObject("attributes");
                    Iterator it = attributes.keys();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        Log.i("asdfgh", "jsonkey==" + key);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Toast.makeText(FilterActivity.this, "1312312", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onFailed(String error) {
                Toast.makeText(FilterActivity.this, "22", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
      //  DaisyUtils.getVodApplication(this).removeActivtyFromPool(this.toString());
        if(bitmapDecoder != null && bitmapDecoder.isAlive()){
        	bitmapDecoder.interrupt();
        }
        super.onDestroy();
    }

    @Override
    public void setSelectedFragment(BackHandledFragment selectedFragment) {
        this.mBackHandedFragment = selectedFragment;
    }

    @Override
    public void onBackPressed() {
        if (mBackHandedFragment == null || !mBackHandedFragment.onBackPressed()) {
            if (getFragmentManager().getBackStackEntryCount() == 0 || getFragmentManager().getBackStackEntryCount() == 1) {
                finish();
            } else {
                getFragmentManager().popBackStack();
            }
        }
    }
}
