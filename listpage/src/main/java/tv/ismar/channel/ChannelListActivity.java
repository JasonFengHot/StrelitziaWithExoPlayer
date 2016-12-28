package tv.ismar.channel;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.VipMark;
import tv.ismar.app.ui.HGridView;
import tv.ismar.app.ui.HeadFragment;
import tv.ismar.app.util.BitmapDecoder;
import tv.ismar.app.widget.ItemOffLinePopWindow;
import tv.ismar.app.widget.ModuleMessagePopWindow;
import tv.ismar.listpage.R;

public class ChannelListActivity extends BaseActivity {
	
	private final static String TAG = "ChannelListActivity";
	
	private OnMenuToggleListener mOnMenuToggleListener;
    private ChannelFragment channelFragment;
    private View filter;
    private HGridView mHgridView;
    private BitmapDecoder bitmapDecoder;
	private HeadFragment headFragment;
	private FrameLayout head;
	private VipMark dip;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.channel_layout);
        final View vv = findViewById(R.id.large_layout);
        bitmapDecoder = new BitmapDecoder();
        bitmapDecoder.decode(this, R.drawable.main_bg, new BitmapDecoder.Callback() {
            @Override
            public void onSuccess(BitmapDrawable bitmapDrawable) {
                vv.setBackgroundDrawable(bitmapDrawable);
            }
        });
		head= (FrameLayout) findViewById(R.id.head_layout);
		dip=VipMark.getInstance();
		Intent intent = getIntent();
		String title = null;
		String url = null;
		String channel = null;
		int portraitflag =1;
		if(intent!=null){
			Bundle bundle = intent.getExtras();
			if(bundle!=null){
				url =bundle.getString("url");
				
				title = bundle.getString("title");
				
				channel = bundle.getString("channel");
				portraitflag = bundle.getInt("portraitflag");
			}else{
				url =intent.getStringExtra("url");

				title = intent.getStringExtra("title");

				channel = intent.getStringExtra("channel");
				portraitflag = intent.getIntExtra("portraitflag",0);
			}
		}
		if(url==null) {
			//url = "http://cord.tvxio.com/api/tv/sections/chinesemovie/";
		//	url = SimpleRestClient.root_url+"/api/tv/sections/chinesemovie/";
		//	url = "http://cord.tvxio.com/api/live/channel/movie/";
		}else if(url.startsWith("/api/tv/sections/")){
				url= "http://"+IsmartvActivator.getInstance().getApiDomain()+url;
		}else if(url.startsWith("/api/tv/section/")){
			url="http://"+IsmartvActivator.getInstance().getApiDomain()+"/api/tv/sections/"+channel;
		}
		if(title==null) {
			title = "华语电影";
		}
		if(channel==null) {
			channel = "$histories_dd";
		}
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		if(channel!=null) {
			if(channel.equals("$bookmarks")) {
				head.setVisibility(View.GONE);
				Fragment favoriteFragment = new FavoriteFragment();
				fragmentTransaction.add(R.id.fragment_container, favoriteFragment);
			} else if(channel.equals("histories")) {
				head.setVisibility(View.GONE);
				Fragment historyFragment = new HistoryFragment();
				fragmentTransaction.add(R.id.fragment_container, historyFragment);
			} 
			else if(channel.equals("search")){
//				Intent searchIntent = new Intent();
//				searchIntent.setClass(ChannelListActivity.this, SearchActivity.class);
//				startActivity(searchIntent);
//				finish();
			}
			else {
				head.setVisibility(View.VISIBLE);
				addHead(title);
				channelFragment = new ChannelFragment();
				if(1 == portraitflag){
                channelFragment.setIsPOrtrait(false);
				}else if(2 == portraitflag){
					channelFragment.setIsPOrtrait(true);					
				}
				channelFragment.mChannel = channel;
				channelFragment.mTitle = title;  //chinesemovie
             
                channelFragment.mUrl = url;
				fragmentTransaction.add(R.id.fragment_container, channelFragment);
			}
			fragmentTransaction.commit();
		}
		//DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(), this);
	}
	public void addHead(String title){
		headFragment = new HeadFragment();
		Bundle bundle = new Bundle();
		bundle.putString("type", HeadFragment.HEADER_LISTPAGE);
		bundle.putString("channel_name", title);
		headFragment = new HeadFragment();
		headFragment.setArguments(bundle);
		getSupportFragmentManager().beginTransaction().add(R.id.head_layout, headFragment).commit();
	}

	 @Override
	    protected void onNewIntent(Intent intent) {
	        super.onNewIntent(intent);
	        setIntent(intent);
	        setContentView(R.layout.channel_layout);
			String title = null;
			String url = null;
			String channel = null;
			int portraitflag =1;
			if(intent!=null){
				Bundle bundle = intent.getExtras();
				if(bundle!=null){
					url =bundle.getString("url");
					
					title = bundle.getString("title");
					
					channel = bundle.getString("channel");
					portraitflag = bundle.getInt("portraitflag");
				}
			}
			if(url==null) {
				//url = "http://cord.tvxio.com/api/tv/sections/chinesemovie/";
			//	url = SimpleRestClient.root_url+"/api/tv/sections/chinesemovie/";
			//	url = "http://cord.tvxio.com/api/live/channel/movie/";
			}
			if(title==null) {
				title = "华语电影";
			}
			if(channel==null) {
				channel = "$histories_dd";
			}
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			if(channel!=null) {
				if(channel.equals("$bookmarks")) {
					Fragment favoriteFragment = new FavoriteFragment();
					fragmentTransaction.add(R.id.fragment_container, favoriteFragment);
				} else if(channel.equals("histories")) {
					Fragment historyFragment = new HistoryFragment();
					fragmentTransaction.add(R.id.fragment_container, historyFragment);
				} 
				else if(channel.equals("search")){
//					Intent searchIntent = new Intent();
//					searchIntent.setClass(ChannelListActivity.this, SearchActivity.class);
//					startActivity(searchIntent);
//					finish();
				}
				else {
					channelFragment = new ChannelFragment();
					if(1 == portraitflag){
	                channelFragment.setIsPOrtrait(false);
					}else if(2 == portraitflag){
						channelFragment.setIsPOrtrait(true);				
					}
					channelFragment.mChannel = channel;
					channelFragment.mTitle = title;  //chinesemovie
	             
	                channelFragment.mUrl = url;
					fragmentTransaction.add(R.id.fragment_container, channelFragment);
				}
				fragmentTransaction.commit();
			}
			
		//	DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(), this);
	 }
	@Override
	protected void onDestroy() {
//		System.exit(0);
		if(bitmapDecoder != null && bitmapDecoder.isAlive())
			bitmapDecoder.interrupt();
	//	DaisyUtils.getVodApplication(this).removeActivtyFromPool(this.toString());
		super.onDestroy();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_MENU) {
			if(mOnMenuToggleListener!=null) {
				mOnMenuToggleListener.OnMenuToggle();
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

    public void registerOnMenuToggleListener(OnMenuToggleListener listener) {
		mOnMenuToggleListener = listener;
	}
	
	public void unregisterOnMenuToggleListener() {
		mOnMenuToggleListener = null;
	}
	
	public interface OnMenuToggleListener {
		public void OnMenuToggle();
	}
	public  int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
}
