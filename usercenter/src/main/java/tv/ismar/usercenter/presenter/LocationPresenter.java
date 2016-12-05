package tv.ismar.usercenter.presenter;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import okhttp3.ResponseBody;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.core.weather.WeatherInfoHandler;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.WeatherEntity;
import tv.ismar.usercenter.LocationContract;
import tv.ismar.usercenter.view.LocationFragment;
import tv.ismar.usercenter.view.UserCenterActivity;

/**
 * Created by huibin on 10/28/16.
 */

public class LocationPresenter implements LocationContract.Presenter {
    private LocationFragment mFragment;
    private UserCenterActivity mActivity;
    private SkyService mSkyService;


    public LocationPresenter(LocationFragment locationFragment) {
        locationFragment.setPresenter(this);
        mFragment = locationFragment;

    }

    @Override
    public void start() {
        mActivity = (UserCenterActivity) mFragment.getActivity();
        mSkyService = mActivity.mWeatherSkyService;
    }

    @Override
    public void stop() {

    }

    @Override
    public void fetchWeather(String geoId) {
        mSkyService.apifetchWeatherInfo(geoId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            parseXml(responseBody.string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    private void parseXml(String xml) {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = saxParserFactory.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            WeatherInfoHandler weatherInfoHandler = new WeatherInfoHandler();
            xmlReader.setContentHandler(weatherInfoHandler);
            InputSource inputSource = new InputSource(new StringReader(xml));
            xmlReader.parse(inputSource);

            WeatherEntity weatherEntity = weatherInfoHandler.getWeatherEntity();
            mFragment.refreshWeather(weatherEntity);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
