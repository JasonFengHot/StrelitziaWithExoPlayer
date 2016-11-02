package tv.ismar.app;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

//import com.konka.android.media.KKMediaPlayer;

import java.io.InputStream;

import cn.ismartv.truetime.TrueTime;
import tv.ismar.app.core.DaisyUtils;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.app.player.ISTVVodMenu;
import tv.ismar.app.widget.AsyncImageView;
import tv.ismar.app.widget.CustomDialog;

public abstract class VodMenuAction extends BaseActivity {
	static final int MSG_SEK_ACTION = 103;
	static final int BUFFER_COUNTDOWN_ACTION = 113;
	static final int DISMISS_AD_DIALOG = 114;
	static final int AD_COUNT_ACTION = 115;
	static final int NETERROR = 116;
	protected int buffercountDown = 0;
	protected static final String BUFFERING = " 正在加载 ";
	Dialog dialog = null;
	private DialogInterface.OnClickListener mPositiveListener;
	private DialogInterface.OnClickListener mNegativeListener;
	protected boolean isBuffer = true;
	protected LinearLayout bufferLayout;
	protected long bufferDuration = 0;
	protected TextView bufferText;
	protected CallaPlay callaPlay = new CallaPlay();
    protected String section;
    protected String channel;
    protected String slug;
    protected String fromPage;
	protected boolean mounted = false;
	public abstract boolean onVodMenuClicked(ISTVVodMenu menu, int id);

	public abstract void onVodMenuClosed(ISTVVodMenu menu);

	public void showDialog(String str) {
		if (dialog == null) {
			mPositiveListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			};

			mNegativeListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					dialog.dismiss();
					VodMenuAction.this.finish();
				}
			};
			dialog = new CustomDialog.Builder(this).setMessage(str)
					.setPositiveButton(R.string.vod_cancel, mPositiveListener)
					.setNegativeButton(R.string.vod_ok, mNegativeListener)
					.create();
		}
		dialog.show();
	}




	class AdImageDialog extends Dialog {
		private int width;
		private int height;
		private String url;
        private int media_id;
        private long duration;
		private AsyncImageView zanting_image;
        private String title;
		private ImageView close_btn;


		public AdImageDialog(Context context, int theme, String imageurl,String title,int id ) {
			super(context, theme);
			WindowManager wm = (WindowManager) getContext().getSystemService(
					Context.WINDOW_SERVICE);
			width = wm.getDefaultDisplay().getWidth();
			height = wm.getDefaultDisplay().getHeight();
            this.title = title;
            this.media_id = id;
			url = imageurl;
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			this.setContentView(R.layout.zantingguanggao);
			zanting_image = (AsyncImageView) this.findViewById(R.id.zantingguanggao);
			close_btn = (ImageView) this.findViewById(R.id.zanting_close);
			zanting_image.setOnImageViewLoadListener(new AsyncImageView.OnImageViewLoadListener() {

				@Override
				public void onLoadingStarted(AsyncImageView imageView) {
					callaPlay.pause_ad_download(title, media_id, url, "bestv");
				}

				@Override
				public void onLoadingFailed(AsyncImageView imageView, Throwable throwable) {
//					callaPlay.pause_ad_except(throwable., errorContent);
				}

				@Override
				public void onLoadingEnded(AsyncImageView imageView, Bitmap image) {
					close_btn.setVisibility(View.VISIBLE);
					close_btn.requestFocus();
				}
			});
			zanting_image.setUrl(url);
			if(DaisyUtils.getImageCache(getContext()).get(url) != null){
				close_btn.setVisibility(View.VISIBLE);
				close_btn.requestFocus();
			}
            duration = TrueTime.now().getTime();
			resizeWindow();
		}

		private void resizeWindow() {
			Window dialogWindow = getWindow();
			WindowManager.LayoutParams lp = dialogWindow.getAttributes();
			lp.width = ((int) (width * 0.53));
			lp.height = ((int) (height * 0.53));
			lp.gravity = Gravity.CENTER;
			close_btn.requestFocus();
			close_btn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
		}

		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
				dismiss();
				return true;
			}
			return super.onKeyDown(keyCode, event);
		}

		@Override
		public void dismiss() {
			super.dismiss();
            duration = TrueTime.now().getTime()-duration;
            callaPlay.pause_ad_play(title,media_id,url,duration,"bestv");
//			resumeItem();
		}

		@Override
		public void onBackPressed() {
			super.onBackPressed();
		}

	};

	@Override
	public  void onResume(){
		super.onResume();
		IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_CHECKING);
		filter.setPriority(1000);
		filter.addDataScheme("file");
		registerReceiver(mountrecevicer, filter);
		try {
			Class.forName("com.konka.android.media.KKMediaPlayer");
//			KKMediaPlayer localKKMediaPlayer1 = new KKMediaPlayer();
//			KKMediaPlayer.setContext(this);
//			localKKMediaPlayer1.setAspectRatio(2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mountrecevicer);
	}
	 protected void setGesturebackground(View view,int id) {

	        BitmapFactory.Options opt = new BitmapFactory.Options();

	        opt.inPreferredConfig = Bitmap.Config.ALPHA_8;

	        opt.inPurgeable = true;

	        opt.inInputShareable = true;
	        opt.inTargetDensity = getResources().getDisplayMetrics().densityDpi;
	        opt.inDensity = getResources().getDisplayMetrics().densityDpi;

	        InputStream is = getResources().openRawResource(
	                id);

	        Bitmap bm = BitmapFactory.decodeStream(is, null, opt);

	        BitmapDrawable bd = new BitmapDrawable(getResources(), bm);
	        view.setBackgroundDrawable(bd);
	    }

	private BroadcastReceiver mountrecevicer = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)){
				mounted = true;
			}
		}
	};
}
