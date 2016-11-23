package tv.ismar.searchpage.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.List;

import tv.ismar.searchpage.R;
import tv.ismar.searchpage.model.Expense;
import tv.ismar.app.models.Recommend;
import tv.ismar.searchpage.weight.ItemContainer;
import tv.ismar.searchpage.weight.ReflectionTransformationBuilder;
import tv.ismar.searchpage.weight.RotateTextView;

/**
 * Created by admin on 2016/2/1.
 */
public class RecommendAdapter extends BaseAdapter {

    private Context mContext;
    Transformation mTransformation = new ReflectionTransformationBuilder()
            .setIsHorizontal(true)
            .build();
    private List<Recommend.ObjectsEntity> mData;
    public void setmData(List<Recommend.ObjectsEntity> mData) {
        this.mData = mData;
    }

    public RecommendAdapter(Context mContext, List<Recommend.ObjectsEntity> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Recommend.ObjectsEntity getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_poster_search, null);
            viewHolder = new ViewHolder();
            viewHolder.iv_poster = (ImageView) convertView.findViewById(R.id.iv_poster);
            viewHolder.ItemBeanScore = (TextView) convertView.findViewById(R.id.ItemBeanScore);
            viewHolder.expense_txt = (RotateTextView) convertView.findViewById(R.id.expense_txt);
            viewHolder.poster_title = (TextView) convertView.findViewById(R.id.poster_title);
            viewHolder.item_container= (ItemContainer) convertView.findViewById(R.id.item_container);
            viewHolder.tv_focus= (TextView) convertView.findViewById(R.id.tv_focus);
            viewHolder.expense_txt.setDegrees(315);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Recommend.ObjectsEntity objectsEntity = mData.get(position);
        if (objectsEntity.adlet_url!= null && !objectsEntity.adlet_url.equals("")) {
            if ("movie".equals(objectsEntity.content_model)) {
                if(objectsEntity.bean_score>0) {
                    viewHolder.ItemBeanScore.setText(objectsEntity.bean_score + "");
                    viewHolder.ItemBeanScore.setVisibility(View.VISIBLE);
                }
                if(objectsEntity.expense!=null){
                    if(objectsEntity.expense.cptitle!=null){
                        viewHolder.expense_txt.setText(objectsEntity.expense.cptitle);
                        viewHolder.expense_txt.setVisibility(View.VISIBLE);
                        if(objectsEntity.expense.pay_type== Expense.SEPARATE_CHARGE){
                            viewHolder.expense_txt.setBackgroundResource(R.drawable.list_single_buy);
                        }else if((objectsEntity.expense.cpid == Expense.ISMARTV_CPID)){
                            viewHolder.expense_txt.setBackgroundResource(R.drawable.list_ismar);
                        }else if((objectsEntity.expense.cpid == Expense.IQIYI_CPID)){
                            viewHolder.expense_txt.setBackgroundResource(R.drawable.list_lizhi);
                        }
                    } else {
                        viewHolder.expense_txt.setVisibility(View.GONE);
                    }

                }
                Picasso.with(mContext)
                        .load(objectsEntity.vertical_url)
                        .memoryPolicy(MemoryPolicy.NO_STORE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .error(R.drawable.vertical_preview_bg)
                        .placeholder(R.drawable.vertical_preview_bg)
                        .transform(mTransformation)
                        .into(viewHolder.iv_poster);
            } else {
                if(objectsEntity.bean_score>0) {
                           viewHolder.ItemBeanScore.setText(objectsEntity.bean_score + "");
                    viewHolder.ItemBeanScore.setVisibility(View.VISIBLE);
                }
                if(objectsEntity.expense!=null){
                    if(objectsEntity.expense.cptitle!=null){
                        viewHolder.expense_txt.setText(objectsEntity.expense.cptitle);
                        viewHolder.expense_txt.setVisibility(View.VISIBLE);
                        if(objectsEntity.expense.pay_type==1){
                            viewHolder.expense_txt.setBackgroundResource(R.drawable.list_single_buy);
                        }else if((objectsEntity.expense.cpname).startsWith("ismar")){
                            viewHolder.expense_txt.setBackgroundResource(R.drawable.list_ismar);
                        }else if("iqiyi".equals(objectsEntity.expense.cpname)){
                            viewHolder.expense_txt.setBackgroundResource(R.drawable.list_lizhi);
                        }
                    } else {
                        viewHolder.expense_txt.setVisibility(View.GONE);
                    }

                }
                Picasso.with(mContext)
                        .load(objectsEntity.adlet_url)
                        .memoryPolicy(MemoryPolicy.NO_STORE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .error(R.drawable.vertical_preview_bg)
                        .placeholder(R.drawable.vertical_preview_bg)
                        .transform(mTransformation)
                        .into(viewHolder.iv_poster);
            }

        }

        viewHolder.poster_title.setText(objectsEntity.title);
        viewHolder.tv_focus.setText(objectsEntity.focus);
        return convertView;

    }

    public static class ViewHolder {
        ImageView iv_poster;
        TextView ItemBeanScore;
        RotateTextView expense_txt;
        TextView poster_title;
        TextView tv_focus;
        ItemContainer item_container;
    }

}
