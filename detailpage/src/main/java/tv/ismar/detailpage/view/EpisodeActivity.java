package tv.ismar.detailpage.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.Source;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.ui.HeadFragment;
import tv.ismar.app.widget.LoadingDialog;
import tv.ismar.detailpage.R;

import static tv.ismar.app.core.PageIntentInterface.EXTRA_ITEM_JSON;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_SOURCE;

/**
 * Created by huibin on 11/29/16.
 */

public class EpisodeActivity extends BaseActivity {
    private ItemEntity mItemEntity;
    private List<ItemEntity> mItemEntityList;

    private ItemEntity mSubItem;
    private ImageView mImageBackground;
    private ImageView mDramaImageLabel;
    private TextView mTvDramaName;
    private TextView mTvDramaAll;
    private TextView mTvDramaType;
    private TextView one_drama_order_info;
    private Button down_btn;
    private Button up_btn;
    private LoadingDialog loadDialog;
    private boolean paystatus = false;


    private String source;

    private RecyclerViewTV mEpisodeRecyclerView;

    private View tmp;

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

        HeadFragment headFragment = new HeadFragment();
        Bundle headFragmentBundle = new Bundle();
        headFragmentBundle.putString("type", HeadFragment.HEADER_DETAILPAGE);
        headFragmentBundle.putString("channel_name", "剧集列表");
        headFragment.setArguments(headFragmentBundle);
        getSupportFragmentManager().beginTransaction().add(R.id.detail_head, headFragment).commit();

        initLayout();

        int horizontal = getResources().getDimensionPixelSize(R.dimen.episode_item_horizontal_space);
        int vertical = getResources().getDimensionPixelSize(R.dimen.episode_item_vertical_space);
        mEpisodeRecyclerView.setSelectedItemAtCentered(false);
        mEpisodeRecyclerView.addItemDecoration(new SpacesItemDecoration(horizontal, vertical));
        mEpisodeRecyclerView.setLayoutManager(new GridLayoutManager(this, 10));
        mEpisodeRecyclerView.setAdapter(new EpisodeAdapter(this, mItemEntity.getSubitems()));


    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private void initViews() {
        tmp = findViewById(R.id.tmp);
        mEpisodeRecyclerView = findView(R.id.episode_recycler_view);
        mImageBackground = (ImageView) findViewById(R.id.image_daram_back);
        mDramaImageLabel = (ImageView) findViewById(R.id.image_daram_label);
        mTvDramaName = (TextView) findViewById(R.id.tv_drama_name);
        mTvDramaAll = (TextView) findViewById(R.id.tv_daram_all);
        mTvDramaType = (TextView) findViewById(R.id.tv_daram_type);
        one_drama_order_info = (TextView) findViewById(R.id.one_drama_order_info);
    }


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

        if (loadDialog != null && loadDialog.isShowing()) {
            loadDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
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


    private class EpisodeAdapter extends RecyclerView.Adapter<EpisodeViewHolder> implements
            View.OnHoverListener, View.OnFocusChangeListener, View.OnClickListener {
        private Context mContext;
        private ItemEntity[] mItemEntities;

        public EpisodeAdapter(Context context, ItemEntity[] itemEntities) {
            mContext = context;
            mItemEntities = itemEntities;
        }

        @Override
        public EpisodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.episode_recycler_item, parent, false);
            EpisodeViewHolder viewHolder = new EpisodeViewHolder(itemView);
            viewHolder.episodeBtn.setOnClickListener(this);
            viewHolder.episodeBtn.setOnHoverListener(this);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(EpisodeViewHolder holder, int position) {
            ItemEntity itemEntity = mItemEntities[position];
            Button episodeBtn = holder.episodeBtn;
            episodeBtn.setTag(itemEntity);
            episodeBtn.setText(String.valueOf(position + 1));


        }

        @Override
        public int getItemCount() {
            return mItemEntities.length;

        }

        @Override
        public boolean onHover(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                case MotionEvent.ACTION_HOVER_MOVE:
                    v.requestFocus();
                    v.requestFocusFromTouch();
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    tmp.requestFocus();
                    tmp.requestFocusFromTouch();
                    break;
            }
            return true;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {

        }

        @Override
        public void onClick(View v) {
            ItemEntity subItemEntity = (ItemEntity) v.getTag();
            PageIntent pageIntent = new PageIntent();
            pageIntent.toPlayPage(mContext, mItemEntity.getPk(), subItemEntity.getPk(), Source.LIST);
        }
    }

    private class EpisodeViewHolder extends RecyclerView.ViewHolder {
        private View mItemView;
        private Button episodeBtn;

        public EpisodeViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            episodeBtn = (Button) itemView.findViewById(R.id.btn_count);
        }
    }

    private class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int horizontal;
        private int vertical;

        public SpacesItemDecoration(int horizonal, int vertical) {
            this.horizontal = horizonal;
            this.vertical = vertical;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.top = vertical;
            outRect.bottom = vertical;
            outRect.left = horizontal;
            outRect.right = horizontal;
        }
    }
}
