package tv.ismar.app.util;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import tv.ismar.app.R;

/** Created by huaijie on 11/2/15. */
public class PicassoUtils {
    public static void load(final Context context, String path, final ImageView target) {
        if (TextUtils.isEmpty(path)) {
            Picasso.with(context)
                    .load(R.drawable.default_recommend_bg)
                    .memoryPolicy(MemoryPolicy.NO_STORE)
                    .into(target);
        } else {
            Picasso.with(context)
                    .load(path)
                    .error(R.drawable.default_recommend_bg)
                    .memoryPolicy(MemoryPolicy.NO_STORE)
                    .into(target);
        }
    }
}
