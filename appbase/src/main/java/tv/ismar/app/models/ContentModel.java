package tv.ismar.app.models;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class ContentModel implements Serializable {

    private static final long serialVersionUID = -8011567414214674419L;
    public String content_model;
    public String title;
    public int main_type;
    public LinkedHashMap<String, String> attributes;
    public String persion_attirbutes;
}
