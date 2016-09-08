package tv.ismar.app.core;

import android.content.Context;

/**
 * Created by huibin on 9/8/16.
 */
public interface PageIntentInterface {
    String EXTRA_MODEL = "content_model";
    String EXTRA_PK = "pk";

    void toDetailPage(Context context, String contentModel, int pk);
}
