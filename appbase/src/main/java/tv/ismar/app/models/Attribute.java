package tv.ismar.app.models;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class Attribute implements Serializable {

    private static final long serialVersionUID = 3409800758940535030L;

    public String air_date;
    public LinkedHashMap map;

    public static class Info implements Serializable {
        private static final long serialVersionUID = 148464239713571723L;
        public Integer id;
        public String name;
    }
}
