package tv.ismar.app.network.entity;

import java.util.List;
import java.util.Map;

/** Created by huaijie on 8/4/14. */
public class FeedBackEntity {

    private String city;
    private String description;
    private String ip;
    private String phone;
    private String isp;
    private String location;
    private String mail;
    private int option;
    private String is_correct;
    private String width;
    private List<Map<String, String>> speed;

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String descriptionl) {
        this.description = descriptionl;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public int getOption() {
        return option;
    }

    public void setOption(int option) {
        this.option = option;
    }

    public String getIs_correct() {
        return is_correct;
    }

    public void setIs_correct(String is_correct) {
        this.is_correct = is_correct;
    }

    public List<Map<String, String>> getSpeed() {
        return speed;
    }

    public void setSpeed(List<Map<String, String>> speed) {
        this.speed = speed;
    }
}
