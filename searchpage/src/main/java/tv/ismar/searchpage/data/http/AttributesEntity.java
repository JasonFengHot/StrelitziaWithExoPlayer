package tv.ismar.searchpage.data.http;

/**
 * Created by huaijie on 2/22/16.
 */
public class AttributesEntity {
    private Object[][] actor;
    private Object[][] director;
    private Object[] area;
    private Object[][] attendee;


    public Object[][] getActor() {
        return actor;
    }

    public void setActor(Object[][] actor) {
        this.actor = actor;
    }

    public Object[][] getDirector() {
        return director;
    }

    public void setDirector(Object[][] director) {
        this.director = director;
    }

    public Object[] getArea() {
        return area;
    }

    public void setArea(Object[] area) {
        this.area = area;
    }

    public Object[][] getAttendee() {
        return attendee;
    }

    public void setAttendee(Object[][] attendee) {
        this.attendee = attendee;
    }
}
