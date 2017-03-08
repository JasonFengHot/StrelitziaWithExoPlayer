package tv.ismar.app.core;

import android.app.Activity;
import android.app.LauncherActivity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import tv.ismar.app.R;
import tv.ismar.app.network.entity.FeedBackEntity;
import tv.ismar.app.ui.MessageDialogFragment;

/**
 * Created by huibin on 9/13/16.
 */
public class PageIntent implements PageIntentInterface {

    @Override
    public void toDetailPage(final Context context, final String source, final int pk) {

                Intent intent = new Intent();
                intent.setAction("tv.ismar.daisy.detailpage");
                intent.putExtra(EXTRA_PK, pk);
                intent.putExtra(EXTRA_SOURCE, source);
                intent.putExtra(EXTRA_TYPE, DETAIL_TYPE_ITEM);
                if(context instanceof Activity){
                    ((Activity)context).startActivityForResult(intent,1);
                }else {
                    context.startActivity(intent);
                }



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
    public void toPackageDetail(final Context context, final String source, final int pk) {

                Intent intent = new Intent();
                intent.setAction("tv.ismar.daisy.detailpage");
                intent.putExtra(EXTRA_PK, pk);
                intent.putExtra(EXTRA_SOURCE, source);
                intent.putExtra(EXTRA_TYPE, DETAIL_TYPE_PKG);
                if(context instanceof Activity){
                    ((Activity)context).startActivityForResult(intent,1);
                }else {
                     context.startActivity(intent);
                 }

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


//    @Override
//    public void toPayment(Context context, String fromPage, PaymentInfo paymentInfo) {
//        Intent intent = new Intent();
//        switch (paymentInfo.getJumpTo()) {
//            //直接支付
//            case PAYMENT:
//                intent.setAction("tv.ismar.pay.payment");
//                intent.putExtra(EXTRA_PK, paymentInfo.getPk());
//                intent.putExtra(EXTRA_PRODUCT_CATEGORY, paymentInfo.getCategory().toString());
//                break;
//            case PAY:
//                intent.setAction("tv.ismar.pay.pay");
//                intent.putExtra("item_id", paymentInfo.getPk());
//                break;
//            case PAYVIP:
//                intent.setAction("tv.ismar.pay.payvip");
//                intent.putExtra("cpid", paymentInfo.getCpid());
//                intent.putExtra("item_id", paymentInfo.getPk());
//                break;
//            default:
//                throw new IllegalArgumentException();
//        }
//        context.startActivity(intent);
//    }

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


    public void toPlayPage(Context context, int pk, int sub_item_pk, Source source) {
        Log.i("toPlayPage","startpalyer");
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.Player");
        intent.putExtra(PageIntentInterface.EXTRA_PK, pk);
        intent.putExtra(PageIntentInterface.EXTRA_SUBITEM_PK, sub_item_pk);
        intent.putExtra(PageIntentInterface.EXTRA_SOURCE, source.getValue());
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
    public void toPackageList(Context context, String source, int pk) {
        Intent intent=new Intent();
        intent.setAction("tv.ismar.daisy.packagelist");
        intent.putExtra("pk",pk);
        context.startActivity(intent);
    }

    public void toHistory(Context context) {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.Channel");
        intent.putExtra("channel", "histories");
        context.startActivity(intent);
    }

    public void toFavorite(Context context) {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.Channel");
        intent.putExtra("channel", "$bookmarks");
        context.startActivity(intent);
    }

    public void toSearch(Context context) {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.searchpage.search");
        intent.putExtra("frompage","search");
        context.startActivity(intent);
    }

    @Override
    public void toFilmStar(Context context, String title, long pk) {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.searchpage.filmstar");
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_PK, pk);
        context.startActivity(intent);
    }

    @Override
    public void toEpisodePage(Context context, String source, String itemJson) {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.episode");
        intent.putExtra(EXTRA_ITEM_JSON, itemJson);
        intent.putExtra(EXTRA_SOURCE, source);
        context.startActivity(intent);
    }

    @Override
    public void toHelpPage(Context context) {
        Intent intent = new Intent();
        try {
            intent.setAction("cn.ismartv.speedtester.feedback");
            context.startActivity(intent);
        }catch (ActivityNotFoundException e) {
            intent.setAction("cn.ismar.sakura.launcher");
            context.startActivity(intent);
        }
    }

    private static void showNetErrorPopup(Context context, String message) {
        View rootView = ((Activity) context).getWindow().getDecorView();
        final MessageDialogFragment dialog = new MessageDialogFragment(context, message, null);

        dialog.setButtonText(context.getString(R.string.vod_i_know), null);
        dialog.showAtLocation(rootView, Gravity.CENTER,
                new MessageDialogFragment.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        dialog.dismiss();
                    }
                }, null);

    }


}
