package tv.ismar.Utils;

import java.util.HashMap;

import tv.ismar.app.core.client.NetworkUtils;

/** Created by liucan on 2016/12/7. */
public class LogUtils {
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
            String version,
            String code,
            String detail) {
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
