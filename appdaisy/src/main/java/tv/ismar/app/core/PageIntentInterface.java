package tv.ismar.app.core;

import android.content.Context;

/**
 * Created by huibin on 9/8/16.
 */
public interface PageIntentInterface {
    String EXTRA_MODEL = "content_model";
    String EXTRA_PK = "pk";
    String EXTRA_ITEM_JSON = "item_json";
    // 电视剧等多集片子集pk,与文档相同
    String EXTRA_SUBITEM_PK = "sub_item_pk";

    void toDetailPage(Context context, String contentModel, int pk);

    void toPayment(Context context, String pk, String jumpTo, String cpid, String model);

    void toPlayPage(Context context, int pk, int sub_item_pk);
}
