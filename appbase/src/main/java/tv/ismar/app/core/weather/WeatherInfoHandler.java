package tv.ismar.app.core.weather;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import tv.ismar.app.db.weather.WeatherEntity;

/**
 * Created by huaijie on 9/15/15.
 */
public class WeatherInfoHandler extends DefaultHandler {

    private final int TODAY = 5;
    private final int TOMORROW = 6;

    private final int UPDATED = 7;
    private final int REGION = 8;


    private final int CONDITION = 1;
    private final int TEMPHIGH = 2;
    private final int TEMPLOW = 3;
    private final int IMAGE_URL = 4;

    private int flg = 0;

    private WeatherEntity weatherEntity;
    private WeatherEntity.WeatherDetail weatherDetail;


    public WeatherEntity getWeatherEntity() {
        return weatherEntity;
    }

    @Override
    public void startDocument() throws SAXException {
        weatherEntity = new WeatherEntity();
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }


    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (localName.equals("WeatherWindow")) {
            flg = 0;
            return;
        }

        if (localName.equals("updated")) {
            flg = UPDATED;
            return;
        }

        if (localName.equals("region")) {
            flg = REGION;
            return;
        }

        if (localName.equals("today")) {
            weatherDetail = new WeatherEntity.WeatherDetail();
            flg = TODAY;
            return;
        }


        if (localName.equals("tomorrow")) {
            weatherDetail = new WeatherEntity.WeatherDetail();
            flg = TOMORROW;
            return;
        }

        if (localName.equals("condition")) {
            flg = CONDITION;
            return;
        }

        if (localName.equals("temphigh")) {
            flg = TEMPHIGH;
            return;
        }

        if (localName.equals("templow")) {
            flg = TEMPLOW;
            return;
        }

        if (localName.equals("image_url")) {
            flg = IMAGE_URL;
            return;
        }


    }


    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (localName.equals("today")) {
            weatherEntity.setToday(weatherDetail);
            weatherDetail = null;
            return;
        }

        if (localName.equals("tomorrow")) {
            weatherEntity.setTomorrow(weatherDetail);
            weatherDetail = null;
            return;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String str = new String(ch, start, length);

        switch (flg) {
            case UPDATED:
                weatherEntity.setUpdated(str);
                break;
            case REGION:
                weatherEntity.setRegion(str);
                break;
            case CONDITION:
                weatherDetail.setCondition(str);
                break;
            case TEMPHIGH:
                weatherDetail.setTemphigh(str);
                break;
            case TEMPLOW:
                weatherDetail.setTemplow(str);
                break;
            case IMAGE_URL:
                weatherDetail.setImage_url(str);
                break;
            default:
                return;
        }
    }
}
