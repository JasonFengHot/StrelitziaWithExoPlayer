package tv.ismar.usercenter.view;

import android.content.Context;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.open.androidtvwidget.bridge.RecyclerViewBridge;
import com.open.androidtvwidget.leanback.recycle.GridLayoutManagerTV;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import com.open.androidtvwidget.view.MainUpView;

import java.util.HashMap;
import java.util.List;

import cn.ismartv.injectdb.library.query.Select;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.AppConstant;
import tv.ismar.app.BaseFragment;
import tv.ismar.app.db.location.CityTable;
import tv.ismar.app.db.location.ProvinceTable;
import tv.ismar.app.network.entity.WeatherEntity;
import tv.ismar.usercenter.LocationContract;
import tv.ismar.usercenter.R;
import tv.ismar.usercenter.databinding.FragmentLocationBinding;
import tv.ismar.usercenter.viewmodel.LocationViewModel;

/**
 * Created by huibin on 10/27/16.
 */

public class LocationFragment extends BaseFragment implements LocationContract.View,
        RecyclerViewTV.OnItemListener, RecyclerViewTV.OnItemClickListener, OnHoverListener {
    private static final String TAG = LocationFragment.class.getSimpleName();
    private LocationViewModel mViewModel;
    private LocationContract.Presenter mPresenter;

    public static LocationFragment newInstance() {
        return new LocationFragment();
    }


    private RecyclerViewTV proviceGridView;

    private MainUpView mMainUpView;
    private RecyclerViewBridge mRecyclerViewBridge;
    private View oldView;

    private PopupWindow areaPopup;

    private ProvinceTable mProvinceTable;
    private CityTable mCityTable;
    private FragmentLocationBinding locationBinding;

    private boolean fragmentIsPause = false;

    private ProvinceAdapter provinceAdapter;
    private int[] citySelectedPosition = {-1};
    private View[] cityOldView;
    private TextView[] citySelectedView;

    private int provinceSelectedPosition = -1;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        locationBinding = FragmentLocationBinding.inflate(inflater, container, false);
        locationBinding.setTasks(mViewModel);
        locationBinding.setActionHandler(mPresenter);

        View root = locationBinding.getRoot();
        proviceGridView = locationBinding.provinceList;
        proviceGridView.addItemDecoration(new SpacesItemDecoration(getResources().getDimensionPixelSize(R.dimen.usercenter_province_recycler_item_spacing)));
        proviceGridView.setOnItemClickListener(this);
        proviceGridView.setFocusable(false);
        proviceGridView.setOnItemListener(this);

        mMainUpView = locationBinding.mainUpView;

        mMainUpView.setEffectBridge(new RecyclerViewBridge());
        // 注意这里，需要使用 RecyclerViewBridge 的移动边框 Bridge.
        mRecyclerViewBridge = (RecyclerViewBridge) mMainUpView.getEffectBridge();
        mRecyclerViewBridge.setUpRectResource(R.drawable.launcher_selector);
        float density = getResources().getDisplayMetrics().density;
        RectF receF = new RectF(40, 40, 40, 40);
        mRecyclerViewBridge.setDrawUpRectPadding(receF);

        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        locationBinding.tmp.setNextFocusLeftId(R.id.usercenter_help);
        locationBinding.getRoot().setNextFocusLeftId(R.id.usercenter_help);
        createLocationView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }



    @Override
    public void onResume() {
        super.onResume();
        AppConstant.purchase_page = "location";
        fragmentIsPause = false;
        Log.d(TAG, "onResume");
        mPresenter.start();


        HashMap<String, String> hashMap = IsmartvActivator.getInstance().getCity();
        String geoId = hashMap.get("geo_id");
        mPresenter.fetchWeather(geoId);

    }

    @Override
    public void onPause() {
        fragmentIsPause = true;
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
    }

    public void setViewModel(LocationViewModel viewModel) {
        mViewModel = viewModel;
    }

    @Override
    public void setPresenter(LocationContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void refreshWeather(WeatherEntity weatherEntity) {
        mViewModel.loadWeather(weatherEntity);
    }

    private void createLocationView() {
        List<ProvinceTable> provinceTables = new Select().from(ProvinceTable.class).execute();
        if (provinceTables != null && !provinceTables.isEmpty()) {
            provinceAdapter = new ProvinceAdapter(getContext(), provinceTables);
            proviceGridView.setLayoutManager(new GridLayoutManagerTV(getActivity(), 6));
            proviceGridView.setAdapter(provinceAdapter);

        }
    }

    @Override
    public void onItemPreSelected(RecyclerViewTV recyclerViewTV, View itemView, int i) {
        if (recyclerViewTV.getAdapter() instanceof ProvinceAdapter) {
            mRecyclerViewBridge.setUnFocusView(oldView);
            TextView textView = (TextView) itemView.findViewById(R.id.province_text);
            textView.setTextColor(getResources().getColor(R.color.color_base_white));
        } else if (recyclerViewTV.getAdapter() instanceof CityAdapter) {
            mRecyclerViewBridge.setUnFocusView(cityOldView[0]);
            TextView textView = (TextView) itemView.findViewById(R.id.province_text);
            if (citySelectedPosition[0] == i) {
                textView.setTextColor(getResources().getColor(R.color.blue));
            } else {
                textView.setTextColor(getResources().getColor(R.color.color_base_white));
            }
        }

    }

    @Override
    public void onItemSelected(RecyclerViewTV recyclerViewTV, View itemView, int i) {
        ((UserCenterActivity) getActivity()).clearTheLastHoveredVewState();
        if (recyclerViewTV.getAdapter() instanceof ProvinceAdapter) {
            TextView textView = (TextView) itemView.findViewById(R.id.province_text);
            textView.setTextColor(getResources().getColor(R.color.location_text_focus));
            mRecyclerViewBridge.setFocusView(itemView, 1.2f);
            oldView = itemView;
        } else if (recyclerViewTV.getAdapter() instanceof CityAdapter) {
            TextView textView = (TextView) itemView.findViewById(R.id.province_text);
            if (citySelectedPosition[0] == i) {
                textView.setTextColor(getResources().getColor(R.color.blue));
            } else {
                textView.setTextColor(getResources().getColor(R.color.location_text_focus));
            }
            mRecyclerViewBridge.setFocusView(itemView, 1.2f);
            cityOldView[0] = itemView;
        }
    }

    @Override
    public void onReviseFocusFollow(RecyclerViewTV recyclerViewTV, View itemView, int i) {
        if (recyclerViewTV.getAdapter() instanceof ProvinceAdapter) {
            mRecyclerViewBridge.setFocusView(itemView, 1.2f);
            oldView = itemView;
        } else if (recyclerViewTV.getAdapter() instanceof CityAdapter) {
            mRecyclerViewBridge.setFocusView(itemView, 1.2f);
            cityOldView[0] = itemView;
        }
    }

    @Override
    public void onItemClick(RecyclerViewTV recyclerViewTV, View view, int i) {
        Log.d(TAG, "onItemClick: position: " + i);
        if (recyclerViewTV.getAdapter() instanceof CityAdapter) {
            showPromptLayout(View.VISIBLE, i);

        } else if (recyclerViewTV.getAdapter() instanceof ProvinceAdapter) {
            locationBinding.tmp.requestFocus();
            provinceSelectedPosition = i;
            ProvinceAdapter provinceAdapter = (ProvinceAdapter) recyclerViewTV.getAdapter();
            ProvinceTable provinceTable = provinceAdapter.getProvinceTableList().get(i);
            mProvinceTable = provinceTable;
            showAreaPopup(provinceTable);
        }
    }

    private void showAreaPopup(ProvinceTable provinceTable) {
        cityOldView = new View[1];
        citySelectedPosition[0] = -1;
        citySelectedView = new TextView[1];
        String provinceId = provinceTable.province_id;
        final List<CityTable> locationTableList = new Select().from(CityTable.class).where(CityTable.PROVINCE_ID + " = ?", provinceId).execute();
//
        CityAdapter cityAdapter = new CityAdapter(getContext(), locationTableList);
        proviceGridView.setAdapter(cityAdapter);

        locationBinding.confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationBinding.tmp.requestFocus();
                IsmartvActivator activator = IsmartvActivator.getInstance();
                activator.setProvince(mProvinceTable.province_name, mProvinceTable.pinyin);
                activator.setCity(mCityTable.city, String.valueOf(mCityTable.geo_id));
                mPresenter.fetchWeather(String.valueOf(mCityTable.geo_id));
                locationBinding.promptLayout.setVisibility(View.INVISIBLE);
                mViewModel.setSelectedCity("");
                mViewModel.loadselectedCity();

                proviceGridView.setAdapter(provinceAdapter);
                ((UserCenterActivity) getActivity()).refreshWeather();
            }
        });

        locationBinding.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationBinding.tmp.requestFocus();
                citySelectedPosition[0] = -1;
                if (citySelectedView[0] != null) {
                    citySelectedView[0].setTextColor(getResources().getColor(R.color.color_base_white));
                    citySelectedView[0] = null;
                }
                locationBinding.promptLayout.setVisibility(View.INVISIBLE);
                mViewModel.setSelectedCity("");
                mViewModel.loadselectedCity();
                setNextFocusDown(true);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        View focusView = proviceGridView.getChildAt(0);
                        focusView.requestFocusFromTouch();
                        focusView.requestFocus();
                        mRecyclerViewBridge.setFocusView(focusView, 1.2f);
                        ((TextView) focusView.findViewById(R.id.province_text)).setTextColor(getResources().getColor(R.color.location_text_focus));
                        cityOldView[0] = focusView;
                    }
                }, 100);

//
            }
        });

        locationBinding.confirmBtn.setOnHoverListener(this);
        locationBinding.cancelBtn.setOnHoverListener(this);

        setNextFocusDown(true);

    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                v.requestFocus();
                v.requestFocusFromTouch();
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                locationBinding.tmp.requestFocus();
                break;
        }
        return true;
    }

    private class ProvinceAdapter extends RecyclerView.Adapter<LocationViewHolder> implements OnHoverListener {
        private Context mContext;

        private List<ProvinceTable> mProvinceTableList;

        public List<ProvinceTable> getProvinceTableList() {
            return mProvinceTableList;
        }

        public ProvinceAdapter(Context context, List<ProvinceTable> provinceTableList) {
            mContext = context;
            mProvinceTableList = provinceTableList;
        }

        @Override

        public LocationViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_province, viewGroup, false);
            view.setOnHoverListener(this);
            LocationViewHolder holder = new LocationViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(LocationViewHolder holder, int position) {
            ProvinceTable provinceTable = mProvinceTableList.get(position);
            StringBuffer stringBuffer = new StringBuffer(provinceTable.province_name);
            if (stringBuffer.length() == 2) {
                stringBuffer.insert(1, "    ");
            }
            holder.mTextView.setText(stringBuffer);

            holder.itemView.setId(View.generateViewId());
            if (position == 0 || position == 1 || position == 2 || position == 3 || position == 4) {
                holder.itemView.setNextFocusUpId(holder.itemView.getId());
            }

            if ((position + 1) % 6 == 0) {
                holder.itemView.setNextFocusRightId(holder.itemView.getId());
            }
            if (position == 5) {
                holder.itemView.setNextFocusUpId(holder.itemView.getId());
            }

            if (position <= mProvinceTableList.size() - 1 - 1 && position >= mProvinceTableList.size() - 1 - 3) {
                holder.itemView.setNextFocusDownId(holder.itemView.getId());
            }

            if (position == provinceSelectedPosition) {
                holder.itemView.requestFocus();
                mRecyclerViewBridge.setFocusView(holder.itemView, 1.2f);
                holder.mTextView.setTextColor(getResources().getColor(R.color.location_text_focus));
            }

            if ((position + 1) % 6 == 1) {
                holder.itemView.setNextFocusLeftId(R.id.usercenter_location);
            }
        }

        @Override
        public int getItemCount() {
            return mProvinceTableList.size();
        }

        @Override
        public boolean onHover(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                case MotionEvent.ACTION_HOVER_MOVE:
                    if (!v.hasFocus()) {
                        v.requestFocus();
                        v.requestFocusFromTouch();
                    }
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    if (!fragmentIsPause) {
                        locationBinding.tmp.requestFocus();
                        locationBinding.tmp.requestFocusFromTouch();
                    }
                    break;
            }
            return true;
        }

    }

    private class CityAdapter extends RecyclerView.Adapter<LocationViewHolder> implements OnHoverListener {
        private Context mContext;

        private List<CityTable> mCityTableList;


        public List<CityTable> getCityTableList() {
            return mCityTableList;
        }

        public CityAdapter(Context context, List<CityTable> cityTableList) {
            mContext = context;
            mCityTableList = cityTableList;
        }

        @Override
        public LocationViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_province, viewGroup, false);
            view.setOnHoverListener(this);
            LocationViewHolder holder = new LocationViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(LocationViewHolder holder, int position) {
            CityTable cityTable = mCityTableList.get(position);
            StringBuffer stringBuffer = new StringBuffer(cityTable.city);
            if (stringBuffer.length() == 2) {
                stringBuffer.insert(1, "    ");
            }
            holder.mTextView.setText(stringBuffer);

            holder.itemView.setId(View.generateViewId());
            if (position == 0 || position == 1 || position == 2 || position == 3 || position == 4) {
                holder.itemView.setNextFocusUpId(holder.itemView.getId());
            }

            if ((position + 1) % 6 == 0) {
                holder.itemView.setNextFocusRightId(holder.itemView.getId());
            }
            if (position == 5) {
                holder.itemView.setNextFocusUpId(holder.itemView.getId());
            }


            if (position == 0) {
                holder.itemView.requestFocus();
                mRecyclerViewBridge.setFocusView(holder.itemView, 1.2f);
                holder.mTextView.setTextColor(getResources().getColor(R.color.location_text_focus));
                cityOldView[0] = holder.itemView;
            }

            if ((position + 1) % 6 == 1) {
                holder.itemView.setNextFocusLeftId(R.id.usercenter_location);
            }

            if (position == mCityTableList.size() - 1) {
                holder.itemView.setNextFocusRightId(holder.itemView.getId());
            }

//
//            if (position <= mCityTableList.size() - 1 - 1 && position >= mCityTableList.size() - 1 - 3) {
//                holder.itemView.setNextFocusDownId(holder.itemView.getId());
//            }
//
            int remainCount = mCityTableList.size() % 6;
            if (remainCount != 0) {
                if (position + 1 > mCityTableList.size() && position + 1 <= mCityTableList.size() - remainCount) {
                    holder.itemView.setNextFocusDownId(View.NO_ID);
                }

                if (position + 1 > mCityTableList.size() - remainCount) {
                    holder.itemView.setNextFocusDownId(holder.itemView.getId());
                }
            }
        }

        @Override
        public int getItemCount() {
            return mCityTableList.size();
        }

        @Override
        public boolean onHover(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                case MotionEvent.ACTION_HOVER_MOVE:
                    if (!v.hasFocus()) {
                        ((UserCenterActivity) getActivity()).clearTheLastHoveredVewState();
                        v.requestFocus();
                        v.requestFocusFromTouch();
                    }
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    if (!fragmentIsPause) {
                        locationBinding.tmp.requestFocus();
                        locationBinding.tmp.requestFocusFromTouch();
                    }
                    break;
            }

            return true;
        }
    }

    private class LocationViewHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private TextView mTextView;

        public LocationViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            mTextView = (TextView) itemView.findViewById(R.id.province_text);
        }
    }


    public void clearStatus() {
        if (areaPopup != null) {
            areaPopup.dismiss();
            areaPopup = null;
        }
    }

    private void showPromptLayout(int visibility, int position) {
        if (citySelectedPosition[0] >= 0) {
            View lastSelectedView = proviceGridView.getChildAt(citySelectedPosition[0]);
            TextView lastTextView = (TextView) lastSelectedView.findViewById(R.id.province_text);
            lastTextView.setTextColor(getResources().getColor(R.color.color_base_white));
        }
        citySelectedPosition[0] = position;

        TextView textView = (TextView) proviceGridView.getChildAt(position).findViewById(R.id.province_text);
        textView.setTextColor(getResources().getColor(R.color.blue));
        citySelectedView[0] = textView;

        mCityTable = ((CityAdapter) proviceGridView.getAdapter()).getCityTableList().get(position);
        mViewModel.setSelectedCity(mCityTable.city);
        mViewModel.loadselectedCity();
        locationBinding.promptLayout.setVisibility(visibility);
        locationBinding.confirmBtn.requestFocus();

        locationBinding.confirmBtn.setNextFocusUpId(proviceGridView.getChildAt(proviceGridView.getChildCount() - 1).getId());
        locationBinding.cancelBtn.setNextFocusUpId(proviceGridView.getChildAt(proviceGridView.getChildCount() - 1).getId());
        setNextFocusDown(false);


    }

    private void setNextFocusDown(boolean self) {

        int remainCount = proviceGridView.getChildCount() % 6;
        if (self) {
            for (int position = 0; position < proviceGridView.getChildCount(); position++) {
                if (remainCount != 0) {
                    if (position + 1 > proviceGridView.getChildCount() - remainCount) {
                        proviceGridView.getChildAt(position).setNextFocusDownId(proviceGridView.getChildAt(position).getId());
                    }
                }
            }
        } else {
            for (int position = 0; position < proviceGridView.getChildCount(); position++) {
                if (remainCount != 0) {

                    if (position + 1 > proviceGridView.getChildCount() - remainCount) {
                        proviceGridView.getChildAt(position).setNextFocusDownId(locationBinding.confirmBtn.getId());
                    }
                }
            }
        }
    }

    protected boolean onBackPressed() {
        if (proviceGridView.getAdapter() instanceof CityAdapter) {
            mViewModel.setSelectedCity("");
            mViewModel.loadselectedCity();
            locationBinding.promptLayout.setVisibility(View.INVISIBLE);
            proviceGridView.setAdapter(provinceAdapter);
            return true;
        } else {
            return false;
        }
    }
}
