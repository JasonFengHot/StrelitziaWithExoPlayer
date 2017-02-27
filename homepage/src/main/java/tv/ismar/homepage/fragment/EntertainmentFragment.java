package tv.ismar.homepage.fragment;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.entity.HomePagerEntity;
import tv.ismar.app.entity.HomePagerEntity.Carousel;
import tv.ismar.app.entity.HomePagerEntity.Poster;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.homepage.R;
import tv.ismar.homepage.view.HomePageActivity;
import tv.ismar.homepage.widget.HomeItemContainer;
import tv.ismar.homepage.widget.LabelImageView3;
import tv.ismar.homepage.widget.LinerLayoutContainer;

/**
 * Created by huaijie on 5/18/15.
 */
public class EntertainmentFragment extends ChannelBaseFragment {

    private static final String TAG = "EntertainmentFragment";
    private final int IMAGE_SWITCH_KEY = 0X11;
    private int PADDING = 22;

    private LabelImageView3 vaiety_post;
    private ImageView vaiety_thumb1;
    private ImageView vaiety_thumb2;
    private ImageView vaiety_thumb3;
    private TextView vaiety_fouce_label;
//    private RelativeLayout vaiety_card1;
    private LabelImageView3 vaiety_card1_image;
    private TextView vaiety_card1_subtitle;
//    private RelativeLayout vaiety_card2;
    private LabelImageView3 vaiety_card2_image;
    private TextView vaiety_card2_subtitle;
//    private RelativeLayout vaiety_card3;
    private LabelImageView3 vaiety_card3_image;
    private TextView vaiety_card3_subtitle;
//    private RelativeLayout vaiety_card4;
    private LabelImageView3 vaiety_card4_image;
    private TextView vaiety_card4_subtitle;
    private LabelImageView3 vaiety_channel1_image;
    private TextView vaiety_channel1_subtitle;
    private LabelImageView3 vaiety_channel2_image;
    private TextView vaiety_channel2_subtitle;
    private LabelImageView3 vaiety_channel3_image;
    private TextView vaiety_channel3_subtitle;
    private LabelImageView3 vaiety_channel4_image;
    private TextView vaiety_channel4_subtitle;
    private HomeItemContainer vaiety_channel5;
    private HomePagerEntity entity;
    private ArrayList<String> looppost = new ArrayList<String>();
    private int loopindex = 0;

    private Subscription dataSubscription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entertainment, null);
//        int thumbH = getResources().getDimensionPixelOffset(R.dimen.entertainment_thumb_h2);
//        PADDING = thumbH / 2;
        vaiety_post = (LabelImageView3) view.findViewById(R.id.vaiety_post);
        vaiety_thumb1 = (ImageView) view.findViewById(R.id.vaiety_thumb1);
        vaiety_thumb2 = (ImageView) view.findViewById(R.id.vaiety_thumb2);
        vaiety_thumb3 = (ImageView) view.findViewById(R.id.vaiety_thumb3);
        vaiety_fouce_label = (TextView) view
                .findViewById(R.id.vaiety_fouce_label);
//        vaiety_card1 = (RelativeLayout) view.findViewById(R.id.vaiety_card1);
        vaiety_card1_image = (LabelImageView3) view
                .findViewById(R.id.vaiety_card1_image);
        vaiety_card1_subtitle = (TextView) view
                .findViewById(R.id.vaiety_card1_subtitle);
//        vaiety_card2 = (RelativeLayout) view.findViewById(R.id.vaiety_card2);
        vaiety_card2_image = (LabelImageView3) view
                .findViewById(R.id.vaiety_card2_image);
        vaiety_card2_subtitle = (TextView) view
                .findViewById(R.id.vaiety_card2_subtitle);
//        vaiety_card3 = (RelativeLayout) view.findViewById(R.id.vaiety_card3);
        vaiety_card3_image = (LabelImageView3) view
                .findViewById(R.id.vaiety_card3_image);
        vaiety_card3_subtitle = (TextView) view
                .findViewById(R.id.vaiety_card3_subtitle);
//        vaiety_card4 = (RelativeLayout) view.findViewById(R.id.vaiety_card4);
        vaiety_card4_image = (LabelImageView3) view
                .findViewById(R.id.vaiety_card4_image);
        vaiety_card4_subtitle = (TextView) view
                .findViewById(R.id.vaiety_card4_subtitle);
        vaiety_channel1_image = (LabelImageView3) view
                .findViewById(R.id.vaiety_channel1_image);
        vaiety_channel1_subtitle = (TextView) view
                .findViewById(R.id.vaiety_channel1_subtitle);
        vaiety_channel2_image = (LabelImageView3) view
                .findViewById(R.id.vaiety_channel2_image);
        vaiety_channel2_subtitle = (TextView) view
                .findViewById(R.id.vaiety_channel2_subtitle);
        vaiety_channel3_image = (LabelImageView3) view
                .findViewById(R.id.vaiety_channel3_image);
        vaiety_channel3_subtitle = (TextView) view
                .findViewById(R.id.vaiety_channel3_subtitle);
        vaiety_channel4_image = (LabelImageView3) view
                .findViewById(R.id.vaiety_channel4_image);
        vaiety_channel4_subtitle = (TextView) view
                .findViewById(R.id.vaiety_channel4_subtitle);
        vaiety_channel5 = (HomeItemContainer) view
                .findViewById(R.id.listmore);
        vaiety_card1_image.setOnClickListener(ItemClickListener);
        vaiety_card2_image.setOnClickListener(ItemClickListener);
        vaiety_card3_image.setOnClickListener(ItemClickListener);
        vaiety_card4_image.setOnClickListener(ItemClickListener);
        vaiety_channel1_image.setOnClickListener(ItemClickListener);
        vaiety_channel2_image.setOnClickListener(ItemClickListener);
        vaiety_channel3_image.setOnClickListener(ItemClickListener);
        vaiety_channel4_image.setOnClickListener(ItemClickListener);
        vaiety_channel5.setOnClickListener(ItemClickListener);
        vaiety_post.setOnClickListener(ItemClickListener);
        vaiety_channel5.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                if (arg1) {
                    ((HomePageActivity) (getActivity())).setLastViewTag("bottom");
                }
            }
        });
        vaiety_channel1_image.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                if (arg1) {
                    ((HomePageActivity) (getActivity())).setLastViewTag("bottom");
                }
            }
        });
        vaiety_card2_image.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                if (arg1) {
                    ((HomePageActivity) (getActivity())).setLastViewTag("");
                }
            }
        });
        vaiety_card4_image.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                if (arg1) {
                    ((HomePageActivity) (getActivity())).setLastViewTag("");
                }
            }
        });
        vaiety_post.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                if (arg1) {
                    ((HomePageActivity) (getActivity())).setLastViewTag("");
                }
            }
        });

        View[] views = {vaiety_post,vaiety_card1_image,vaiety_card3_image,vaiety_card2_image,
                vaiety_card4_image,vaiety_channel1_image,vaiety_channel2_image,vaiety_channel3_image,vaiety_channel4_image};
        for (int i = 0; i < views.length; i++){
            views[i].setTag(R.id.view_position_tag, i);
        }
        return view;
    }

    @Override
    public void onResume() {
        imageswitch.sendEmptyMessageDelayed(IMAGE_SWITCH_KEY, 6000);
        super.onResume();
    }

    @Override
    public void onPause() {
        imageswitch.removeMessages(IMAGE_SWITCH_KEY);
        super.onPause();
        if (dataSubscription != null && !dataSubscription.isUnsubscribed()) {
            dataSubscription.unsubscribe();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        fetchPage(channelEntity.getHomepage_url());
        vaiety_thumb1.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.setPadding(0, 0, 0, 0);
                    vaiety_thumb2.setPadding(0, PADDING, 0, -PADDING);
                    vaiety_thumb3.setPadding(0, PADDING, 0, -PADDING);
                    if (v.getTag() != null) {
                        Picasso.with(mContext).load(v.getTag().toString()).memoryPolicy(MemoryPolicy.NO_STORE)
                                .into(vaiety_post);
                        vaiety_fouce_label.setText(v.getTag(R.id.vaiety_post)
                                .toString());
                    }
                    imageswitch.removeMessages(IMAGE_SWITCH_KEY);
                    vaiety_post.setTag(R.drawable.launcher_selector, v.getTag(R.drawable.launcher_selector));
                } else {
                    v.setPadding(0, PADDING, 0, -PADDING);
                    imageswitch.sendEmptyMessageDelayed(IMAGE_SWITCH_KEY, 6000);
                }
            }
        });
        vaiety_thumb2.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.setPadding(0, 0, 0, 0);
                    vaiety_thumb1.setPadding(0, PADDING, 0, -PADDING);
                    vaiety_thumb3.setPadding(0, PADDING, 0, -PADDING);
                    Picasso.with(mContext).load(v.getTag().toString()).memoryPolicy(MemoryPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_STORE)
                            .into(vaiety_post);
                    vaiety_fouce_label.setText(v.getTag(R.id.vaiety_post)
                            .toString());
                    imageswitch.removeMessages(IMAGE_SWITCH_KEY);
                    vaiety_post.setTag(R.drawable.launcher_selector, v.getTag(R.drawable.launcher_selector));
                } else {
                    v.setPadding(0, PADDING, 0, -PADDING);
                    imageswitch.sendEmptyMessageDelayed(IMAGE_SWITCH_KEY, 6000);
                }
            }
        });
        vaiety_thumb3.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.setPadding(0, 0, 0, 0);
                    vaiety_thumb2.setPadding(0, PADDING, 0, -PADDING);
                    vaiety_thumb1.setPadding(0, PADDING, 0, -PADDING);
                    Picasso.with(mContext).load(v.getTag().toString()).memoryPolicy(MemoryPolicy.NO_STORE)
                            .into(vaiety_post);
                    vaiety_fouce_label.setText(v.getTag(R.id.vaiety_post)
                            .toString());
                    imageswitch.removeMessages(IMAGE_SWITCH_KEY);
                    vaiety_post.setTag(R.drawable.launcher_selector, v.getTag(R.drawable.launcher_selector));
                } else {
                    v.setPadding(0, PADDING, 0, -PADDING);
                    imageswitch.sendEmptyMessageDelayed(IMAGE_SWITCH_KEY, 6000);
                }
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

    private void fillData(ArrayList<Carousel> carousellist,
                          ArrayList<Poster> postlist) {
        if (scrollFromBorder) {
            if (isRight) {//右侧移入
                if ("bottom".equals(bottomFlag)) {//下边界移入
                    vaiety_channel5.requestFocus();
                } else {//上边界边界移入
                    vaiety_card2_image.requestFocus();
                }
//        		}
            } else {//左侧移入
                if ("bottom".equals(bottomFlag)) {
                    vaiety_channel1_image.requestFocus();
                } else {
                    vaiety_post.requestFocus();
                }
//        	}
            }
            ((HomePageActivity) getActivity()).resetBorderFocus();
        }
        ImageView[] vaietys = new ImageView[]{vaiety_thumb1, vaiety_thumb2, vaiety_thumb3};
        looppost.clear();
        for (int i = 0; i < carousellist.size() && i < 3; i++) {
            carousellist.get(i).setPosition(i);
            Picasso.with(mContext).load(carousellist.get(i).getThumb_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(vaietys[i]);
            vaietys[i].setTag(carousellist.get(i).getVideo_image());
            vaietys[i].setTag(R.id.vaiety_post, carousellist.get(i).getTitle());
            vaietys[i].setTag(R.drawable.launcher_selector, carousellist.get(i));
            looppost.add(carousellist.get(i).getVideo_image());
        }

        imageswitch.sendEmptyMessage(IMAGE_SWITCH_KEY);
        vaiety_fouce_label.setText(carousellist.get(0).getTitle());
        postlist.get(0).setPosition(0);
        Picasso.with(mContext).load(postlist.get(0).getCustom_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(vaiety_card1_image);
        vaiety_card1_image.setTitle(postlist.get(0).getIntroduction());
        vaiety_card1_image.setTag(postlist.get(0));
        vaiety_card1_subtitle.setText(postlist.get(0).getTitle());
        vaiety_card1_image.setModeType(0);
        if (postlist.get(0).getCorner() == 2) {
            vaiety_card1_image.setModeType(1);
        } else if (postlist.get(0).getCorner() == 3) {
            vaiety_card1_image.setModeType(2);
        }
        postlist.get(1).setPosition(1);
        Picasso.with(mContext).load(postlist.get(1).getCustom_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(vaiety_card2_image);
        vaiety_card2_image.setTitle(postlist.get(1).getIntroduction());
        vaiety_card2_image.setTag(postlist.get(1));
        vaiety_card2_subtitle.setText(postlist.get(1).getTitle());
        vaiety_card2_image.setModeType(0);
        if (postlist.get(1).getCorner() == 2) {
            vaiety_card2_image.setModeType(1);
        } else if (postlist.get(1).getCorner() == 3) {
            vaiety_card2_image.setModeType(2);
        }
        postlist.get(2).setPosition(2);
        Picasso.with(mContext).load(postlist.get(2).getCustom_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(vaiety_card3_image);
        vaiety_card3_image.setTitle(postlist.get(2).getIntroduction());
        vaiety_card3_subtitle.setText(postlist.get(2).getTitle());
        vaiety_card3_image.setTag(postlist.get(2));
        vaiety_card3_image.setModeType(0);
        if (postlist.get(2).getCorner() == 2) {
            vaiety_card3_image.setModeType(1);
        } else if (postlist.get(2).getCorner() == 3) {
            vaiety_card3_image.setModeType(2);
        }
        postlist.get(3).setPosition(3);
        Picasso.with(mContext).load(postlist.get(3).getCustom_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(vaiety_card4_image);
        vaiety_card4_image.setTitle(postlist.get(3).getIntroduction());
        vaiety_card4_subtitle.setText(postlist.get(3).getTitle());
        vaiety_card4_image.setTag(postlist.get(3));
        vaiety_card4_image.setModeType(0);
        if (postlist.get(3).getCorner() == 2) {
            vaiety_card4_image.setModeType(1);
        } else if (postlist.get(3).getCorner() == 3) {
            vaiety_card4_image.setModeType(2);
        }
        postlist.get(4).setPosition(4);
        Picasso.with(mContext).load(postlist.get(4).getCustom_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(vaiety_channel1_image);
        vaiety_channel1_image.setTitle(postlist.get(4).getIntroduction());
        vaiety_channel1_subtitle.setText(postlist.get(4).getTitle());
        vaiety_channel1_image.setTag(postlist.get(4));
        vaiety_channel1_image.setModeType(0);
        if (postlist.get(4).getCorner() == 2) {
            vaiety_channel1_image.setModeType(1);
        } else if (postlist.get(4).getCorner() == 3) {
            vaiety_channel1_image.setModeType(2);
        }
        postlist.get(5).setPosition(5);
        Picasso.with(mContext).load(postlist.get(5).getCustom_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(vaiety_channel2_image);
        vaiety_channel2_image.setTitle(postlist.get(5).getIntroduction());
        vaiety_channel2_subtitle.setText(postlist.get(5).getTitle());
        vaiety_channel2_image.setTag(postlist.get(5));
        vaiety_channel2_image.setModeType(0);
        if (postlist.get(5).getCorner() == 2) {
            vaiety_channel2_image.setModeType(1);
        } else if (postlist.get(5).getCorner() == 3) {
            vaiety_channel2_image.setModeType(2);
        }
        postlist.get(6).setPosition(6);
        Picasso.with(mContext).load(postlist.get(6).getCustom_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(vaiety_channel3_image);
        vaiety_channel3_image.setTitle(postlist.get(6).getIntroduction());
        vaiety_channel3_subtitle.setText(postlist.get(6).getTitle());
        vaiety_channel3_image.setTag(postlist.get(6));
        vaiety_channel3_image.setModeType(0);
        if (postlist.get(6).getCorner() == 2) {
            vaiety_channel3_image.setModeType(1);
        } else if (postlist.get(6).getCorner() == 3) {
            vaiety_channel3_image.setModeType(2);
        }
        postlist.get(7).setPosition(7);
        Picasso.with(mContext).load(postlist.get(7).getCustom_image()).memoryPolicy(MemoryPolicy.NO_STORE).into(vaiety_channel4_image);
        vaiety_channel4_image.setTitle(postlist.get(7).getIntroduction());
        vaiety_channel4_subtitle.setText(postlist.get(7).getTitle());
        vaiety_channel4_image.setTag(postlist.get(7));
        vaiety_channel4_image.setModeType(0);
        if (postlist.get(7).getCorner() == 2) {
            vaiety_channel4_image.setModeType(1);
        } else if (postlist.get(7).getCorner() == 3) {
            vaiety_channel4_image.setModeType(2);
        }
    }

    private Handler imageswitch = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (looppost.size() < 3)
                return;
            Picasso.with(mContext).load(looppost.get(++loopindex)).memoryPolicy(MemoryPolicy.NO_STORE)
                    .into(vaiety_post);
            if (loopindex == 0) {
                vaiety_thumb1.setPadding(0, 0, 0, 0);
                vaiety_thumb2.setPadding(0, PADDING, 0, -PADDING);
                vaiety_thumb3.setPadding(0, PADDING, 0, -PADDING);
                vaiety_fouce_label.setText(vaiety_thumb1.getTag(
                        R.id.vaiety_post).toString());
                vaiety_post.setTag(R.drawable.launcher_selector, vaiety_thumb1.getTag(R.drawable.launcher_selector));
            } else if (loopindex == 1) {
                vaiety_thumb1.setPadding(0, PADDING, 0, -PADDING);
                vaiety_thumb2.setPadding(0, 0, 0, 0);
                vaiety_thumb3.setPadding(0, PADDING, 0, -PADDING);
                vaiety_fouce_label.setText(vaiety_thumb2.getTag(
                        R.id.vaiety_post).toString());
                vaiety_post.setTag(R.drawable.launcher_selector, vaiety_thumb2.getTag(R.drawable.launcher_selector));
            } else if (loopindex == 2) {
                vaiety_thumb1.setPadding(0, PADDING, 0, -PADDING);
                vaiety_thumb2.setPadding(0, PADDING, 0, -PADDING);
                vaiety_thumb3.setPadding(0, 0, 0, 0);
                vaiety_fouce_label.setText(vaiety_thumb3.getTag(
                        R.id.vaiety_post).toString());
                vaiety_post.setTag(R.drawable.launcher_selector, vaiety_thumb3.getTag(R.drawable.launcher_selector));
            }

            if (loopindex >= 2)
                loopindex = -1;
            if (imageswitch.hasMessages(IMAGE_SWITCH_KEY))
                imageswitch.removeMessages(IMAGE_SWITCH_KEY);
            imageswitch.sendEmptyMessageDelayed(IMAGE_SWITCH_KEY, 6000);
        }
    };

    @Override
    public void onDetach() {
        imageswitch.removeMessages(IMAGE_SWITCH_KEY);
        super.onDetach();
    }

    public void refreshData() {
        imageswitch.removeCallbacksAndMessages(null);
        fetchPage(channelEntity.getHomepage_url());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void fetchPage(String url) {
        dataSubscription = ((HomePageActivity) getActivity()).mSkyService.fetchHomePage(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(((HomePageActivity) getActivity()).new BaseObserver<HomePagerEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(HomePagerEntity homePagerEntity) {
                        entity = homePagerEntity;
                        if (mContext == null || entity == null) {
                            new CallaPlay().exception_except("launcher", "launcher", channelEntity.getChannel(),
                                    "", 0, channelEntity.getHomepage_url(),
                                    SimpleRestClient.appVersion, "data", ""
                            );
                            super.onError(new Exception("数据异常"));
                            return;
                        }
                        ArrayList<Carousel> carousellist = entity.getCarousels();
                        ArrayList<Poster> postlist = entity.getPosters();
                        fillData(carousellist, postlist);
                    }
                });
    }
}
