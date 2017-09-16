package tv.ismar.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import cn.ismartv.imagereflection.ReflectionTransformationBuilder;
import tv.ismar.app.core.VipMark;
import tv.ismar.app.entity.Item;
import tv.ismar.app.ui.view.AsyncImageView;
import tv.ismar.app.ui.view.LabelImageView;
import tv.ismar.listpage.R;

public class RelatedAdapter extends BaseAdapter implements AsyncImageView.OnImageViewLoadListener {

    private Context mContext;
    private List<Item> mItemList;
    private HashSet<AsyncImageView> mOnLoadingImageQueue;
    private boolean isPortrait = false;
    private Transformation mTransformation =
            new ReflectionTransformationBuilder().setIsHorizontal(true).build();

    public RelatedAdapter(Context context, List<Item> itemList, boolean isPortrait) {
        this.isPortrait = isPortrait;
        mContext = context;
        mOnLoadingImageQueue = new HashSet<AsyncImageView>();
        if (itemList != null && itemList.size() > 0) {
            if (itemList.size() <= 12) {
                mItemList = itemList;
            } else {
                mItemList = new ArrayList<Item>();
                for (int i = 0; i < 12; ++i) {
                    mItemList.add(itemList.get(i));
                }
            }

        } else {
            mItemList = new ArrayList<Item>();
        }
    }

    @Override
    public int getCount() {
        return mItemList.size();
    }

    @Override
    public Item getItem(int position) {

        return mItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder = null;
        if (convertView == null) {
            holder = new Holder();
            if (!isPortrait)
                convertView =
                        LayoutInflater.from(mContext)
                                .inflate(R.layout.list_view_related_item, null);
            else
                convertView =
                        LayoutInflater.from(mContext)
                                .inflate(R.layout.list_portrait_relateditem, null);
            //			AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(348, 252);
            //			View titleView = convertView.findViewById(R.id.list_item_title);
            //			titleView.setFocusable(true);
            //			convertView.setClickable(true);
            //			convertView.setLayoutParams(layoutParams);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.previewImage = (AsyncImageView) convertView.findViewById(R.id.list_item_preview_img);
        holder.title = (TextView) convertView.findViewById(R.id.list_item_title);
        if (!isPortrait) {
            holder.previewImage.setUrl(mItemList.get(position).adlet_url);
        } else {
            if (mItemList
                    .get(position)
                    .list_url
                    .equals(
                            "http://res.tvxio.bestv.com.cn/media/upload/20160321/36c8886fd5b4163ae48534a72ec3a555.png")) {
                Picasso.with(mContext)
                        .load(mItemList.get(position).adlet_url)
                        .error(R.drawable.list_item_ppreview_bg)
                        .placeholder(null)
                        .transform(mTransformation)
                        .into(holder.previewImage);
            } else {
                holder.previewImage.setUrl(mItemList.get(position).list_url);
            }
            if (mItemList.get(position).focus != null)
                ((LabelImageView) holder.previewImage).setFocustitle(mItemList.get(position).focus);
        }
        holder.title.setText(mItemList.get(position).title);
        holder.qualityLabel = (ImageView) convertView.findViewById(R.id.list_item_quality_label);
        holder.ItemBeanScore = (TextView) convertView.findViewById(R.id.ItemBeanScore);
        holder.price = (ImageView) convertView.findViewById(R.id.expense_txt);
        if (mItemList.get(position).bean_score > 0) {
            holder.ItemBeanScore.setText("" + mItemList.get(position).bean_score);
            holder.ItemBeanScore.setVisibility(View.VISIBLE);
        } else {
            holder.ItemBeanScore.setVisibility(View.INVISIBLE);
        }
        if (mItemList.get(position).expense != null) {
            if (mItemList.get(position).expense.cptitle != null) {
                holder.price.setVisibility(View.VISIBLE);
                String imageUrl =
                        VipMark.getInstance()
                                .getImage(
                                        (Activity) mContext,
                                        mItemList.get(position).expense.pay_type,
                                        mItemList.get(position).expense.cpid);
                Picasso.with(mContext).load(imageUrl).into(holder.price);
            }
        } else {
            holder.price.setVisibility(View.GONE);
        }
        return convertView;
    }

    public void cancel() {
        for (AsyncImageView imageView : mOnLoadingImageQueue) {
            imageView.stopLoading();
        }
        mOnLoadingImageQueue.clear();
        mOnLoadingImageQueue = null;
        mItemList = null;
    }

    @Override
    public void onLoadingStarted(AsyncImageView imageView) {
        mOnLoadingImageQueue.add(imageView);
    }

    @Override
    public void onLoadingEnded(AsyncImageView imageView, Bitmap image) {
        mOnLoadingImageQueue.remove(imageView);
    }

    @Override
    public void onLoadingFailed(AsyncImageView imageView, Throwable throwable) {
        mOnLoadingImageQueue.remove(imageView);
    }

    static class Holder {
        AsyncImageView previewImage;
        TextView title;
        ImageView qualityLabel;
        TextView ItemBeanScore;
        ImageView price;
    }
}
