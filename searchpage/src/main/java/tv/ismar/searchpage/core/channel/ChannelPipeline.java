package tv.ismar.searchpage.core.channel;

import java.util.LinkedList;
import java.util.Map;

/**
 * Created by huaijie on 4/27/16.
 */
public abstract class ChannelPipeline extends LinkedList<Map<String, ChannelHandler>> {

    public abstract ChannelPipeline addLast(String name, ChannelHandler handler);

}
