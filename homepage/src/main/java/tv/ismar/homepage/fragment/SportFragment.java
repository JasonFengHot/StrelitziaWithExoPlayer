package tv.ismar.homepage.fragment;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

//import org.apache.commons.lang3.StringUtils;

import com.blankj.utilcode.utils.StringUtils;

import java.util.ArrayList;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.AppConstant;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.Source;
import tv.ismar.app.entity.HomePagerEntity;
import tv.ismar.app.entity.HomePagerEntity.Carousel;
import tv.ismar.app.entity.HomePagerEntity.Poster;
import tv.ismar.app.models.Game;
import tv.ismar.app.models.Sport;
import tv.ismar.app.models.SportGame;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.app.player.InitPlayerTool;
import tv.ismar.app.util.PicassoUtils;
import tv.ismar.app.util.Utils;
import tv.ismar.homepage.R;
import tv.ismar.homepage.view.HomePageActivity;
import tv.ismar.homepage.widget.HomeItemContainer;
import tv.ismar.homepage.widget.LabelImageView3;

/**
 * Created by huaijie on 5/18/15.
 */
public class SportFragment extends ChannelBaseFragment {
    private static final String TAG = "SportFragment";

    private final int IMAGE_SWITCH_KEY = 0X11;
    private ArrayList<SportGame> games;
    private LabelImageView3 sport_card1;
    private LabelImageView3 sport_card2;
    private LabelImageView3 sport_card3;
    private LabelImageView3 sportspost;
    private LabelImageView3 sports_live1;
    private LabelImageView3 sports_live2;
    private LabelImageView3 sports_live3;
    private LabelImageView3 sport_channel1_image;
    private TextView sport_channel1_subtitle;
    private LabelImageView3 sport_channel2_image;
    private TextView sport_channel2_subtitle;
    private LabelImageView3 sport_channel3_image;
    private TextView sport_channel3_subtitle;
    private LabelImageView3 sport_channel4_image;
    private TextView sport_channel4_subtitle;
    private HomeItemContainer sport_channel5;
    private ImageView arrowUp;
    private ImageView arrowDown;
    private ArrayList<Carousel> looppost = new ArrayList<Carousel>();
    private int loopindex = -1;
    private int currentLiveIndex = 0;
    private InitPlayerTool tool;
    private Subscription dataSubscription;
    private Subscription sportSubscription;
    private Subscription gameSubscription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sport, null);
        sport_card1 = (LabelImageView3) view.findViewById(R.id.sport_card1);
        sport_card2 = (LabelImageView3) view.findViewById(R.id.sport_card2);
        sport_card3 = (LabelImageView3) view.findViewById(R.id.sport_card3);
        sportspost = (LabelImageView3) view.findViewById(R.id.sportspost);
        sport_channel1_image = (LabelImageView3) view
                .findViewById(R.id.sport_channel1_image);
        sport_channel1_subtitle = (TextView) view
                .findViewById(R.id.sport_channel1_subtitle);
        sport_channel2_image = (LabelImageView3) view
                .findViewById(R.id.sport_channel2_image);
        sport_channel2_subtitle = (TextView) view
                .findViewById(R.id.sport_channel2_subtitle);
        sport_channel3_image = (LabelImageView3) view
                .findViewById(R.id.sport_channel3_image);
        sport_channel3_subtitle = (TextView) view
                .findViewById(R.id.sport_channel3_subtitle);
        sport_channel4_image = (LabelImageView3) view
                .findViewById(R.id.sport_channel4_image);
        sport_channel4_subtitle = (TextView) view
                .findViewById(R.id.sport_channel4_subtitle);
        sport_channel5 = (HomeItemContainer) view
                .findViewById(R.id.listmore);
        arrowUp = (ImageView) view.findViewById(R.id.sec_one_list_1_arrowup);
        arrowDown = (ImageView) view
                .findViewById(R.id.sec_one_list_1_arrowdown);
        sports_live1 = (LabelImageView3) view.findViewById(R.id.sports_live1);
        sports_live2 = (LabelImageView3) view.findViewById(R.id.sports_live2);
        sports_live3 = (LabelImageView3) view.findViewById(R.id.sports_live3);
        arrowUp.setOnHoverListener(onHoverListener);
        arrowDown.setOnHoverListener(onHoverListener);
        arrowUp.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
                if (games.size() == 6 && currentLiveIndex == 3) {
                    currentLiveIndex -= 3;
                } else {
                    currentLiveIndex -= 1;
                }
                Message msg = new Message();
                msg.arg1 = 1;
                msg.what = 0;
                test.sendMessage(msg);
			}
		});

        arrowDown.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				 if (games.size() == 6) {
                     currentLiveIndex += 3;
                 } else {
                     currentLiveIndex += 1;
                 }
                 Message msg = new Message();
                 msg.arg1 = 1;
                 msg.what = 0;
                 test.sendMessage(msg);
			}
		});
        sports_live1.setTag(R.id.view_position_tag, 3);
        sports_live1.setOnClickListener(arrowClickListener);
        sports_live1.setTag(R.id.view_position_tag, 5);
        sports_live2.setOnClickListener(arrowClickListener);
        sports_live1.setTag(R.id.view_position_tag, 7);
        sports_live3.setOnClickListener(arrowClickListener);
        sports_live1.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                if (!arg1 && arrowUp.isFocused() && !arrowUp.isHovered()) {
                    if (games.size() == 6 && currentLiveIndex == 3) {
                        currentLiveIndex -= 3;
                    } else {
                        currentLiveIndex -= 1;
                    }
                    Message msg = new Message();
                    msg.arg1 = 1;
                    msg.what = 0;
                    test.sendMessage(msg);
                }
                if (arg1) {
                    ((HomePageActivity) (getActivity())).setLastViewTag("");
                }
            }
        });

        sports_live2.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                if (arg1) {
                    ((HomePageActivity) (getActivity())).setLastViewTag("");
                }
            }
        });

        sports_live3.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                if (!arg1 && arrowDown.isFocused()&& !arrowDown.isHovered()) {
                    if (games.size() == 6) {
                        currentLiveIndex += 3;
                    } else {
                        currentLiveIndex += 1;
                    }
                    Message msg = new Message();
                    msg.arg1 = 1;
                    msg.what = 0;
                    test.sendMessage(msg);
                }
                if (arg1) {
                    arrowDown.setFocusable(true);
                    ((HomePageActivity) (getActivity())).setLastViewTag("");
                }
            }
        });
        sport_channel1_image.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                if (arg1) {
                    ((HomePageActivity) (getActivity())).setLastViewTag("bottom");
                }
            }
        });
        sport_channel5.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                if (arg1) {
                    ((HomePageActivity) (getActivity())).setLastViewTag("bottom");
                    arrowDown.setFocusable(false);
                }
            }
        });

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(tool != null)
        	tool.removeAsycCallback();
        imageswitch.removeMessages(IMAGE_SWITCH_KEY);
        test.removeMessages(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dataSubscription != null && !dataSubscription.isUnsubscribed()) {
            dataSubscription.unsubscribe();
        }
        if (sportSubscription != null && !sportSubscription.isUnsubscribed()) {
            sportSubscription.unsubscribe();
        }
        if (gameSubscription != null && !gameSubscription.isUnsubscribed()) {
            gameSubscription.unsubscribe();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        games = new ArrayList<SportGame>();
        if(channelEntity != null) {
            fetchSportGame(channelEntity.getHomepage_url());
        }
    }
    private void fetchSportGame(String url) {
        if (dataSubscription != null && !dataSubscription.isUnsubscribed()) {
            dataSubscription.unsubscribe();
        }
        dataSubscription = ((HomePageActivity) getActivity()).mSkyService.fetchHomePage(url).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(((HomePageActivity) getActivity()).new BaseObserver<HomePagerEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(HomePagerEntity homePagerEntity) {
                        if (homePagerEntity == null) {
                            new CallaPlay().exception_except("launcher", "launcher", channelEntity.getChannel(),
                                    "", 0, channelEntity.getHomepage_url(),
                                    SimpleRestClient.appVersion, "data", ""
                            );
                            super.onError(new Exception("数据异常"));
                            return;
                        }
                        ArrayList<Poster> postlist = homePagerEntity.getPosters();
                        ArrayList<Carousel> carousels = homePagerEntity.getCarousels();
                        BaseActivity.baseChannel = channelEntity.getChannel();
                        if("sport".equals(channelEntity.getChannel())) {
                            getSport();
                        }else if("game".equals(channelEntity.getChannel())){
                            getGame();
                        }
                        fillData(carousels, postlist);
                    }
                });
    }

       private void getSport() {
           if (sportSubscription != null && !sportSubscription.isUnsubscribed()) {
               sportSubscription.unsubscribe();
           }
           sportSubscription = ((HomePageActivity) getActivity()).mSkyService.apiSport().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(((HomePageActivity) getActivity()).new BaseObserver<Sport>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(Sport sport) {
                        if (sport == null) {
                            new CallaPlay().exception_except("launcher", "launcher", channelEntity.getChannel(),
                                    "", 0, "api/tv/living_video/sport/",
                                    SimpleRestClient.appVersion, "data", ""
                            );
                            super.onError(new Exception("数据异常"));
                            return;
                        }
                        games.clear();
                        games.addAll(sport.getLiving());
                        fillLiveData();
                    }
                });
    }

    private void getGame() {
        if (gameSubscription != null && !gameSubscription.isUnsubscribed()) {
            gameSubscription.unsubscribe();
        }
        gameSubscription = ((HomePageActivity) getActivity()).mSkyService.apiGame().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(((HomePageActivity) getActivity()).new BaseObserver<Game>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(Game game) {
                        if (game == null) {
                            new CallaPlay().exception_except("launcher", "launcher", channelEntity.getChannel(),
                                    "", 0, "api/tv/living_video/game/",
                                    SimpleRestClient.appVersion, "data", ""
                            );
                            super.onError(new Exception("数据异常"));
                            return;
                        }
                        games.clear();
                        games.addAll(game.getHighlight());
                        games.addAll(game.getLiving());
                        fillLiveData();
                    }
                });
    }

    private OnCancelListener mLoadingCancelListener = new OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            getActivity().finish();
            dialog.dismiss();
        }
    };

    private void fillData(ArrayList<Carousel> carousels, ArrayList<Poster> postlist) {
    	looppost.clear();
        LabelImageView3[] sportCards = {sport_card1, sport_card2, sport_card3};
        for (int i = 0; i < 3; i++) {
            PicassoUtils.load(mContext, carousels.get(i).getThumb_image(), sportCards[i]);
            carousels.get(i).setPosition(i);
            sportCards[i].setTag(R.drawable.launcher_selector, carousels.get(i));
            sportCards[i].setOnFocusChangeListener(ItemOnFocusListener);
            sportCards[i].setOnClickListener(ItemClickListener);
            looppost.add(carousels.get(i));

            if (i == 0){
                sportCards[i].setTag(R.id.view_position_tag, 1);
            }else if (i ==1){
                sportCards[i].setTag(R.id.view_position_tag, 4);
            }else if (i ==2){
                sportCards[i].setTag(R.id.view_position_tag, 6);
            }

        }
        imageswitch.sendEmptyMessage(IMAGE_SWITCH_KEY);

        LabelImageView3[] sportChannelImages = {sport_channel1_image, sport_channel2_image, sport_channel3_image, sport_channel4_image};
        TextView[] sportChannleSubtitles = {sport_channel1_subtitle, sport_channel2_subtitle, sport_channel3_subtitle, sport_channel4_subtitle};
        for (int i = 0; i < 4; i++) {
        	postlist.get(i).setPosition(i);
            PicassoUtils.load(mContext, postlist.get(i).getCustom_image(), sportChannelImages[i]);
            sportChannelImages[i].setTitle(postlist.get(i).getIntroduction());
            sportChannleSubtitles[i].setText(postlist.get(i).getTitle());
            sportChannelImages[i].setTag(postlist.get(i));
            sportChannelImages[i].setOnClickListener(ItemClickListener);

            if (i == 0){
                sportChannelImages[i].setTag(R.id.view_position_tag, 8);
            }else if (i ==1){
                sportChannelImages[i].setTag(R.id.view_position_tag, 9);
            }else if (i == 2){
                sportChannelImages[i].setTag(R.id.view_position_tag, 10);
            }else if (i == 3){
                sportChannelImages[i].setTag(R.id.view_position_tag, 11);
            }

        }

        sport_channel5.setOnClickListener(ItemClickListener);
        sportspost.setOnClickListener(ItemClickListener);
        sportspost.setTag(R.id.view_position_tag, 2);

        sportspost.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    getActivity().findViewById(R.id.sport_lefttop).bringToFront();
                    getActivity().findViewById(R.id.sport_right_top_layout).bringToFront();
                }
            }
        });

        if (scrollFromBorder) {
            if (isRight) {//右侧移入
                if ("bottom".equals(bottomFlag)) {//下边界移入
                    sport_channel5.requestFocus();
                } else {//上边界边界移入
                    sports_live1.requestFocus();
                }
//	        		}
            } else {//左侧移入
                if ("bottom".equals(bottomFlag)) {
                    sport_channel1_image.requestFocus();
                } else {
                    sport_card1.requestFocus();
                }
//	        	}
            }
            ((HomePageActivity) getActivity()).resetBorderFocus();
        }
    }

    private View.OnFocusChangeListener ItemOnFocusListener = new View.OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                ((HomePageActivity) (getActivity())).setLastViewTag("");
                Carousel carousel = (Carousel) v.getTag(R.drawable.launcher_selector);
                PicassoUtils.load(mContext, carousel.getVideo_image(), sportspost);

                sportspost.setTag(R.drawable.launcher_selector, carousel);
                if (!StringUtils.isEmpty(carousel.getIntroduction())) {
                    sportspost.setTitle(carousel.getIntroduction());
                } else {
                    sportspost.setTitle(null);
                }
                int i = v.getId();
                if (i == R.id.sport_card1) {
                    sport_card1.setCustomFocus(true);
                    sport_card2.setCustomFocus(false);
                    sport_card3.setCustomFocus(false);
                    loopindex = -1;

                } else if (i == R.id.sport_card2) {
                    sport_card1.setCustomFocus(false);
                    sport_card2.setCustomFocus(true);
                    sport_card3.setCustomFocus(false);
                    loopindex = 0;

                } else if (i == R.id.sport_card3) {
                    sport_card1.setCustomFocus(false);
                    sport_card2.setCustomFocus(false);
                    sport_card3.setCustomFocus(true);
                    loopindex = 1;


                } else {
                }
                imageswitch.removeMessages(IMAGE_SWITCH_KEY);
            } else {
                imageswitch.sendEmptyMessageDelayed(IMAGE_SWITCH_KEY, 6000);
            }
        }
    };

    private Handler imageswitch = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(looppost.size() < 3)
            	return;
            PicassoUtils.load(mContext, looppost.get(++loopindex).getVideo_image(), sportspost);
            sportspost.setTag(R.drawable.launcher_selector,
                    looppost.get(loopindex));
            if (!StringUtils.isEmpty(looppost.get(loopindex)
                    .getIntroduction())) {
                sportspost.setTitle(looppost.get(loopindex)
                        .getIntroduction());
            } else {
                sportspost.setTitle(null);
            }
            if (loopindex == 0) {
                sport_card1.setCustomFocus(true);
                sport_card2.setCustomFocus(false);
                sport_card3.setCustomFocus(false);
            } else if (loopindex == 1) {
                sport_card1.setCustomFocus(false);
                sport_card2.setCustomFocus(true);
                sport_card3.setCustomFocus(false);
            } else if (loopindex == 2) {
                sport_card1.setCustomFocus(false);
                sport_card2.setCustomFocus(false);
                sport_card3.setCustomFocus(true);
            }
            if (loopindex >= 2)
                loopindex = -1;
            imageswitch.sendEmptyMessageDelayed(IMAGE_SWITCH_KEY, 6000);
            // pendingView.requestFocus();
        }
    };

    private View.OnClickListener arrowClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            Object o = arg0.getTag(R.id.view_position_tag);
            if (o !=null){
                int viewPosition = (int) o;
                AppConstant.purchase_tab = String.valueOf(viewPosition);
            }

            SportGame data = (SportGame) arg0.getTag();
            if(data == null)
            	return;
            if (data.is_complex()) {
                int pk = SimpleRestClient.getItemId(data.getUrl(), new boolean[1]);
                PageIntent pageIntent = new PageIntent();
                pageIntent.toDetailPage(mContext, "homepage", pk);

                CallaPlay play = new CallaPlay();
                play.homepage_vod_click(pk, data.getTitle(), BaseActivity.baseChannel, data.getPosition(), data.getModel_name());
//                Intent intent = new Intent();
//                intent.setAction("tv.ismar.daisy.Item");
//                intent.putExtra("url", data.getUrl());
//                intent.putExtra("channel", channelEntity.getChannel());
//                intent.putExtra("fromPage","homepage");
//                mContext.startActivity(intent);
            } else {
                int itemPk = Utils.getItemPk(data.getUrl());
                PageIntent pageIntent = new PageIntent();
                pageIntent.toPlayPage(mContext, itemPk, -1, Source.HOMEPAGE);

                CallaPlay play = new CallaPlay();
                play.homepage_vod_click(itemPk, data.getTitle(), BaseActivity.baseChannel, data.getPosition(), data.getModel_name());
//                tool = new InitPlayerTool(mContext);
//                tool.channel=channelEntity.getChannel();
//                tool.fromPage="homepage";
//                tool.initClipInfo(data.getUrl(),
//                        InitPlayerTool.FLAG_URL);
            }
        }
    };

    private View.OnFocusChangeListener arrowFocusChangeListener = new View.OnFocusChangeListener() {

        @Override
        public void onFocusChange(View arg0, boolean arg1) {
            arg0.bringToFront();
        }
    };

    private void fillLiveData() {
        int index = 0;

        LabelImageView3[] sportLives = {sports_live1, sports_live2, sports_live3};
		if (currentLiveIndex < 0)
			currentLiveIndex = 0;
        for (int position = currentLiveIndex; position < games.size(); position++) {
//            PicassoUtils.load(mContext, games.get(position).getImageurl(), sportLives[position]);
            switch (index++) {
                case 0:
                    PicassoUtils.load(mContext, games.get(position).getPoster_url(), sports_live1);
                    sports_live1.setTag(games.get(position));
                    if (games.get(position).is_complex()) {
                        sports_live1.setModeType(4);
                    } else {
                        sports_live1.setModeType(6);
                    }
                    sports_live1.setTitle(games.get(position).getName());
                    sports_live1.setTag(R.id.view_position_tag, 3);
                    break;
                case 1:
                    PicassoUtils.load(mContext, games.get(position).getPoster_url(), sports_live2);
                    sports_live2.setTag(games.get(position));
                    if (games.get(position).is_complex()) {
                        sports_live2.setModeType(4);
                    } else {
                        sports_live2.setModeType(6);
                    }
                    sports_live2.setTitle(games.get(position).getName());
                    sports_live1.setTag(R.id.view_position_tag, 5);
                    break;
                case 2:
                    PicassoUtils.load(mContext, games.get(position).getPoster_url(), sports_live3);

                    sports_live3.setTag(games.get(position));
                    if (games.get(position).is_complex()) {
                        sports_live3.setModeType(4);
                    } else {
                        sports_live3.setModeType(6);
                    }
                    sports_live3.setTitle(games.get(position).getName());
                    sports_live1.setTag(R.id.view_position_tag, 7);
                    break;
            }
        }

        if (games.size() - currentLiveIndex > 3) {
            arrowDown.setVisibility(View.VISIBLE);
            sports_live3.setNextFocusDownId(R.id.sec_one_list_1_arrowdown);
        } else {
            arrowDown.setVisibility(View.INVISIBLE);
            sports_live3.setNextFocusDownId(R.id.listmore);
        }
        if (currentLiveIndex > 0) {
            arrowUp.setVisibility(View.VISIBLE);
            sports_live1.setNextFocusUpId(R.id.sec_one_list_1_arrowup);
        } else {
            arrowUp.setVisibility(View.INVISIBLE);
            sports_live1.setNextFocusUpId(R.string.vod_movielist_title_history);
        }
        if (arrowDown.getVisibility() == View.VISIBLE) {
            arrowDown.bringToFront();
        }
        if (arrowUp.getVisibility() == View.VISIBLE) {
            arrowUp.bringToFront();
        }
    }

    private Handler test = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            fillLiveData();
            if (msg.arg1 == 1) {
                sports_live1.requestFocus();
            } else {
                sports_live3.requestFocus();
            }
        }

    };
    
    private View.OnHoverListener onHoverListener = new View.OnHoverListener() {

		@Override
		public boolean onHover(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_HOVER_ENTER:
			case MotionEvent.ACTION_HOVER_MOVE:
				v.setHovered(true);
				v.setFocusable(true);
				v.setFocusableInTouchMode(true);
				v.requestFocusFromTouch();
				break;
			case MotionEvent.ACTION_HOVER_EXIT:
				v.setHovered(false);
				break;
			}
			return false;
		}
	};

	  public void refreshData(){
		    imageswitch.removeCallbacksAndMessages(null);
            if(channelEntity != null)
              fetchSportGame(channelEntity.getHomepage_url());
	    }
}
