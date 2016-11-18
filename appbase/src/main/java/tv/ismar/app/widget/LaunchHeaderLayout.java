package tv.ismar.app.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.utils.StringUtils;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import cn.ismartv.injectdb.library.query.Select;
import okhttp3.ResponseBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.R;
import tv.ismar.app.core.WeatherInfoHandler;
import tv.ismar.app.core.preferences.AccountSharedPrefs;
import tv.ismar.app.db.location.CityTable;
import tv.ismar.app.network.entity.WeatherEntity;

/**
 * Created by huaijie on 2015/7/21.
 */
public class LaunchHeaderLayout extends FrameLayout implements View.OnClickListener, View.OnFocusChangeListener, OnHoverListener {
    private static final String TAG = "LaunchHeaderLayout";
    private Context context;


    private TextView titleTextView;
    private TextView subTitleTextView;
    private TextView weatherInfoTextView;

    private ImageView dividerImage;

    private LinearLayout guideLayout;

//    private SharedPreferences locationSharedPreferences;

    private List<View> indicatorTableList;

    public LaunchHeaderLayout(Context context) {
        super(context);
        this.context = context;
    }

    public LaunchHeaderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        indicatorTableList = new ArrayList<View>();

        View view = LayoutInflater.from(context).inflate(R.layout.fragment_head, null);


        titleTextView = (TextView) view.findViewById(R.id.title);
        subTitleTextView = (TextView) view.findViewById(R.id.sub_title);
        weatherInfoTextView = (TextView) view.findViewById(R.id.weather_info);
        guideLayout = (LinearLayout) view.findViewById(R.id.indicator_layout);
        dividerImage = (ImageView) view.findViewById(R.id.divider);

        titleTextView.setText(R.string.app_name);
        subTitleTextView.setText(R.string.front_page);

//        locationSharedPreferences = context.getSharedPreferences(LocationFragment.LOCATION_PREFERENCE_NAME, Context.MODE_PRIVATE);

        createGuideIndicator();
        String cityName = AccountSharedPrefs.getInstance().getSharedPrefs(AccountSharedPrefs.CITY);

//        String geoId = locationSharedPreferences.getString(LocationFragment.LOCATION_PREFERENCE_GEOID, "101020100");

        CityTable cityTable = new Select().from(CityTable.class).where(CityTable.CITY + " = ?", cityName).executeSingle();
        if (cityTable != null) {
            fetchWeatherInfo(String.valueOf(cityTable.geo_id));
        }
        addView(view);
    }


    private static final int[] INDICATOR_RES_LIST = {
            R.string.vod_movielist_title_history,
            R.string.guide_my_favorite,
            R.string.guide_user_center,
            R.string.guide_search
    };

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        try {
            if (visibility == VISIBLE) {
                AccountSharedPrefs.getInstance().getSharedPreferences().registerOnSharedPreferenceChangeListener(changeListener);
            } else {
                AccountSharedPrefs.getInstance().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(changeListener);
            }
        } catch (Exception e) {
        }
    }

    private SharedPreferences.OnSharedPreferenceChangeListener changeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//           String geoId = locationSharedPreferences.getString(LocationFragment.LOCATION_PREFERENCE_GEOID, "101020100");
            try {
                String cityName = AccountSharedPrefs.getInstance().getSharedPrefs(AccountSharedPrefs.CITY);
                CityTable cityTable = new Select().from(CityTable.class).where(CityTable.CITY + " = ?", cityName).executeSingle();

                if (cityTable != null) {
                    if (key.equals(AccountSharedPrefs.GEO_ID)) {
                        fetchWeatherInfo(String.valueOf(cityTable.geo_id));
                    }
                }
            } catch (Exception e) {

            }
        }
    };


    private void createGuideIndicator() {
        int i = 0;
        indicatorTableList.clear();
        for (int res : INDICATOR_RES_LIST) {

            View view = LayoutInflater.from(context).inflate(R.layout.item_header_indicator, null);
            TextView textView = (TextView) view.findViewById(R.id.weather_indicator);
            view.setOnClickListener(this);
            view.setOnFocusChangeListener(this);
            view.setOnHoverListener(this);
            textView.setText(res);
            view.setId(res);
            if (i == 0) {
                view.setNextFocusLeftId(view.getId());
            }
            if (i == INDICATOR_RES_LIST.length - 1) {
             //   view.setRight(-20);
                view.setNextFocusRightId(view.getId());
            }
            guideLayout.addView(view);
            indicatorTableList.add(view);
            i++;
        }
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    public void setSubTitle(String subTitle) {
        if (TextUtils.isEmpty(subTitle)) {
            hideSubTiltle();
        } else {
            subTitleTextView.setText(subTitle.replace(" ", ""));
        }
    }

    public void hideSubTiltle() {
        subTitleTextView.setVisibility(View.GONE);
        dividerImage.setVisibility(View.GONE);

    }

    private void fetchWeatherInfo(String geoId) {
//       Log.i(TAG, "fetchWeatherInfo: " + getContext().toString());
        String weather = PreferenceManager.getDefaultSharedPreferences(context).getString(AccountSharedPrefs.WEATHER_INFO, null);
        if (!StringUtils.isEmpty(weather)) {
            parseXml(weather);
        }
        ((BaseActivity) context).mWeatherSkyService.apifetchWeatherInfo(geoId).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(((BaseActivity) context).new BaseObserver<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {

                        String result = null;
                        try {
                            result = responseBody.string();
                            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(AccountSharedPrefs.WEATHER_INFO, result);
                            parseXml(result);
                        } catch (IOException e) {
                            Log.e(TAG, "解析天气数据失败");
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

            Date now = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");//可以方便地修改日期格式
            String todayTime = dateFormat.format(now);

//            weatherInfoTextView.setText("");
//                    weatherInfoTextView.append("   " + calendar.get(Calendar.YEAR) + context.getText(R.string.year).toString() +
//                            calendar.get(Calendar.MONTH) + context.getText(R.string.month).toString() +
//                            calendar.get(Calendar.DATE) + context.getText(R.string.day).toString() + "   ");
//            weatherInfoTextView.append("   " + todayTime + "   ");

            weatherInfoTextView.append(weatherEntity.getToday().getCondition() + "   ");
            if (weatherEntity.getToday().getTemplow().equals(weatherEntity.getToday().getTemphigh())) {
                weatherInfoTextView.append(weatherEntity.getToday().getTemplow() + context.getText(R.string.degree));
            } else {
                weatherInfoTextView.append(weatherEntity.getToday().getTemplow() + " ~ " + weatherEntity.getToday().getTemphigh() + context.getText(R.string.degree));
            }


        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if (mHeadItemClickListener != null) {
            int i = v.getId();
            if (i == R.string.vod_movielist_title_history) {
                mHeadItemClickListener.onHistoryClick();
            } else if (i == R.string.guide_my_favorite) {
                mHeadItemClickListener.onFavoriteClick();
            } else if (i == R.string.guide_user_center) {
                mHeadItemClickListener.onUserCenterClick();

            } else if (i == R.string.guide_search) {
                mHeadItemClickListener.onSearchClick();
            }
        }

//        PageIntent pageIntent = new PageIntent();
//        Intent intent = new Intent();
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//        int i = v.getId();
//        if (i == R.string.vod_movielist_title_history) {
//            intent.setClassName("tv.ismar.daisy",
//                    "tv.ismar.daisy.ChannelListActivity");
//            intent.putExtra("channel", "histories");
//
//        } else if (i == R.string.guide_my_favorite) {
//            intent.setClassName("tv.ismar.daisy",
//                    "tv.ismar.daisy.ChannelListActivity");
//            intent.putExtra("channel", "$bookmarks");
//
//        } else if (i == R.string.guide_user_center) {
//            pageIntent.toUserCenter(context);
//        } else if (i == R.string.guide_search) {
//            if (isAppInstalled(context, "cn.ismartv.Jasmine")) {
//                intent.setAction("cn.ismartv.jasmine.wordsearchactivity");
//            } else {
//                intent.setAction("tv.ismar.daisy.Search");
//            }
//
//        }
//        context.startActivity(intent);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        ImageView imageView = (ImageView) v.findViewById(R.id.indicator_image);
        TextView textView = (TextView) v.findViewById(R.id.weather_indicator);
        if (hasFocus) {
            textView.setTextColor(getResources().getColor(R.color._ff9c3c));

            imageView.setVisibility(View.VISIBLE);

        } else {
            textView.setTextColor(getResources().getColor(R.color.association_normal));
            imageView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        ImageView imageView = (ImageView) v.findViewById(R.id.indicator_image);
        TextView textView = (TextView) v.findViewById(R.id.weather_indicator);
        if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
            textView.setTextColor(getResources().getColor(R.color._ff9c3c));
            imageView.setVisibility(View.VISIBLE);
            v.requestFocus();
        } else if (event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
            textView.setTextColor(getResources().getColor(R.color._ff9c3c));
            imageView.setVisibility(View.VISIBLE);
        } else {
            textView.setTextColor(getResources().getColor(
                    R.color.association_normal));
            imageView.setVisibility(View.INVISIBLE);
        }
        return false;
    }

    public void hideIndicatorTable() {
        for (View textView : indicatorTableList) {
            textView.setVisibility(View.GONE);
        }
    }

    public void hideWeather() {
        weatherInfoTextView.setVisibility(View.INVISIBLE);
    }


    public interface HeadItemClickListener {
        void onUserCenterClick();

        void onHistoryClick();

        void onFavoriteClick();

        void onSearchClick();
    }

    private HeadItemClickListener mHeadItemClickListener;


    public void setHeadItemClickListener(HeadItemClickListener headItemClickListener) {
        mHeadItemClickListener = headItemClickListener;
    }

    public boolean isAppInstalled(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        List<String> pName = new ArrayList<String>();
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                pName.add(pn);
            }
        }
        return pName.contains(packageName);
    }


}
