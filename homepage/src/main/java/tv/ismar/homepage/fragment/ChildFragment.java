package tv.ismar.homepage.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.entity.HomePagerEntity;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.homepage.R;
import tv.ismar.homepage.view.HomePageActivity;
import tv.ismar.homepage.widget.ChildThumbImageView;
import tv.ismar.homepage.widget.LabelImageView3;

/**
 * Created by huaijie on 5/18/15.
 */
@SuppressWarnings("ResourceType")
public class ChildFragment extends ChannelBaseFragment implements Flag.ChangeCallback {
    private static final String TAG = "ChildFragment";

    private LinearLayout leftLayout;
    private LinearLayout bottomLayout;
    private LinearLayout rightLayout;
    private ImageView imageSwitcher;
    private LabelImageView3 image_switcher_focus;
    private ChildThumbImageView[] indicatorImgs;
    private TextView indicatorTitle;

    private ImageButton childMore;

    private boolean focusFlag = true;

    private Flag flag;

    private ArrayList<HomePagerEntity.Carousel> carousels;

    private MessageHandler messageHandler;
    private View lefttop;
    private View leftBottom;
    private View righttop;
    private Subscription dataSubscription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_child, null);
        leftLayout = (LinearLayout) mView.findViewById(R.id.left_layout);
        bottomLayout = (LinearLayout) mView.findViewById(R.id.bottom_layout);
        rightLayout = (LinearLayout) mView.findViewById(R.id.right_layout);
        image_switcher_focus = (LabelImageView3) mView.findViewById(R.id.image_switcher_focus);
        imageSwitcher = (ImageView) mView.findViewById(R.id.image_switcher);
        imageSwitcher.setTag(R.id.view_position_tag, 8);
        indicatorImgs = new ChildThumbImageView[]{
                (ChildThumbImageView) mView.findViewById(R.id.indicator_1),
                (ChildThumbImageView) mView.findViewById(R.id.indicator_2),
                (ChildThumbImageView) mView.findViewById(R.id.indicator_3)
        };
        indicatorTitle = (TextView) mView.findViewById(R.id.indicator_title);
        childMore = (ImageButton) mView.findViewById(R.id.child_more);
        childMore.setOnClickListener(ItemClickListener);
        image_switcher_focus.setOnClickListener(ItemClickListener);
        image_switcher_focus.setOnHoverListener(new View.OnHoverListener() {
			
			@Override
			public boolean onHover(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER
						|| event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                      v.requestFocus();
				}
				return false;
			}
		});
        flag = new Flag(this);
        messageHandler = new MessageHandler();
        childMore.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				if (arg1) {
					((HomePageActivity) (getActivity())).setLastViewTag("bottom");
				}
			}
		});
        childMore.setOnHoverListener(new View.OnHoverListener() {

            @Override
            public boolean onHover(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER
                        || event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                    v.requestFocus();
                }
                return false;
            }
        });
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    	if(channelEntity != null)
        fetchChild(channelEntity.getHomepage_url());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dataSubscription != null && !dataSubscription.isUnsubscribed()) {
            dataSubscription.unsubscribe();
        }
    }

    @Override
    public void onDestroyView() {
    	leftLayout.removeAllViews();
    	bottomLayout.removeAllViews();
    	rightLayout.removeAllViews();
    	leftLayout = null;
    	bottomLayout = null;
    	rightLayout = null;
        super.onDestroyView();

    }

    private void fetchChild(String url) {
        dataSubscription = ((HomePageActivity)getActivity()).mSkyService.fetchHomePage(url).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(((HomePageActivity) getActivity()).new BaseObserver<HomePagerEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(HomePagerEntity homePagerEntity) {
                        if(mContext == null || leftLayout == null || rightLayout == null && bottomLayout ==null)
                            return;
                        if (homePagerEntity == null) {
                            new CallaPlay().exception_except("launcher", "launcher", channelEntity.getChannel(),
                                    "", 0, channelEntity.getHomepage_url(),
                                    SimpleRestClient.appVersion, "data", ""
                            );
                            super.onError(new Exception("数据异常"));
                            return;
                        }
                        ArrayList<HomePagerEntity.Poster> posters = homePagerEntity.getPosters();
                        ArrayList<HomePagerEntity.Carousel> carousels = homePagerEntity.getCarousels();
                        initPosters(posters);
                        initCarousel(carousels);
                        if(scrollFromBorder){
                            if(isRight){//右侧移入
                                if("bottom".equals(bottomFlag)){//下边界移入
                                    childMore.requestFocus();
                                }else{//上边界边界移入
                                    righttop.requestFocus();
                                }
//                  		}
                            }else{//左侧移入
                                if("bottom".equals(bottomFlag)){
                                    leftBottom.requestFocus();
                                }else{
                                    lefttop.requestFocus();
                                }
//                  	}
                            }
                            ((HomePageActivity)getActivity()).resetBorderFocus();
                        }
                    }
                });
    }

    @Override
    public void onDetach() {
        messageHandler.removeMessages(0);
        super.onDetach();
    }

    private void initPosters(ArrayList<HomePagerEntity.Poster> posters) {
        if(mContext==null){
            return;
        }

        int marginTP = getResources().getDimensionPixelOffset(R.dimen.child_img_small_space);
        int itemWidth = getResources().getDimensionPixelOffset(R.dimen.child_img_small_w);
        int itemHeight = getResources().getDimensionPixelOffset(R.dimen.child_img_small_h);

        for (int i = 0; i < 7; i++) {
            View itemContainer = LayoutInflater.from(mContext).inflate(R.layout.item_comic_fragment, null);
            ImageView itemImg = (ImageView) itemContainer.findViewById(R.id.item_img);
            TextView itemText = (TextView) itemContainer.findViewById(R.id.item_title);
            LabelImageView3 item_img_focus = (LabelImageView3) itemContainer.findViewById(R.id.item_img_focus);

            posters.get(i).setPosition(i);
            item_img_focus.setTag(posters.get(i));
            item_img_focus.setTag(R.id.view_position_tag, i +1);
            item_img_focus.setOnClickListener(ItemClickListener);
//            item_img_focus.setOnHoverListener(new View.OnHoverListener() {
//
//				@Override
//				public boolean onHover(View v, MotionEvent event) {
//					if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER
//							|| event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
//                          v.requestFocus();
//					}
//					return false;
//				}
//			});

            if(mContext==null)
                return;
            Picasso.with(mContext).load(posters.get(i).getCustom_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(itemImg);
            itemText.setText(posters.get(i).getTitle());

            /**
             * left layout
             */
            if (i >= 0 && i < 3) {
                LinearLayout.LayoutParams verticalParams = new LinearLayout.LayoutParams(itemWidth, itemHeight);
                verticalParams.width = itemWidth;
                verticalParams.height = itemHeight;
                if (i == 1) {
                    verticalParams.setMargins(0, marginTP, 0, marginTP);
                }
                if(i ==0){
                	lefttop = item_img_focus;
                }
                if(i <2){
                    item_img_focus.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            			@Override
            			public void onFocusChange(View arg0, boolean arg1) {
            				if (arg1) {
            					((HomePageActivity) (getActivity())).setLastViewTag("");
            				}
            			}
            		});
                }else{
                	leftBottom = item_img_focus;
//                	item_img_focus.setNextFocusDownId(R.id.toppage_divide_view);
                    item_img_focus.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            			@Override
            			public void onFocusChange(View arg0, boolean arg1) {
            				if (arg1) {
            					((HomePageActivity) (getActivity())).setLastViewTag("bottom");
            				}
            			}
            		});
                }
                itemContainer.setLayoutParams(verticalParams);
                leftLayout.addView(itemContainer);
            }

            /**
             * center layout
             */
            if (i >= 3 && i < 5) {
                LinearLayout.LayoutParams horizontalParams = new LinearLayout.LayoutParams(itemWidth, itemHeight);
                horizontalParams.width = itemWidth;
                horizontalParams.height = itemHeight;

                int marginLeft = getResources().getDimensionPixelOffset(R.dimen.child_bottom_space);

                if (i == 4) {
                    horizontalParams.setMargins(marginLeft, 0, 0, 0);
                    item_img_focus.setId(12435688);
                    childMore.setNextFocusLeftId(12435688);
                }

                itemContainer.setLayoutParams(horizontalParams);
                bottomLayout.addView(itemContainer);
            }

            /**
             * right layout
             */
            if (i >= 5 && i < 7) {
                LinearLayout.LayoutParams verticalParams = new LinearLayout.LayoutParams(itemWidth, itemHeight);
                verticalParams.width = itemWidth;
                verticalParams.height = itemHeight;
                if(i == 5){
                    item_img_focus.setNextFocusRightId(R.id.home_scroll_right);
                	righttop = item_img_focus;
                }
                if (i == 6) {
                    verticalParams.setMargins(0, marginTP, 0, 0);
                }
                itemContainer.setLayoutParams(verticalParams);
                rightLayout.addView(itemContainer);
                item_img_focus.setOnFocusChangeListener(new View.OnFocusChangeListener() {

        			@Override
        			public void onFocusChange(View arg0, boolean arg1) {
        				if (arg1) {
        					((HomePageActivity) (getActivity())).setLastViewTag("");
        				}
        			}
        		});
            }
        }
        rightLayout.requestLayout();
    }

    private void initCarousel(ArrayList<HomePagerEntity.Carousel> carousels) {

        this.carousels = carousels;
//

        View.OnFocusChangeListener itemFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                focusFlag = true;
                for (ChildThumbImageView imageView : indicatorImgs) {
                    focusFlag = focusFlag && (!imageView.isFocused());
                }

                if (hasFocus) {
                    int position = (Integer) v.getTag();
                    flag.setPosition(position);
                    playCarousel();
                }
            }
        };


        for (int i = 0; i < 3; i++) {
            indicatorImgs[i].setTag(i);
            indicatorImgs[i].setOnFocusChangeListener(itemFocusChangeListener);
            indicatorImgs[i].setOnClickListener(ItemClickListener);
            indicatorImgs[i].setTag(R.drawable.launcher_selector, carousels.get(i));
            Picasso.with(mContext).load(carousels.get(i).getThumb_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(indicatorImgs[i]);
        }

        flag.setPosition(0);
        playCarousel();

    }

    private void playCarousel() {
        messageHandler.removeMessages(0);
        image_switcher_focus.setTag(R.drawable.launcher_selector, carousels.get(flag.getPosition()));
        Picasso.with(mContext).load(carousels.get(flag.getPosition()).getVideo_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(imageSwitcher, new Callback() {
            int pauseTime = Integer.parseInt(carousels.get(flag.getPosition()).getPause_time());

            @Override
            public void onSuccess() {
                messageHandler.sendEmptyMessageDelayed(0, pauseTime * 1000);
            }

            @Override
            public void onError() {
                messageHandler.sendEmptyMessageDelayed(0, pauseTime * 1000);
            }
        });

    }

    @Override
    public void change(int position) {
        for (int i = 0; i < indicatorImgs.length; i++) {
            ChildThumbImageView imageView = indicatorImgs[i];
            if (position != i) {
                if (imageView.getAlpha() == 1) {
                    imageView.zoomNormalImage();


                }
            } else {
                imageView.zoomInImage();
                imageView.setAlpha((float) 1);
                indicatorTitle.setText(carousels.get(flag.getPosition()).getTitle());

            }
        }
    }

    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (focusFlag) {
                if (flag.getPosition() + 1 >= carousels.size()) {
                    flag.setPosition(0);
                } else {
                    flag.setPosition(flag.getPosition() + 1);
                }
            }
            playCarousel();
        }
    }

}


