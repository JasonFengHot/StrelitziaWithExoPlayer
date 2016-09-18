package tv.ismar.app.core;

import android.content.Context;
import android.content.Intent;

/**
 * Created by huibin on 9/13/16.
 */
public class PageIntent implements PageIntentInterface {
    @Override
    public void toDetailPage(Context context, String contentModel, int pk) {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.detailpage");
        intent.putExtra(EXTRA_MODEL, contentModel);
        intent.putExtra(EXTRA_PK, pk);
        context.startActivity(intent);
    }

    @Override
    public void toPayment() {

    }
}
