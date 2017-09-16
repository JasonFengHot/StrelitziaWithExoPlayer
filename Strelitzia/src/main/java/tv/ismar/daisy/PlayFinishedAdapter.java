package tv.ismar.daisy;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.InputStream;

import tv.ismar.app.core.VipMark;
import tv.ismar.app.core.client.NetworkUtils;
import tv.ismar.app.entity.Item;

public class PlayFinishedAdapter extends BaseAdapter {
    Context mContext;
    Item item;
    ViewHolder holder;
    private Item[] listItem;
    private Item[] listItemSort;
    private int sourceid;
    private LayoutInflater mLayoutInflater;

    public PlayFinishedAdapter(Context context, Item[] items, int sourceid) {
        this.mContext = context;
        this.listItemSort = items;
        this.sourceid = sourceid;
        this.mLayoutInflater = LayoutInflater.from(context);
        sortItem();
    }

    public static Object[] getImage(Item item, Context context) {
        Object[] object = new Object[2];
        int resourceLabel = 0;
        //		int H = DaisyUtils.getVodApplication(context).getheightPixels(context);
        //		if(H==720||(H>720&&H<1080))
        //			object[0] =
        // ImageUtils.getBitmapFromInputStream(NetworkUtils.getInputStream(item.adlet_url),
        // (int)(188), (int)(106));
        //		else
        //			object[0] =
        // ImageUtils.getBitmapFromInputStream(NetworkUtils.getInputStream(item.adlet_url),
        // (int)(282), (int)(158));

        object[0] =
                getBitmapFromInputStream(
                        NetworkUtils.getInputStream(item.adlet_url),
                        context.getResources()
                                .getDimensionPixelSize(
                                        R.dimen.play_finished_image_vodie_backgroud_W),
                        context.getResources()
                                .getDimensionPixelSize(
                                        R.dimen.play_finished_image_vodie_backgroud_H));
        //		object[0] =
        // (BitmapFactory.decodeStream(HttpUtil.getHttpConnectionByGet(item.adlet_url).getInputStream()));
        switch (item.quality) {
            case 3:
                resourceLabel = R.drawable.label_uhd;
                break;
            case 4:
                resourceLabel = R.drawable.label_hd;
                break;
            default:
                resourceLabel = 0;
                break;
        }
        object[1] = resourceLabel;

        return object;
    }

    public static Bitmap getBitmapFromInputStream(InputStream in, int width, int height) {
        if (in != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.outWidth = width;
            options.outHeight = height;
            options.inDither = true;
            options.inScaled = true;
            options.inTargetDensity = 160;
            options.inDensity = 160;
            return BitmapFactory.decodeStream(in, null, options);
        } else {
            return null;
        }
    }

    private void sortItem() {
        if (null == listItemSort) {
            return;
        }
        if (listItemSort.length <= 9) {
            listItem = listItemSort;
            return;
        }
        listItem = new Item[9];
        for (int i = 0; i < 9; i++) {
            listItem[i] = listItemSort[i];
        }
    }

    @Override
    public int getCount() {
        return listItem.length;
    }

    @Override
    public Item getItem(int position) {
        return listItem[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        item = getItem(position);
        holder = new ViewHolder();
        convertView = mLayoutInflater.inflate(sourceid, null);
        holder.imageView = (ImageView) convertView.findViewById(R.id.itemImage);
        holder.tvItemText = (TextView) convertView.findViewById(R.id.itemText);
        holder.imageLabel = (ImageView) convertView.findViewById(R.id.iv_label);
        holder.ItemBeanScore = (TextView) convertView.findViewById(R.id.ItemBeanScore);
        holder.expense_txt = (ImageView) convertView.findViewById(R.id.expense_txt);
        holder.tvItemText.setText(item.title);
        showImage(holder.imageView, holder.imageLabel, item);
        if (item != null) {
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
                if (item.bean_score > 0) {
                    holder.ItemBeanScore.setVisibility(View.VISIBLE);
                    holder.ItemBeanScore.setText(item.bean_score + "");
                }
            }
        }
        return convertView;
    }

    private void showImage(ImageView imageView, ImageView imageLabel, Item item) {
        LoadImageTask task = new LoadImageTask();
        // 参数传给了doInBackground
        task.execute(imageView, imageLabel, item);
    }

    public static class ViewHolder {
        ImageView imageView;
        TextView tvItemText;
        ImageView imageLabel;
        ImageView expense_txt;
        TextView ItemBeanScore;
    }

    private final class LoadImageTask extends AsyncTask<Object, Integer, Object[]> {
        // 主图片
        ImageView imageView;
        // 高清标签
        ImageView imageLabel;

        @Override
        protected Object[] doInBackground(Object... params) { // 耗时操作,运行在子线程
            imageView = (ImageView) params[0];
            imageLabel = (ImageView) params[1];
            try {
                return getImage((Item) params[2], mContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object[] object) {
            try {
                imageView.setImageBitmap((Bitmap) object[0]); // 显示照片
                if (null == object[1]) {
                    imageLabel.setVisibility(View.GONE);
                } else {
                    imageLabel.setBackgroundResource((Integer) object[1]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
