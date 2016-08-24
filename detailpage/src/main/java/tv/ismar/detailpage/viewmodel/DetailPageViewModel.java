package tv.ismar.detailpage.viewmodel;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.detailpage.BR;
import tv.ismar.detailpage.DetailPageContract;

/**
 * Created by huibin on 8/18/16.
 */
public class DetailPageViewModel extends BaseObservable {
    private Context mContext;
    private final DetailPageContract.Presenter mPresenter;
    public ObservableField<String> itemTitle;
    private ItemEntity mItemEntity = new ItemEntity();


    public DetailPageViewModel(Context context, DetailPageContract.Presenter presenter) {
        mContext = context;
        mPresenter = presenter;
        itemTitle = new ObservableField<>();
    }

    public void replaceItem(ItemEntity itemEntity) {
        mItemEntity = itemEntity;
        itemTitle.set(itemEntity.getTitle());
        notifyPropertyChanged(BR.itemPostUrl);
        notifyPropertyChanged(BR.description);
        notifyPropertyChanged(BR.purchaseVisibility);

    }


    @BindingAdapter({"imageUrl"})
    public static void loadImage(ImageView view, String imageUrl) {
        Picasso.with(view.getContext())
                .load(imageUrl)
                .into(view);
    }

    @Bindable
    public String getItemPostUrl() {
        return mItemEntity.getDetailUrl();
    }

    @Bindable
    public String getDescription() {
        return mItemEntity.getDescription();
    }

    @Bindable
    public int getPurchaseVisibility() {
        return mItemEntity.getExpense() == null ? View.GONE : View.VISIBLE;
    }



}
