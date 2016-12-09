package tv.ismar.usercenter.view;

import android.content.Context;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.open.androidtvwidget.bridge.RecyclerViewBridge;
import com.open.androidtvwidget.leanback.recycle.RecyclerViewTV;
import com.open.androidtvwidget.view.MainUpView;

import java.util.HashMap;
import java.util.List;

import cn.ismartv.injectdb.library.query.Select;
import tv.ismar.account.IsmartvActivator;
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

public class LocationFragment extends BaseFragment implements LocationContract.View, RecyclerViewTV.OnItemListener, RecyclerViewTV.OnItemClickListener {
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
    private View cityTmpView;

    private boolean fragmentIsPause = false;


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
        fragmentIsPause = false;
        Log.d(TAG, "onResume");
        mPresenter.start();
        createLocationView();

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
            ProvinceAdapter provinceAdapter = new ProvinceAdapter(getContext(), provinceTables);
            proviceGridView.setLayoutManager(new GridLayoutManager(getActivity(), 6));
            proviceGridView.setAdapter(provinceAdapter);

        }
    }

    @Override
    public void onItemPreSelected(RecyclerViewTV recyclerViewTV, View itemView, int i) {
        mRecyclerViewBridge.setUnFocusView(oldView);
        TextView textView = (TextView) itemView.findViewById(R.id.province_text);
        textView.setTextColor(getResources().getColor(R.color.color_base_white));

    }

    @Override
    public void onItemSelected(RecyclerViewTV recyclerViewTV, View itemView, int i) {
        TextView textView = (TextView) itemView.findViewById(R.id.province_text);
        textView.setTextColor(getResources().getColor(R.color.location_text_focus));
        mRecyclerViewBridge.setFocusView(itemView, 1.2f);
        oldView = itemView;
    }

    @Override
    public void onReviseFocusFollow(RecyclerViewTV recyclerViewTV, View itemView, int i) {
        mRecyclerViewBridge.setFocusView(itemView, 1.2f);
        oldView = itemView;
    }

    @Override
    public void onItemClick(RecyclerViewTV recyclerViewTV, View view, int i) {
        Log.d(TAG, "onItemClick: position: " + i);
        ProvinceAdapter provinceAdapter = (ProvinceAdapter) recyclerViewTV.getAdapter();
        ProvinceTable provinceTable = provinceAdapter.getProvinceTableList().get(i);
        mProvinceTable = provinceTable;
        showAreaPopup(getContext(), provinceTable);
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
            if (position == mCityTableList.size() -1){
                holder.itemView.setNextFocusDownId(R.id.confirm_btn);
            }
            CityTable cityTable = mCityTableList.get(position);
            StringBuffer stringBuffer = new StringBuffer(cityTable.city);
            if (stringBuffer.length() == 2) {
                stringBuffer.insert(1, "    ");
            }
            holder.mTextView.setText(stringBuffer);


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
                        cityTmpView.requestFocus();
                        cityTmpView.requestFocusFromTouch();
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

    private void showAreaPopup(final Context mContext, final ProvinceTable provinceTable) {
        final View[] cityOldView = new View[1];
        final int[] citySelectedPosition = {-1};
        final TextView[] citySelectedView = new TextView[1];

        String provinceId = provinceTable.province_id;
        final View popupLayout = LayoutInflater.from(mContext).inflate(R.layout.popup_area, null);
        cityTmpView = popupLayout.findViewById(R.id.tmp);
        final View promptLayout = popupLayout.findViewById(R.id.prompt_layout);

        RecyclerViewTV cityGridView = (RecyclerViewTV) popupLayout.findViewById(R.id.area_grid);
        cityGridView.addItemDecoration(new SpacesItemDecoration(getResources().getDimensionPixelSize(R.dimen.usercenter_province_recycler_item_spacing)));
        cityGridView.setNextFocusDownId(R.id.cancel_btn);

        final Button confirmBtn = (Button) popupLayout.findViewById(R.id.confirm_btn);
        final Button cancelBtn = (Button) popupLayout.findViewById(R.id.cancel_btn);
        cancelBtn.setNextFocusDownId(R.id.cancel_btn);
        confirmBtn.setNextFocusDownId(R.id.confirm_btn);
        confirmBtn.setOnHoverListener(new OnHoverListener() {

            @Override
            public boolean onHover(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER
                        || event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                    v.requestFocus();
                }
                return false;
            }
        });
        cancelBtn.setOnHoverListener(new OnHoverListener() {

            @Override
            public boolean onHover(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER
                        || event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                    v.requestFocus();
                }
                return false;
            }
        });


        final TextView selectPrompt = (TextView) popupLayout.findViewById(R.id.area_select_prompt);
        int width = (int) mContext.getResources().getDimension(R.dimen.location_area_pop_width);
        int height = (int) mContext.getResources().getDimension(R.dimen.location_area_pop_height);
        areaPopup = new PopupWindow(popupLayout, width, height);
        areaPopup.setFocusable(true);
        areaPopup.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.transparent));
        int xOffset = (int) mContext.getResources().getDimensionPixelSize(R.dimen.locationFragment_areaPop_xOffset);
        int yOffset = (int) mContext.getResources().getDimensionPixelSize(R.dimen.locationFragment_areaPop_yOffset);
        areaPopup.showAtLocation(getView(), Gravity.CENTER, xOffset, yOffset);
        final List<CityTable> locationTableList = new Select().from(CityTable.class).where(CityTable.PROVINCE_ID + " = ?", provinceId).execute();

        CityAdapter cityAdapter = new CityAdapter(mContext, locationTableList);

        cityGridView.setLayoutManager(new GridLayoutManager(mContext, 6));

        cityGridView.setAdapter(cityAdapter);
        cityGridView.setOnItemClickListener(new RecyclerViewTV.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerViewTV recyclerViewTV, View itemView, int i) {
                if (citySelectedPosition[0] >= 0) {
                    View lastSelectedView = recyclerViewTV.getChildAt(citySelectedPosition[0]);
                    TextView lastTextView = (TextView) lastSelectedView.findViewById(R.id.province_text);
                    lastTextView.setTextColor(getResources().getColor(R.color.color_base_white));
                }
                citySelectedPosition[0] = i;

                TextView textView = (TextView) itemView.findViewById(R.id.province_text);
                textView.setTextColor(getResources().getColor(R.color.blue));
                citySelectedView[0] = textView;

                mCityTable = ((CityAdapter) recyclerViewTV.getAdapter()).getCityTableList().get(i);
                mViewModel.setSelectedCity(mCityTable.city);
                mViewModel.loadselectedCity();
                promptLayout.setVisibility(View.VISIBLE);
                confirmBtn.requestFocus();

            }
        });

        cityGridView.setOnItemListener(new RecyclerViewTV.OnItemListener() {
            @Override
            public void onItemPreSelected(RecyclerViewTV recyclerViewTV, View itemView, int i) {
                Log.d(TAG, "onItemPreSelected");
                mRecyclerViewBridge.setUnFocusView(cityOldView[0]);
                TextView textView = (TextView) itemView.findViewById(R.id.province_text);
                if (citySelectedPosition[0] == i) {
                    textView.setTextColor(getResources().getColor(R.color.blue));
                } else {
                    textView.setTextColor(getResources().getColor(R.color.color_base_white));
                }
            }

            @Override
            public void onItemSelected(RecyclerViewTV recyclerViewTV, View itemView, int i) {
                Log.d(TAG, "onItemSelected");
                TextView textView = (TextView) itemView.findViewById(R.id.province_text);
                if (citySelectedPosition[0] == i) {
                    textView.setTextColor(getResources().getColor(R.color.blue));
                } else {
                    textView.setTextColor(getResources().getColor(R.color.location_text_focus));
                }
                mRecyclerViewBridge.setFocusView(itemView, 1.2f);
                cityOldView[0] = itemView;
            }

            @Override
            public void onReviseFocusFollow(RecyclerViewTV recyclerViewTV, View itemView, int i) {
                Log.d(TAG, "onReviseFocusFollow");
                mRecyclerViewBridge.setFocusView(itemView, 1.2f);
                cityOldView[0] = itemView;
            }

        });
        cancelBtn.setOnHoverListener(cityAdapter);
        confirmBtn.setOnHoverListener(cityAdapter);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IsmartvActivator activator = IsmartvActivator.getInstance();
                activator.setProvince(mProvinceTable.province_name, mProvinceTable.pinyin);
                activator.setCity(mCityTable.city, String.valueOf(mCityTable.geo_id));
                mPresenter.fetchWeather(String.valueOf(mCityTable.geo_id));
                promptLayout.setVisibility(View.INVISIBLE);

                mViewModel.setSelectedCity("");
                mViewModel.loadselectedCity();
                areaPopup.dismiss();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                citySelectedPosition[0] = -1;
                if (citySelectedView[0] != null) {
                    citySelectedView[0].setTextColor(getResources().getColor(R.color.color_base_white));
                    citySelectedView[0] = null;
                }
                promptLayout.setVisibility(View.INVISIBLE);
                mViewModel.setSelectedCity("");
                mViewModel.loadselectedCity();
            }
        });

    }

    public void clearStatus() {
        if (areaPopup != null) {
            areaPopup.dismiss();
            areaPopup = null;
        }
    }
}
