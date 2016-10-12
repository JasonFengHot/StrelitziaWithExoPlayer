package tv.ismar.app.core;

import android.content.Context;

/**
 * Created by huibin on 9/8/16.
 */
public interface PageIntentInterface {
    String EXTRA_FROMPAGE = "fromPage";
    String EXTRA_MODEL = "content_model";
    String EXTRA_PK = "pk";
    String EXTRA_ITEM_JSON = "item_json";
    // 电视剧等多集片子集pk,与文档相同
    String EXTRA_SUBITEM_PK = "sub_item_pk";

    String EXTRA_PRODUCT_CATEGORY = "product_category";


    void toDetailPage(Context context, String contentModel, int pk);

    void toDetailPage(Context context, String fromPage, String json);

    void toPayment(Context context, String fromPage, PaymentInfo paymentInfo);

    void toPlayPage(Context context, int pk, int sub_item_pk);

    enum FromPage {
        unknown
    }

    enum ProductCategory {
        item,
        _package
    }

    class PaymentInfo {
        public PaymentInfo(ProductCategory category, int pk, int jumpTo, int cpid) {
            this.category = category;
            this.pk = pk;
            this.jumpTo = jumpTo;
            this.cpid = cpid;
        }

        private ProductCategory category;
        private int pk;
        private int jumpTo;
        private int cpid;

        public ProductCategory getCategory() {
            return category;
        }

        public int getPk() {
            return pk;
        }

        public int getJumpTo() {
            return jumpTo;
        }

        public int getCpid() {
            return cpid;
        }
    }
}
