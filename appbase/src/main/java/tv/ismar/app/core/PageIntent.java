package tv.ismar.app.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * Created by huibin on 9/13/16.
 */
public class PageIntent implements PageIntentInterface {

    @Override
    public void toDetailPage(Context context, String source, int pk) {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.detailpage");
        intent.putExtra(EXTRA_PK, pk);
        intent.putExtra(EXTRA_SOURCE, source);
        intent.putExtra(EXTRA_TYPE, DETAIL_TYPE_ITEM);
        context.startActivity(intent);
    }


    @Override
    public void toDetailPage(Context context, String source, String json) {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.detailpage");
        intent.putExtra(EXTRA_SOURCE, source);
        intent.putExtra(EXTRA_ITEM_JSON, json);
        intent.putExtra(EXTRA_TYPE, DETAIL_TYPE_ITEM);
        context.startActivity(intent);
    }

    @Override
    public void toPackageDetail(Context context, String source, int pk) {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.detailpage");
        intent.putExtra(EXTRA_PK, pk);
        intent.putExtra(EXTRA_SOURCE, source);
        intent.putExtra(EXTRA_TYPE, DETAIL_TYPE_PKG);
        context.startActivity(intent);
    }

    @Override
    public void toPackageDetail(Context context, String source, String json) {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.detailpage");
        intent.putExtra(EXTRA_SOURCE, source);
        intent.putExtra(EXTRA_ITEM_JSON, json);
        intent.putExtra(EXTRA_TYPE, DETAIL_TYPE_PKG);
        context.startActivity(intent);
    }



    @Override
    public void toPayment(Context context, String fromPage, PaymentInfo paymentInfo) {
        Intent intent = new Intent();
        switch (paymentInfo.getJumpTo()) {
            //直接支付
            case PAYMENT:
                intent.setAction("tv.ismar.pay.payment");
                intent.putExtra(EXTRA_PK, paymentInfo.getPk());
                intent.putExtra(EXTRA_PRODUCT_CATEGORY, paymentInfo.getCategory().toString());
                break;
            case PAY:
                intent.setAction("tv.ismar.pay.pay");
                intent.putExtra("item_id", paymentInfo.getPk());
                break;
            case PAYVIP:
                intent.setAction("tv.ismar.pay.payvip");
                intent.putExtra("cpid", paymentInfo.getCpid());
                intent.putExtra("item_id", paymentInfo.getPk());
                break;
            default:
                throw new IllegalArgumentException();
        }
        context.startActivity(intent);
    }

    @Override
    public void toPaymentForResult(Activity activity, String fromPage, PaymentInfo paymentInfo) {
        Intent intent = new Intent();
        switch (paymentInfo.getJumpTo()) {
            case PAYMENT:
                intent.setAction("tv.ismar.pay.payment");
                intent.putExtra(EXTRA_PK, paymentInfo.getPk());
                intent.putExtra(EXTRA_PRODUCT_CATEGORY, paymentInfo.getCategory().toString());
                break;
            case PAY:
                intent.setAction("tv.ismar.pay.pay");
                intent.putExtra("item_id", paymentInfo.getPk());
                break;
            case PAYVIP:
                intent.setAction("tv.ismar.pay.payvip");
                intent.putExtra("cpid", paymentInfo.getCpid());
                intent.putExtra("item_id", paymentInfo.getPk());
                break;
            default:
                throw new IllegalArgumentException();
        }
        activity.startActivityForResult(intent, PAYMENT_REQUEST_CODE);
    }


    public void toPlayPage(Context context, int pk, int sub_item_pk, String source) {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.Player");
        intent.putExtra(PageIntentInterface.EXTRA_PK, pk);
        intent.putExtra(PageIntentInterface.EXTRA_SUBITEM_PK, sub_item_pk);
        intent.putExtra(PageIntentInterface.EXTRA_SOURCE, source);
        context.startActivity(intent);
    }

    @Override
    public void toUserCenter(Context context) {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.usercenter");
        context.startActivity(intent);
    }

    @Override
    public void toUserCenterLocation(Context context) {

    }

    @Override
    public void toPackageList(Context context, String source, long pk) {

    }

    public void toHistory(Context context){
        Intent intent=new Intent();
        intent.setAction( "tv.ismar.daisy.Channel");
        intent.putExtra("channel", "histories");
        context.startActivity(intent);
    }
    public void toFavorite(Context context){
        Intent intent=new Intent();
        intent.setAction("tv.ismar.daisy.Channel");
        intent.putExtra("channel", "$bookmarks");
        context.startActivity(intent);
    }
    public void toSearch(Context context){
        Intent intent=new Intent();
        intent.setAction("tv.ismar.searchpage.search");
        context.startActivity(intent);
    }
}
