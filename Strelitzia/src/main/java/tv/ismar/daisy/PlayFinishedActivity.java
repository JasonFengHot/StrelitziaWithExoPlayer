package tv.ismar.daisy;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.AppConstant;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.DaisyUtils;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.Source;
import tv.ismar.app.db.FavoriteManager;
import tv.ismar.app.db.HistoryManager;
import tv.ismar.app.entity.Favorite;
import tv.ismar.app.entity.Item;
import tv.ismar.app.exception.NetworkException;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.player.InitPlayerTool;
import tv.ismar.app.ui.ZGridView;
import tv.ismar.app.util.BitmapDecoder;
import tv.ismar.app.util.Utils;
import tv.ismar.app.widget.AsyncImageView;

public class PlayFinishedActivity extends BaseActivity implements OnFocusChangeListener, OnItemClickListener, OnClickListener {

    private static final String TAG = "PlayFinishedActivity/LH";
    private ItemEntity mItemEntity;
    //	private Bitmap bitmap;
    LinearLayout linearLeft;
    LinearLayout linearRight;
    TextView tvVodName;
    AsyncImageView imageBackgroud;
    ImageView imageVodLabel;
    Button btnReplay;
    Button btnFavorites;
    ZGridView gridview;
    PlayFinishedAdapter playAdapter;
    private Item[] items;
    private static final int UPDATE = 1;
    private static final int UPDATE_BITMAP = 2;
    private static final int NETWORK_EXCEPTION = -1;
    final SimpleRestClient simpleRest = new SimpleRestClient();
    private FavoriteManager mFavoriteManager;
    private HistoryManager mHistorymanager;
    private InitPlayerTool tool;
    private String source;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_finished);

        initViews();
        mFavoriteManager = DaisyUtils.getFavoriteManager(this);
        mHistorymanager = DaisyUtils.getHistoryManager(this);
        try {
            Intent intent = getIntent();
            if (null != intent) {
//                DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(), this);
                String itemJson = intent.getStringExtra("itemJson");
                mItemEntity = new Gson().fromJson(itemJson, ItemEntity.class);
                source = intent.getStringExtra("source");
            }
        } catch (Exception e) {
            e.printStackTrace();
            finish();
            return;
        }
        // 实际这些已经封装好了
        new Thread(mRelatedTask).start();
        initLayout();
    }

    private Runnable mRelatedTask = new Runnable() {
        @Override
        public void run() {
            try {
                items = simpleRest.getRelatedItem("/api/tv/relate/" + mItemEntity.getItemPk() + "/");
                Log.i(TAG, "relate==" + mItemEntity.getItemPk());
            } catch (NetworkException e) {
                e.printStackTrace();
            }
            if (items == null || items.length == 0) {
                mHandle.sendEmptyMessage(NETWORK_EXCEPTION);
            } else {
                mHandle.sendEmptyMessage(UPDATE);
            }
        }
    };

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        AppConstant.purchase_referer = "video";
        AppConstant.purchase_page = "finished";
        if (isFavorite()) {
            btnFavorites.setPadding(getResources().getDimensionPixelSize(R.dimen.play_finished_btn_fav_pl), 0, 0, 0);
            btnFavorites.setText(getResources().getString(R.string.favorited));
        } else {
            btnFavorites.setPadding(getResources().getDimensionPixelSize(R.dimen.play_finished_btn_pl), 0, 0, 0);
            btnFavorites.setText(getResources().getString(R.string.favorite));
        }
    }

    private void initViews() {
//        final View background = findViewById(R.id.large_layout);
//        new BitmapDecoder().decode(this, R.drawable.main_bg, new BitmapDecoder.Callback() {
//            @Override
//            public void onSuccess(BitmapDrawable bitmapDrawable) {
//                background.setBackgroundDrawable(bitmapDrawable);
//            }
//        });

        linearLeft = (LinearLayout) findViewById(R.id.linear_left);
//        linearLeft.setOnHoverListener(mOnHoverListener);
        linearRight = (LinearLayout) findViewById(R.id.linear_right);
        tvVodName = (TextView) findViewById(R.id.tv_vodie_name);
        imageBackgroud = (AsyncImageView) findViewById(R.id.image_vodie_backgroud);
        imageVodLabel = (ImageView) findViewById(R.id.image_vod_label);
        btnReplay = (Button) findViewById(R.id.btn_replay);
        btnReplay.setOnClickListener(this);
        btnReplay.setOnFocusChangeListener(this);
        btnReplay.setOnHoverListener(mOnHoverListener);
        btnFavorites = (Button) findViewById(R.id.btn_favorites);
        btnFavorites.setOnClickListener(this);
        btnFavorites.setOnFocusChangeListener(this);
        btnFavorites.setOnHoverListener(mOnHoverListener);
        gridview = (ZGridView) findViewById(R.id.gridview_related);
        gridview.setOnFocusChangeListener(this);
        gridview.setOnItemClickListener(this);
//		gridview.setNumColumns(3);
//		int H = DaisyUtils.getVodApplication(this).getheightPixels(this);
//		if(H==720){
//			gridview.setVerticalSpacing(20);
//			gridview.setHorizontalSpacing(50);
//		}
//		else{
//			gridview.setVerticalSpacing(40);
//			gridview.setHorizontalSpacing(100);
//		}

    }

    private Handler mHandle = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE:
                    playAdapter = new PlayFinishedAdapter(PlayFinishedActivity.this, items, R.layout.playfinish_gridview_item);
                    gridview.setAdapter(playAdapter);
                    gridview.setFocusable(true);
                    break;
                case NETWORK_EXCEPTION:
                    break;
            }
        }
    };

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.gridview_related:
                try {
                    if (hasFocus) {
                    } else {
                        gridview.setSelection(-1);
                    }
                } catch (Exception e) {
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long postions) {
        Intent intent = new Intent();
        if (items[position].expense != null) {
            if ("movie".equals(items[position].content_model)) {
                intent.setAction("tv.ismar.daisy.PFileItem");
                intent.putExtra("title", "电影");
                intent.putExtra("url", items[position].item_url);
                intent.putExtra("fromPage", "related");
            } else {
                intent.setAction("tv.ismar.daisy.Item");
                intent.putExtra("url", items[position].item_url);
                intent.putExtra("fromPage", "related");
            }
            try {
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            DaisyUtils.gotoSpecialPage(PlayFinishedActivity.this,
                    items[position].content_model, items[position].item_url,
                    "related");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_replay:
                if (mItemEntity != null) {
//                    String url = SimpleRestClient.root_url + "/api/item/" + item.item_pk + "/";
//                    int sub_item_pk = -1;
//                    History history = null;
//                    if (SimpleRestClient.isLogin())
//                        history = mHistorymanager.getHistoryByUrl(url, "yes");
//                    else
//                        history = mHistorymanager.getHistoryByUrl(url, "no");
//                    if (history != null) {
//                        history.last_position = 0;
//                        if (SimpleRestClient.isLogin())
//                            mHistorymanager.addHistory(history, "yes");
//                        else
//                            mHistorymanager.addHistory(history, "no");
//                        if (history.sub_url != null) {
//                            url = history.sub_url;
//                            sub_item_pk = Utils.getItemPk(url);
//                        }
//                    }
                    PageIntent pageIntent = new PageIntent();
                    pageIntent.toPlayPage(PlayFinishedActivity.this, mItemEntity.getItemPk(), 0, Source.getSource(source));
                    finish();
                }
                break;
            case R.id.btn_favorites:
                String isnet = "";
                if (!Utils.isEmptyText(IsmartvActivator.getInstance().getAuthToken())) {
                    isnet = "yes";
                } else {
                    isnet = "no";
                }
                if (isFavorite()) {
                    String url = IsmartvActivator.getInstance().getApiDomain() + "/api/item/" + mItemEntity.getItemPk() + "/";
                    if (IsmartvActivator.getInstance().isLogin()) {
                        deleteFavoriteByNet();
                        mFavoriteManager.deleteFavoriteByUrl(url, "yes");
                    } else {
                        mFavoriteManager.deleteFavoriteByUrl(url, "no");
                    }

                    showToast(getResources().getString(R.string.vod_bookmark_remove_success));
                } else {
                    String url = IsmartvActivator.getInstance().getApiDomain() + "/api/item/" + mItemEntity.getItemPk() + "/";
                    Favorite favorite = new Favorite();
                    favorite.title = mItemEntity.getTitle();
                    favorite.adlet_url = mItemEntity.getAdletUrl();
                    favorite.content_model = mItemEntity.getContentModel();
                    favorite.url = url;
                    favorite.quality = mItemEntity.getQuality();
                    favorite.is_complex = mItemEntity.getIsComplex();
                    favorite.isnet = isnet;
                    if (isnet.equals("yes")) {
                        createFavoriteByNet();
                    }
                    ArrayList<Favorite> favorites = DaisyUtils.getFavoriteManager(getApplicationContext()).getAllFavorites("no");
                    if(favorites.size()>49){
                        mFavoriteManager.deleteFavoriteByUrl(favorites.get(favorites.size()-1).url, "no");
                    }
                    mFavoriteManager.addFavorite(favorite, isnet);
                    // mFavoriteManager.addFavorite(item.title, url, mItem.content_model);
                    showToast(getResources().getString(R.string.vod_bookmark_add_success));
                }
                if (isFavorite()) {
                    btnFavorites.setPadding(getResources().getDimensionPixelSize(R.dimen.play_finished_btn_fav_pl), 0, 0, 0);
                    btnFavorites.setText(getResources().getString(R.string.favorited));
                } else {
                    btnFavorites.setPadding(getResources().getDimensionPixelSize(R.dimen.play_finished_btn_pl), 0, 0, 0);
                    btnFavorites.setText(getResources().getString(R.string.favorite));
                }
                break;
            default:
                break;
        }

    }

    private void deleteFavoriteByNet() {
        simpleRest.doSendRequest("/api/bookmarks/remove/", "post", "access_token=" +
                IsmartvActivator.getInstance().getAuthToken() + "&device_token=" + SimpleRestClient.device_token + "&item=" + mItemEntity.getItemPk(), new SimpleRestClient.HttpPostRequestInterface() {

            @Override
            public void onSuccess(String info) {
                // TODO Auto-generated method stub
                if ("200".equals(info)) {

                }
            }

            @Override
            public void onPrepare() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onFailed(String error) {
                // TODO Auto-generated method stub

            }
        });
    }

    private void createFavoriteByNet() {
        simpleRest.doSendRequest("/api/bookmarks/create/", "post", "access_token=" + IsmartvActivator.getInstance().getAuthToken() + "&device_token=" + SimpleRestClient.device_token + "&item=" + mItemEntity.getItemPk(), new SimpleRestClient.HttpPostRequestInterface() {

            @Override
            public void onSuccess(String info) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPrepare() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onFailed(String error) {
                // TODO Auto-generated method stub

            }
        });
    }

    /*
     * get the favorite status of the item.
     */
    private boolean isFavorite() {
        if (mItemEntity != null) {
            String url = mItemEntity.getItem_url();
            if (url == null && mItemEntity.getItemPk() != 0) {
                url = IsmartvActivator.getInstance().getApiDomain() + "/api/item/" + mItemEntity.getItemPk() + "/";
            }
            Favorite favorite;
            if (IsmartvActivator.getInstance().isLogin()) {
                favorite = mFavoriteManager.getFavoriteByUrl(url, "yes");
            } else {
                favorite = mFavoriteManager.getFavoriteByUrl(url, "no");
            }
            if (favorite != null) {
                return true;
            }
        }
        return false;
    }

    private void initLayout() {
        tvVodName.setText(mItemEntity.getTitle());
        switch (mItemEntity.getQuality()) {
            case 3:
                imageVodLabel.setBackgroundResource(R.drawable.label_uhd);
                break;
            case 4:
                imageVodLabel.setBackgroundResource(R.drawable.label_hd);
                break;
            default:
                imageVodLabel.setVisibility(View.GONE);
                break;
        }
        imageBackgroud.setUrl(mItemEntity.getPosterUrl());
    }

    @Override
    protected void onDestroy() {
        if (tool != null)
            tool.removeAsycCallback();
        playAdapter = null;
        mFavoriteManager = null;
//        DaisyUtils.getVodApplication(this).removeActivtyFromPool(this.toString());
        super.onDestroy();
    }

    private void showToast(String text) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.simple_toast, (ViewGroup) findViewById(R.id.simple_toast_root));
        TextView toastText = (TextView) layout.findViewById(R.id.toast_text);
        toastText.setText(text);
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    private View.OnHoverListener mOnHoverListener = new View.OnHoverListener() {

        @Override
        public boolean onHover(View v, MotionEvent keycode) {
            switch (keycode.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                case MotionEvent.ACTION_HOVER_MOVE:
                    v.requestFocusFromTouch();
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    break;
                default:
                    break;
            }
            return false;
        }
    };
}
