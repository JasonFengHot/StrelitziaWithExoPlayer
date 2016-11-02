package tv.ismar.app.player;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.ismartv.truetime.TrueTime;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.VodUserAgent;
import tv.ismar.app.entity.Clip;
import tv.ismar.app.entity.Item;
import tv.ismar.app.t.AccessProxy;

public class InitPlayerTool {
    Context mContext;
    Intent intent;
    private SimpleRestClient simpleRestClient;
    public final static String FLAG_URL = "url";
    public final static String FLAG_ITEM = "item";
    private boolean mIsPreviewVideo = false;
    private int price = 0;
    public String slug;
    public String channel;
    public boolean isSubitemPreview = false;
    public String fromPage="";
    private ItemByUrlTask urltask;
    private Item seraItem;
	private boolean isLiveVideoNotStart = false;
	public InitPlayerTool(Context context){
		this.mContext = context;
		intent = new Intent();
		simpleRestClient = new SimpleRestClient();
	}
	
	public void initClipInfo(Object item,String flag) {
		simpleRestClient = new SimpleRestClient();
		urltask = new ItemByUrlTask();
		urltask.execute(item,flag);
	}
	public void initClipInfo(Object item,String flag,boolean isPreviewVideo,Item seraiItem) {
		simpleRestClient = new SimpleRestClient();
		this.mIsPreviewVideo = isPreviewVideo;
		urltask = new ItemByUrlTask();
		urltask.execute(item,flag);
		this.seraItem = seraiItem;
	}
    public void initClipInfo(Object item,String flag,int price ) {
        simpleRestClient = new SimpleRestClient();
        this.price = price;
		urltask = new ItemByUrlTask();
		urltask.execute(item,flag);
    }
	private Item item = null;
	// 初始化播放地址url
	private class ItemByUrlTask extends AsyncTask<Object, Void, String> {

		@Override
		protected void onPostExecute(String result) {
			if(isLiveVideoNotStart){
				Toast.makeText(mContext, "直播节目还未开始！", Toast.LENGTH_SHORT).show();
				isLiveVideoNotStart = false;
				return;
			}
			if(result.equals("iqiyi")){
				intent.setAction("tv.ismar.daisy.qiyiPlay");
				String info = AccessProxy.getvVideoClipInfo();
				intent.putExtra("iqiyi", info);		
			}
			else{
				String ismartv = AccessProxy.getvVideoClipInfo();
				intent.setAction("tv.ismar.daisy.Play");
				intent.putExtra("ismartv", ismartv);
			}
			if(!"".equals(result))
				if(!mIsPreviewVideo)
				   mContext.startActivity(intent);
				else
			       ((Activity)mContext).startActivityForResult(intent, 20);
			if(mListener!=null)
				mListener.onPostExecute();	
		}
		@Override
		protected String doInBackground(Object... params) {

			String sn = VodUserAgent.getMACAddress();
            AccessProxy.init(VodUserAgent.getModelName(),
                    ""+SimpleRestClient.appVersion, SimpleRestClient.sn_token);
            String flag = (String) params[1];
            if(flag.equals("url")){
					if(params[0]!=null)

					((BaseActivity)mContext).mSkyService.apifetchItem((String) params[0]).subscribeOn(Schedulers.io())
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(((BaseActivity)mContext).new BaseObserver<Item>() {
								@Override
								public void onCompleted() {

								}

								@Override
								public void onNext(Item mItem) {
									item=mItem;
								}
							});
            }
            else{
            	item = (Item) params[0];
            }
            if(seraItem != null){
            	intent.putExtra("seraItem", seraItem);
            }
            String info = "";
            if(item!=null){
            	Clip clip;
            	if(!mIsPreviewVideo)
            	    clip = item.clip;
            	else{
                    if(isSubitemPreview){
                        clip = item.clip;
                    }
                    else{
						if(item.preview != null)
                        clip = item.preview;
						else
						clip = item.clip;
                    }
            		item.isPreview = true;
            	}
            	if(item.clip != null&&clip!=null){
                    item.channel = channel;
                    item.slug = slug;
                    if(fromPage!=null&&!fromPage.equals(""))
                        item.fromPage = fromPage;
                	intent.putExtra("item", item);
    				info = AccessProxy.getVideoInfo(SimpleRestClient.root_url
    						+ "/api/clip/" + clip.pk + "/",
    						VodUserAgent.getAccessToken(sn));
            	}
				if(item.live_video){
					String startTime = item.start_time;
					if(!TextUtils.isEmpty(startTime) && !startTime.equalsIgnoreCase("null")){
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						try {
							Date date = sdf.parse(startTime);
							long startMillisecond = date.getTime();
							long nowTime = TrueTime.now().getTime();
							if(nowTime < startMillisecond && startMillisecond - nowTime > 10000 * 60){
								isLiveVideoNotStart = true;
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
				}
            }
			return info;

		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			//mLoadingDialog.show();
			if(mListener!=null)
			   mListener.onPreExecute(intent);
		}
	}
	public interface onAsyncTaskHandler{
		public void onPostExecute();
		public void onPreExecute(Intent intent);
	}
	private onAsyncTaskHandler mListener;
	
	public void removeAsycCallback(){
		if(urltask != null && urltask.getStatus()!=AsyncTask.Status.FINISHED && !urltask.isCancelled())
		urltask.cancel(true);
		urltask = null;
	}

	public void setonAsyncTaskListener(onAsyncTaskHandler l){
		this.mListener = l;
	}
}
