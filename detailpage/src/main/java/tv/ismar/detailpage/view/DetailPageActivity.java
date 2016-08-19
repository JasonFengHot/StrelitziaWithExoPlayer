package tv.ismar.detailpage.view;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import tv.ismar.app.util.Constants;
import tv.ismar.detailpage.R;

/**
 * Created by huibin on 8/18/16.
 */
public class DetailPageActivity extends Activity {

    private static final String TAG = "DetailPageActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailpage_movie);

        Log.i(TAG, Constants.TEST);
    }

}
