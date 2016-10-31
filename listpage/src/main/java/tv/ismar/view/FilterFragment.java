package tv.ismar.view;

import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import okhttp3.ResponseBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.DaisyUtils;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.entity.FilterItem;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.ui.view.AlertDialogFragment;
import tv.ismar.app.ui.view.MyViewGroup;
import tv.ismar.app.widget.LoadingDialog;
import tv.ismar.channel.FilterActivity;
import tv.ismar.listpage.R;


/**
 * Created by zhangjiqiang on 15-6-18.
 */
public class FilterFragment extends BackHandledFragment {

    private String content_model;
    public String mChannel;//chinesemovie;overseas
    private SimpleRestClient mRestClient;
    private View fragmentView;
    private LoadingDialog mLoadingDialog;
    private ArrayList<String> keys;
    private LinearLayout filtermenulayout;
    private String nolimit="";
    private ArrayList<String>labels;
    private HashMap<String,ArrayList<String>> mapValues;
    private static final int LABEL_TEXT_COLOR_NOFOCUSED = 0xffffffff;
    private static final int LABEL_TEXT_COLOR_FOCUSED = 0xffff9c3c;
    private static final int LABEL_TEXT_COLOR_CLICK = 0xff00a8ff;
    private String submitFilteStr = "";
    private Button submitBtn;
    private ArrayList<String> conditions;
    private ArrayList<String> realNames;
    private String realFilterStr="";
    public boolean isPortrait = false;
    private SkyService skyService;
    private float rate;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rate = DaisyUtils.getVodApplication(getActivity()).getRate(getActivity());
        if(fragmentView==null){
            fragmentView = inflater.inflate(R.layout.filter_view, container, false);
            conditions = new ArrayList<String>();
            realNames = new ArrayList<String>();
            mLoadingDialog = new LoadingDialog(getActivity(),R.style.LoadingDialog);
            mLoadingDialog.setTvText(getResources().getString(R.string.loading));
            skyService=SkyService.ServiceManager.getService();
            mRestClient = new SimpleRestClient();
            initView(fragmentView);
            mLoadingDialog.show();
            doFilterRequest();
        }
        return fragmentView;

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        if(submitBtn!=null){
            submitBtn.setFocusable(true);
            submitBtn.requestFocus();
        }
        super.onResume();
    }

    private void initView(View fragmentView){
        filtermenulayout = (LinearLayout)fragmentView.findViewById(R.id.filtermenulayout);
        submitBtn = (Button)fragmentView.findViewById(R.id.filter_submit);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(String str: conditions){
                    submitFilteStr += str + "!";
                }
                for(String str: realNames){
                    realFilterStr += str + "!";
                }
                if(!"".equals(submitFilteStr)&&conditions.size()>0){
                    submitFilteStr = submitFilteStr.substring(0,submitFilteStr.length()-1);
                }
                if(!"".equals(realFilterStr)){
                    realFilterStr = realFilterStr.substring(0,realFilterStr.length()-1);
                }
                FilterResultFragment resultFragment = new FilterResultFragment();
                if((!mChannel.equals("chinesemovie")||!mChannel.equals("overseas"))&&"".equals(submitFilteStr)){
                    submitFilteStr = nolimit;
                }
                resultFragment.conditions = realFilterStr;
                resultFragment.filterCondition = submitFilteStr;
                resultFragment.mChannel = mChannel;
                resultFragment.content_model = content_model;
                resultFragment.isPortrait = isPortrait;
                loadFragment(resultFragment);
                submitFilteStr = "";
                realFilterStr = "";
            }
        });
        submitBtn.setOnHoverListener(new View.OnHoverListener() {
			
			@Override
			public boolean onHover(View v, MotionEvent event) {
				
				if(event.getAction() == MotionEvent.ACTION_HOVER_ENTER || event.getAction() == MotionEvent.ACTION_HOVER_MOVE){
//					v.setBackgroundResource(R.color.search_bg_focus);
					v.requestFocus();
				}else{
//					v.setBackgroundResource(R.drawable.filter_btn_normal);		
				}
				return false;
			}
		});
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void doFilterRequest(){
        skyService.getFilters(mChannel).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(((BaseActivity) getActivity()).new BaseObserver<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            Log.i("OnNext","succuse");
                            String info=responseBody.string();
                            JSONObject jsonObject = new JSONObject(info);
                            content_model = jsonObject.getString("content_model");
                            nolimit = jsonObject.getString("default");
                            JSONObject attributes = jsonObject.getJSONObject("attributes");
                            Iterator it = attributes.keys();
                            mLoadingDialog.dismiss();
                            submitBtn.setVisibility(View.VISIBLE);
                            submitBtn.setFocusable(true);
                            submitBtn.requestFocus();
                            while(it.hasNext()){
                                String key = (String) it.next();
                                JSONObject jsonObj = attributes.getJSONObject(key);
                                String label = jsonObj.getString("label");
                                JSONArray values = jsonObj.getJSONArray("values");
                                int arrayCount = values.length();
                                if(getActivity()==null){
                                    return;
                                }
                                LayoutInflater mInflater = LayoutInflater.from(getActivity());
                                View view = mInflater.inflate(R.layout.filter_condition_item,null);
                                TextView condition_txt = (TextView) view.findViewById(R.id.condition_txt);
                                MyViewGroup valueViews = (MyViewGroup)view.findViewById(R.id.line_group);
                                valueViews.setFocusable(true);
                                valueViews.setFocusableInTouchMode(true);
                                valueViews.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
                                condition_txt.setText(label+" :");
                                RadioButton nolimitRbtn = new RadioButton(getActivity());
                                nolimitRbtn.setFocusable(true);
                                nolimitRbtn.setFocusableInTouchMode(true);
                                FilterItem noLimitItem = new FilterItem();
                                if(nolimit.startsWith("area")&&key.equals("area")&&(mChannel.equals("chinesemovie")||mChannel.equals("overseas"))){
                                    noLimitItem.value = nolimit;
                                }
                                else{
                                    noLimitItem.value = "";
                                }
                                noLimitItem.type = key;
                                noLimitItem.nolimitView = null;
                                noLimitItem.name = "不限";
                                nolimitRbtn.setText("不限");
                                nolimitRbtn.setTag(noLimitItem);
                                initRadioButton(nolimitRbtn);

                                nolimitRbtn.setChecked(true);
                                valueViews.addView(nolimitRbtn,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,(int)(100/1)));
                                for(int i=0; i<arrayCount; i++){
                                    JSONArray subArray = values.getJSONArray(i);
                                    FilterItem item = new FilterItem();
                                    item.type = key;
                                    item.value = subArray.getString(0);
                                    item.nolimitView = nolimitRbtn;
                                    item.name = subArray.getString(1);
                                    RadioButton rbtn = new RadioButton(getActivity());
                                    rbtn.setText(subArray.getString(1));
                                    rbtn.setTag(item);
                                    initRadioButton(rbtn);
                                    valueViews.addView(rbtn,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,(int)(100/1)));
                                }
                                filtermenulayout.addView(view,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,(int)(165/1)));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
    private void initRadioButton(RadioButton rbtn){
        rbtn.setButtonDrawable(android.R.color.transparent);
        rbtn.setTextColor(LABEL_TEXT_COLOR_NOFOCUSED);
        rbtn.setTextSize(36/rate);

        rbtn.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    if(!((RadioButton)view).isChecked()){
                        ((RadioButton)view).setTextSize(48/rate);
                        ((RadioButton)view).setTextColor(LABEL_TEXT_COLOR_FOCUSED);
//                        if(((FilterItem)view.getTag()).nolimitView!=null){
//                                ((FilterItem)view.getTag()).nolimitView.setChecked(false);
//                        }
                    }
                }else{
                    if(!((RadioButton)view).isChecked()){
                        ((RadioButton)view).setTextSize(36/rate);
                        ((RadioButton)view).setTextColor(LABEL_TEXT_COLOR_NOFOCUSED);
                    }
                }

            }
        });

        rbtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                 if(b){
                   //  Toast.makeText(getActivity(), ((FilterItem)compoundButton.getTag()).value, Toast.LENGTH_SHORT).show();
                     compoundButton.setTextSize(48/rate);
                     compoundButton.setTextColor(LABEL_TEXT_COLOR_CLICK);
                     if(((FilterItem)compoundButton.getTag()).nolimitView!=null){
                         if(((FilterItem)compoundButton.getTag()).nolimitView!=compoundButton){
                             ((FilterItem)compoundButton.getTag()).nolimitView.setChecked(false);
                         }
                     }
                     String str = ((FilterItem)compoundButton.getTag()).value;
                     if(!str.equals("")){
                         conditions.add(str);
                         String s = ((FilterItem)compoundButton.getTag()).name;
                         if(!"不限".equals(s))
                            realNames.add(s);
                     }

                 }
                 else{
                     compoundButton.setTextSize(36/rate);
                     compoundButton.setTextColor(LABEL_TEXT_COLOR_NOFOCUSED);
                     String str = ((FilterItem)compoundButton.getTag()).value;
                     if(!"".equals(str)){
                         conditions.remove(str);
                         String s = ((FilterItem)compoundButton.getTag()).name;
                         if(!"不限".equals(s))
                           realNames.remove(s);
                     }
                 }
            }
        });

        rbtn.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_HOVER_ENTER:
                    case MotionEvent.ACTION_HOVER_MOVE:
                    	view.setFocusable(true);
                    	view.setFocusableInTouchMode(true);
                    	view.requestFocus();
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        break;
                }
                return false;
            }
        });
    }
    private CompoundButton lastview;
    public void showDialog() {
        AlertDialogFragment newFragment = AlertDialogFragment.newInstance(AlertDialogFragment.NETWORK_EXCEPTION_DIALOG);
        newFragment.setPositiveListener(new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                doFilterRequest();
                dialog.dismiss();
            }
        });
        newFragment.setNegativeListener(new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
                dialog.dismiss();
            }
        });
        FragmentManager manager = getFragmentManager();

        if(manager!=null) {
            newFragment.show(manager, "dialog");
        }
    }


    @Override
    public boolean onBackPressed() {
        return false;
    }
 
}