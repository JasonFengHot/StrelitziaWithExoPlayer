package tv.ismar.searchpage.utils;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;

import java.util.HashMap;

import tv.ismar.app.core.client.NetworkUtils;
import tv.ismar.app.network.entity.EventProperty;
import tv.ismar.searchpage.R;

/** Created by admin on 2016/1/13. */
public class JasmineUtil {

    public static void scaleOut(View view) {

        Animator animator =
                AnimatorInflater.loadAnimator(view.getContext(), R.animator.scaleout_hotword);
        animator.setTarget(view);
        animator.start();
    }

    public static void scaleIn(View view) {

        Animator animator =
                AnimatorInflater.loadAnimator(view.getContext(), R.animator.scalein_hotword);
        animator.setTarget(view);
        animator.start();
    }

    public static void scaleOut1(View view) {

        Animator animator =
                AnimatorInflater.loadAnimator(view.getContext(), R.animator.scaleout_poster);
        animator.setTarget(view);
        animator.start();
    }

    public static void scaleIn1(View view) {

        Animator animator =
                AnimatorInflater.loadAnimator(view.getContext(), R.animator.scalein_poster);
        animator.setTarget(view);
        animator.start();
    }

    public static void showKeyboard(Context context, final View view) {
        ObjectAnimator showAnimator =
                ObjectAnimator.ofFloat(
                        view,
                        "translationX",
                        -context.getResources().getDimensionPixelOffset(R.dimen.keyboard_width),
                        0);
        showAnimator.setDuration(500);
        showAnimator.start();
    }

    public static void hideKeyboard(Context context, View view) {
        ObjectAnimator showAnimator =
                ObjectAnimator.ofFloat(
                        view,
                        "translationX",
                        0,
                        -context.getResources().getDimensionPixelOffset(R.dimen.keyboard_width));
        showAnimator.setDuration(500);
        showAnimator.start();
    }

    /** 打开搜索app日志上报 */
    public static void app_start(
            String sn,
            String device,
            String size,
            String os_version,
            long sd_size,
            long sd_free_size,
            String userid,
            String province,
            String city,
            String isp,
            String source,
            String Mac,
            String title,
            String code,
            String version) {
        HashMap<String, Object> tempMap = new HashMap<>();
        tempMap.put(EventProperty.SN, sn);
        tempMap.put(EventProperty.DEVICE, device);
        tempMap.put(EventProperty.SIZE, size);
        tempMap.put(EventProperty.OS_VERSION, os_version);
        tempMap.put(EventProperty.SD_SIZE, sd_size);
        tempMap.put(EventProperty.SD_FREE_SIZE, sd_free_size);
        tempMap.put(EventProperty.USER_ID, userid);
        tempMap.put(EventProperty.PROVINCE, province);
        tempMap.put(EventProperty.CITY, city);
        tempMap.put(EventProperty.ISP, isp);
        tempMap.put(EventProperty.SOURCE, source);
        tempMap.put(EventProperty.MAC, Mac);
        tempMap.put(EventProperty.TITLE, title);
        tempMap.put(EventProperty.CODE, code);
        tempMap.put(EventProperty.VERSION, version);
        String eventName = NetworkUtils.APP_START;
        HashMap<String, Object> properties = tempMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }

    /** 点击搜索结果日志上报 */
    public static void video_search_arrive(
            String qWord, String content_type, int item, int subitem, String title) {
        if (content_type != null && content_type.equals("person")) {
            content_type = "star";
        }
        HashMap<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put(EventProperty.Q, qWord);
        tempMap.put(EventProperty.CONTENT_TYPE, content_type);
        tempMap.put(EventProperty.ITEM, item);
        tempMap.put(EventProperty.SUBITEM, subitem);
        tempMap.put(EventProperty.TITLE, title);
        String eventName = NetworkUtils.VIDEO_SEARCH_ARRIVE;
        HashMap<String, Object> properties = tempMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }

    /** 搜索日志上报 */
    public static void video_search(String content_type, String qWord) {
        if (content_type.equals("person")) {
            content_type = "star";
        }
        HashMap<String, Object> tempMap = new HashMap<>();
        tempMap.put(EventProperty.INTERFACE_TYPE, "text");
        tempMap.put(EventProperty.CONTENT_TYPE, content_type);
        tempMap.put(EventProperty.Q, qWord);
        String eventName = NetworkUtils.VIDEO_SEARCH;
        HashMap<String, Object> properties = tempMap;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }

    /**
     * 页面加载失败日志上报
     *
     * @param referer 进入当前页面的入口
     * @param page 表示页面的类型
     * @param channel 页面所属频道
     * @param tab 用于表示出错的具体标签页，例如频道的列表页中每个section为一个标签页。如果没有细分标签页，则值为空字符串
     * @param item 如果页面的异常发生在某个视频、产品包上，则item值为异常视频item或者产品包的ID，否则该值为空字符串
     * @param url 该页面发生异常的区域对应的服务器数据接口的URL及参数，对于不需要请求服务器数据的异常，设置为空值。
     * @param version 表示视云客户端的版本号
     * @param code
     *     表示异常产生的原因。可选的值为：server（表示服务端、网络、或CDN的各类错误或异常）、data（表示拿到的数据错误或不完整）、system（电视的系统级异常导致）、client（视云客户端产生的异常导致）、unknown（未知原因的异常）
     * @param detail
     *     表示出错的具体信息，该字段的定义权保留给开发人员，用于辅助开发人员、客户端维护人员、CMS服务维护人员用于追踪except的细节用途，由开发自行定义值的内部格式和含义。如果不需要该字段，则不需要在事件中保留该字段，这是一个可选字段。
     */
    public static void loadException(
            String referer,
            String page,
            String channel,
            String tab,
            int item,
            String url,
            int version,
            String code,
            String detail) {
        if (channel.equals("person")) {
            channel = "star";
        }
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("referer", referer);
        properties.put("page", page);
        properties.put("channel", channel);
        properties.put("tab", tab);
        properties.put("item", item);
        properties.put("url", url);
        properties.put("version", version);
        properties.put("code", code);
        properties.put("detail", detail);
        String eventName = NetworkUtils.EXCEPTION_EXIT;
        new NetworkUtils.DataCollectionTask().execute(eventName, properties);
    }
}
