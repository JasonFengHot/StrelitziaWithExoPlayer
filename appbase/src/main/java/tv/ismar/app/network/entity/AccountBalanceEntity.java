package tv.ismar.app.network.entity;

import java.math.BigDecimal;

/** Created by huibin on 2016/9/14. */
public class AccountBalanceEntity {
    private BigDecimal balance;
    private BigDecimal sn_balance;

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getSn_balance() {
        return sn_balance;
    }

    public void setSn_balance(BigDecimal sn_balance) {
        this.sn_balance = sn_balance;
    }
}
