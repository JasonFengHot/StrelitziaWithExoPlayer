package tv.ismar.app.network.entity;

import java.util.List;

public class ChatMsgEntity {

    private int count;

    private List<Data> data;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public class Data {
        private String reply;
        private String commont;
        private String reply_time;
        private String submit_time;

        public String getReply() {
            if (null == reply)
                return "";
            return reply;
        }

        public void setReply(String reply) {
            this.reply = reply;
        }

        public String getCommont() {
            if (null == commont)
                return "";
            return commont;
        }

        public void setCommont(String commont) {
            this.commont = commont;
        }

        public String getReply_time() {
            if (null == reply_time)
                return "";
            return reply_time;
        }

        public void setReply_time(String reply_time) {
            this.reply_time = reply_time;
        }

        public String getSubmit_time() {
            if (null == submit_time)
                return "";
            return submit_time;
        }

        public void setSubmit_time(String submit_time) {
            this.submit_time = submit_time;
        }
    }
}
