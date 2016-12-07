package tv.ismar.helperpage.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tv.ismar.helperpage.LauncherActivity;
import tv.ismar.helperpage.R;
import tv.ismar.helperpage.core.FeedbackProblem;
import tv.ismar.helperpage.core.UploadFeedback;
import tv.ismar.helperpage.ui.adapter.FeedbackListAdapter;
import tv.ismar.helperpage.ui.widget.FeedBackListView;
import tv.ismar.helperpage.ui.widget.MessageSubmitButton;
import tv.ismar.helperpage.ui.widget.SakuraEditText;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.preferences.AccountSharedPrefs;
import tv.ismar.app.network.entity.ChatMsgEntity;
import tv.ismar.app.network.entity.FeedBackEntity;
import tv.ismar.app.network.entity.ProblemEntity;
import tv.ismar.app.network.entity.TeleEntity;
import tv.ismar.app.ui.MessageDialogFragment;


/**
 * Created by huaijie on 2015/4/8.
 */
public class FeedbackFragment extends Fragment implements RadioGroup.OnCheckedChangeListener,
        View.OnClickListener {
    private static final String TAG = "FeedbackFragment";

    private Context mContext;

    private int problemTextFlag = 6;
    private RadioGroup problemType;
    private TextView snCodeTextView;
    private FeedBackListView feedBackListView;
    private MessageSubmitButton submitButton;

    private SakuraEditText phoneNumberText;
    private SakuraEditText descriptioinText;

    private ImageView arrowUp;
    private ImageView arrowDown;


    private String snCode = TextUtils.isEmpty(SimpleRestClient.sn_token) ? "sn is null" : SimpleRestClient.sn_token;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sakura_fragment_feedback, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        problemType = (RadioGroup) view.findViewById(R.id.problem_options);
        problemType.setOnCheckedChangeListener(this);
        snCodeTextView = (TextView) view.findViewById(R.id.sn_code);
        snCodeTextView.append(snCode);
        feedBackListView = (FeedBackListView) view.findViewById(R.id.feedback_list);
        submitButton = (MessageSubmitButton) view.findViewById(R.id.submit_btn);
        submitButton.setOnClickListener(this);
        phoneNumberText = (SakuraEditText) view.findViewById(R.id.phone_number_edit);
        descriptioinText = (SakuraEditText) view.findViewById(R.id.description_edit);

        arrowUp = (ImageView) view.findViewById(R.id.arrow_up);
        arrowDown = (ImageView) view.findViewById(R.id.arrow_down);

        arrowUp.setOnClickListener(this);
        arrowDown.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        createProblemsRadio(FeedbackProblem.getInstance().getCache());
        fetchFeedback(snCode, "5");
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.submit_btn) {
            initPopWindow();

        } else if (i == R.id.arrow_down) {
            feedBackListView.smoothScrollBy(100, 1);

        } else if (i == R.id.arrow_up) {
            feedBackListView.smoothScrollBy(-100, 1);

        }
    }

    private void createProblemsRadio(List<ProblemEntity> problemEntities) {
        RadioButton mRadioButton = null;
        for (int i = 0; i < problemEntities.size(); i++) {
            RadioButton radioButton = new RadioButton(getActivity());
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, (int) getResources().getDimension(R.dimen.feedback_radiogroup_margin), 0);
            radioButton.setLayoutParams(params);
            radioButton.setTextSize(getResources().getDimension(R.dimen.sakura_h8_text_size));
            radioButton.setText(problemEntities.get(i).getPoint_name());
            radioButton.setId(problemEntities.get(i).getPoint_id());

            if (i == 0)
                mRadioButton = radioButton;
            problemType.addView(radioButton);
        }

        if (null != mRadioButton)
            mRadioButton.setChecked(true);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        problemTextFlag = i;
    }


    private void fetchFeedback(String sn, String top) {
        ((LauncherActivity)getActivity()).mIrisService.Feedback(sn,top)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ChatMsgEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }


                    @Override
                    public void onNext(ChatMsgEntity chatMsgEntity) {
                        try {
                            if (chatMsgEntity.getCount() == 0) {
                                arrowDown.setVisibility(View.INVISIBLE);
                                arrowUp.setVisibility(View.INVISIBLE);
                            } else {
                                arrowDown.setVisibility(View.VISIBLE);
                                arrowUp.setVisibility(View.VISIBLE);
                            }
                            feedBackListView.setAdapter(new FeedbackListAdapter(FeedbackFragment.this.mContext, chatMsgEntity.getData()));
                        } catch (JsonSyntaxException e) {
                            arrowDown.setVisibility(View.INVISIBLE);
                            arrowUp.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }


    private void uploadFeedback() {


        String contactNumber = phoneNumberText.getText().toString();
        if (TextUtils.isEmpty(contactNumber)) {
            submitButton.setEnabled(true);
            Toast.makeText(getActivity(), R.string.fill_contact_number, Toast.LENGTH_LONG).show();

        } else if ((!isMobile(contactNumber) && !isPhone(contactNumber))) {
            submitButton.setEnabled(true);
            Toast.makeText(getActivity(), R.string.you_should_give_an_phone_number, Toast.LENGTH_LONG).show();

        } else {
            AccountSharedPrefs accountSharedPrefs = AccountSharedPrefs.getInstance();

            FeedBackEntity feedBack = new FeedBackEntity();
            feedBack.setDescription(descriptioinText.getText().toString());
            feedBack.setPhone(contactNumber);
            feedBack.setOption(problemTextFlag);
            feedBack.setCity(accountSharedPrefs.getSharedPrefs(AccountSharedPrefs.CITY));
            feedBack.setIp(accountSharedPrefs.getSharedPrefs(AccountSharedPrefs.IP));
            feedBack.setIsp(accountSharedPrefs.getSharedPrefs(AccountSharedPrefs.ISP));
            feedBack.setLocation(accountSharedPrefs.getSharedPrefs(AccountSharedPrefs.PROVINCE));
            UploadFeedback.getInstance().excute(feedBack, snCode, new UploadFeedback.Callback() {
                @Override
                public void success(String msg) {
                    Log.d(TAG, "uploadFeedback: " + msg);
                    fetchFeedback(snCode, "10");
                    Toast.makeText(mContext, "提交成功!", Toast.LENGTH_LONG).show();
                    submitButton.setEnabled(true);
                }

                @Override
                public void failure(String msg) {
//                    Log.d(TAG, "uploadFeedback: " + msg);
                    Toast.makeText(mContext, "提交失败!", Toast.LENGTH_LONG).show();
                    submitButton.setEnabled(true);
                }
            });
        }
    }


    /**
     * 手机号验证
     *
     * @param str
     * @return 验证通过返回true
     */
    public static boolean isMobile(String str) {
        Pattern p = null;
        Matcher m = null;
        boolean b = false;
        p = Pattern.compile("^[1][3,4,5,8][0-9]{9}$"); // 验证手机号
        m = p.matcher(str);
        b = m.matches();
        return b;
    }

    /**
     * 电话号码验证
     *
     * @param str
     * @return 验证通过返回true
     */
    public static boolean isPhone(String str) {
        Pattern p1 = null, p2 = null;
        Matcher m = null;
        boolean b = false;
        p1 = Pattern.compile("^[0][0-9]{2,3}-[0-9]{5,10}$");  // 验证带区号的
        p2 = Pattern.compile("^[1-9]{1}[0-9]{5,8}$");         // 验证没有区号的
        if (str.length() > 9) {
            m = p1.matcher(str);
            b = m.matches();
        } else {
            m = p2.matcher(str);
            b = m.matches();
        }
        return b;
    }

    private void initPopWindow() {
        submitButton.clearFocus();


        final MessageDialogFragment messageDialogFragment = new MessageDialogFragment(mContext, "是否提交反馈信息?", null);
        messageDialogFragment.showAtLocation(getView(), Gravity.CENTER, new MessageDialogFragment.ConfirmListener() {
            @Override
            public void confirmClick(View view) {
                messageDialogFragment.dismiss();
                submitButton.setEnabled(false);
                uploadFeedback();
            }
        }, new MessageDialogFragment.CancelListener() {
            @Override
            public void cancelClick(View view) {
                messageDialogFragment.dismiss();
            }
        });

    }
}
