package tv.ismar.app.ui.view;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import tv.ismar.app.R;

public class MenuFragment extends DialogFragment implements OnItemClickListener {

    public static final String TAG = "MenuFragment";

    public ArrayList<MenuItem> mMenuList;

    private ListView mMenuListView;

    private OnMenuItemClickedListener mOnMenuItemClickedListener;

    private int resid;

    public static MenuFragment newInstance() {
        return new MenuFragment();
    }

    public void setResId(int resid) {
        this.resid = resid;
    }

    public void setOnMenuItemClickedListener(OnMenuItemClickedListener listener) {
        mOnMenuItemClickedListener = listener;
    }

    public void setTitle(int resid) {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMenuList = new ArrayList<MenuItem>();
        //		MenuItem deleteItem = new MenuItem();
        //		deleteItem.id = 1;
        //		deleteItem.isEnable = true;
        //		deleteItem.title = getResources().getString(R.string.delete_history);
        //		mMenuList.add(deleteItem);
        MenuItem clearItem = new MenuItem();
        clearItem.id = 2;
        clearItem.isEnable = true;
        clearItem.title = getResources().getString(resid);
        mMenuList.add(clearItem);
        MenuItem kefu = new MenuItem();
        kefu.id = 3;
        kefu.isEnable = true;
        kefu.title = getResources().getString(R.string.kefucentertitle);
        mMenuList.add(kefu);
        MenuItem ordingmenu = new MenuItem();
        ordingmenu.id = 4;
        ordingmenu.isEnable = true;
        ordingmenu.title = getResources().getString(R.string.orderlisttitle);
        mMenuList.add(ordingmenu);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View layout = LayoutInflater.from(getActivity()).inflate(R.layout.menu_layout, null);
        Dialog menuDialog =
                new Dialog(getActivity(), android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        menuDialog.addContentView(
                layout,
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT));
        mMenuListView = (ListView) layout.findViewById(R.id.menu_list);
        initLayout();
        return menuDialog;
    }

    private void initLayout() {
        MenuAdapter adapter = new MenuAdapter(getActivity(), mMenuList);
        mMenuListView.setAdapter(adapter);
        mMenuListView.setOnItemClickListener(this);
        //		mMenuListView.requestFocus();
    }

    public void inflate(int resId, ArrayList<MenuItem> menuList) {
        XmlResourceParser parser = null;
        try {
            parser = getResources().getLayout(resId);
            AttributeSet attrs = Xml.asAttributeSet(parser);

            parseMenu(parser, attrs, menuList);
        } catch (XmlPullParserException e) {
            throw new InflateException("Error inflating menu XML", e);
        } catch (IOException e) {
            throw new InflateException("Error inflating menu XML", e);
        } finally {
            if (parser != null) parser.close();
        }
    }

    private void parseMenu(
            XmlResourceParser parser, AttributeSet attrs, ArrayList<MenuItem> menuList)
            throws XmlPullParserException, IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mOnMenuItemClickedListener != null) {
            mOnMenuItemClickedListener.onMenuItemClicked(
                    (MenuItem) (mMenuListView.getAdapter().getItem(position)));
            dismiss();
        }
    }

    public boolean isShowing() {
        return getDialog() != null && getDialog().isShowing();
    }

    public interface OnMenuItemClickedListener {
        void onMenuItemClicked(MenuItem item);
    }

    public interface OnMenuItemClickListener {
        void onMenuItemClick();
    }

    public static class MenuItem {
        public int id;
        public boolean isEnable;
        public String title;
        public ArrayList<MenuItem> subMenu;
        public int parentId;
    }

    public class MenuAdapter extends BaseAdapter {

        private ArrayList<MenuItem> mMenuList;
        private Context mContext;

        public MenuAdapter(Context context, ArrayList<MenuItem> list) {
            mContext = context;
            mMenuList = list;
        }

        @Override
        public int getCount() {
            return mMenuList.size();
        }

        @Override
        public MenuItem getItem(int position) {
            return mMenuList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mMenuList.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.menu_layout_item, null);
            TextView title = (TextView) convertView.findViewById(R.id.menu_title);
            title.setText(mMenuList.get(position).title);
            convertView.setTag(position);
            convertView.setOnHoverListener(
                    new View.OnHoverListener() {
                        @Override
                        public boolean onHover(View v, MotionEvent keycode) {
                            switch (keycode.getAction()) {
                                case MotionEvent.ACTION_HOVER_ENTER:
                                case MotionEvent.ACTION_HOVER_MOVE:
                                    mMenuListView.requestFocusFromTouch();
                                    mMenuListView.setSelection((Integer) (v.getTag()));
                                    break;
                                case MotionEvent.ACTION_HOVER_EXIT:
                                    break;
                                default:
                                    break;
                            }
                            return false;
                        }
                    });
            return convertView;
        }
    }
}
