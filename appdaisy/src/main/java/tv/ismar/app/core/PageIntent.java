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
    public void toPayment(Context context, String pk, String jumpTo, String cpid, String model) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        switch (jumpTo) {
            case "1":
                intent.setAction("tv.ismar.pay.payment");
                intent.putExtra(EXTRA_PK, pk);
                intent.putExtra("model", model);
                break;
            case "0":
                intent.setAction("tv.ismar.pay.pay");
                intent.putExtra("item_id", pk);
                break;
            case "2":
                intent.setAction("tv.ismar.pay.payvip");
                intent.putExtra("cpid", cpid);
                intent.putExtra("item_id", pk);
                break;
        }
        context.startActivity(intent);
    }

    @Override
    public void toPlayPage(Context context, int pk, int sub_item_pk) {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.Player");
        intent.putExtra(PageIntentInterface.EXTRA_PK, pk);
        intent.putExtra(PageIntentInterface.EXTRA_SUBITEM_PK, sub_item_pk);
        context.startActivity(intent);
    }
}
