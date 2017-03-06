package tv.ismar.app.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
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

public class HGridAdapterImpl extends HGridAdapter<ItemCollection> implements AsyncImageView.OnImageViewLoadListener {
	
	private final static String TAG = "HGridAdapterImpl";
	
	private Context mContext;
	private boolean mHasSection = true;
	private int mSize = 0;
	public HGridView hg;
	private HashSet<AsyncImageView> mOnLoadingImageQueue = new HashSet<AsyncImageView>();
	private HashSet<RelativeLayout> mOnLoadinglayoutQueue = new HashSet<RelativeLayout>();
    private boolean isPortrait = false;
    private int template = 0;  //1 2
	private Transformation mTransformation = new ReflectionTransformationBuilder()
			.setIsHorizontal(true)
			.build();
	public void setTemplate(int flag){
		this.template = flag;
	}
    public void setIsPortrait(boolean isPortrait){
        this.isPortrait = isPortrait;
    }
	public HGridAdapterImpl(Context context, ArrayList<ItemCollection> list) {
		mContext = context;
		if(list != null && list.size()>0) {
			mList = list;
			for(int i=0;i < list.size(); i++) {
				mSize +=list.get(i).count;
			}
		}
		
	}
	public HGridAdapterImpl(Context context, ArrayList<ItemCollection> list,boolean hasSection) {
		mContext = context;
		if(list != null && list.size()>0) {
			mList = list;
			for(int i=0;i < list.size(); i++) {
				mSize +=list.get(i).count;
			}
		}
		this.mHasSection = hasSection;
	}
	@Override
	public void setList(ArrayList<ItemCollection> list) {
		mSize = 0;
		for(int i=0;i < list.size(); i++) {
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
		for(int i=0; i<mList.size(); i++) {
			final int sectionCount = mList.get(i).count;
			if(size +sectionCount > position) {
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
	public View getView( final int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if(convertView == null) {
            if(!this.isPortrait){
				if(this.template == 1)
				    convertView = LayoutInflater.from(mContext).inflate(R.layout.list_view_variety_no_month_item,null);
				else if(this.template == 2)
					convertView = LayoutInflater.from(mContext).inflate(R.layout.list_view_variety_month_item,null);
				else
				    convertView = LayoutInflater.from(mContext).inflate(R.layout.list_view_item,null);
			}
            else
                convertView = LayoutInflater.from(mContext).inflate(R.layout.list_portrait_item,null);
			holder = new Holder();
			holder.title = (TextView) convertView.findViewById(R.id.list_item_title);
			holder.previewImage = (LabelImageView) convertView.findViewById(R.id.list_item_preview_img);
			holder.qualityLabel = (ImageView) convertView.findViewById(R.id.list_item_quality_label);
			holder.listLayout = (RelativeLayout)convertView.findViewById(R.id.list_item_layout);
			holder.price = (ImageView) convertView.findViewById(R.id.expense_txt);
			holder.ItemBeanScore = (TextView)convertView.findViewById(R.id.ItemBeanScore);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
//		holder.previewImage.setOnFocusChangeListener(new OnFocusChangeListener() {
//			
//			@Override
//			public void onFocusChange(View v, boolean hasFocus) {
//				// TODO Auto-generated method stub
//				if(hasFocus){
//					v.setBackgroundColor(Color.BLUE);
//				}
//				else{
//					v.setBackgroundColor(Color.RED);
//				}
//			}
//		});
//		convertView.setOnGenericMotionListener(new OnGenericMotionListener() {
//			
//			@Override
//			public boolean onGenericMotion(View v, MotionEvent event) {
//				// TODO Auto-generated method stub
//				int what = event.getButtonState();
//				switch (what) {
//				case MotionEvent.ACTION_DOWN:
//
//					break;	
//				case MotionEvent.BUTTON_PRIMARY:
//			if(hg!=null){
//				hg.performItemClick(v, position, 0);
//			}
//					break;	
//				case MotionEvent.BUTTON_TERTIARY:
//
//					break;		
//				case MotionEvent.BUTTON_SECONDARY:
//					
//					break;	
//	
//				}
//				return false;
//			}
//		});
		int itemCount = 0;
		int sectionIndex = 0;
		int indexOfCurrentSection = 0;
		for(int i=0; i<mList.size(); i++) {
			final int sectionCount = mList.get(i).count;
			if(itemCount + sectionCount> position) {
				sectionIndex = i;
				indexOfCurrentSection = position - itemCount;
				break;
			}
			itemCount += sectionCount;
		}

		// This ItemCollection's currentIndex has been filled.
        if(mList.size()>0){
            if(mList.get(sectionIndex).isItemReady(indexOfCurrentSection)) {
                final Item item = mList.get(sectionIndex).objects.get(indexOfCurrentSection);
                if(item!=null){
                    if(item.expense!=null){
						if(item.expense.cptitle!=null){
							holder.price.setVisibility(View.VISIBLE);
							String imageUrl =  VipMark.getInstance().getImage((Activity) mContext, item.expense.pay_type,item.expense.cpid);
							Picasso.with(mContext).load(imageUrl).into(holder.price);
						}  else{
							holder.price.setVisibility(View.GONE);
						}

                    }
                    else{
                        holder.price.setVisibility(View.GONE);
                    }
						if (isPortrait) {
							if(item.list_url.equals("http://res.tvxio.bestv.com.cn/media/upload/20160321/36c8886fd5b4163ae48534a72ec3a555.png")){
								Picasso.with(mContext)
										.load(item.adlet_url)
										.error(R.drawable.list_item_ppreview_bg)
										.placeholder(R.drawable.list_item_ppreview_bg)
										.transform(mTransformation)
										.config(Bitmap.Config.RGB_565)
										.into(holder.previewImage);

							}else {
								holder.previewImage.setUrl(item.list_url);
							}
						} else {
							if(item.adlet_url.equals("http://res.tvxio.bestv.com.cn/media/upload/20160504/5eae6db53f065ff0269dfc71fb28a4ec.png")){
								Picasso.with(mContext)
										.load(item.list_url)
										.error(R.drawable.list_item_ppreview_bg)
										.placeholder(R.drawable.list_item_ppreview_bg)
										.transform(mTransformation)
										.config(Bitmap.Config.RGB_565)
										.into(holder.previewImage);
							}else {
								holder.previewImage.setUrl(item.adlet_url);
							}
						}
                    if(isPortrait){
                        if(item.focus!=null){
                            holder.previewImage.setFocustitle(item.focus);
                            Log.i("zhangjiqiangzxcvbnm","film focus=="+item.focus);
                        }

                        holder.title.setText(item.title);
                    }
                    else{
                        if(template==1||template==2){
                            if(item.focus!=null)
                                holder.previewImage.setFocustitle(item.focus);
                            holder.title.setText(item.subtitle);
                        }
                        else{
                            holder.previewImage.setFocustitle("");
                            holder.title.setText(item.title);
                        }

                    }
                    if(item.bean_score>0){
                        holder.ItemBeanScore.setVisibility(View.VISIBLE);
                        holder.ItemBeanScore.setText(item.bean_score+"");
                    }
                    else{
                        holder.ItemBeanScore.setVisibility(View.INVISIBLE);
                    }
//				if(item.quality==3) {
//					holder.qualityLabel.setImageResource(R.drawable.label_hd_small);
//				} else if(item.quality==4 || item.quality==5) {
//					holder.qualityLabel.setImageResource(R.drawable.label_uhd_small);
//				} else {
//					holder.qualityLabel.setImageDrawable(null);
//				}
                }
            } else {
                // This ItemCollection's currentIndex has not filled yet.
                // Show the default info.
                holder.title.setText(mContext.getResources().getString(R.string.onload));
                holder.previewImage.setUrl(null);
                //holder.qualityLabel.setImageDrawable(null);
                holder.price.setVisibility(View.GONE);
            }
        }

		return convertView;
	}

		static class Holder {
		LabelImageView previewImage;
		TextView title;
		ImageView price;
		ImageView qualityLabel;
		RelativeLayout listLayout;
		TextView ItemBeanScore;
	}

	@Override
	public int getSectionIndex(int position) {
		int size = 0;
		for(int i=0; i<mList.size(); i++) {
			size += mList.get(i).count;
			if(size > position) {
				return i;
			}
		}
		return 0;
	}
	
	@Override
	public boolean hasSection() {
		if(this.mHasSection)
		    return true;
		else
			return false;
	}

	@Override
	public int getSectionCount(int sectionIndex) {
        if(mList.size()>0)
		   return mList.get(sectionIndex).count;
        else
            return 0;
	}

	@Override
	public String getLabelText(int sectionIndex) {
		if(this.mHasSection)
		    return mList.get(sectionIndex).title;
		else
			return " ";
	}
	
	public void cancel() {
		for(AsyncImageView imageView: mOnLoadingImageQueue) {

			imageView.stopLoading();
            if(imageView.getmBitmap()!=null){
                imageView.getmBitmap().recycle();
                imageView.setmBitmap();
            }
		}
		mOnLoadingImageQueue.clear();
       // DaisyUtils.getImageCache(mContext).flush();
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
}
