package tv.ismar.searchpage.core.handler;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import tv.ismar.searchpage.data.http.WeatherEntity;

/**
 * Created by huaijie on 1/20/16.
 */
public class WeatherXmlParser {
    public static WeatherEntity parse(String xml) {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = saxParserFactory.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            WeatherInfoHandler weatherInfoHandler = new WeatherInfoHandler();
            xmlReader.setContentHandler(weatherInfoHandler);
            InputSource inputSource = new InputSource(new StringReader(xml));
            xmlReader.parse(inputSource);

            WeatherEntity weatherEntity = weatherInfoHandler.getWeatherEntity();

            return weatherEntity;
//            if (weatherEntity.getToday().getTemplow().equals(weatherEntity.getToday().getTemphigh())) {
//                todayWeatherTemperature.setText(weatherEntity.getToday().getTemplow() + "℃ ");
//            } else {
//                todayWeatherTemperature.setText(weatherEntity.getToday().getTemplow() + "℃ ~ " + weatherEntity.getToday().getTemphigh() + "℃");
//            }
//            todayWeatherInfo.setText(weatherEntity.getToday().getCondition());
//            Picasso.with(mContext).load(weatherEntity.getToday().getImage_url()).into(todayWeatherIcon1);
//
//
//            if (weatherEntity.getTomorrow().getTemplow().equals(weatherEntity.getTomorrow().getTemphigh())) {
//                tomorrowWeatherTemperature.setText(weatherEntity.getTomorrow().getTemplow() + "℃ ");
//            } else {
//                tomorrowWeatherTemperature.setText(weatherEntity.getTomorrow().getTemplow() + "℃ ~ " + weatherEntity.getTomorrow().getTemphigh() + "℃");
//            }
//            tomorrowWeatherInfo.setText(weatherEntity.getTomorrow().getCondition());
//            Picasso.with(mContext).load(weatherEntity.getTomorrow().getImage_url()).into(tomorrowWeatherIcon1);


        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new WeatherEntity();
    }
}
