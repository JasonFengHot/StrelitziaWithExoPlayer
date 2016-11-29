package tv.ismar.detailpage.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.PageIntentInterface;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.ui.ZGridView;
import tv.ismar.app.widget.LoadingDialog;
import tv.ismar.detailpage.R;

import static tv.ismar.app.core.PageIntentInterface.EXTRA_ITEM_JSON;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_SOURCE;
import static tv.ismar.app.core.PageIntentInterface.FromPage.unknown;
import static tv.ismar.app.core.PageIntentInterface.ProductCategory.item;

/**
 * Created by huibin on 11/29/16.
 */

public class EpisodeActivity extends BaseActivity implements AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {

    private static final String TAG = "DramaListActivity";
    public static String ORDER_CHECK_BASE_URL = "/api/play/check/";
    public final static int visableItems = 30;
    private final static int DISABLE_ORDER_ALL_DRANA = 0x10;
    private final static int ORDER_ALL_DRANA_SUCCESS = 0x11;

    private ItemEntity mItemEntity;
    private List<ItemEntity> mItemEntityList;

    private DaramAdapter mDramaAdapter;
    private ItemEntity mSubItem;
    private GridView mDramaView;
    private ImageView mImageBackground;
    private ImageView mDramaImageLabel;
    private TextView mTvDramaName;
    private TextView mTvDramaAll;
    private TextView mTvDramaType;
    private TextView one_drama_order_info;
    private Button orderAll_drama;
    private Button down_btn;
    private Button up_btn;
    private LoadingDialog loadDialog;
    private boolean paystatus = false;

    private HashMap<String, Object> mDataCollectionProperties = new HashMap<>();

    private String source;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode);
        final View vv = findViewById(R.id.large_layout);

        initViews();
        if (loadDialog != null && !loadDialog.isShowing()) {
            loadDialog.show();
        }

        Bundle bundle = getIntent().getExtras();

        if (null == bundle)
            return;

        mItemEntity = new Gson().fromJson(bundle.getString(EXTRA_ITEM_JSON), ItemEntity.class);
        source = bundle.getString(EXTRA_SOURCE);
        mItemEntityList = new ArrayList<>();

        for (int i = 0; i < mItemEntity.getSubitems().length; i++) {
            mSubItem = mItemEntity.getSubitems()[i];
            mItemEntityList.add(mSubItem);
        }


        initLayout();
        mDramaView.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                } else {

                }
            }
        });

        mDramaView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    View convertView = view.getChildAt(0);
                    if (convertView != null) {
                        Button btn = (Button) convertView.findViewById(R.id.btn_count);
                        btn.requestFocus();
                    }
                }

                if (visibleItemCount >= totalItemCount) {
                    down_btn.setVisibility(View.INVISIBLE);
                }
                if (firstVisibleItem == 0) {
                    up_btn.setVisibility(View.INVISIBLE);
                }
                if (firstVisibleItem > 0) {
                    up_btn.setVisibility(View.VISIBLE);
                }
                if (firstVisibleItem == 0 && firstVisibleItem + visibleItemCount < totalItemCount) {
                    down_btn.setVisibility(View.VISIBLE);
                }
                if (firstVisibleItem != 0 && firstVisibleItem + visibleItemCount >= totalItemCount) {
                    down_btn.setVisibility(View.INVISIBLE);
                    up_btn.setVisibility(View.VISIBLE);
                }
            }
        });


        down_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mDramaView.scrollListBy(500);
            }
        });
        up_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mDramaView.scrollListBy(-500);
            }
        });
        down_btn.setOnHoverListener(onHoverListener);
        up_btn.setOnHoverListener(onHoverListener);
        up_btn.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && lastdaramView != null) {
                    lastdaramView.setSelected(false);
                }
            }
        });
        down_btn.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && lastdaramView != null) {
                    lastdaramView.setSelected(false);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent data = new Intent();
            data.putExtra("result", paystatus);
            setResult(20, data);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initViews() {
        View v = (View) findViewById(R.id.drama_gridview);
        down_btn = (Button) v.findViewById(R.id.down_btn);
        up_btn = (Button) v.findViewById(R.id.up_btn);
        mDramaView = (GridView) v.findViewById(R.id.drama_zgridview);
        mDramaView.setOnItemSelectedListener(this);
        mDramaView.setOnItemClickListener(this);
        mDramaView.setNumColumns(10);
        mDramaView.setVerticalSpacing(30);

        mImageBackground = (ImageView) findViewById(R.id.image_daram_back);
        mDramaImageLabel = (ImageView) findViewById(R.id.image_daram_label);
        mTvDramaName = (TextView) findViewById(R.id.tv_drama_name);
        mTvDramaAll = (TextView) findViewById(R.id.tv_daram_all);
        mTvDramaType = (TextView) findViewById(R.id.tv_daram_type);
        one_drama_order_info = (TextView) findViewById(R.id.one_drama_order_info);
        orderAll_drama = (Button) findViewById(R.id.orderAll_drama);
        orderAll_drama.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pk = mItemEntity.getPk();
                int jumpTo = mItemEntity.getExpense().getJump_to();
                int cpid = mItemEntity.getExpense().getCpid();
                PageIntentInterface.PaymentInfo paymentInfo = new PageIntentInterface.PaymentInfo(item, pk, jumpTo, cpid);
                new PageIntent().toPayment(EpisodeActivity.this, unknown.name(), paymentInfo);
            }
        });
        orderAll_drama.setOnHoverListener(onHoverListener);
        orderAll_drama.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && lastdaramView != null) {
//					View butvView = lastdaramView.findViewById(R.id.btn_count);
//					lastdaramView.setSelected(false);
//					butvView.setBackgroundResource(R.drawable.daram_grid_selector);
                }
            }
        });
//        loadDialog = new LoadingDialog(this, getString(R.string.vod_loading));
    }

//    private Handler myHandler = new Handler() {
//
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case DISABLE_ORDER_ALL_DRANA: {
//                    orderAll_drama.setEnabled(false);
//                    orderAll_drama.setFocusable(false);
//                    orderAll_drama.setText("已购买");
//                }
//                case ORDER_ALL_DRANA_SUCCESS: {
//                    orderAll_drama.setEnabled(false);
//                    orderAll_drama.setFocusable(false);
//                    orderAll_drama.setText("已购买");
//                    for (Item item : mList) {
//                        item.remainDay = mItem.expense.duration + 1;
//                        mDramaAdapter.notifyDataSetChanged();
//                    }
//                }
//            }
//        }
//
//    };


    private void initLayout() {
        if (mItemEntity.getExpense() != null) {
            String expensevalue = getResources().getString(R.string.one_drama_order_info);
            one_drama_order_info.setText(String.format(expensevalue, mItemEntity.getExpense().getSubprice(), mItemEntity.getExpense().getDuration()));
            one_drama_order_info.setVisibility(View.VISIBLE);
        }
        if (mItemEntity.getPosterUrl() != null) {
            Picasso.with(this).load(mItemEntity.getPosterUrl()).into(mImageBackground);
        }

        // 名称
        mTvDramaName.setText(mItemEntity.getTitle());

        if (mItemEntity.getSubitems().length > 0) {
            String update_to_episode = getResources().getString(R.string.update_to_episode);
            mTvDramaAll.setText(String.format(update_to_episode, mItemEntity.getSubitems().length));
        }

        switch (mItemEntity.getQuality()) {
            case 3:
                mDramaImageLabel.setBackgroundResource(R.drawable.label_uhd);
                break;
            case 4:
                mDramaImageLabel.setBackgroundResource(R.drawable.label_hd);
                break;
            default:
                mDramaImageLabel.setVisibility(View.GONE);
                break;
        }

        mDramaAdapter = new DaramAdapter(this, mItemEntityList, mItemEntity, R.layout.drama_gridview_item);
        mDramaView.setAdapter(mDramaAdapter);
        mDramaAdapter.mTvDramaType = mTvDramaType;
        if (mDramaAdapter.getCount() <= mDramaView.getCount()) {
            down_btn.setVisibility(View.INVISIBLE);
        }
        if (loadDialog != null && loadDialog.isShowing()) {
            loadDialog.dismiss();
        }
    }

    private View lastdaramView;

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view,
                               int postion, long arg3) {
        if (down_btn != null && !down_btn.isFocusable()) {
            down_btn.setFocusable(true);
            down_btn.setFocusableInTouchMode(true);
        }
        mSubItem = mItemEntityList.get(postion);
        // 分类
        mTvDramaType.setText(mSubItem.getTitle());
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int postion, long postions) {
        mSubItem = mItemEntityList.get(postion);
        int sub_id = mSubItem.getPk();
        String title = mItemEntity.getTitle() + "(" + mSubItem.getEpisode() + ")";
    }

    @Override
    protected void onDestroy() {
        mDramaAdapter = null;
        mSubItem = null;
        loadDialog = null;
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (loadDialog != null && loadDialog.isShowing()) {
            loadDialog.dismiss();
        }
        super.onPause();
    }

    private View.OnHoverListener onHoverListener = new View.OnHoverListener() {

        @Override
        public boolean onHover(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                case MotionEvent.ACTION_HOVER_MOVE:
                    v.setFocusable(true);
                    v.setFocusableInTouchMode(true);
                    v.requestFocusFromTouch();
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    break;
            }
            return true;
        }
    };

    private class DaramAdapter extends BaseAdapter implements View.OnHoverListener, View.OnFocusChangeListener {
        Context mContext;
        private List<ItemEntity> subitemlist;
        private int sourceid;
        private LayoutInflater mLayoutInflater;
        private ItemEntity dramaItem;
        public TextView mTvDramaType;

        public DaramAdapter(Context context, List<ItemEntity> subitemlist, ItemEntity dramaitem, int sourceid) {
            this.mContext = context;
            this.subitemlist = subitemlist;
            this.sourceid = sourceid;
            this.dramaItem = dramaitem;
            this.mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return subitemlist.size();
        }

        @Override
        public ItemEntity getItem(int position) {
            return subitemlist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        ItemEntity subitem;
        ViewHolder holder;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            subitem = getItem(position);
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mLayoutInflater.inflate(sourceid, null);
                holder.btnCount = (Button) convertView.findViewById(R.id.btn_count);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
//            if (subitem.remainDay > 0) {
//                holder.btnCount.setBackgroundResource(R.drawable.episode_grid_payed_selector);
//            } else {
            holder.btnCount.setBackgroundResource(R.drawable.daram_grid_selector);
//            }
            holder.btnCount.setText(String.valueOf(position + 1));
            holder.btnCount.setTag(String.valueOf(position));
            holder.btnCount.setOnFocusChangeListener(this);
            holder.btnCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = Integer.parseInt((String) v.getTag());
                    subitem = getItem(position);

                }
            });

            return convertView;
        }

        private class ViewHolder {
            Button btnCount;
        }

        @Override
        public boolean onHover(View v, MotionEvent event) {
            int what = event.getAction();
            switch (what) {
                case MotionEvent.ACTION_HOVER_ENTER:
                case MotionEvent.ACTION_HOVER_MOVE:
                    break;
            }
            return false;
        }

        @Override
        public void onFocusChange(View v, boolean hasfocus) {
            int position = Integer.parseInt((String) v.getTag());
            subitem = getItem(position);
            if (hasfocus) {
                mTvDramaType.setText(subitem.getTitle());
            }
        }
    }
}
