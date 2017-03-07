package tv.ismar.statistics;

import java.util.HashMap;

import cn.ismartv.truetime.TrueTime;
import tv.ismar.app.AppConstant;
import tv.ismar.app.core.client.NetworkUtils;

/**
 * Created by huibin on 12/26/2016.
 */

public class PurchaseStatistics {


    /**
     * @param item     媒体id
     * @param clip     视频id。如果是单片或电视剧的单集，则为视频的实际clip。如果是电视剧，该值恒为-1
     * @param userid   用户ID, 例如：13932831006
     * @param title    名称, 例如：绝色武器
     * @param vendor   视频版权方，如果无法提供值为空字符串
     * @param price    产品包或单片的价格
     * @param player   播放器(qiyi|bestv)
     * @param result   预览结束状态 (cancel|purchase) cancel表示用户半途退出预览播放，purchase表示视频播放完自动进入购买页
     * @param duration 预览花的时间长度
     * @param time     事件发生的时间戳
     */
    public void expenseVideoPreview(int item, int clip, String userid, String title, String vendor,
                                    float price, String player, String result, int duration, long time) {
        HashMap<String, Object> dataCollectionProperties = new HashMap<>();
        dataCollectionProperties.put("item", item);
        dataCollectionProperties.put("clip", clip);
        dataCollectionProperties.put("userid", userid);
        dataCollectionProperties.put("title", title);
        dataCollectionProperties.put("vendor", vendor);
        dataCollectionProperties.put("price", price);
        dataCollectionProperties.put("player", player);
        dataCollectionProperties.put("result", result);
        dataCollectionProperties.put("duration", duration);
        dataCollectionProperties.put("time", time);
        new NetworkUtils.DataCollectionTask().execute("expense_video_preview", dataCollectionProperties);

    }


    /**
     * @param item   媒体id, 例如：186151
     * @param userid 用户ID, 例如：18679793885
     * @param title  名称, 例如：铁拳
     * @param clip   视频id, 例如: 153976
     * @param time   事件发生的时间戳
     */
    public void expenseVideoClick(String item, String userid, String title, String clip, String time) {
        HashMap<String, Object> dataCollectionProperties = new HashMap<>();
        dataCollectionProperties.put("item", item);
        dataCollectionProperties.put("userid", userid);
        dataCollectionProperties.put("title", title);
        dataCollectionProperties.put("clip", clip);
        dataCollectionProperties.put("time", time);
        new NetworkUtils.DataCollectionTask().execute("expense_video_click", dataCollectionProperties);
    }


    /**
     * @param wares_id 产品包的内部ID，跟order_urserorder表保持一致
     * @param title    产品包名称, 例如：vip包月
     * @param price    产品包价格
     * @param time     时间戳
     * @param result   点击购买进入产品包购买页面，或者按返回退出 (enter|cancel)
     */
    public void expensePacketDetail(int wares_id, String title, float price, String result, long time) {
        HashMap<String, Object> dataCollectionProperties = new HashMap<>();
        dataCollectionProperties.put("wares_id", wares_id);
        dataCollectionProperties.put("title", title);
        dataCollectionProperties.put("price", price);
        dataCollectionProperties.put("result", result);
        dataCollectionProperties.put("time", time);
        new NetworkUtils.DataCollectionTask().execute("expense_packet_detail", dataCollectionProperties);
    }

    /**
     * @param wares_id 产品包的内部ID，跟order_urserorder表保持一致
     * @param title    产品包名称, 例如：vip包月
     * @param price    产品包价格
     * @param result   点击购买进入产品包购买页面，或者按返回退出 (enter|cancel)
     * @param time     时间戳
     */
    public void expensePacketChoose(int wares_id, String title, String price, String result, long time) {
        HashMap<String, Object> dataCollectionProperties = new HashMap<>();
        dataCollectionProperties.put("wares_id", wares_id);
        dataCollectionProperties.put("title", title);
        dataCollectionProperties.put("price", price);
        dataCollectionProperties.put("result", result);
        dataCollectionProperties.put("time", time);
        new NetworkUtils.DataCollectionTask().execute("expense_packet_choose", dataCollectionProperties);

    }


    /**
     * 视频入口位置相关的字段，见“视频入口定位”标签页。不同的入口页的字段不尽相同，请根据具体页面相应的设置
     * video_item 触发该页面的单片媒体ID，如果不是视频触发，则该字段省略
     * video_title 触发该页面的单片名称，如果不是视频触发，则该字段省略, 例如：绝色武器
     * userid 用户ID, 例如：13932831006，如果用户未登录就退出，则值为空
     * type 付费类型，(vip|independent|package) vip表示vip单点，independent表示独立单点，package产品包
     * wares_id 产品包的内部ID，跟order_urserorder表保持一致
     * subject 订购的产品包或视频的名称, 例如：vip包月price 产品包的价格
     * result 购买的最终状态(success|cancel|allow|except) 分别表示购买成功、取消购买、用户登录之后发现该用户拥有权限、发生异常
     * except 如果发生购买发生异常，该字段用于描述异常的具体信息，如果没有异常，该字段为空
     * source 购买的渠道，可选的值跟peony库的order_userorder表的source字段一致，目前可用的值包括(alipay|card|ismartv|lenovo|sky|weixin|alipay_wh|weixin_mp|bftv|alipay_mb|sharp)，如果取消订单或异常发生，该值可为空
     * trade_no 交易发生的ID号，跟order_userorder表trade_no字段保持一致，如果取消订单或异常发生，该值可为空
     * time 事件发生的时间戳
     */
    public void expensePageExit(
            String video_item, String video_title, String userid,
            String type, String wares_id, String subject,
            String result, String except, String trade_no) {
        HashMap<String, Object> dataCollectionProperties = new HashMap<>();
        dataCollectionProperties.put("referer", AppConstant.purchase_referer);
        dataCollectionProperties.put("page", AppConstant.purchase_page);
        dataCollectionProperties.put("channel", AppConstant.purchase_channel);
        dataCollectionProperties.put("tab", AppConstant.purchase_tab);
        dataCollectionProperties.put("video_item", video_item);
        dataCollectionProperties.put("video_title", video_title);
        dataCollectionProperties.put("userid", userid);
        dataCollectionProperties.put("type", type);
        dataCollectionProperties.put("wares_id", wares_id);
        dataCollectionProperties.put("subject", subject);
        dataCollectionProperties.put("result", result);
        dataCollectionProperties.put("except", except);
        dataCollectionProperties.put("trade_no", trade_no);
        dataCollectionProperties.put("time", TrueTime.now().getTime());
        new NetworkUtils.DataCollectionTask().execute("expense_page_exit", dataCollectionProperties);
    }
}
