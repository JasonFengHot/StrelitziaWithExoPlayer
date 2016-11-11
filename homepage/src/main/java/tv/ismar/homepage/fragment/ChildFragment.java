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
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.entity.HomePagerEntity;
import tv.ismar.homepage.R;
import tv.ismar.homepage.activity.HomePageActivity;
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
    private LabelImageView3 imageSwitcher;
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_child, null);
        leftLayout = (LinearLayout) mView.findViewById(R.id.left_layout);
        bottomLayout = (LinearLayout) mView.findViewById(R.id.bottom_layout);
        rightLayout = (LinearLayout) mView.findViewById(R.id.right_layout);
        imageSwitcher = (LabelImageView3) mView.findViewById(R.id.image_switcher);
        indicatorImgs = new ChildThumbImageView[]{
                (ChildThumbImageView) mView.findViewById(R.id.indicator_1),
                (ChildThumbImageView) mView.findViewById(R.id.indicator_2),
                (ChildThumbImageView) mView.findViewById(R.id.indicator_3)
        };
        indicatorTitle = (TextView) mView.findViewById(R.id.indicator_title);
        childMore = (ImageButton) mView.findViewById(R.id.child_more);
        childMore.setOnClickListener(ItemClickListener);
        imageSwitcher.setOnClickListener(ItemClickListener);
        imageSwitcher.setOnHoverListener(new View.OnHoverListener() {
			
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
        ((HomePageActivity)getActivity()).mSkyService.fetchHomePage(url).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<HomePagerEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "fetchHomePage: " + e.getMessage());
                    }

                    @Override
                    public void onNext(HomePagerEntity homePagerEntity) {
                        if(mContext == null || leftLayout == null || rightLayout == null && bottomLayout ==null)
                            return;
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

        int marginTP = (int) mContext.getResources().getDimension(R.dimen.child_fragment_item_margin_tp);

        int itemWidth = (int) mContext.getResources().getDimension(R.dimen.child_fragment_item_width);
        int itemHeight = (int) mContext.getResources().getDimension(R.dimen.child_fragment_item_height);

        for (int i = 0; i < 7; i++) {
            View itemContainer = LayoutInflater.from(mContext).inflate(R.layout.item_comic_fragment, null);
            posters.get(i).setPosition(i);
            itemContainer.setTag(posters.get(i));
            itemContainer.setOnClickListener(ItemClickListener);
            itemContainer.setOnHoverListener(new View.OnHoverListener() {
				
				@Override
				public boolean onHover(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER
							|| event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                          v.requestFocus();
					}
					return false;
				}
			});
            ImageView itemImg = (ImageView) itemContainer.findViewById(R.id.item_img);
            TextView itemText = (TextView) itemContainer.findViewById(R.id.item_title);
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
                	lefttop = itemContainer;
                }
                if(i <2){
                	itemContainer.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            			@Override
            			public void onFocusChange(View arg0, boolean arg1) {
            				if (arg1) {
            					((HomePageActivity) (getActivity())).setLastViewTag("");
            				}
            			}
            		});
                }else{
                	leftBottom = itemContainer;
                	itemContainer.setNextFocusDownId(R.id.toppage_divide_view);
                	itemContainer.setOnFocusChangeListener(new View.OnFocusChangeListener() {

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

                int marginLeft = (int) mContext.getResources().getDimension(R.dimen.child_fragment_center_layout_item_margin_left);

                if (i == 4) {
                    horizontalParams.setMargins(marginLeft, 0, 0, 0);
                    itemContainer.setId(12435688);
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
                	righttop =itemContainer;
                }
                if (i == 6) {
                    verticalParams.setMargins(0, marginTP, 0, 0);
                }
                itemContainer.setLayoutParams(verticalParams);
                rightLayout.addView(itemContainer);
                itemContainer.setOnFocusChangeListener(new View.OnFocusChangeListener() {

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
        imageSwitcher.setTag(R.drawable.launcher_selector, carousels.get(flag.getPosition()));
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


