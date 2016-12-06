package cn.ismartv.helperpage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.util.List;

import cn.ismartv.helperpage.core.FeedbackProblem;
import cn.ismartv.helperpage.ui.activity.HomeActivity;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.network.entity.ProblemEntity;

public class LauncherActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "LauncherActivity";


    private ImageView indicatorNode;
    private ImageView indicatorFeedback;
    private ImageView indicatorHelp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sakura_activity_launch);
        initViews();
        fetchProblems();
    }

    private void initViews() {
        indicatorNode = (ImageView) findViewById(R.id.indicator_node_image);
        indicatorFeedback = (ImageView) findViewById(R.id.indicator_feedback_image);
        indicatorHelp = (ImageView) findViewById(R.id.indicator_help_image);

        indicatorNode.setOnClickListener(this);
        indicatorFeedback.setOnClickListener(this);
        indicatorHelp.setOnClickListener(this);


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

}
