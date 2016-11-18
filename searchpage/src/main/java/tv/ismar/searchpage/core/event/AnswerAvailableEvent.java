package tv.ismar.searchpage.core.event;

/**
 * Created by huaijie on 2/27/16.
 */
public class AnswerAvailableEvent {
    public static final int NETWORK_ERROR = 0x0001;

    public static final int REFRESH_APP_DATA_FIRST_TIEM = 0x0002;
    public static final int REFRESH_APP_DATA_AGAIN = 0x0003;
    public static final int REFRESH_VOD_DATA_FIRST_TIEM = 0x0002;
    public static final int REFRESH_VOD_DATA_AGAIN = 0x0003;


    private EventType eventType;
    private int eventCode;
    private Object msg;

    public AnswerAvailableEvent(EventType eventType, int eventCode) {
        this.eventType = eventType;
        this.eventCode = eventCode;
    }

    public AnswerAvailableEvent(EventType eventType) {
        this.eventType = eventType;
    }

    public Object getMsg() {
        return msg;
    }

    public void setMsg(Object msg) {
        this.msg = msg;
    }

    public EventType getEventType() {
        return eventType;
    }

    public int getEventCode() {
        return eventCode;
    }

    public enum EventType {
        NETWORK_ERROR,
        REFRESH_DATA,
        APP_UPDATE
    }


}
