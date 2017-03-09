package tv.ismar.usercenter.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.open.androidtvwidget.leanback.recycle.GridLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.List;

import tv.ismar.app.AppConstant;
import tv.ismar.app.BaseFragment;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.VipMark;
import tv.ismar.app.core.client.NetworkUtils;
import tv.ismar.app.entity.Item;
import tv.ismar.app.network.entity.YouHuiDingGouEntity;
import tv.ismar.app.ui.ZGridView;
import tv.ismar.usercenter.ProductContract;
import tv.ismar.usercenter.R;
import tv.ismar.usercenter.databinding.FragmentProductBinding;
import tv.ismar.usercenter.viewmodel.ProductViewModel;

/**
 * Created by huibin on 10/27/16.
 */

public class ProductFragment extends BaseFragment implements ProductContract.View, AdapterView.OnItemClickListener {
    private static final String TAG = ProductFragment.class.getSimpleName();
    private ProductViewModel mViewModel;
    private ProductContract.Presenter mPresenter;

    public static ProductFragment newInstance() {
        return new ProductFragment();
    }


    private ZGridView gridView;

    private YouHuiDingGouEntity mYouHuiDingGouEntity;
    private FragmentProductBinding productBinding;

    private boolean fragmentIsPause = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        mPresenter.start();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        productBinding = FragmentProductBinding.inflate(inflater, container, false);
        productBinding.setTasks(mViewModel);
        productBinding.setActionHandler(mPresenter);

        gridView = productBinding.recyclerview;
        gridView.setOnItemClickListener(this);
        View root = productBinding.getRoot();
//        root.getViewTreeObserver().addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
//            @Override
//            public void onGlobalFocusChanged(View oldFocus, View newFocus) {
//                Log.d(TAG, "onGlobalFocusChanged: " + newFocus);
//            }
//        });
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        AppConstant.purchase_page = "expense";
        fragmentIsPause = false;
        Log.d(TAG, "onResume");



    }

    @Override
    public void onPause() {
        fragmentIsPause = true;
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        mPresenter.stop();
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
    }

    public void setViewModel(ProductViewModel viewModel) {
        mViewModel = viewModel;
    }

    @Override
    public void setPresenter(ProductContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void loadProductItem(YouHuiDingGouEntity entity) {
        mYouHuiDingGouEntity = entity;
        ProductAdapter adapter = new ProductAdapter(getContext(), entity.getObjects());
        gridView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PageIntent pageIntent = new PageIntent();
        pageIntent.toPackageDetail(getContext(), "usercenter", (int) mYouHuiDingGouEntity.getObjects().get(position).getPk());
    }


    private class ProductAdapter extends BaseAdapter {
        private Context mContext;
        private List<YouHuiDingGouEntity.Object> mObjects;

        public ProductAdapter(Context mContext, List<YouHuiDingGouEntity.Object> mObjects) {
            this.mContext = mContext;
            this.mObjects = mObjects;
        }

        @Override
        public int getCount() {
            return mObjects.size();
        }

        @Override
        public YouHuiDingGouEntity.Object getItem(int position) {
            return mObjects.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ProductViewHolder productViewHolder;
            if (convertView == null) {
                productViewHolder = new ProductViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_product_list, null);
                productViewHolder.imageView = (ImageView) convertView.findViewById(R.id.package_list_image);
                productViewHolder.textView = (TextView) convertView.findViewById(R.id.package_list_title);
                convertView.setTag(productViewHolder);

            } else {
                productViewHolder = (ProductViewHolder) convertView.getTag();
            }

            productViewHolder.textView.setText(mObjects.get(position).getTitle());
            if (mObjects.get(position).getPoster_url().trim().length() == 0) {
                Picasso.with(mContext).load(R.drawable.list_item_preview_bg).into(productViewHolder.imageView);
            } else {
                Picasso.with(mContext).load(mObjects.get(position).getPoster_url()).memoryPolicy(MemoryPolicy.NO_STORE).config(Bitmap.Config.RGB_565).into(productViewHolder.imageView);
            }


            if (position == 0 || position == 1 || position == 2 || position == 3) {
                convertView.setId(View.generateViewId());
                convertView.setNextFocusUpId(convertView.getId());
            }


            int theLastLineCount = (position + 1) % 4;

            if (theLastLineCount != 0) {
                if (position >= mObjects.size() - 1 - theLastLineCount) {
                    convertView.setId(View.generateViewId());
                    convertView.setNextFocusDownId(convertView.getId());
                }

            }

            if ((position + 1) % 4 == 1) {
                convertView.setNextFocusLeftId(R.id.usercenter_store);
            }
            return convertView;
        }

        private class ProductViewHolder {
            ImageView imageView;
            TextView textView;
        }
    }


//    private class ProductAdapter extends RecyclerView.Adapter<ProductViewHolder> implements View.OnHoverListener, View.OnFocusChangeListener, OnClickListener {
//        private Context mContext;
//
//        private List<YouHuiDingGouEntity.Object> mObjects;
//
//
//        public ProductAdapter(Context context, List<YouHuiDingGouEntity.Object> objects) {
//            mContext = context;
//            mObjects = objects;
//        }
//
//        @Override
//
//        public ProductViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
//            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_product_list, viewGroup, false);
//            view.setOnHoverListener(this);
//            view.setOnFocusChangeListener(this);
//            ProductViewHolder holder = new ProductViewHolder(view);
//            holder.itemView.setOnClickListener(this);
//            return holder;
//        }
//
//        @Override
//        public void onBindViewHolder(ProductViewHolder holder, int position) {
//            YouHuiDingGouEntity.Object item = mObjects.get(position);
//            holder.itemView.setTag(position);
//            holder.mTextView.setText(item.getTitle());
//            if (item.getPoster_url().trim().length() == 0) {
//                Picasso.with(mContext).load(R.drawable.list_item_preview_bg).into(holder.mImageView);
//            } else {
//                Picasso.with(mContext).load(item.getPoster_url()).into(holder.mImageView);
//            }
//            if (position == 0 || position == 1 || position == 2 || position == 3) {
//                holder.itemView.setId(View.generateViewId());
//                holder.itemView.setNextFocusUpId(holder.itemView.getId());
//            }
//
////            if (position == mObjects.size() - 1 || position == mObjects.size() - 2 || position == mObjects.size() - 3 || position == mObjects.size() - 4) {
////                holder.mImageView.setNextFocusDownId(holder.mImageView.getId());
////            }
//
//            int theLastLineCount = (position + 1) % 4;
//
//            if (theLastLineCount != 0) {
//                if (position >= mObjects.size() - 1 - theLastLineCount) {
//                    holder.itemView.setId(View.generateViewId());
//                    holder.itemView.setNextFocusDownId(holder.itemView.getId());
//                }
//
//            }
//
//            if ((position + 1) % 4 == 1) {
//                holder.itemView.setNextFocusLeftId(R.id.usercenter_store);
//            }
//        }
//
//        @Override
//        public int getItemCount() {
//            return mObjects.size();
//        }
//
//        @Override
//        public boolean onHover(View v, MotionEvent event) {
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_HOVER_ENTER:
//                case MotionEvent.ACTION_HOVER_MOVE:
//                    if (!v.hasFocus()) {
//                        v.requestFocus();
//                        v.requestFocusFromTouch();
//                    }
//                    break;
//                case MotionEvent.ACTION_HOVER_EXIT:
//                    if (!fragmentIsPause) {
//                        productBinding.tmp.requestFocus();
//                        productBinding.tmp.requestFocusFromTouch();
//                    }
//                    break;
//
//            }
//            return true;
//        }
//
//        @Override
//        public void onFocusChange(View v, boolean hasFocus) {
//            ((UserCenterActivity) getActivity()).clearTheLastHoveredVewState();
//            if (v.getId() != R.id.tmp) {
//                View imageView = v.findViewById(R.id.package_list_image);
//                if (hasFocus) {
//                    imageView.setSelected(true);
//                } else {
//                    imageView.setSelected(false);
//                }
//            }
//        }
//
//        @Override
//        public void onClick(View v) {
//            int position = (int) v.getTag();
//            PageIntent pageIntent = new PageIntent();
//            pageIntent.toPackageDetail(getContext(), "usercenter", (int) mYouHuiDingGouEntity.getObjects().get(position).getPk());
//        }
//    }
//
//    private class ProductViewHolder extends RecyclerView.ViewHolder {
//        private ImageView mImageView;
//        private TextView mTextView;
//
//        public ProductViewHolder(View itemView) {
//            super(itemView);
//            mImageView = (ImageView) itemView.findViewById(R.id.package_list_image);
//            mImageView.setTag("right");
//            mTextView = (TextView) itemView.findViewById(R.id.package_list_title);
//        }
//    }
}
