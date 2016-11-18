package tv.ismar.searchpage;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.models.HotWords;
import tv.ismar.app.models.VodFacetEntity;
import tv.ismar.app.models.VodObjectEntity;
import tv.ismar.app.models.VodSearchRequestEntity;
import tv.ismar.searchpage.adapter.KeyboardAdapter;
import tv.ismar.searchpage.adapter.PosterAdapter;
import tv.ismar.searchpage.adapter.RecommendAdapter;
import tv.ismar.searchpage.adapter.T9KeyboardAdapter;
import tv.ismar.searchpage.model.NineTKey;
import tv.ismar.app.models.Recommend;;
import tv.ismar.searchpage.utils.GsonTools;
import tv.ismar.searchpage.utils.JasmineUtil;
import tv.ismar.searchpage.utils.SharedPreferencesUtils;
import tv.ismar.searchpage.weight.MyDialog;
import tv.ismar.searchpage.weight.ZGridView;
import tv.ismar.searchpage.weight.ZGridView1;
import tv.ismar.searchpage.weight.ZGridView2;


public class WordSearchActivity extends BaseActivity implements View.OnClickListener, View.OnFocusChangeListener, View.OnHoverListener {
    private static final String TAG = "WordSearchActivity";
    private EditText et_input;
    private ZGridView1 keyboard;
    private TextView tv_search_all;
    private String[] keys;
    private String[] t9_nums;
    private String[] t9_letters;
    private String[] tabs;
    private View tv_back;
    private View tv_t9;
    private View tv_key_0;
    private Editable editable;
    private TextView today_hotword;
    private final int HOTWORDS = 0;
    private final int SEARCH_RESULT = 1;
    private final int VODSEARCH = 2;
    private final int RECOMMEND = 3;
    private final int VODSEARCH_CLASS = 4;
    private View clickView;
    private List<HotWords> hotWords;
    private List<HotWords> searchResult;
    private List<String> hotWordsList;
    private KeyboardAdapter keyboardAdapter;
    private ImageView iv_left_arrow;
    private ImageView iv_right_arrow;
    private HorizontalScrollView scrowview;
    private LinearLayout top_tabs;
    private ZGridView poster_gridview;
    private View search_guide;
    private boolean isHide = false;
    private StringBuffer sb;
    private int selectedTab = -1;
    private LinearLayout ll_hotwords;
    private int selectdHotWord = -1;
    private ImageView iv_toggle;
    private String[] tags;
    private PosterAdapter posterAdapter;
    private View rl_search_subject;
    private String keyWord_now;
    private String type_now;
    private int page = 1;
    private View view;
    private TextView tv_recommend;
    private ZGridView2 t9_keyboard;
    private List<NineTKey> keyList;
    private boolean first = true;
    private boolean firstin = true;
    private MyDialog errorDialog;
    private int index = 0;
    private boolean right;
    private boolean left;
    private View t9_key_0;
    private T9KeyboardAdapter t9KeyboardAdapter;
    private PopupWindow popupWindow;
    private View view_line;
    private View rl_recognize_hotword;
    private int count;
    private long lastClicktime = 0;
    private ImageView iv_top_arrow;
    private ImageView iv_down_arrow;
    private View junp_view;
    private MediaPlayer mediaPlayer;
    private android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1 && errorDialog != null) {
                lay_focus.requestFocus();
                errorDialog.show();

            }
        }
    };
    private View search_keyboard;
    private RecommendAdapter recommendAdapter;
    private BufferedReader reader;
    private int firstTab;
    private int lastTab;
    private View loading;
    private int clickposition;
    private final int SEARCH_ALL = 0;
    private final int SEARCH_WORDS = 1;
    private final int TOPTABS = 2;
    private boolean noResult = false;
    private boolean isT9;
    private View lay_focus;
    private int scroll = 0;
    //    private String domain ="http://skytest.tvxio.com/v2_0/SKY2/evg/";
    private String domain = "http://skytest.tvxio.com/v1_0/SKY/0g0/";
    private String app_update_domain = "";
    private String sn;
    private String device_token;
    private String acess_token;
    private String location;
    private int dimension;
    private List<String> searchResults;

    /**
     * Called when the activity is first created.
     * http://skytest.tvxio.com/v1_0/SKY/0g0
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_search);
        view = findViewById(R.id.view);
        try {
            Context data = createPackageContext("tv.ismar.daisy", CONTEXT_IGNORE_SECURITY);
            SharedPreferences spf = data.getSharedPreferences("account", Context.MODE_MULTI_PROCESS);
            location = spf.getString("province_py", "");
            domain = spf.getString("api_domain", "http://skytest.tvxio.com/v1_0/SKY/0g0");
//            domain ="http://skytest.tvxio.com/v3_0/SKY2/tou0" ;
            app_update_domain=spf.getString("app_update_domain","oak.t.tvxio.com");
            Log.e("Jasmine",app_update_domain);
            if (!domain.contains("http")) {
                domain = "http://" + domain;
            }
            sn = spf.getString("sn_token", "");
            device_token = spf.getString("device_token", "");
            acess_token = spf.getString("acess_token", "");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        errorDialog = new MyDialog(WordSearchActivity.this, getResources().getString(R.string.network_error) +
                getResources().getString(R.string.check_connect));
        errorDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == 4) {
                    if (handler.hasMessages(1)) {
                        handler.removeMessages(1);
                    }
                    if (loading.getVisibility() == View.VISIBLE) {
                        handler.sendEmptyMessageDelayed(1, 15000);
                    }
                } else {
                    errorDialog.confirm.requestFocus();
                }
                return false;
            }
        });
        initView();
        initData();

//        checkNetWork(this);
    }

    /**
     * 判断网络是否连接
     */
    private void checkNetWork(final Context context) {
        if (JasmineUtil.isNetworkConnected(context) || JasmineUtil.isWifiConnected(context)) {
            search(null,HOTWORDS);
            lay_focus.requestFocus();
        } else {
            lay_focus.requestFocus();
            errorDialog.show();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && firstin) {
            if (errorDialog == null) {
                errorDialog = new MyDialog(WordSearchActivity.this, getResources().getString(R.string.network_error) +
                        getResources().getString(R.string.check_connect));
            }
            checkNetWork(this);
            firstin = false;
            isT9 = SharedPreferencesUtils.getBoolean(this, "T9", false);
            if (isT9) {
                keyboard.setVisibility(View.GONE);
                tv_key_0.setVisibility(View.INVISIBLE);
                tv_key_0.setFocusable(false);
                tv_t9.findViewById(R.id.tv_T9_keyboard).setVisibility(View.GONE);
                tv_t9.findViewById(R.id.tv_full_keyboard).setVisibility(View.VISIBLE);
                t9_keyboard.setVisibility(View.VISIBLE);
                t9_key_0.setVisibility(View.VISIBLE);
                t9_keyboard.requestFocus();
                t9_keyboard.requestFocusFromTouch();
            } else {
                keyboard.setVisibility(View.VISIBLE);
                tv_key_0.setVisibility(View.VISIBLE);
                tv_key_0.setFocusable(true);
                tv_key_0.setFocusableInTouchMode(true);
                tv_t9.findViewById(R.id.tv_T9_keyboard).setVisibility(View.VISIBLE);
                tv_t9.findViewById(R.id.tv_full_keyboard).setVisibility(View.GONE);
                t9_keyboard.setVisibility(View.GONE);
                t9_key_0.setVisibility(View.GONE);
                keyboard.requestFocus();
                keyboard.requestFocusFromTouch();
            }
        }
    }

    @Override
    protected void onResume() {
        /**
         * 检查新版本
         */
        super.onResume();
    }

    private void initData() {
        keys = getResources().getStringArray(R.array.key);
        t9_nums = getResources().getStringArray(R.array.number);
        t9_letters = getResources().getStringArray(R.array.letter);
        keyList = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            NineTKey key = new NineTKey(t9_nums[i], t9_letters[i]);
            keyList.add(key);
        }
        tabs = getResources().getStringArray(R.array.tab);
        tags = getResources().getStringArray(R.array.tab_eng);
        keyboardAdapter = new KeyboardAdapter(this, keys);
        keyboard.setAdapter(keyboardAdapter);
        t9KeyboardAdapter = new T9KeyboardAdapter(this, keyList);
        t9_keyboard.setAdapter(t9KeyboardAdapter);
        for (int i = 0; i < 10; i++) {
            View view = View.inflate(this, R.layout.test, null);
            if (i == 9) {
                view.setNextFocusDownId(view.getId());
            }
            view.setNextFocusRightId(R.id.poster_gridview);
            view.setOnHoverListener(this);
            ll_hotwords.addView(view);
        }
        ll_hotwords.setVisibility(View.INVISIBLE);
        for (int i = 0; i < 10; i++) {
            final int finalI1 = i;
            ll_hotwords.getChildAt(i).setOnFocusChangeListener(new View.OnFocusChangeListener() {

                @Override
                public void onFocusChange(View view, boolean b) {
                    if (b) {
                        JasmineUtil.scaleOut(view.findViewById(R.id.tv_hotword));
                        ((TextView) view.findViewById(R.id.tv_hotword)).setTextColor(getResources().getColor(R.color.word_focus));
                        view.findViewById(R.id.iv_line).setVisibility(View.INVISIBLE);
                        if (finalI1 - 1 >= 0) {
                            ll_hotwords.getChildAt(finalI1 - 1).findViewById(R.id.iv_line).setVisibility(View.INVISIBLE);
                        }

                    } else {
                        if (selectdHotWord == finalI1) {
                            ((TextView) view.findViewById(R.id.tv_hotword)).setTextColor(getResources().getColor(R.color.word_selected));

                                } else {
                            ((TextView) view.findViewById(R.id.tv_hotword)).setTextColor(getResources().getColor(R.color.word_nomal));
                        }
                        JasmineUtil.scaleIn(view.findViewById(R.id.tv_hotword));
                        view.findViewById(R.id.iv_line).setVisibility(View.VISIBLE);
                        if (finalI1 - 1 >= 0) {
                            ll_hotwords.getChildAt(finalI1 - 1).findViewById(R.id.iv_line).setVisibility(View.VISIBLE);

                        }
                    }
                }
            });
            final int finalI = i;
            ll_hotwords.getChildAt(i).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(final View view) {
                    long time = System.currentTimeMillis();
                    if (lastClicktime == 0) {
                        lastClicktime = time;
                    } else {
                        if (time - lastClicktime < 500) {
                            return;
                        }
                        lastClicktime = time;
                    }
                    clickposition = SEARCH_WORDS;
                    iv_left_arrow.setVisibility(View.INVISIBLE);
                    iv_right_arrow.setVisibility(View.INVISIBLE);
                    first = true;
                    count = 0;
//                            for (int j = 0; j < top_tabs.getChildCount(); j++) {
//                                top_tabs.getChildAt(j).setVisibility(View.GONE);
//                            }
                    index = finalI;
                    scrowview.scrollTo(0, 0);
                    rl_search_subject.setVisibility(View.VISIBLE);
                    if (selectdHotWord != -1) {
                        ((TextView) ll_hotwords.getChildAt(selectdHotWord).findViewById(R.id.tv_hotword)).setTextColor(getResources().getColor(R.color.word_nomal));
                    }
                    selectdHotWord = finalI;
                    search_guide.setVisibility(View.GONE);
                    if (!isHide) {
                        JasmineUtil.hideKeyboard(WordSearchActivity.this, ((View) search_guide.getParent()));
                        isHide = true;

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                search_keyboard.setVisibility(View.INVISIBLE);
                                rl_recognize_hotword.setBackgroundColor(getResources().getColor(R.color.hotword_bg_dark));
                                iv_toggle.setVisibility(View.VISIBLE);
                            }
                        }, 500);

                    }
                    rl_search_subject.setVisibility(View.INVISIBLE);
                    loading.setVisibility(View.VISIBLE);
                    if (handler.hasMessages(1)) {
                        handler.removeMessages(1);
                    }
                    handler.sendEmptyMessageDelayed(1, 15000);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SystemClock.sleep(500);
                            String title = ((TextView) view.findViewById(R.id.tv_hotword)).getText().toString().trim();
                            keyWord_now = title.contains("...")?title.substring(0,8):title;
//                            postHttp(searchResults.get(finalI), null, page);
                            postHttp(title, null, page);
                        }
                    }).start();
                    view.requestFocus();
                    ((TextView) view.findViewById(R.id.tv_hotword)).setTextColor(getResources().getColor(R.color.word_focus));
                }
            });
        }
        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = getAssets().openFd("Windows Ding.wav");
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
                    fileDescriptor.getStartOffset(),
                    fileDescriptor.getLength());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initView() {
        lay_focus = findViewById(R.id.lay_focus);
        lay_focus.setOnHoverListener(this);
        lay_focus.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                for (int i = 0; i < count - 5; i++) {
//                    top_tabs.getChildAt(i).setVisibility(View.VISIBLE);
//                }
                if (keyCode != 4) {
                    if (t9_keyboard.getVisibility() == View.VISIBLE && !isHide) {
                        t9_keyboard.requestFocus();
                    } else if (keyboard.getVisibility() == View.VISIBLE && !isHide) {
                        keyboard.requestFocus();
                    } else if (isHide) {
                        poster_gridview.requestFocus();
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });
        ll_hotwords = (LinearLayout) findViewById(R.id.ll_hotwords);
        rl_search_subject = findViewById(R.id.rl_search_subject);
        rl_search_subject.setOnHoverListener(this);
        rl_recognize_hotword = findViewById(R.id.rl_recognize_hotword);
        t9_keyboard = (ZGridView2) findViewById(R.id.t9_keyboard);
        search_keyboard = findViewById(R.id.search_keyboard);
        loading = findViewById(R.id.loading);
        junp_view = findViewById(R.id.jump_view);
        junp_view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    tv_search_all.requestFocus();
                }
            }
        });

        et_input = (EditText) findViewById(R.id.et_input);
        keyboard = (ZGridView1) findViewById(R.id.keyboard);
        iv_toggle = (ImageView) findViewById(R.id.iv_toggle);
        iv_toggle.setOnClickListener(this);
        iv_toggle.setOnHoverListener(this);
        tv_search_all = (TextView) findViewById(R.id.tv_search_all);
        tv_search_all.setOnHoverListener(this);
        tv_search_all.setOnClickListener(this);
        tv_search_all.setNextFocusRightId(R.id.poster_gridview);
        tv_search_all.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    JasmineUtil.scaleOut1(v);
                    v.invalidate();
                } else {
                    JasmineUtil.scaleIn1(v);
                    v.invalidate();
                }
            }
        });
        today_hotword = (TextView) findViewById(R.id.today_hotword);
        t9_keyboard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    showPopupWindow(t9KeyboardAdapter, view, position);
                } else {
                    TextView tv_num = (TextView) view.findViewById(R.id.tv_num);
                    if (et_input.getText().toString().length() <= 26) {
                        et_input.append(tv_num.getText().toString());
                    } else {
                        mediaPlayer.start();
                    }
                }
            }
        });
        //����
        tv_back = findViewById(R.id.tv_back);
        //t9
        tv_t9 = findViewById(R.id.tv_T9);
        //0��
        tv_key_0 = findViewById(R.id.tv_key_0);
        t9_key_0 = findViewById(R.id.t9_key_0);
        t9_key_0.setOnClickListener(this);
        t9_key_0.setOnHoverListener(this);
        keyboard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view.findViewById(R.id.btn_key);
                String keyValue = textView.getText().toString();
                if (et_input.getText().toString().length() <= 26) {
                    et_input.append(keyValue);
                } else {
                    mediaPlayer.start();
                }
            }
        });
        tv_back.setOnClickListener(this);
        tv_key_0.setOnClickListener(this);
        tv_t9.setOnClickListener(this);
        tv_back.setOnFocusChangeListener(this);
        tv_back.setOnHoverListener(this);
        tv_key_0.setOnFocusChangeListener(this);
        tv_key_0.setOnHoverListener(this);
        tv_t9.setOnFocusChangeListener(this);
        tv_t9.setOnHoverListener(this);
        et_input.addTextChangedListener(new TextWatcher() {
                                            @Override
                                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                            }

                                            @Override
                                            public void onTextChanged(final CharSequence charSequence, int i, int i1, int i2) {
                                                if (charSequence != null && charSequence.length() != 0) {
                                                    index = -1;
                                                    tv_search_all.setVisibility(View.VISIBLE);
                                                    today_hotword.setVisibility(View.INVISIBLE);
                                                    tv_search_all.setText((charSequence.toString().length() > 7 ? charSequence.toString().substring(0, 6) + "..." : charSequence.toString()) + "  " + getResources().getString(R.string.search_all));
                                                    search(charSequence.toString(), SEARCH_RESULT);

                                                } else {
                                                    index = 0;
                                                    ll_hotwords.setVisibility(View.VISIBLE);
                                                    tv_search_all.setVisibility(View.INVISIBLE);
                                                    today_hotword.setVisibility(View.VISIBLE);
                                                    search(null,HOTWORDS);
                                                }

                                            }

                                            @Override
                                            public void afterTextChanged(Editable editable) {

                                            }
                                        }

        );
        search_guide = findViewById(R.id.search_guide);
        /**
         * �������ҳ
         */
        iv_left_arrow = (ImageView) findViewById(R.id.iv_left_arrow);
        iv_right_arrow = (ImageView) findViewById(R.id.iv_right_arrow);
        iv_top_arrow = (ImageView) findViewById(R.id.iv_top_arrow);
        iv_down_arrow = (ImageView) findViewById(R.id.iv_down_arrow);
        iv_left_arrow.setOnHoverListener(this);
        iv_down_arrow.setOnHoverListener(this);
        iv_top_arrow.setOnHoverListener(this);
        iv_right_arrow.setOnHoverListener(this);
        iv_left_arrow.setOnClickListener(this);
        iv_down_arrow.setOnClickListener(this);
        iv_top_arrow.setOnClickListener(this);
        iv_right_arrow.setOnClickListener(this);
        iv_left_arrow.setOnFocusChangeListener(this);
        iv_down_arrow.setOnFocusChangeListener(this);
        iv_top_arrow.setOnFocusChangeListener(this);
        iv_right_arrow.setOnFocusChangeListener(this);
        tv_recommend = (TextView) findViewById(R.id.tv_recommend);
        view_line = findViewById(R.id.view_line);
        scrowview = (HorizontalScrollView) findViewById(R.id.scrowview);
        scrowview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        top_tabs = (LinearLayout)

                findViewById(R.id.top_tabs);

        poster_gridview = (ZGridView)

                findViewById(R.id.poster_gridview);
        poster_gridview.setScale(1.1f);
        poster_gridview.setUpView(iv_top_arrow);
        poster_gridview.setDownView(iv_down_arrow);
        poster_gridview.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (!isHide) {
                        JasmineUtil.hideKeyboard(WordSearchActivity.this, ((View) search_guide.getParent()));
                        isHide = true;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                search_keyboard.setVisibility(View.INVISIBLE);
                                rl_recognize_hotword.setBackgroundColor(getResources().getColor(R.color.hotword_bg_dark));
                                iv_toggle.setVisibility(View.VISIBLE);
                            }
                        }, 500);
                    }
                }
            }
        });

        for (int i = 0; i < top_tabs.getChildCount(); i++) {
            final int finalI = i;
            top_tabs.getChildAt(i).setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        System.out.println("keykeykeykey");
//                        for (int i = 0; i < count - 5; i++) {
//                            top_tabs.getChildAt(i).setVisibility(View.VISIBLE);
//                        }
                    }
                    return false;
                }
            });
            top_tabs.getChildAt(i).
                    setOnClickListener(new View.OnClickListener() {

                                           @Override
                                           public void onClick(View v) {
                                               first = true;
                                               clickposition = TOPTABS;
                                               lay_focus.requestFocus();
                                               if (selectedTab != -1) {
                                                   ((TextView) ((ViewGroup) top_tabs.getChildAt(selectedTab)).getChildAt(0)).setTextColor(getResources().getColor(R.color.word_nomal));
                                               }
                                               selectedTab = finalI;
                                               poster_gridview.setNextFocusUpId(top_tabs.getChildAt(finalI).getId());
                                               if (iv_left_arrow.getVisibility() == View.VISIBLE) {
                                                   left = true;
                                               } else {
                                                   left = false;
                                               }
                                               if (iv_right_arrow.getVisibility() == View.VISIBLE) {
                                                   right = true;
                                               } else {
                                                   right = false;
                                               }
                                               rl_search_subject.setVisibility(View.INVISIBLE);
                                               loading.setVisibility(View.VISIBLE);
                                               if (handler.hasMessages(1)) {
                                                   handler.removeMessages(1);
                                               }
                                               handler.sendEmptyMessageDelayed(1, 15000);
                                               new Thread(new Runnable() {

                                                   @Override
                                                   public void run() {
                                                       type_now = tags[selectedTab];
                                                       postHttp(keyWord_now, tags[selectedTab], page);
                                                   }
                                               }).start();

                                           }
                                       }

                    );
            final int finalI1 = i;
            top_tabs.getChildAt(i).
                    setOnFocusChangeListener(new View.OnFocusChangeListener() {

                                                 @Override
                                                 public void onFocusChange(View v, boolean hasFocus) {
                                                     if (hasFocus) {
                                                         if (!isHide) {
                                                             JasmineUtil.hideKeyboard(WordSearchActivity.this, ((View) search_guide.getParent()));
                                                             isHide = true;
                                                             handler.postDelayed(new Runnable() {
                                                                 @Override
                                                                 public void run() {
                                                                     search_keyboard.setVisibility(View.INVISIBLE);
                                                                     rl_recognize_hotword.setBackgroundColor(getResources().getColor(R.color.hotword_bg_dark));
                                                                     iv_toggle.setVisibility(View.VISIBLE);
                                                                 }
                                                             }, 500);
                                                         }
                                                         System.out.println("================" + scrowview.getScrollX());
                                                         int scrollX = scrowview.getScrollX();
                                                         if (scrollX > getResources().getDimension(R.dimen.show_left_arrow)) {
                                                             iv_left_arrow.setVisibility(View.VISIBLE);
                                                         } else {
                                                             iv_left_arrow.setVisibility(View.INVISIBLE);
                                                         }
                                                         if (count > 5) {
                                                             dimension = 0;
                                                             switch (count) {
                                                                 case 6:
                                                                     dimension = 270;
                                                                     break;
                                                                 case 7:
                                                                     dimension = 540;
                                                                     break;
                                                                 case 8:
                                                                     dimension = 810;
                                                                     break;
                                                                 case 9:
                                                                     dimension = 1080;
                                                                     break;
                                                                 case 10:
                                                                     dimension = 1350;
                                                                     break;
                                                                 case 11:
                                                                     dimension = 1620;
                                                                     break;
                                                                 case 12:
                                                                     dimension = 1890;
                                                                     break;

                                                             }
                                                             if (scrollX < dimension) {
                                                                 iv_right_arrow.setVisibility(View.VISIBLE);
                                                             } else {
                                                                 iv_right_arrow.setVisibility(View.INVISIBLE);
                                                             }
                                                         } else {
                                                             iv_right_arrow.setVisibility(View.INVISIBLE);
                                                         }

                                                         JasmineUtil.scaleOut(((ViewGroup) top_tabs.getChildAt(finalI)).getChildAt(0));
                                                         ((TextView) ((ViewGroup) top_tabs.getChildAt(finalI)).getChildAt(0)).setTextColor(getResources().getColor(R.color.word_focus));
                                                         ((ViewGroup) top_tabs.getChildAt(finalI)).getChildAt(1).setVisibility(View.VISIBLE);

                                                     } else {
                                                         if (selectedTab == finalI) {
                                                             ((TextView) ((ViewGroup) top_tabs.getChildAt(finalI)).getChildAt(0)).setTextColor(getResources().getColor(R.color.word_selected));
                                                         } else {
                                                             ((TextView) ((ViewGroup) top_tabs.getChildAt(finalI)).getChildAt(0)).setTextColor(getResources().getColor(R.color.word_nomal));
                                                         }
                                                         JasmineUtil.scaleIn(((ViewGroup) top_tabs.getChildAt(finalI)).getChildAt(0));
                                                         ((ViewGroup) top_tabs.getChildAt(finalI)).getChildAt(1).setVisibility(View.INVISIBLE);
                                                     }
                                                 }
                                             }

                    );
            top_tabs.getChildAt(i).setOnHoverListener(this);

        }
        poster_gridview.setOnItemClickListener(new AdapterView.OnItemClickListener()

                                               {
                                                   @Override
                                                   public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                       if (poster_gridview.getAdapter() instanceof PosterAdapter) {
                                                           gotoSpecialPage(WordSearchActivity.this, ((VodObjectEntity) posterAdapter.getItem(position)).getPk(),((VodObjectEntity) posterAdapter.getItem(position)).getTitle(),((VodObjectEntity) posterAdapter.getItem(position)).getContent_model(), ((VodObjectEntity) posterAdapter.getItem(position)).getUrl(), "search",(((VodObjectEntity) posterAdapter.getItem(position)).getExpense() != null));
                                                       } else {
                                                           gotoSpecialPage(WordSearchActivity.this,0,null, ((Recommend.ObjectsEntity) recommendAdapter.getItem(position)).content_model, ((Recommend.ObjectsEntity) recommendAdapter.getItem(position)).url, "search",(((Recommend.ObjectsEntity) recommendAdapter.getItem(position)).expense != null));

                                                       }
                                                   }
                                               }

        );

    }

    /**
     * 跳转到详情页
     */
    public static void gotoSpecialPage(Context context, long pk, String title, String contentMode, String url, String from, boolean isexpensive) {
        Intent intent = new Intent();
        if (("variety".equals(contentMode) || "entertainment".equals(contentMode)) && !isexpensive) {
            intent.setAction("tv.ismar.daisy.EntertainmentItem");
            intent.putExtra("title", context.getResources().getString(R.string.entertainment));
        } else if ("movie".equals(contentMode)) {
            intent.setAction("tv.ismar.daisy.PFileItem");
            intent.putExtra("title", context.getResources().getString(R.string.film));
        } else if("person".equals(contentMode)) {
            intent.setAction("cn.ismartv.voice.filmstar");
            intent.putExtra("pk",pk);
            intent.putExtra("title",title);
        }else{
            intent.setAction("tv.ismar.daisy.Item");
        }
        intent.putExtra("url", url);
        intent.putExtra("fromPage", from);
        context.startActivity(intent);
    }

    /**
     * �������������
     */

    private void search(String args,final int flag) {
        //访问网络，获取数据
        if(flag==HOTWORDS) {
            mSkyService.apiSearchHotwords()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new BaseObserver<ArrayList<HotWords>>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onNext(ArrayList<HotWords> hotWords) {
                            Log.e("daisy1", hotWords.size()+"");
                            /**
                             * 填充hotwords列表
                             */
                            if (hotWords.size() == 0) {
                                index = -1;
                            }
                            if (hotWords.size() > 0) {
                                for (int i = 0; i < hotWords.size() ; i++) {
                                    ((TextView) ll_hotwords.getChildAt(i).findViewById(R.id.tv_hotword)).setText(hotWords.get(i).title);
                                }
                                if(hotWords.size()<10){
                                    for (int i =hotWords.size(); i <10 ; i++) {
                                        ll_hotwords.getChildAt(i).setVisibility(View.INVISIBLE);
                                    }
                                }
                                ll_hotwords.setVisibility(View.VISIBLE);
                            } else {
                                ll_hotwords.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
        }else if(flag==RECOMMEND){
            mSkyService.apiSearchRecommend()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new BaseObserver<Recommend>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onNext(Recommend recommend) {
                            Log.e("daisy2", recommend.count+"");
                            recommendAdapter = new RecommendAdapter(WordSearchActivity.this, recommend.objects);
                            poster_gridview.setAdapter(recommendAdapter);
                            noResult = true;
                            recommendAdapter.notifyDataSetChanged();
                            handler.removeMessages(1);
                            loading.setVisibility(View.INVISIBLE);
                            rl_search_subject.setVisibility(View.VISIBLE);
                            if (clickposition == SEARCH_ALL) {
                                tv_search_all.requestFocus();
                            } else if (clickposition == SEARCH_WORDS) {
                                ll_hotwords.getChildAt(selectdHotWord).requestFocus();
                            } else {
                                top_tabs.getChildAt(selectedTab).requestFocus();
                            }
                        }
                    });
        }else if(flag==SEARCH_RESULT){
            mSkyService.apiSearchSuggest(args)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new BaseObserver<List<String>>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onNext(List<String> resultWord) {
                            Log.e("daisy3", resultWord.size()+"");
                            if ( resultWord.size() > 0) {
                                /**
                                 * 刷新数据为推荐热词
                                 *
                                 * */
                                ll_hotwords.setVisibility(View.VISIBLE);
                                junp_view.setVisibility(View.GONE);
                                for (int i = 0; i < ( resultWord.size() > 10 ? 10 :  resultWord.size()); i++) {
                                    ll_hotwords.getChildAt(i).setVisibility(View.VISIBLE);
                                    ((TextView) ll_hotwords.getChildAt(i).findViewById(R.id.tv_hotword)).setText( resultWord.get(i));
                                }
                                if( resultWord.size()<10){
                                    for (int i =  resultWord.size(); i <10 ; i++) {
                                        ll_hotwords.getChildAt(i).setVisibility(View.INVISIBLE);
                                    }
                                }
                                if (selectdHotWord != -1) {
                                    ((TextView) ll_hotwords.getChildAt(selectdHotWord).findViewById(R.id.tv_hotword)).setTextColor(getResources().getColor(R.color.word_nomal));
                                }
                            } else {
                                ll_hotwords.setVisibility(View.INVISIBLE);
                                junp_view.setVisibility(View.VISIBLE);

                            }
                        }
                    });
        }

    }


    private void processData(String result, int flag) {

        if (VODSEARCH == flag) {

            final VodFacetEntity vodFacetEntity = new Gson().fromJson(result, VodFacetEntity.class);
            String test = new Gson().toJson(vodFacetEntity);
            scrowview.setVisibility(View.VISIBLE);
            tv_recommend.setVisibility(View.GONE);
            view_line.setVisibility(View.GONE);
            if(vodFacetEntity!=null) {
                posterAdapter = new PosterAdapter(this, vodFacetEntity.facet.get(0).objects);
                poster_gridview.setAdapter(posterAdapter);
                noResult = false;
                posterAdapter.notifyDataSetChanged();
                handler.removeMessages(1);
                loading.setVisibility(View.INVISIBLE);
                rl_search_subject.setVisibility(View.VISIBLE);
                if (clickposition == SEARCH_ALL) {
                    tv_search_all.requestFocus();
                } else if (clickposition == SEARCH_WORDS) {
                    ll_hotwords.getChildAt(selectdHotWord).requestFocus();
                } else {
                    top_tabs.getChildAt(selectedTab).requestFocus();
                }
            }

        }else if (VODSEARCH_CLASS == flag) {
            VodFacetEntity vodFacetEntity = GsonTools.changeGsonToBean(result, VodFacetEntity.class);
            if (vodFacetEntity != null) {
                if (vodFacetEntity.facet.size() > 0) {
                    //显示有分类的toptab
                    for (int i = 0; i <top_tabs.getChildCount(); i++) {
                        top_tabs.getChildAt(i).setVisibility(View.GONE);
                    }
                    for (int i = 0; i < vodFacetEntity.facet.size(); i++) {
                        switch (vodFacetEntity.facet.get(i).content_type) {
                            case "entertainment":
                                top_tabs.findViewById(R.id.tab6).setVisibility(View.VISIBLE);
                                count++;
                                break;
                            case "variety":
                                top_tabs.findViewById(R.id.tab3).setVisibility(View.VISIBLE);
                                count++;
                                break;
                            case "sport":
                                top_tabs.findViewById(R.id.tab7).setVisibility(View.VISIBLE);
                                count++;
                                break;
                            case "music":
                                top_tabs.findViewById(R.id.tab5).setVisibility(View.VISIBLE);
                                count++;
                                break;
                            case "movie":
                                top_tabs.findViewById(R.id.tab1).setVisibility(View.VISIBLE);
                                count++;
                                break;
                            case "comic":
                                top_tabs.findViewById(R.id.tab4).setVisibility(View.VISIBLE);
                                count++;
                                break;
                            case "teleplay":
                                top_tabs.findViewById(R.id.tab2).setVisibility(View.VISIBLE);
                                count++;
                                break;
                            case "game":
                                top_tabs.findViewById(R.id.tab8).setVisibility(View.VISIBLE);
                                count++;
                                break;
                            case "documentary":
                                top_tabs.findViewById(R.id.tab9).setVisibility(View.VISIBLE);
                                count++;
                                break;
                            case "trailer":
                                top_tabs.findViewById(R.id.tab10).setVisibility(View.VISIBLE);
                                count++;
                                break;
                            case "education":
                                top_tabs.findViewById(R.id.tab11).setVisibility(View.VISIBLE);
                                count++;
                                break;
                            case "person":
                                top_tabs.findViewById(R.id.tab0).setVisibility(View.VISIBLE);
                                count++;
                                break;
                            default:
                                break;
                        }
                    }
                    System.out.println("count===========");
                    if (count > 5) {
                        right = true;
                        iv_right_arrow.setVisibility(View.VISIBLE);
                    } else {
                        right = false;
                        iv_right_arrow.setVisibility(View.INVISIBLE);
                    }
                    for (int j = 0; j < top_tabs.getChildCount(); j++) {
                        if (top_tabs.getChildAt(j).getVisibility() == View.VISIBLE) {
                            final int finalJ = j;
                            firstTab = j;
                            if (selectedTab != -1) {
                                ((TextView) ((ViewGroup) top_tabs.getChildAt(selectedTab)).getChildAt(0)).setTextColor(getResources().getColor(R.color.word_nomal));
                            }
                            selectedTab = finalJ;
                            ((TextView) ((ViewGroup) top_tabs.getChildAt(finalJ)).getChildAt(0)).setTextColor(getResources().getColor(R.color.word_selected));
                            if (handler.hasMessages(1)) {
                                handler.removeMessages(1);
                            }
                            handler.sendEmptyMessageDelayed(1, 15000);

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    postHttp(keyWord_now, tags[finalJ], 1);
                                    poster_gridview.setNextFocusUpId(top_tabs.getChildAt(finalJ).getId());
                                }
                            }).start();
                            break;
                        }
                    }
                    for (int i = top_tabs.getChildCount()-1; i >= 0; i--) {
                        if (top_tabs.getChildAt(i).getVisibility() == View.VISIBLE) {
                            lastTab = i;
                            break;
                        }
                    }
                }
            }
            if (count == 0) {
                //8个推荐位视频
                scrowview.setVisibility(View.GONE);
                iv_right_arrow.setVisibility(View.GONE);
                iv_left_arrow.setVisibility(View.GONE);
                tv_recommend.setVisibility(View.VISIBLE);
                view_line.setVisibility(View.VISIBLE);
                poster_gridview.setNextFocusUpId(R.id.poster_gridview);
                if (handler.hasMessages(1)) {
                    handler.removeMessages(1);
                }
                handler.sendEmptyMessageDelayed(1, 15000);
                search(null,RECOMMEND);
            }
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if(scrowview.getScrollX()<1890) {
//            for (int i = 0; i < count - 5; i++) {
//                top_tabs.getChildAt(i).setVisibility(View.VISIBLE);
//            }
//        }
        if (loading.getVisibility() == View.VISIBLE) {
            if (keyCode == 4) {
                loading.setVisibility(View.INVISIBLE);
                rl_search_subject.setVisibility(View.VISIBLE);
                if(clickposition==TOPTABS) {
                    top_tabs.getChildAt(selectedTab).requestFocus();
                }else if(clickposition==SEARCH_WORDS){
                    ll_hotwords.getChildAt(selectdHotWord).requestFocus();
                }else{
                    tv_search_all.requestFocus();
                }
            }
            return true;
        }
        iv_left_arrow.setHovered(false);
        iv_right_arrow.setHovered(false);
        iv_top_arrow.setHovered(false);
        iv_down_arrow.setHovered(false);
        switch (keyCode) {
            case 21:
                if (poster_gridview.isFocused()) {
                    if (index != -1) {
                        ll_hotwords.getChildAt(index).requestFocus();
                    } else {
                        tv_search_all.requestFocus();
                    }
                    return true;
                }
                if (top_tabs.getChildAt(firstTab).isFocused() || iv_top_arrow.isFocused() || iv_down_arrow.isFocused() || iv_left_arrow.isFocused()) {
                    return true;
                }
                if (tv_search_all.isFocused()) {
                    if (isHide) {
                        iv_toggle.requestFocus();
                    } else {
                        if (keyboard.getVisibility() == View.VISIBLE) {
                            keyboard.setSelection(4);
                        } else {
                            t9_keyboard.setSelection(2);
                        }
                    }

                }
                break;
            case 22:
                if (t9_keyboard.isFocused() || keyboard.isFocused() || tv_t9.isFocused() || t9_key_0.isFocused() || iv_toggle.isFocused()) {
                    if (index != -1 && ll_hotwords.getVisibility() == View.VISIBLE) {
                        ll_hotwords.getChildAt(index).requestFocus();
                    } else {
                        if (tv_search_all.getVisibility() == View.VISIBLE) {
                            tv_search_all.requestFocus();
                        } else {
                            return true;
                        }
                    }
                    return true;
                }
                if (tv_search_all.isFocused() || ll_hotwords.getFocusedChild() != null) {
                    if (search_guide.getVisibility() == View.VISIBLE) {
                        return true;
                    }
                }
                if (top_tabs.getChildAt(lastTab).isFocused() || iv_top_arrow.isFocused() || iv_down_arrow.isFocused() || iv_right_arrow.isFocused()) {
                    return true;
                }
//                if(!tv_back.isFocused()&&!tv_key_0.isFocused()){
//                   if(rl_search_subject.getVisibility()==View.INVISIBLE) {
//                       return true;
//                   }
//                }
                break;
            case 19:
                if (t9_key_0.isFocused()) {
                    t9_keyboard.setSelection(7);
                }
                if (ll_hotwords.getChildAt(0).isFocused() && tv_search_all.getVisibility() == View.INVISIBLE || iv_left_arrow.isFocused() || iv_right_arrow.isFocused()) {
                    return true;
                }
                if (keyboard.getVisibility() == View.VISIBLE) {
                    if (tv_key_0.isFocused()) {
                        keyboard.setSelection(32);
                    } else if (tv_back.isFocused()) {
                        keyboard.setSelection(31);
                    } else if (tv_t9.isFocused()) {
                        keyboard.setSelection(33);
                    }

                }

                break;
            case 20:
                if (top_tabs.getFocusedChild() != null) {
                    poster_gridview.requestFocus();
                }
                if (iv_left_arrow.isFocused() || iv_right_arrow.isFocused()) {
                    return true;
                }
                break;
        }
        tv_search_all.setHovered(false);
        return super.onKeyDown(keyCode, event);
    }

    private void postHttp(String keywords, final String type, int page) {
        try {
            VodSearchRequestEntity requestEntity = new VodSearchRequestEntity();

            requestEntity.setKeyword(keywords);
            if (type != null) {
                requestEntity.setContent_type(type);
                requestEntity.setPage_no(page);
                requestEntity.setPage_count(300);
            } else {
                requestEntity.setContent_type("");
            }


            URL url = new URL(domain + JasmineUtil.VODSEARCH_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            conn.setRequestProperty("Accept", "application/json");
            conn.connect();
            OutputStreamWriter out = new OutputStreamWriter(
                    conn.getOutputStream(), "UTF-8");
            out.write(new Gson().toJson(requestEntity));
            out.flush();
            out.close();
            int status = conn.getResponseCode();
            if (status != 200) {
                lay_focus.requestFocus();
                errorDialog.show();
                return;
            }
            if (status == 200) {
                reader = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                String lines;
                sb = new StringBuffer("");
                while ((lines = reader.readLine()) != null) {
                    lines = new String(lines.getBytes(), "utf-8");
                    sb.append(lines);
                }
                if (sb.toString().startsWith("<html>")) {
                    lay_focus.requestFocus();
                    errorDialog.show();
                    return;
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (type == null || type == "") {
                                processData(sb.toString(), VODSEARCH_CLASS);
                                sb = new StringBuffer("");
                            } else {
                                processData(sb.toString(), VODSEARCH);
                                sb = new StringBuffer("");
                            }

                        }
                    });
                }
                System.out.println("sb=============" + sb);
                reader.close();
                // 断开连接
                conn.disconnect();
            } else {
                lay_focus.requestFocus();
                errorDialog.show();
                reader.close();
                conn.disconnect();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * ���˺�T9��ť�ĵ���¼��ļ���
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        editable = et_input.getText();
        int i1 = view.getId();
        if (i1 == R.id.tv_back) {
            int start = et_input.getSelectionStart();
            if (editable != null && editable.length() > 0)
                editable.delete(start - 1, start);
            if (editable.length() == 0) {
                //��������գ���ʾ�����ȴʽ���
                tv_search_all.setVisibility(View.INVISIBLE);
                today_hotword.setVisibility(View.VISIBLE);
                /**
                 * 刷新列表
                 */
                if (hotWordsList != null) {
                    ll_hotwords.setVisibility(View.VISIBLE);
                    for (int i = 0; i < hotWordsList.size(); i++) {
                        if (hotWordsList.size() > 0) {
                            ((TextView) ll_hotwords.getChildAt(i).findViewById(R.id.tv_hotword)).setText(hotWordsList.get(i));
                        }
                    }
                } else {
                    ll_hotwords.setVisibility(View.INVISIBLE);
                }
            }

        } else if (i1 == R.id.tv_key_0) {
            if (et_input.getText().toString().length() <= 26) {
                et_input.append("0");
            } else {
                mediaPlayer.start();
            }

        } else if (i1 == R.id.t9_key_0) {
            if (et_input.getText().toString().length() <= 26) {
                et_input.append("0");
            } else {
                mediaPlayer.start();
            }

        } else if (i1 == R.id.tv_T9) {
            if (keyboard.getVisibility() == View.VISIBLE) {
                keyboard.setVisibility(View.GONE);
                tv_key_0.setVisibility(View.INVISIBLE);
                tv_key_0.setFocusable(false);
                tv_t9.findViewById(R.id.tv_T9_keyboard).setVisibility(View.GONE);
                tv_t9.findViewById(R.id.tv_full_keyboard).setVisibility(View.VISIBLE);
                t9_keyboard.setVisibility(View.VISIBLE);
                t9_key_0.setVisibility(View.VISIBLE);
            } else {
                keyboard.setVisibility(View.VISIBLE);
                tv_key_0.setVisibility(View.VISIBLE);
                tv_key_0.setFocusable(true);
                tv_t9.findViewById(R.id.tv_T9_keyboard).setVisibility(View.VISIBLE);
                tv_t9.findViewById(R.id.tv_full_keyboard).setVisibility(View.GONE);
                t9_keyboard.setVisibility(View.GONE);
                t9_key_0.setVisibility(View.GONE);
            }

        } else if (i1 == R.id.tv_search_all) {
            first = true;
            count = 0;
            clickposition = SEARCH_ALL;
            loading.setVisibility(View.VISIBLE);
            rl_search_subject.setVisibility(View.INVISIBLE);
            clickView = tv_search_all;
            index = -1;
            scrowview.scrollTo(0, 0);
            iv_left_arrow.setVisibility(View.INVISIBLE);
            if (!isHide) {
                JasmineUtil.hideKeyboard(WordSearchActivity.this, ((View) search_guide.getParent()));
                isHide = true;
//                    rl_search_subject.setVisibility(View.VISIBLE);
                search_guide.setVisibility(View.GONE);
                if (handler.hasMessages(1)) {
                    handler.removeMessages(1);
                }
                handler.sendEmptyMessageDelayed(1, 15000);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        search_keyboard.setVisibility(View.INVISIBLE);
                        rl_recognize_hotword.setBackgroundColor(getResources().getColor(R.color.hotword_bg_dark));
                        iv_toggle.setVisibility(View.VISIBLE);
                    }
                }, 500);
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SystemClock.sleep(500);
                    keyWord_now = et_input.getText().toString().trim();
                    postHttp(keyWord_now, null, page);
                }
            }).start();

        } else if (i1 == R.id.iv_toggle) {
            iv_toggle.setVisibility(View.GONE);
            isHide = false;
            search_keyboard.setVisibility(View.VISIBLE);
            rl_recognize_hotword.setBackgroundColor(getResources().getColor(R.color.hotword_bg_light));
            JasmineUtil.showKeyboard(this, ((View) search_guide.getParent()));
            if (index != -1) {
                ll_hotwords.getChildAt(index).requestFocus();
            } else {
                tv_search_all.requestFocus();
            }

        } else if (i1 == R.id.iv_top_arrow) {
            poster_gridview.arrowScroll(View.FOCUS_UP);
            poster_gridview.arrowScroll(View.FOCUS_UP);

        } else if (i1 == R.id.iv_down_arrow) {
            poster_gridview.arrowScroll(View.FOCUS_DOWN);
            poster_gridview.arrowScroll(View.FOCUS_DOWN);

        } else if (i1 == R.id.iv_left_arrow) {
            scroll = scrowview.getScrollX();
            if (scroll - 1355 > 0) {
                scrowview.scrollTo(scroll - 1355, 0);
                iv_right_arrow.setVisibility(View.VISIBLE);
            } else {
                scrowview.scrollTo(0, 0);
                iv_left_arrow.setVisibility(View.INVISIBLE);
                iv_right_arrow.setVisibility(View.VISIBLE);
            }
//                for (int i = 0; i < count - 5; i++) {
//                    top_tabs.getChildAt(i).setVisibility(View.VISIBLE);
//                }


        } else if (i1 == R.id.iv_right_arrow) {
            scroll = scrowview.getScrollX();
            if (scroll + 1355 < dimension) {
                scrowview.scrollTo(scroll + 1355, 0);
                iv_left_arrow.setVisibility(View.VISIBLE);
            } else {
                scrowview.scrollTo(1897, 0);
                iv_right_arrow.setVisibility(View.INVISIBLE);
                iv_left_arrow.setVisibility(View.VISIBLE);
            }
//                for (int i = 0; i < count - 5; i++) {
//                    top_tabs.getChildAt(i).setVisibility(View.INVISIBLE);
//                }

        } else {
            if (et_input.getText().toString().length() <= 26) {
                et_input.append(((TextView) view).getText().toString());
            } else {
                mediaPlayer.start();
            }
            popupWindow.dismiss();

        }

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            if (v.getId() == R.id.iv_top_arrow || v.getId() == R.id.iv_down_arrow || v.getId() == R.id.iv_left_arrow || v.getId() == R.id.iv_right_arrow || v.getId() == R.id.tv_key_0) {
                JasmineUtil.scaleOut(v);
            } else {
                JasmineUtil.scaleOut1(v);
            }
        } else {
            if (v.getId() == R.id.iv_top_arrow || v.getId() == R.id.iv_down_arrow || v.getId() == R.id.iv_left_arrow || v.getId() == R.id.iv_right_arrow || v.getId() == R.id.iv_right_arrow || v.getId() == R.id.tv_key_0) {
                JasmineUtil.scaleIn(v);
            } else {
                JasmineUtil.scaleIn1(v);
            }
        }

    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        int what = event.getAction();
        switch (what) {
            case MotionEvent.ACTION_HOVER_ENTER:
                lay_focus.setFocusable(true);
                lay_focus.setFocusableInTouchMode(true);
                break;
            case MotionEvent.ACTION_HOVER_MOVE:
                if (loading.getVisibility() == View.INVISIBLE) {
                    if (v.getId() == R.id.rl_search_subject) {
                        if (!isHide) {
                            JasmineUtil.hideKeyboard(WordSearchActivity.this, ((View) search_guide.getParent()));
                            isHide = true;
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    search_keyboard.setVisibility(View.INVISIBLE);
                                    rl_recognize_hotword.setBackgroundColor(getResources().getColor(R.color.hotword_bg_dark));
                                    iv_toggle.setVisibility(View.VISIBLE);
                                }
                            }, 500);
                        }
                    } else {
                        scroll = scrowview.getScrollX();
                        v.requestFocus();
                        scrowview.scrollTo(scroll, 0);
                    }
                }else{

                }
                break;

            case MotionEvent.ACTION_HOVER_EXIT:
                break;
        }
        return false;
    }

    private void showPopupWindow(T9KeyboardAdapter adapterView, View view, int i) {
        View contentView = View.inflate(this, R.layout.t9_key_popup, null);
        TextView tv_center = (TextView) contentView.findViewById(R.id.tv_center);
        TextView tv_top = (TextView) contentView.findViewById(R.id.tv_top);
        TextView tv_bottom = (TextView) contentView.findViewById(R.id.tv_bottom);
        TextView tv_left = (TextView) contentView.findViewById(R.id.tv_left);
        TextView tv_right = (TextView) contentView.findViewById(R.id.tv_right);
        tv_center.setOnClickListener(this);
        tv_top.setOnClickListener(this);
        tv_bottom.setOnClickListener(this);
        tv_left.setOnClickListener(this);
        tv_right.setOnClickListener(this);
        tv_center.setOnHoverListener(this);
        tv_top.setOnHoverListener(this);
        tv_bottom.setOnHoverListener(this);
        tv_left.setOnHoverListener(this);
        tv_right.setOnHoverListener(this);
        NineTKey nineTKey = (NineTKey) adapterView.getItem(i);
        tv_center.setText(nineTKey.num);
        char[] letters = nineTKey.letter.toCharArray();
        tv_left.setText(letters[0] + "");
        tv_top.setText(letters[1] + "");
        tv_right.setText(letters[2] + "");
        if (letters.length == 4) {
            tv_bottom.setText(letters[3] + "");
        } else {
            tv_bottom.setText("");
            tv_bottom.setFocusable(false);
        }
        popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.t9_background));
        popupWindow.showAsDropDown(view, -51, -172);

        tv_center.requestFocus();

    }

    @Override
    protected void onDestroy() {
        if (keyboard.getVisibility() == View.VISIBLE) {
            SharedPreferencesUtils.saveBoolean(this, "T9", false);
        } else {
            SharedPreferencesUtils.saveBoolean(this, "T9", true);
        }
        hotWordsList = null;
        posterAdapter = null;
        top_tabs = null;
        ll_hotwords = null;
        popupWindow = null;
        errorDialog = null;
        super.onDestroy();
    }

}
