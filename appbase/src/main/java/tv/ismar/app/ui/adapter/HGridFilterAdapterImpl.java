package tv.ismar.app.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.HashSet;

import cn.ismartv.imagereflection.ReflectionTransformationBuilder;
import tv.ismar.app.R;
import tv.ismar.app.core.VipMark;
import tv.ismar.app.entity.Item;
import tv.ismar.app.entity.ItemCollection;
import tv.ismar.app.ui.HGridView;
import tv.ismar.app.ui.view.AsyncImageView;
import tv.ismar.app.ui.view.LabelImageView;

public class HGridFilterAdapterImpl extends HGridAdapter<ItemCollection>
        implements AsyncImageView.OnImageViewLoadListener {

    private static final String TAG = "HGridAdapterImpl";
    public HGridView hg;
    private Context mContext;
    private boolean mHasSection = true;
    private int mSize = 0;
    private HashSet<AsyncImageView> mOnLoadingImageQueue = new HashSet<AsyncImageView>();
    private HashSet<RelativeLayout> mOnLoadinglayoutQueue = new HashSet<RelativeLayout>();
    private boolean isPortrait = false;
    private Transformation mTransformation =
            new ReflectionTransformationBuilder().setIsHorizontal(true).build();

    public HGridFilterAdapterImpl(Context context, ArrayList<ItemCollection> list) {
        mContext = context;
        if (list != null && list.size() > 0) {
            mList = list;
            for (int i = 0; i < list.size(); i++) {
                mSize += list.get(i).count;
            }
        }
    }

    public HGridFilterAdapterImpl(
            Context context, ArrayList<ItemCollection> list, boolean hasSection) {
        mContext = context;
        if (list != null && list.size() > 0) {
            mList = list;
            for (int i = 0; i < list.size(); i++) {
                mSize += list.get(i).count;
            }
        }
        this.mHasSection = hasSection;
    }

    public void setIsPortrait(boolean isPortrait) {
        this.isPortrait = isPortrait;
    }

    @Override
    public void setList(ArrayList<ItemCollection> list) {
        mSize = 0;
        for (int i = 0; i < list.size(); i++) {
            mSize += list.get(i).count;
        }
        cancel();
        super.setList(list);
    }

    @Override
    public int getCount() {
        return mSize;
    }

    @Override
    public Item getItem(int position) {
        int size = 0;
        for (int i = 0; i < mList.size(); i++) {
            final int sectionCount = mList.get(i).count;
            if (size + sectionCount > position) {
                int indexOfCurrentSection = position - size;
                return mList.get(i).objects.get(indexOfCurrentSection);
            }
            size += sectionCount;
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder = null;
        if (convertView == null) {
            if (!this.isPortrait)
                convertView =
                        LayoutInflater.from(mContext).inflate(R.layout.filter_list_view_item, null);
            else
                convertView =
                        LayoutInflater.from(mContext)
                                .inflate(R.layout.filter_portraitlist_view_item, null);
            holder = new Holder();
            holder.title = (TextView) convertView.findViewById(R.id.filter_list_item_title);
            holder.previewImage =
                    (AsyncImageView) convertView.findViewById(R.id.filter_list_item_preview_img);
            holder.ItemBeanScore = (TextView) convertView.findViewById(R.id.ItemBeanScore);
            holder.expense_txt = (ImageView) convertView.findViewById(R.id.expense_txt);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        int itemCount = 0;
        int sectionIndex = 0;
        int indexOfCurrentSection = 0;
        for (int i = 0; i < mList.size(); i++) {
            final int sectionCount = mList.get(i).count;
            if (itemCount + sectionCount > position) {
                sectionIndex = i;
                indexOfCurrentSection = position - itemCount;
                break;
            }
            itemCount += sectionCount;
        }

        // This ItemCollection's currentIndex has been filled.
        if (mList.size() > 0) {
            if (mList.get(sectionIndex).isItemReady(indexOfCurrentSection)) {
                final Item item = mList.get(sectionIndex).objects.get(indexOfCurrentSection);
                if (item != null) {
                    if (this.isPortrait) {
                        if (item.list_url.equals(
                                "http://res.tvxio.bestv.com.cn/media/upload/20160321/36c8886fd5b4163ae48534a72ec3a555.png")) {
                            Picasso.with(mContext)
                                    .load(item.adlet_url)
                                    .error(R.drawable.list_item_ppreview_bg)
                                    .placeholder(R.drawable.list_item_ppreview_bg)
                                    .transform(mTransformation)
                                    .into(holder.previewImage);

                        } else {
                            holder.previewImage.setUrl(item.list_url);
                        }
                    } else {
                        if (item.adlet_url.equals(
                                "http://res.tvxio.bestv.com.cn/media/upload/20160504/5eae6db53f065ff0269dfc71fb28a4ec.png")) {
                            Picasso.with(mContext)
                                    .load(item.list_url)
                                    .error(R.drawable.list_item_ppreview_bg)
                                    .placeholder(R.drawable.list_item_ppreview_bg)
                                    .transform(mTransformation)
                                    .into(holder.previewImage);
                        } else {
                            holder.previewImage.setUrl(item.adlet_url);
                        }
                    }
                    holder.title.setText(item.title);
                    if (item.bean_score > 0) {
                        holder.ItemBeanScore.setText(item.bean_score + "");
                        holder.ItemBeanScore.setVisibility(View.VISIBLE);
                    }
                    if (item.expense != null) {
                        if (item.expense.cptitle != null) {
                            holder.expense_txt.setVisibility(View.VISIBLE);

                            String imageUrl =
                                    VipMark.getInstance()
                                            .getImage(
                                                    (Activity) mContext,
                                                    item.expense.pay_type,
                                                    item.expense.cpid);
                            Picasso.with(mContext).load(imageUrl).into(holder.expense_txt);
                        }
                    }
                }
                if (item.focus != null)
                    ((LabelImageView) holder.previewImage).setFocustitle(item.focus);
            } else {
                // This ItemCollection's currentIndex has not filled yet.
                // Show the default info.
                holder.title.setText(mContext.getResources().getString(R.string.onload));
                holder.previewImage.setUrl(null);
            }
        }

        return convertView;
    }

    @Override
    public int getSectionIndex(int position) {
        int size = 0;
        for (int i = 0; i < mList.size(); i++) {
            size += mList.get(i).count;
            if (size > position) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public boolean hasSection() {
        if (this.mHasSection) return true;
        else return false;
    }

    @Override
    public int getSectionCount(int sectionIndex) {
        if (mList.size() > 0) return mList.get(sectionIndex).count;
        else return 0;
    }

    @Override
    public String getLabelText(int sectionIndex) {
        if (this.mHasSection) return mList.get(sectionIndex).title;
        else return " ";
    }

    public void cancel() {
        for (AsyncImageView imageView : mOnLoadingImageQueue) {
            imageView.stopLoading();
        }
        mOnLoadingImageQueue.clear();
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
        TextView ItemBeanScore;
        ImageView expense_txt;
    }
}
