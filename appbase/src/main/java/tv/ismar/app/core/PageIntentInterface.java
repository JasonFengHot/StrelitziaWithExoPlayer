package tv.ismar.app.core;

import android.app.Activity;
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
    String EXTRA_SOURCE = "fromPage";
    String EXTRA_TYPE = "type";
    String EXTRA_TITLE= "title";
    String EXTRA_START_TIME= "time";
    String POSITION= "position";
    String TYPE= "type";

    String EXTRA_PRODUCT_CATEGORY = "product_category";

    int PAYMENT_REQUEST_CODE = 0xd6;
    int PAYMENT_SUCCESS_CODE = 0x5c;
    int PAYMENT_FAILURE_CODE = 0xd2;


    int DETAIL_TYPE_PKG = 0x8a;
    int DETAIL_TYPE_ITEM = 0x37;

    int PAYMENT = 1;
    int PAY = 0;
    int PAYVIP = 2;


    void toDetailPage(Context context, String source, int pk);


    void toDetailPage(Context context, String source, String json);

    void toPackageDetail(Context context, String source, int pk);


    void toPackageDetail(Context context, String source, String json);

//    void toPayment(Context context, String fromPage, PaymentInfo paymentInfo);

    void toPaymentForResult(Activity context, String fromPage, PaymentInfo paymentInfo);

    void toPlayPage(Context context, int pk, int sub_item_pk, Source source);

    void toUserCenter(Context context);

    void toUserCenterLocation(Context context);


    void toPackageList(Context context, String source, int pk);

    void toFilmStar(Context context, String title, long pk);

    void toEpisodePage(Context context, String source, String itemJson);

    void toHelpPage(Context context);

    class PaymentInfo {
        private ProductCategory category;
        private int pk;
        private int jumpTo;
        private int cpid;

        public PaymentInfo(ProductCategory category, int pk, int jumpTo, int cpid) {
            this.category = category;
            this.pk = pk;
            this.jumpTo = jumpTo;
            this.cpid = cpid;
        }

        public PaymentInfo(ProductCategory category, int pk, int jumpTo) {
            this.category = category;
            this.pk = pk;
            this.jumpTo = jumpTo;
        }

        public PaymentInfo(int pk, int jumpTo, int cpid) {
            this.pk = pk;
            this.jumpTo = jumpTo;
            this.cpid = cpid;
        }

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


    enum FromPage {
        unknown
    }

    enum ProductCategory {
        item,
        Package,
        charge,
        subitem;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

}
