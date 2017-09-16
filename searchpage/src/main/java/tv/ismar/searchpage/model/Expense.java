package tv.ismar.searchpage.model;

import java.io.Serializable;

public class Expense implements Serializable {

    public static final int ISMARTV_CPID = 3;
    public static final int IQIYI_CPID = 2;
    public static final int SEPARATE_CHARGE = 1;
    private static final long serialVersionUID = 8475391000819295987L;
    public float price;
    public float subprice;
    public int duration;
    public int cpid;
    public String cpname;
    public String cptitle;
    public int pay_type;
    public String cplogo;
    public boolean sale_subitem;
    public int jump_to;
}
