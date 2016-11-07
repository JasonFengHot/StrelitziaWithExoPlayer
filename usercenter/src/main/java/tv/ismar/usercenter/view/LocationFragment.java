package tv.ismar.usercenter.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.util.List;

import cn.ismartv.injectdb.library.query.Select;
import tv.ismar.app.BaseFragment;
import tv.ismar.app.db.location.ProvinceTable;
import tv.ismar.usercenter.LocationContract;
import tv.ismar.usercenter.R;
import tv.ismar.usercenter.databinding.FragmentLocationBinding;
import tv.ismar.usercenter.viewmodel.LocationViewModel;

/**
 * Created by huibin on 10/27/16.
 */

public class LocationFragment extends BaseFragment implements LocationContract.View {
    private static final String TAG = LocationFragment.class.getSimpleName();
    private LocationViewModel mViewModel;
    private LocationContract.Presenter mPresenter;

    public static LocationFragment newInstance() {
        return new LocationFragment();
    }


    private GridView proviceGridView;

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
        FragmentLocationBinding locationBinding = FragmentLocationBinding.inflate(inflater, container, false);
        locationBinding.setTasks(mViewModel);
        locationBinding.setActionHandler(mPresenter);

        View root = locationBinding.getRoot();
        proviceGridView = locationBinding.provinceList;

        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
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
        Log.d(TAG, "onResume");
        mPresenter.start();
        createLocationView();

        mViewLoadCallback.loadComplete();

    }

    @Override
    public void onPause() {
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
    public void refreshWeather(String weatherInfo) {

    }

    private void createLocationView() {
        List<ProvinceTable> provinceTables = new Select().from(ProvinceTable.class).execute();
        if (provinceTables != null && !provinceTables.isEmpty()) {
            ProvinceAdapter provinceAdapter = new ProvinceAdapter(getContext(), provinceTables);
//            provinceAdapter.setOnItemListener(this);
            proviceGridView.setAdapter(provinceAdapter);

        }
    }

    class ProvinceAdapter extends BaseAdapter implements View.OnFocusChangeListener, View.OnClickListener {
        private List<ProvinceTable> provinceTableList;
        private Context context;

//        private OnItemListener onItemListener;

        public ProvinceAdapter(Context context, List<ProvinceTable> provinceTableList) {
            this.context = context;
            this.provinceTableList = provinceTableList;
        }

//        public void setOnItemListener(OnItemListener itemListener) {
//            this.onItemListener = itemListener;
//        }

        @Override
        public int getCount() {
            return provinceTableList.size();
        }

        @Override
        public Object getItem(int position) {
            return provinceTableList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.item_province, null);
                viewHolder.provinceTextView = (TextView) convertView.findViewById(R.id.province_text);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.provinceTextView.setText(provinceTableList.get(position).province_name);
            viewHolder.provinceTextView.setTag(position);
//            viewHolder.provinceTextView.setOnFocusChangeListener(this);
//            viewHolder.provinceTextView.setOnClickListener(this);
//            viewHolder.provinceTextView.setOnHoverListener(new View.OnHoverListener() {
//
//                @Override
//                public boolean onHover(View v, MotionEvent event) {
//                    int what = event.getAction();
//                    switch (what) {
//                        case MotionEvent.ACTION_HOVER_MOVE:
//                        case MotionEvent.ACTION_HOVER_ENTER:
////                            v.requestFocus();
//                            break;
//                    }
//                    return false;
//                }
//            });
            return convertView;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
//            if (onItemListener != null)
//                onItemListener.onFocusChange(v, hasFocus);
        }

        @Override
        public void onClick(View v) {
//            if (onItemListener!= null)
//                onItemListener.onClick(v, (Integer)v.getTag());

        }

        private class ViewHolder {
            private TextView provinceTextView;
        }

        public List<ProvinceTable> getList() {
            return provinceTableList;
        }


//         interface OnItemListener {
//            void onClick(View view, int position);
//
//            void onFocusChange(View v, boolean hasFocus);
//        }
    }
}
