package tv.ismar.usercenter.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import tv.ismar.app.BaseFragment;
import tv.ismar.app.network.entity.YouHuiDingGouEntity;
import tv.ismar.usercenter.ProductContract;
import tv.ismar.usercenter.R;
import tv.ismar.usercenter.databinding.FragmentProductBinding;
import tv.ismar.usercenter.viewmodel.ProductViewModel;

/**
 * Created by huibin on 10/27/16.
 */

public class ProductFragment extends BaseFragment implements ProductContract.View {
    private static final String TAG = ProductFragment.class.getSimpleName();
    private ProductViewModel mViewModel;
    private ProductContract.Presenter mPresenter;

    public static ProductFragment newInstance() {
        return new ProductFragment();
    }


    private RecyclerView mRecyclerView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        FragmentProductBinding productBinding = FragmentProductBinding.inflate(inflater, container, false);
        productBinding.setTasks(mViewModel);
        productBinding.setActionHandler(mPresenter);

        mRecyclerView = productBinding.recyclerview;
        View root = productBinding.getRoot();
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
        Log.d(TAG, "onResume");
        mPresenter.start();
        mViewLoadCallback.loadComplete();

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
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
        ProductAdapter adapter = new ProductAdapter(getContext(), entity.getObjects());
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4 ));
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(getResources().getDimensionPixelSize(R.dimen.product_recycler_item_spacing)));
        mRecyclerView.setAdapter(adapter);
    }


    private class ProductAdapter extends RecyclerView.Adapter<ProductViewHolder> {
        private Context mContext;

        private List<YouHuiDingGouEntity.Object> mObjects;


        public ProductAdapter(Context context, List<YouHuiDingGouEntity.Object> objects) {
            mContext = context;
            mObjects = objects;
        }

        @Override

        public ProductViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_product_list, viewGroup, false);
            ProductViewHolder holder = new ProductViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(ProductViewHolder holder, int position) {
            YouHuiDingGouEntity.Object item = mObjects.get(position);
            holder.mTextView.setText(item.getTitle());
            if (item.getPoster_url().trim().length() == 0) {
                Picasso.with(mContext).load(R.drawable.list_item_preview_bg).into(holder.mImageView);
            } else {
                Picasso.with(mContext).load(item.getPoster_url()).into(holder.mImageView);
            }
        }

        @Override
        public int getItemCount() {
            return mObjects.size();
        }
    }

    private class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;
        private TextView mTextView;

        public ProductViewHolder(View itemView) {
            super(itemView);

            mImageView = (ImageView) itemView.findViewById(R.id.package_list_image);
            mTextView = (TextView) itemView.findViewById(R.id.package_list_title);
        }
    }
}
