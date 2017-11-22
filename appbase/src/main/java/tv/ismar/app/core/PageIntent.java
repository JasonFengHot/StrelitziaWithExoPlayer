package tv.ismar.app.core;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import java.util.UUID;

import tv.ismar.app.PlayerHelper;
import tv.ismar.app.R;
import tv.ismar.app.network.entity.ClipEntity;
import tv.ismar.app.ui.MessageDialogFragment;
import tv.ismar.iqiyiplayer.SdkTestActivity;

/**
 * Created by huibin on 9/13/16.
 */
public class PageIntent implements PageIntentInterface {
    private static final String TAG = "PageIntent";

    public static final String DRM_SCHEME_UUID_EXTRA = "drm_scheme_uuid";
    public static final String DRM_LICENSE_URL = "drm_license_url";
    public static final String DRM_KEY_REQUEST_PROPERTIES = "drm_key_request_properties";
    public static final String PREFER_EXTENSION_DECODERS = "prefer_extension_decoders";

    public static final String ACTION_VIEW = "com.google.android.exoplayer.demo.action.VIEW";
    public static final String EXTENSION_EXTRA = "extension";

    public static final String ACTION_VIEW_LIST =
            "com.google.android.exoplayer.demo.action.VIEW_LIST";
    public static final String URI_LIST_EXTRA = "uri_list";
    public static final String EXTENSION_LIST_EXTRA = "extension_list";

    private static void showNetErrorPopup(Context context, String message) {
        View rootView = ((Activity) context).getWindow().getDecorView();
        final MessageDialogFragment dialog = new MessageDialogFragment(context, message, null);

        dialog.setButtonText(context.getString(R.string.vod_i_know), null);
        dialog.showAtLocation(
                rootView,
                Gravity.CENTER,
                new MessageDialogFragment.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        dialog.dismiss();
                    }
                },
                null);
    }

    @Override
    public void toDetailPage(final Context context, final String source, final int pk) {

        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.detailpage");
        intent.putExtra(EXTRA_PK, pk);
        intent.putExtra(EXTRA_SOURCE, source);
        intent.putExtra(EXTRA_TYPE, DETAIL_TYPE_ITEM);
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, 1);
        } else {
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
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, 1);
        } else {
            context.startActivity(intent);
        }
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
    public void toPackageDetail(Context context, String source, String json) {

        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.detailpage");
        intent.putExtra(EXTRA_SOURCE, source);
        intent.putExtra(EXTRA_ITEM_JSON, json);
        intent.putExtra(EXTRA_TYPE, DETAIL_TYPE_PKG);
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

    public void toPlayPage(final Context context, int pk, int sub_item_pk, Source source) {
        Log.i("toPlayPage", "startpalyer");
        //        Intent intent = new Intent();
        //        intent.setAction("tv.ismar.daisy.Player");
        //        intent.putExtra(PageIntentInterface.EXTRA_PK, pk);
        //        intent.putExtra(PageIntentInterface.EXTRA_SUBITEM_PK, sub_item_pk);
        //        intent.putExtra(PageIntentInterface.EXTRA_SOURCE, source.getValue());
        //        context.startActivity(intent);
        new PlayerHelper(
                pk,
                new PlayerHelper.Callback() {
                    @Override
                    public void success(ClipEntity clipEntity) {
                        //爱奇艺视频源
                        if (!TextUtils.isEmpty(clipEntity.getIqiyi_4_0())) {
                            String iqiyi = clipEntity.getIqiyi_4_0();
                            Log.d(TAG, "iqiyi: " + iqiyi);
                            Intent intent = new Intent();
                            intent.setClass(context, SdkTestActivity.class);
                            intent.putExtra("iqiyi_params", iqiyi);
                            context.startActivity(intent);

                        } else {
                            //视云视频源
                            UriSample uriSample =
                                    new UriSample(
                                            "test",
                                            null,
                                            null,
                                            null,
                                            false,
                                            clipEntity.getM3u8(),
                                            null);
                            Intent intent = uriSample.buildIntent(context);
                            context.startActivity(intent);
                        }
                    }
                });
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
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.packagelist");
        intent.putExtra("pk", pk);
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
        intent.putExtra("frompage", "search");
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
        } catch (ActivityNotFoundException e) {
            intent.setAction("cn.ismar.sakura.launcher");
            context.startActivity(intent);
        }
    }

    private abstract static class Sample {

        public final String name;
        public final boolean preferExtensionDecoders;
        public final UUID drmSchemeUuid;
        public final String drmLicenseUrl;
        public final String[] drmKeyRequestProperties;

        public Sample(
                String name,
                UUID drmSchemeUuid,
                String drmLicenseUrl,
                String[] drmKeyRequestProperties,
                boolean preferExtensionDecoders) {
            this.name = name;
            this.drmSchemeUuid = drmSchemeUuid;
            this.drmLicenseUrl = drmLicenseUrl;
            this.drmKeyRequestProperties = drmKeyRequestProperties;
            this.preferExtensionDecoders = preferExtensionDecoders;
        }

        public Intent buildIntent(Context context) {
            Intent intent = new Intent();
            intent.setClassName(context, "tv.ismar.playerpage.PlayerActivity");
            intent.putExtra(PREFER_EXTENSION_DECODERS, preferExtensionDecoders);
            if (drmSchemeUuid != null) {
                intent.putExtra(DRM_SCHEME_UUID_EXTRA, drmSchemeUuid.toString());
                intent.putExtra(DRM_LICENSE_URL, drmLicenseUrl);
                intent.putExtra(DRM_KEY_REQUEST_PROPERTIES, drmKeyRequestProperties);
            }
            return intent;
        }
    }

    private static final class UriSample extends Sample {

        public final String uri;
        public final String extension;

        public UriSample(
                String name,
                UUID drmSchemeUuid,
                String drmLicenseUrl,
                String[] drmKeyRequestProperties,
                boolean preferExtensionDecoders,
                String uri,
                String extension) {
            super(
                    name,
                    drmSchemeUuid,
                    drmLicenseUrl,
                    drmKeyRequestProperties,
                    preferExtensionDecoders);
            this.uri = uri;
            this.extension = extension;
        }

        @Override
        public Intent buildIntent(Context context) {
            return super.buildIntent(context)
                    .setData(Uri.parse(uri))
                    .putExtra(EXTENSION_EXTRA, extension)
                    .setAction(ACTION_VIEW);
        }
    }

    private static final class PlaylistSample extends Sample {

        public final UriSample[] children;

        public PlaylistSample(
                String name,
                UUID drmSchemeUuid,
                String drmLicenseUrl,
                String[] drmKeyRequestProperties,
                boolean preferExtensionDecoders,
                UriSample... children) {
            super(
                    name,
                    drmSchemeUuid,
                    drmLicenseUrl,
                    drmKeyRequestProperties,
                    preferExtensionDecoders);
            this.children = children;
        }

        @Override
        public Intent buildIntent(Context context) {
            String[] uris = new String[children.length];
            String[] extensions = new String[children.length];
            for (int i = 0; i < children.length; i++) {
                uris[i] = children[i].uri;
                extensions[i] = children[i].extension;
            }
            return super.buildIntent(context)
                    .putExtra(URI_LIST_EXTRA, uris)
                    .putExtra(EXTENSION_LIST_EXTRA, extensions)
                    .setAction(ACTION_VIEW_LIST);
        }
    }
}
