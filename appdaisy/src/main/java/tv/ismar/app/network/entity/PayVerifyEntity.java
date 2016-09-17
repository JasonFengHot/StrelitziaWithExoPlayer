package tv.ismar.app.network.entity;

/**
 * Created by huibin on 2016/9/17.
 */
public class PayVerifyEntity {
    private String status;
    private String err_desc;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErr_desc() {
        return err_desc;
    }

    public void setErr_desc(String err_desc) {
        this.err_desc = err_desc;
    }
}
