package tv.ismar.searchpage.core.channel;

import java.util.HashMap;

/**
 * Created by huaijie on 4/27/16.
 */
public class DefaultChannelPipeline extends ChannelPipeline {

    @Override
    public ChannelPipeline addLast(String name, ChannelHandler handler) {
        HashMap<String, ChannelHandler> hashMap = new HashMap<>();
        hashMap.put(name, handler);
        add(hashMap);
        return this;
    }
}
