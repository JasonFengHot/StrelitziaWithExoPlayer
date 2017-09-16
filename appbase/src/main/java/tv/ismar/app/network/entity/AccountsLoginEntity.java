package tv.ismar.app.network.entity;

/** Created by huibin on 2016/9/14. */
public class AccountsLoginEntity {
    private String auth_token;
    private String zuser_token;

    public String getAuth_token() {
        return auth_token;
    }

    public void setAuth_token(String auth_token) {
        this.auth_token = auth_token;
    }

    public String getZuser_token() {
        return zuser_token;
    }

    public void setZuser_token(String zuser_token) {
        this.zuser_token = zuser_token;
    }
}
