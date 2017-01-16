package tv.ismar.detailpage.viewmodel;
import cn.ismartv.injectdb.library.util.Log;
import cn.ismartv.truetime.TrueTime;

import android.app.Activity;
import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import cn.ismartv.truetime.TrueTime;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.VodApplication;
import tv.ismar.app.core.VipMark;
import tv.ismar.app.db.HistoryManager;
import tv.ismar.app.entity.History;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.network.entity.PlayCheckEntity;
import tv.ismar.app.util.Utils;
import tv.ismar.detailpage.BR;
import tv.ismar.detailpage.R;
import tv.ismar.detailpage.presenter.DetailPagePresenter;

/**
 * Created by huibin on 8/08/06.
 */
public class DetailPageViewModel extends BaseObservable {
    private Context mContext;
    private final DetailPagePresenter mPresenter;
    public ObservableField<String> itemTitle;
    private ItemEntity mItemEntity = new ItemEntity();
    private int mRemandDay = 0;
    private String expireDate;
    private boolean itemIsload = false;
    private HistoryManager historyManager;
    private History mHistory;
    public String to="";

    public DetailPageViewModel(Context context, DetailPagePresenter presenter) {
        mContext = context;
        mPresenter = presenter;
        itemTitle = new ObservableField<>();
    }

    public void replaceItem(ItemEntity itemEntity) {
        mItemEntity = itemEntity;
        itemTitle.set(itemEntity.getTitle());
        notifyPropertyChanged(BR.itemPostUrl);
        notifyPropertyChanged(BR.itemHorizontalUrl);
        notifyPropertyChanged(BR.description);
        notifyPropertyChanged(BR.purchaseVisibility);
        notifyPropertyChanged(BR.director);
        notifyPropertyChanged(BR.directorVisibility);
        notifyPropertyChanged(BR.actor);
        notifyPropertyChanged(BR.actorVisibility);
        notifyPropertyChanged(BR.genre);
        notifyPropertyChanged(BR.genreVisibility);
        notifyPropertyChanged(BR.length);
        notifyPropertyChanged(BR.lengthVisibility);
        notifyPropertyChanged(BR.area);
        notifyPropertyChanged(BR.areaVisibility);
        notifyPropertyChanged(BR.airDate);
        notifyPropertyChanged(BR.airDateVisibility);
        notifyPropertyChanged(BR.length);
        notifyPropertyChanged(BR.lengthVisibility);
        notifyPropertyChanged(BR.classification);
        notifyPropertyChanged(BR.classificationVisibility);
        notifyPropertyChanged(BR.playTextWidthIsLong);
        notifyPropertyChanged(BR.playText);

        notifyPropertyChanged(BR.vipMarkUrl);
        notifyPropertyChanged(BR.vipMarkVisibility);

        notifyPropertyChanged(BR.price);
        notifyPropertyChanged(BR.priceVisibility);

        notifyPropertyChanged(BR.permissionVisibility);

        notifyPropertyChanged(BR.guest);
        notifyPropertyChanged(BR.guestVisibility);
        notifyPropertyChanged(BR.emcee);
        notifyPropertyChanged(BR.emceeVisibility);

        notifyPropertyChanged(BR.episodes);
        notifyPropertyChanged(BR.episodesVisibility);

        notifyPropertyChanged(BR.subitemsVisibility);

        notifyPropertyChanged(BR.bookmarkText);
        notifyPropertyChanged(BR.visibility);
        notifyPropertyChanged(BR.enabled);
        if (historyManager == null) {
            historyManager = ((VodApplication)mContext.getApplicationContext()).getModuleHistoryManager();
        }
        String historyUrl = Utils.getItemUrl(mItemEntity.getPk());
        String isLogin = "no";
        if (!Utils.isEmptyText(IsmartvActivator.getInstance().getAuthToken())) {
            isLogin = "yes";
        }
        mHistory = historyManager.getHistoryByUrl(historyUrl, isLogin);

    }

    public void showLayout() {
        itemIsload = true;
        notifyPropertyChanged(BR.itemLayoutVisibility);
    }

    @BindingAdapter({"imageUrl"})
    public static void loadImage(ImageView view, String imageUrl) {
        Picasso.with(view.getContext())
                .load(imageUrl)
                .into(view);
    }

    @BindingAdapter({"vipMark"})
    public static void vipMark(ImageView view, String imageUrl) {
        Picasso.with(view.getContext())
                .load(imageUrl)
                .rotate(90)
                .into(view);
    }

    @Bindable
    public String getItemPostUrl() {
        return mItemEntity.getDetailUrl();
    }

    @Bindable
    public String getItemHorizontalUrl() {
        return mItemEntity.getPosterUrl();
    }

    @Bindable
    public String getDescription() {
        if (!TextUtils.isEmpty(mItemEntity.getDescription())) {
            return mContext.getString(R.string.detail_page_introduction) + mItemEntity.getDescription();
        }
        return mItemEntity.getDescription();
    }

    @Bindable
    public String getGenre() {
        StringBuffer stringBuffer = new StringBuffer();
        int length;
        try {
            length = mItemEntity.getAttributes().getGenre().length;
        } catch (NullPointerException e) {
            length = 0;
        }
        for (int i = 0; i < length; i++) {
            if (i == length - 1) {
                stringBuffer.append(mItemEntity.getAttributes().getGenre()[i][1]);
            } else {
                stringBuffer.append(mItemEntity.getAttributes().getGenre()[i][1]).append(",");
            }
        }
        return stringBuffer.toString();
    }

    @Bindable
    public int getGenreVisibility() {
        return TextUtils.isEmpty(getGenre()) ? View.GONE : View.VISIBLE;
    }


    @Bindable
    public String getDirector() {
        StringBuffer stringBuffer = new StringBuffer();
        int length;
        try {
            length = mItemEntity.getAttributes().getDirector().length;
        } catch (NullPointerException e) {
            length = 0;
        }
        for (int i = 0; i < length; i++) {
            if (i == length - 1) {
                stringBuffer.append(mItemEntity.getAttributes().getDirector()[i][1]);
            } else {
                stringBuffer.append(mItemEntity.getAttributes().getDirector()[i][1]).append(",");
            }
        }
        return stringBuffer.toString();
    }


    @Bindable
    public int getDirectorVisibility() {
        return TextUtils.isEmpty(getDirector()) ? View.GONE : View.VISIBLE;
    }

    @Bindable
    public String getActor() {
        StringBuffer stringBuffer = new StringBuffer();
        int length;
        try {
            length = mItemEntity.getAttributes().getActor().length;
        } catch (NullPointerException e) {
            length = 0;
        }
        for (int i = 0; i < length; i++) {
            if (i == length - 1) {
                stringBuffer.append(mItemEntity.getAttributes().getActor()[i][1]);
            } else {
                stringBuffer.append(mItemEntity.getAttributes().getActor()[i][1]).append(",");
            }
        }
        return stringBuffer.toString();
    }


    @Bindable
    public int getActorVisibility() {
        return TextUtils.isEmpty(getActor()) ? View.GONE : View.VISIBLE;
    }

    @Bindable
    public String getAirDate() {
        String date;
        try {
            date = mItemEntity.getAttributes().getAirDate();
        } catch (NullPointerException e) {
            date = "";
        }
        return date;
    }

    @Bindable
    public int getAirDateVisibility() {
        return TextUtils.isEmpty(getAirDate()) ? View.GONE : View.VISIBLE;
    }

    @Bindable
    public String getLength() {
        int length;
        try {
            length = Integer.parseInt(mItemEntity.getClip().getLength()) / 60;
        } catch (NullPointerException e) {
            length = 0;
        }
        return String.valueOf(length) + mContext.getString(R.string.minute);
    }


    @Bindable
    public int getLengthVisibility() {
        return getLength().equals("0" + mContext.getString(R.string.minute)) ? View.GONE : View.VISIBLE;
    }


    @Bindable
    public String getArea() {
        String area;
        try {
            area = mItemEntity.getAttributes().getArea()[1];
        } catch (NullPointerException e) {
            area = "";
        }
        return area;
    }


    @Bindable
    public int getAreaVisibility() {
        return TextUtils.isEmpty(getArea()) ? View.GONE : View.VISIBLE;
    }

    @Bindable
    public int getPurchaseVisibility() {
        return mItemEntity.getExpense() != null && mRemandDay <= 0 ? View.VISIBLE : View.GONE;
    }

    @Bindable
    public String getEmcee() {
        StringBuffer stringBuffer = new StringBuffer();
        int length;
        try {
            length = mItemEntity.getAttributes().getEmcee().length;
        } catch (NullPointerException e) {
            length = 0;
        }
        for (int i = 0; i < length; i++) {
            if (i == length - 1) {
                stringBuffer.append(mItemEntity.getAttributes().getEmcee()[i][1]);
            } else {
                stringBuffer.append(mItemEntity.getAttributes().getEmcee()[i][1]).append(",");
            }
        }
        return stringBuffer.toString();
    }

    @Bindable
    public int getEmceeVisibility() {
        return TextUtils.isEmpty(getEmcee()) ? View.GONE : View.VISIBLE;

    }

    @Bindable
    public String getGuest() {
        StringBuffer stringBuffer = new StringBuffer();
        int length;
        try {
            length = mItemEntity.getAttributes().getGuest().length;
        } catch (NullPointerException e) {
            length = 0;
        }
        for (int i = 0; i < length; i++) {
            if (i == length - 1) {
                stringBuffer.append(mItemEntity.getAttributes().getGuest()[i][1]);
            } else {
                stringBuffer.append(mItemEntity.getAttributes().getGuest()[i][1]).append(",");
            }
        }
        return stringBuffer.toString();
    }

    @Bindable
    public int getGuestVisibility() {
        return TextUtils.isEmpty(getGuest()) ? View.GONE : View.VISIBLE;
    }

    @Bindable
    public String getPrice() {
        String price = "0";
        try {
            BigDecimal bigDecimal = new BigDecimal(mItemEntity.getExpense().getPrice());
            DecimalFormat decimalFormat = new DecimalFormat("##0.0");
            price = mContext.getString(R.string.yuan) + decimalFormat.format(bigDecimal);
        } catch (NullPointerException e) {
            price = "0";
        }
        return price;
    }

    @Bindable
    public int getPriceVisibility() {
        try {

            if (getExpireDateVisibility() == View.VISIBLE) {
                return View.GONE;
            }

            if (mItemEntity.getExpense().getPay_type() == 3 || mItemEntity.getExpense().getPay_type() == 0) {

                return View.GONE;
            } else {
                if (TextUtils.isEmpty(expireDate)) {
                    return View.VISIBLE;
                } else {
                    return getPrice().equals("0") ? View.GONE : View.VISIBLE;
                }
            }
        } catch (NullPointerException e) {
            return View.GONE;
        }

    }

    @Bindable
    public String getVipMarkUrl() {
        String url;
        if (mItemEntity.getExpense() != null) {
            url = VipMark.getInstance().getImage((Activity) mContext, mItemEntity.getExpense().getPay_type(),
                    mItemEntity.getExpense().getCpid());
        } else {
            url = "test";
        }
        return url;
    }


    @Bindable
    public int getVipMarkVisibility() {
        return TextUtils.isEmpty(getVipMarkUrl()) ? View.GONE : View.VISIBLE;
    }


    @Bindable
    public int getPermissionVisibility() {
        try {
            if (mItemEntity.getExpense().getPay_type() == 3 || mItemEntity.getExpense().getPay_type() == 0) {
                if (TextUtils.isEmpty(expireDate)) {
                    return View.VISIBLE;
                } else {
                    return View.GONE;
                }

            } else {
                return View.GONE;
            }
        } catch (NullPointerException e) {
            return View.GONE;
        }
    }

    @Bindable
    public int getSubitemsVisibility() {
        try {
            return mItemEntity.getSubitems().length == 0 ? View.GONE : View.VISIBLE;
        } catch (NullPointerException e) {
            return View.GONE;
        }
    }

    @Bindable
    public String getEpisodes() {
        String episodes;
        try {
            episodes = String.valueOf(mItemEntity.getSubitems().length);
            if (episodes.equals("0")) {
                episodes = "";
            } else {
                episodes += String.format(mContext.getString(R.string.update_to_episode), mItemEntity.getSubitems().length);
            }

        } catch (NullPointerException e) {
            episodes = "";
        }
        return episodes;
    }

    @Bindable
    public int getEpisodesVisibility() {

        if (!TextUtils.isEmpty(getEpisodes())) {
            if (mItemEntity.getExpense() != null) {
                if (mRemandDay > 0) {
                    return View.VISIBLE;
                } else {
                    return View.GONE;
                }
            } else {
                return View.VISIBLE;
            }

        } else {
            return View.GONE;
        }
    }

    @Bindable
    public String getPlayText() {

        try {
            switch (mPresenter.getContentModel()) {
                case "entertainment":
                case "variety":
                    ItemEntity[] subItems = mItemEntity.getSubitems();
                    String subitem_title = "";
                    if (mHistory != null && mHistory.sub_url != null) {
                        Log.e("getPlaytext", mHistory.toString());
                        for (int i = 0; i < subItems.length; i++) {
                            if (mItemEntity.getExpense() != null) {
                                break;
                            }

                            if (mHistory.sub_url.contains(subItems[i].getPk() + "")) {
                                subitem_title = subItems[i].getSubtitle();
                                break;
                            }
                        }
                    }
                    if (subItems == null || subItems.length == 0) {
                        return mItemEntity.getExpense() != null && mRemandDay <= 0 ? mContext.getString(R.string.video_preview) :
                                mContext.getString(R.string.video_play);
                    } else {
                        return mItemEntity.getExpense() != null && mRemandDay <= 0 ?
                                mContext.getString(R.string.video_preview)
                                        + " " + subitem_title
//                                    subItems[subItems.length - 1].getSubtitle()
                                :
                                mContext.getString(R.string.video_play) + " " + subitem_title;
//                                    subItems[subItems.length - 1].getSubtitle();
                    }

                default:
                    return mItemEntity.getExpense() != null && mRemandDay <= 0 ? mContext.getString(R.string.video_preview) :
                            mContext.getString(R.string.video_play);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return mContext.getString(R.string.video_play);
    }

    @Bindable
    public boolean getEnabled() {


                if(mItemEntity.getLiveVideo()){
                    if(videoIsStart()){

                         return true;
                    }else{
                        return false;
                    }
                }else{
                        return true;
                }
        }

    @Bindable
    public int getVisibility() {

                if(getPlayText().equals(mContext.getString(R.string.video_preview))){
                    if(mItemEntity.getPreview()==null){
                        return View.GONE;
                    }else{
                        return View.VISIBLE;
                    }

                }else{
                    return View.VISIBLE;
                }

    }
    @BindingAdapter("android:layout_width")
    public static void setLayoutWidth(View view, float width) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = (int) width;
        view.setLayoutParams(layoutParams);
    }

    @Bindable
    public boolean getPlayTextWidthIsLong() {
        return getPlayText().length() > 2;
    }

    @Bindable
    public String getBookmarkText() {

        return mPresenter.isFavorite() ? mContext.getString(R.string.video_favorite_) : mContext.getString(R.string.video_favorite);
    }


    public void notifyPlayCheck(PlayCheckEntity playCheckEntity) {
        mRemandDay = playCheckEntity.getRemainDay();
        expireDate = playCheckEntity.getExpiry_date();
        notifyPropertyChanged(BR.purchaseVisibility);
        notifyPropertyChanged(BR.playText);
        notifyPropertyChanged(BR.expireDate);
        notifyPropertyChanged(BR.expireDateVisibility);
        notifyPropertyChanged(BR.priceVisibility);     
        notifyPropertyChanged(BR.permissionVisibility);
        notifyPropertyChanged(BR.episodesVisibility);
        notifyPropertyChanged(BR.subitemsVisibility);
        notifyPropertyChanged(BR.visibility);
        notifyPropertyChanged(BR.enabled);
    }

    @Bindable
    public String getExpireDate() {
        if (!TextUtils.isEmpty(expireDate)) {
            SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            sourceFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            Date sourceDate = null;
            try {
                sourceDate = sourceFormat.parse(expireDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy年MM月dd日");
            targetFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            String targetDate = targetFormat.format(sourceDate);
            return "有效期至" + targetDate;
        } else {
            return expireDate;
        }
    }

    @Bindable
    public int getExpireDateVisibility() {
        return TextUtils.isEmpty(expireDate) ? View.GONE : View.VISIBLE;
    }


    @Bindable
    public String getClassification() {
        String classification;
        try {
            classification = mItemEntity.getAttributes().getClassification();
        } catch (NullPointerException e) {
            classification = "";
        }
        return classification;
    }

    @Bindable
    public int getClassificationVisibility() {
        return TextUtils.isEmpty(getClassification()) ? View.GONE : View.VISIBLE;
    }

    public void notifyBookmark(boolean isSuccess) {
        if (isSuccess) {
            notifyPropertyChanged(BR.bookmarkText);
        }
    }

    @Bindable
    public int getItemLayoutVisibility() {
        return itemIsload ? View.VISIBLE : View.INVISIBLE;
    }

    @Bindable
    public String getCpLogoUrl() {
        try {
            return mItemEntity.getExpense().getCplogo();
        } catch (NullPointerException e) {
            return "error";
        }
    }

    @Bindable
    public int getCpLogoVisibility() {
        return getCpLogoUrl().equals("error") ? View.INVISIBLE : View.VISIBLE;
    }

    private boolean videoIsStart() {
        if (mItemEntity.getStartTime() != null) {
            Calendar currentCalendar = new GregorianCalendar(TimeZone.getTimeZone("Asia/Shanghai"), Locale.CHINA);
            currentCalendar.setTime(TrueTime.now());
            Calendar startCalendar = new GregorianCalendar(TimeZone.getTimeZone("Asia/Shanghai"), Locale.CHINA);
            startCalendar.setTime(mItemEntity.getStartTime());
            if (currentCalendar.after(startCalendar)) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }


}
