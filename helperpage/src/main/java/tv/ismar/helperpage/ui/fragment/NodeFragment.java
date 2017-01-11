package tv.ismar.helperpage.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import tv.ismar.app.util.BitmapDecoder;
import tv.ismar.helperpage.R;
import tv.ismar.helperpage.core.CdnCacheLoader;
import tv.ismar.helperpage.core.HttpDownloadTask;
import tv.ismar.helperpage.ui.activity.HomeActivity;
import tv.ismar.helperpage.ui.adapter.IspSpinnerAdapter;
import tv.ismar.helperpage.ui.adapter.NodeListAdapter;
import tv.ismar.helperpage.ui.adapter.ProvinceSpinnerAdapter;
import tv.ismar.helperpage.ui.widget.IspSpinnerPopWindow;
import tv.ismar.helperpage.ui.widget.ProvinceSpinnerPopWindow;
import tv.ismar.helperpage.ui.widget.SakuraButton;
import tv.ismar.helperpage.ui.widget.SakuraListView;
import tv.ismar.helperpage.utils.StringUtils;
import cn.ismartv.injectdb.library.ActiveAndroid;
import cn.ismartv.injectdb.library.content.ContentProvider;
import cn.ismartv.injectdb.library.query.Select;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.app.core.DaisyUtils;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.VodUserAgent;
import tv.ismar.app.core.preferences.AccountSharedPrefs;
import tv.ismar.app.db.location.CdnTable;
import tv.ismar.app.db.location.IspTable;
import tv.ismar.app.db.location.ProvinceTable;
import tv.ismar.app.network.entity.BindedCdnEntity;
import tv.ismar.app.network.entity.Empty;
import tv.ismar.app.network.entity.SpeedLogEntity;
import tv.ismar.app.ui.MessageDialogFragment;

/**
 * Created by huaijie on 2015/4/8.
 */
public class NodeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        HttpDownloadTask.OnCompleteListener, View.OnHoverListener,View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "NodeFragment";

    private static String NORMAL_SELECTION = CdnTable.DISTRICT_ID + "=? and " + CdnTable.ISP_ID + "=?" + " or " + CdnTable.CDN_FLAG + "  <> ?" + " ORDER BY " + CdnTable.ISP_ID + " DESC," + CdnTable.SPEED + " DESC";
    private static String OTHER_SELECTION = CdnTable.DISTRICT_ID + "=? and " + CdnTable.ISP_ID + " in (?, ?)" + " or " + CdnTable.CDN_FLAG + "  <> ?" + " ORDER BY " + CdnTable.ISP_ID + " DESC," + CdnTable.SPEED + " DESC";

    private static final String NOT_THIRD_CDN = "0";

    private static final int NORMAL_ISP_FLAG = 01245;
    private static final int OTHER_ISP_FLAG = 3;

    private String TIE_TONG = "";

    private SakuraListView nodeListView;
    private NodeListAdapter nodeListAdapter;
    private TextView currentNodeTextView;
    private SakuraButton unbindButton;
    private TextView provinceSpinner;
    private TextView ispSpinner;
    private SakuraButton speedTestButton;

    private MessageDialogFragment selectNodePup;
    private Dialog cdnTestDialog;
    private MessageDialogFragment cdnTestCompletedPop;


    private String[] selectionArgs = {"0", "0"};
    private String[] cities;

    private String mDistrictId = "";
    private String mIspId = "";

    /**
     * 传入下载中的 CDN 节点 ID
     */
    private List<Integer> cdnCollections;

    private String snCode = TextUtils.isEmpty(SimpleRestClient.sn_token) ? "sn is null" : SimpleRestClient.sn_token;


    private Context mContext;

    private HttpDownloadTask httpDownloadTask;


    private float rate;
    private BitmapDecoder bitmapDecoder;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rate = DaisyUtils.getVodApplication(getActivity()).getRate(getActivity());
        TIE_TONG = StringUtils.getMd5Code("铁通");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.sakura_fragment_node, null);
        bitmapDecoder = new BitmapDecoder();
        bitmapDecoder.decode(mContext, R.drawable.sakura_bg_fragment, new BitmapDecoder.Callback() {
            @Override
            public void onSuccess(BitmapDrawable bitmapDrawable) {
                view.setBackgroundDrawable(bitmapDrawable);
            }
        });
        currentNodeTextView = (TextView) view.findViewById(R.id.current_node_text);
        unbindButton = (SakuraButton) view.findViewById(R.id.unbind_node);
        nodeListView = (SakuraListView) view.findViewById(R.id.node_list);
        nodeListAdapter = new NodeListAdapter(mContext, null, true);
        nodeListView.setAdapter(nodeListAdapter);

        provinceSpinner = (TextView) view.findViewById(R.id.province_spinner);
        ispSpinner = (TextView) view.findViewById(R.id.isp_spinner);
        provinceSpinner.setOnClickListener(this);
        provinceSpinner.setOnHoverListener(this);
        ispSpinner.setOnClickListener(this);
        ispSpinner.setOnHoverListener(this);
        speedTestButton = (SakuraButton) view.findViewById(R.id.speed_test_btn);

        speedTestButton.setOnClickListener(this);
        nodeListView.setOnItemClickListener(this);
        unbindButton.setOnClickListener(this);

        nodeListView.setNextFocusDownId(nodeListView.getId());
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        List<ProvinceTable> provinceTables = new Select().from(ProvinceTable.class).execute();
        ProvinceSpinnerAdapter provinceSpinnerAdapter = new ProvinceSpinnerAdapter(mContext, provinceTables);
      //  provinceSpinner.setAdapter(provinceSpinnerAdapter);
        String accountProvince = AccountSharedPrefs.getInstance().getSharedPrefs(AccountSharedPrefs.PROVINCE);

        ProvinceTable provinceTable = new Select().from(ProvinceTable.class).
                where(ProvinceTable.PROVINCE_NAME + " = ?", accountProvince).executeSingle();
        if (provinceTable != null) {
            provinceSpinner.setText(accountProvince);
            mDistrictId = provinceTable.district_id;
            notifiySourceChanged();
        } else {
            provinceSpinner.setText("北京");
            mDistrictId = "3f0cb8b8c238c3b4e08898ce6d449c8d";
            notifiySourceChanged();
        }



        List<IspTable> ispTables = new Select().from(IspTable.class).execute();
        IspSpinnerAdapter ispSpinnerAdapter = new IspSpinnerAdapter(mContext, ispTables);
     //   ispSpinner.setAdapter(ispSpinnerAdapter);

        String accountIsp = AccountSharedPrefs.getInstance().getSharedPrefs(AccountSharedPrefs.ISP);
        IspTable ispTable = new Select().from(IspTable.class).where(IspTable.ISP_NAME + " = ?", accountIsp).executeSingle();
        if (ispTable != null) {
            ispSpinner.setText(accountIsp);
            mIspId = ispTable.isp_id;
            notifiySourceChanged();
        } else {
            ispSpinner.setText("电信");
            mIspId = "f6f77a498c2b2c7e04ead59fbeec5128";
            notifiySourceChanged();
        }
    //    setSpinnerItemSelectedListener();
        getLoaderManager().initLoader(NORMAL_ISP_FLAG, null, this);

        speedTestButton.requestFocus();


    }

    @Override
    public void onResume() {
        super.onResume();
        speedTestButton.requestFocus();
    }

    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
//                if (nodeListView.getSelectedView() != null) {
//                    nodeListView.getSelectedView().setSelected(false);
//                }
                if (!v.isFocused()) {
                    v.requestFocusFromTouch();
                    v.requestFocus();
                }
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                break;
        }
        return true;
    }
    @Override
    public Loader onCreateLoader(int flag, Bundle args) {
        CursorLoader cacheLoader = new CdnCacheLoader(mContext, ContentProvider.createUri(CdnTable.class, null),
                null, null, null, null);
        switch (flag) {
            case NORMAL_ISP_FLAG:
                cacheLoader.setSelection(NORMAL_SELECTION);
                cacheLoader.setSelectionArgs(selectionArgs);
                break;
            case OTHER_ISP_FLAG:
                cacheLoader.setSelection(OTHER_SELECTION);
                cacheLoader.setSelectionArgs(selectionArgs);
                break;
            default:
                break;
        }
        return cacheLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cdnCollections = cursorToList(data);
        nodeListAdapter.swapCursor(data);
        updateCurrentNode();
    }


    @Override
    public void onLoaderReset(Loader loader) {
        nodeListAdapter.swapCursor(null);
    }

 //   private void setSpinnerItemSelectedListener() {
//        provinceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
//                ProvinceTable provinceTable = new Select().from(ProvinceTable.class).where("_id = ?", position + 1).executeSingle();
//                if (provinceTable != null) {
//                    mDistrictId = provinceTable.district_id;
//                    notifiySourceChanged();
//                }
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });

//        ispSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
//                IspTable ispTable = new Select().from(IspTable.class).where("_id = ?", position + 1).executeSingle();
//                if (ispTable != null) {
//                    mIspId = ispTable.isp_id;
//                    notifiySourceChanged();
//                }
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });
  //  }


    private void notifiySourceChanged() {
        if (mIspId.equals(TIE_TONG)) {

            String unicom = StringUtils.getMd5Code("联通");
            String chinaMobile = StringUtils.getMd5Code("移动");
            selectionArgs = new String[]{mDistrictId, chinaMobile, unicom, NOT_THIRD_CDN};
            getLoaderManager().destroyLoader(NORMAL_ISP_FLAG);
            getLoaderManager().restartLoader(OTHER_ISP_FLAG, null, NodeFragment.this).forceLoad();
        } else {
            selectionArgs = new String[]{mDistrictId, mIspId, NOT_THIRD_CDN};
            getLoaderManager().destroyLoader(OTHER_ISP_FLAG);
            getLoaderManager().restartLoader(NORMAL_ISP_FLAG, null, NodeFragment.this).forceLoad();
        }

    }

    @Override
    public void onDestroy() {
        if (null != selectNodePup) {
            selectNodePup.dismiss();
        }
        if (null != cdnTestCompletedPop) {
            cdnTestCompletedPop.dismiss();
        }
        if (null != cdnTestDialog) {
            cdnTestDialog.dismiss();
        }

        super.onDestroy();
    }

    IspSpinnerPopWindow ispSpinnerPopWindow;
    ProvinceSpinnerPopWindow spinnerPopWindow;
    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.unbind_node) {
            unbindNode(SimpleRestClient.sn_token);

        } else if (i == R.id.speed_test_btn) {
            showCdnTestDialog();

        } else if (i == R.id.isp_spinner) {
            final List<IspTable> ispTables = new Select().from(IspTable.class).execute();
            ispSpinnerPopWindow = new IspSpinnerPopWindow(getContext(), ispTables, new IspSpinnerPopWindow.OnItemClickListener() {
                @Override
                public void onItemClick(View itemView) {
                    IspTable table = (IspTable) itemView.getTag();
                    mIspId = table.isp_id;
                    ispSpinner.setText(table.isp_name);
                    notifiySourceChanged();
                }
            });
            ispSpinnerPopWindow.showAsDropDown(ispSpinner);

        } else if (i == R.id.province_spinner) {
            List<ProvinceTable> provinceTables = new Select().from(ProvinceTable.class).execute();
            spinnerPopWindow = new ProvinceSpinnerPopWindow(getContext(), provinceTables, new ProvinceSpinnerPopWindow.OnItemClickListener() {
                @Override
                public void onItemClick(View itemView) {
                    ProvinceTable table = (ProvinceTable) itemView.getTag();
                    mDistrictId = table.district_id;
                    provinceSpinner.setText(table.province_name);
                    notifiySourceChanged();
                }
            });
            spinnerPopWindow.showAsDropDown(provinceSpinner);

        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        showSelectNodePop((Integer) view.getTag());
    }


    //    /**
//     * fetchBindedCdn
//     *
//     * @param snCode
//     */
    private void fetchBindedCdn(final String snCode) {
        ((HomeActivity)getActivity()).mWxApiService.GetBindCdn(snCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BindedCdnEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ((HomeActivity) getActivity()).showPop(e);
                        e.printStackTrace();
                    }


                    @Override
                    public void onNext(BindedCdnEntity bindedCdnEntity) {
                        if (BindedCdnEntity.NO_RECORD.equals(bindedCdnEntity.getRetcode())) {
                            clearCheck();
                        } else {
                            updateCheck(Integer.parseInt(bindedCdnEntity.getSncdn().getCdnid()));
                        }
                    }
                });

    }

    private void bindCdn(final String snCode, final int cdnId) {
        ((HomeActivity)getActivity()).mWxApiService.BindCdn(snCode,cdnId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Empty>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "bindCdn error");
                        ((HomeActivity) getActivity()).showPop(e);
                    }


                    @Override
                    public void onNext(Empty empty) {
                        Toast.makeText(mContext, R.string.node_bind_success, Toast.LENGTH_LONG).show();
                        fetchBindedCdn(snCode);
                    }
                });

    }

    private void unbindNode(final String snCode) {
        ((HomeActivity)getActivity()).mWxApiService.UnbindNode(snCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Empty>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "unbindNode");
                        ((HomeActivity) getActivity()).showPop(e);
                    }


                    @Override
                    public void onNext(Empty empty) {
                        fetchBindedCdn(snCode);
                    }
                });
    }

    /**
     * updateCheck
     *
     * @param cdnId
     */
    public static void updateCheck(int cdnId) {
        ActiveAndroid.beginTransaction();

        try {
            CdnTable checkedItem = new Select().from(CdnTable.class).where(CdnTable.CHECKED + " = ?", true).executeSingle();
            if (null != checkedItem) {
                checkedItem.checked = false;
                checkedItem.save();
            }

            CdnTable cdnCacheTable = new Select().from(CdnTable.class).where(CdnTable.CDN_ID + " = ?", cdnId).executeSingle();
            if (null != cdnCacheTable) {
                cdnCacheTable.checked = true;
                cdnCacheTable.save();
            }

            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }
    /**
     * clearCheck
     */
    private void clearCheck() {
        CdnTable checkedItem = new Select().from(CdnTable.class).where("checked = ?", true).executeSingle();
        if (null != checkedItem) {
            checkedItem.checked = false;
            checkedItem.save();
        }
    }

    private void updateCurrentNode() {
        CdnTable cdnCacheTable = new Select().from(CdnTable.class).where("checked = ?", true).executeSingle();
        if (cdnCacheTable != null) {
            currentNodeTextView.setText(getText(R.string.current_node) + cdnCacheTable.cdn_nick);
            unbindButton.setText(R.string.switch_to_auto);
            unbindButton.setTextColor(getResources().getColor(R.color._ffffff));
            unbindButton.setEnabled(true);
            unbindButton.setFocusable(true);
            unbindButton.setFocusableInTouchMode(true);
        } else {
            currentNodeTextView.setText(getText(R.string.current_node) + getString(R.string.auto_fetch));
            unbindButton.setText(R.string.already_to_auto);
            unbindButton.setEnabled(false);
            unbindButton.setFocusable(false);
            unbindButton.setFocusableInTouchMode(false);
        }

    }


    /**
     * showSelectNodePop
     *
     * @param cndId
     */
    private void showSelectNodePop(final int cndId) {
        selectNodePup = new MessageDialogFragment(mContext, getString(R.string.are_you_sure_selecte), null);
        selectNodePup.showAtLocation(getView(), Gravity.CENTER, new MessageDialogFragment.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        bindCdn(SimpleRestClient.sn_token, cndId);
                        selectNodePup.dismiss();
                    }
                },
                new MessageDialogFragment.CancelListener() {
                    @Override
                    public void cancelClick(View view) {
                        selectNodePup.dismiss();
                    }
                }
        );
    }


    private void showCdnTestDialog() {
        cdnTestDialog = new Dialog(mContext, R.style.ProgressDialog);
        Window dialogWindow = cdnTestDialog.getWindow();
        dialogWindow.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();

        lp.width = 400;
        lp.height = 150;
        View mView = LayoutInflater.from(mContext).inflate(R.layout.sakura_dialog_cdn_test_progress, null);
        cdnTestDialog.setContentView(mView, lp);
        cdnTestDialog.setCanceledOnTouchOutside(false);

        cdnTestDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                    case KeyEvent.KEYCODE_ESCAPE:
                        dialog.dismiss();
                        showCdnTestCompletedPop(Status.CANCEL);
                        return true;
                }
                return false;
            }
        });

        cdnTestDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                /**
                 * 开始测速
                 */
                httpDownloadTask = new HttpDownloadTask(mContext);
                httpDownloadTask.setCompleteListener(NodeFragment.this);
                if(cdnCollections!=null){
                    httpDownloadTask.execute(cdnCollections);
                }
            }
        });
        cdnTestDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(httpDownloadTask != null)
                httpDownloadTask.cancel(true);
                httpDownloadTask = null;
            }
        });
        cdnTestDialog.show();
    }


    private void showCdnTestCompletedPop(final Status status) {
        int titleRes;
        speedTestButton.clearFocus();
        switch (status) {
            case COMPLETE:

                titleRes = R.string.test_complete_text;
                break;
            case CANCEL:
                titleRes = R.string.test_interupt;
                break;
            default:
                titleRes = R.string.test_complete_text;
                break;
        }


        cdnTestCompletedPop = new MessageDialogFragment(mContext, getString(titleRes), null);
        cdnTestCompletedPop.showAtLocation(getView(), Gravity.CENTER, new MessageDialogFragment.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        cdnTestCompletedPop.dismiss();
                        nodeListView.setSelectionOne();
                    }
                },
                null
        );


    }

    @Override
    public void onSingleComplete(String cndId, String nodeName, String speed) {
        SpeedLogEntity speedLog = new SpeedLogEntity();
        speedLog.setCdn_id(cndId);
        speedLog.setCdn_name(nodeName);
        speedLog.setSpeed(speed);

        speedLog.setLocation(AccountSharedPrefs.getInstance().getSharedPrefs(AccountSharedPrefs.CITY));
        speedLog.setLocation(AccountSharedPrefs.getInstance().getSharedPrefs(AccountSharedPrefs.ISP));


        Gson gson = new Gson();
        String data = gson.toJson(speedLog, SpeedLogEntity.class);
        String base64Data = Base64.encodeToString(data.getBytes(), Base64.DEFAULT);


      //  uploadTestResult(cndId, speed);
     //   uploadCdnTestLog(base64Data, snCode, VodUserAgent.getModelName());
    }

    @Override
    public void onAllComplete() {
        if (cdnTestDialog != null) {
            cdnTestDialog.dismiss();
        }
        showCdnTestCompletedPop(Status.COMPLETE);

    }

    @Override
    public void onCancel() {

    }


    private void uploadCdnTestLog(String data, String snCode, String model) {
        ((HomeActivity)getActivity()).mWxApiService.DeviceLog(data,snCode,model)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Empty>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                    }


                    @Override
                    public void onNext(Empty empty) {
                        Log.d(TAG, "uploadCdnTestLog success");
                    }
                });
    }


    public void uploadTestResult(String cdnId, String speed) {
        String ACTION_TYPE = "submitTestData";
        ((HomeActivity)getActivity()).mWxApiService.UploadResult(ACTION_TYPE,snCode,cdnId,speed)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Empty>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "uploadTestResult");
                        ((HomeActivity) getActivity()).showPop(e);
                    }


                    @Override
                    public void onNext(Empty empty) {
                        Log.i(TAG, "uploadTestResult success");
                    }
                });
    }

    /**
     * 将 cursor 转为 list, 因为 在使用 cursor 的时候,可能已经关闭了
     */
    public static List<Integer> cursorToList(Cursor cursor) {
        List<Integer> cdnCollections = new ArrayList<Integer>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            cdnCollections.add(cursor.getInt(cursor.getColumnIndex("cdn_id")));
        }
        return cdnCollections;
    }

    enum Status {
        CANCEL,
        COMPLETE
    }
}
