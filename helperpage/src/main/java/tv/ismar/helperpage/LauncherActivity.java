package tv.ismar.helperpage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import cn.ismartv.injectdb.library.query.Select;
import okhttp3.ResponseBody;
import tv.ismar.app.core.preferences.AccountSharedPrefs;
import tv.ismar.app.db.location.CityTable;
import tv.ismar.app.db.location.ProvinceTable;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.IpLookUpEntity;
import tv.ismar.helperpage.core.FeedbackProblem;
import tv.ismar.helperpage.ui.activity.HomeActivity;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.network.entity.ProblemEntity;

public class LauncherActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "LauncherActivity";


    private ImageView indicatorNode;
    private ImageView indicatorFeedback;
    private ImageView indicatorHelp;
    private SkyService skyService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sakura_activity_launch);
        initViews();
        fetchProblems();
        fetchIP();
    }

    private void initViews() {
        indicatorNode = (ImageView) findViewById(R.id.indicator_node_image);
        indicatorFeedback = (ImageView) findViewById(R.id.indicator_feedback_image);
        indicatorHelp = (ImageView) findViewById(R.id.indicator_help_image);

        indicatorNode.setOnClickListener(this);
        indicatorFeedback.setOnClickListener(this);
        indicatorHelp.setOnClickListener(this);

        skyService=SkyService.ServiceManager.getService();

    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        intent.setClass(this, HomeActivity.class);

        int i = view.getId();
        if (i == R.id.indicator_node_image) {
            intent.putExtra("position", 0);

        } else if (i == R.id.indicator_feedback_image) {
            intent.putExtra("position", 1);

        } else if (i == R.id.indicator_help_image) {
            intent.putExtra("position", 2);

        }
        startActivity(intent);
    }


    /**
     * fetch tv problems from http server
     */
    private void fetchProblems() {
        mIrisService.Problems()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<ProblemEntity>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(List<ProblemEntity> problemEntity) {
                        FeedbackProblem feedbackProblem = FeedbackProblem.getInstance();
                        feedbackProblem.saveCache(problemEntity);
                    }
                });

    }
    private void fetchIP() {
        String url="http://lily.tvxio.com/iplookup";
        skyService.fetchIP(url).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<IpLookUpEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(IpLookUpEntity ipLookUpEntity) {
                        initializeLocation(ipLookUpEntity);
                    }
                });
    }
    private void initializeLocation(IpLookUpEntity ipLookUpEntity) {
        CityTable cityTable = new Select().from(CityTable.class).where(CityTable.CITY + " = ?", ipLookUpEntity.getCity()==null?"":ipLookUpEntity.getCity()).executeSingle();

        AccountSharedPrefs accountSharedPrefs = AccountSharedPrefs.getInstance();
        accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.PROVINCE, ipLookUpEntity.getProv());
        accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.CITY, ipLookUpEntity.getCity());
        accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.ISP, ipLookUpEntity.getIsp());
        accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.IP, ipLookUpEntity.getIp());
        if (cityTable != null) {
            accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.GEO_ID, String.valueOf(cityTable.geo_id));
        }

        ProvinceTable provinceTable = new Select().from(ProvinceTable.class)
                .where(ProvinceTable.PROVINCE_NAME + " = ?", ipLookUpEntity.getProv()==null?"":ipLookUpEntity.getProv()).executeSingle();
        if (provinceTable != null) {
            accountSharedPrefs.setSharedPrefs(AccountSharedPrefs.PROVINCE_PY, provinceTable.pinyin);
        }
    }
}
