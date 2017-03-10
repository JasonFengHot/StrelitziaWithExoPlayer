package tv.ismar.player.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import tv.ismar.app.util.NetworkUtils;
import tv.ismar.player.R;

public class PlayerMenu extends PlayerMenuItem {
    private static final String TAG = "PlayerMenu";

    private Context context;
    private Animation showAnimation;
    private Animation hideAnimation;
    private ListView listView;
    private boolean visible = false;
    private boolean created = false;
    private PlayerMenuItem menuStack[] = new PlayerMenuItem[10];
    private int menuStackTop = 0;
    private View lastSelectMenu;
    private int onHoveredPosition = -1;

    private OnCreateMenuListener onCreateMenuListener;

    public void setOnCreateMenuListener(OnCreateMenuListener listener) {
        onCreateMenuListener = listener;
    }

    public interface OnCreateMenuListener {

        boolean onMenuClicked(PlayerMenu playerMenu, int id);

        void onMenuCloseed(PlayerMenu playerMenu);

    }

    public PlayerMenu(Context ctx, ListView view) {
        super(-1, "");
        context = ctx;
        listView = view;
        listView.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        pop();
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                            || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (onHoveredPosition >= 0 && listView.getCount() > 2) {
                            Log.i("LH/", "down-lastSelectedId:" + onHoveredPosition);
                            PlayerMenuItem curr = getCurrMenu();
                            if (curr != null) {
                                PlayerMenuItem item = curr.subItems.get(onHoveredPosition);

                                int selection = -1;
                                if (onHoveredPosition == (listView.getCount() - 1)) {
                                    selection = listView.getCount() - 1;
                                } else {
                                    selection = onHoveredPosition + 1;
                                }
                                loadMenu(curr, false, selection);
                            }
                            onHoveredPosition = -1;
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        if (onHoveredPosition >= 0 && listView.getCount() > 2) {
                            Log.i("LH/", "up-lastSelectedId:" + onHoveredPosition);
                            PlayerMenuItem curr = getCurrMenu();
                            if (curr != null) {
                                PlayerMenuItem item = curr.subItems.get(onHoveredPosition);

                                int selection = -1;
                                if (onHoveredPosition == 0) {
                                    selection = 0;
                                } else {
                                    selection = onHoveredPosition - 1;
                                }
                                loadMenu(curr, false, selection);
                            }
                            onHoveredPosition = -1;
                        }
                    }
                }
                return false;
            }
        });
        showAnimation = AnimationUtils.loadAnimation(context,
                R.anim.slide_in_right);
        hideAnimation = AnimationUtils.loadAnimation(context,
                android.R.anim.slide_out_right);
    }

    private PlayerMenuItem getCurrMenu() {
        if (menuStackTop == 0)
            return null;

        return menuStack[menuStackTop - 1];
    }

    private void loadMenu(PlayerMenuItem menu, boolean push, int selection) {
        if (menu.subItems == null) {
            return;
        }
        String titles[] = new String[menu.subItems.size()];
        int sel = -1;

        for (int i = 0; i < titles.length; i++) {
            titles[i] = menu.subItems.get(i).title;
            if (menu.subItems.get(i).selected) {
                sel = i;
            }
        }

        if (selection >= 0) {
            sel = selection;
        }

        listView.setAdapter(new ArrayAdapter<String>(context, R.layout.adapter_player_menu,
                R.id.adapter_menu_text, titles) {
            public View getView(int position, View convertView, ViewGroup parent) {
                convertView = super.getView(position, convertView, parent);
                TextView tv = (TextView) convertView
                        .findViewById(R.id.adapter_menu_text);
                PlayerMenuItem curr = getCurrMenu();
                int id = position;
                TextView checkbox = (TextView) convertView.findViewById(R.id.adapter_menu_checkBox);
                Typeface typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL);
                checkbox.setTypeface(typeface);
                if (curr.subItems.get(id).selected) {
                    if (curr.subItems.get(id).id != 0 && curr.subItems.get(id).id != 100 & curr.subItems.get(id).id != 20 && curr.subItems.get(id).id != 30)
                        checkbox.setVisibility(View.VISIBLE);
                } else {
                    checkbox.setVisibility(View.INVISIBLE);
                }
                convertView.setTag(R.id.adapter_menu_text, id);
                convertView.setOnHoverListener(new View.OnHoverListener() {

                    @Override
                    public boolean onHover(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER
                                || event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                            if (lastSelectMenu != null) {
                                lastSelectMenu.setBackgroundResource(android.R.color.transparent);
                            }
                            v.setBackgroundResource(R.color._ff9c3c);
                            lastSelectMenu = v;
                            onHoveredPosition = (int) v.getTag(R.id.adapter_menu_text);
                        } else {
                            if (lastSelectMenu != null) {
                                lastSelectMenu.setBackgroundResource(android.R.color.transparent);
                            }
                            onHoveredPosition = -1;
                        }
                        return false;
                    }
                });
                return convertView;
            }
        });
        if (sel != -1) {
            listView.setSelection(sel);
        }
        if (push) {
            menuStack[menuStackTop++] = menu;
        }
    }

    private void create() {
        if (!created) {
            created = true;
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(TAG, "select " + id);
                    PlayerMenuItem curr = getCurrMenu();
                    if (curr != null && curr.subItems != null) {
                        PlayerMenuItem item = curr.subItems.get(position);
                        if (item.isSub) {
                            Log.d(TAG, "load " + item.title);
                            loadMenu(item, true, -1);
                        } else {
                            Log.d(TAG, "click " + item.title);
                            if (NetworkUtils.isConnected(context)) {
                                for (PlayerMenuItem sub : curr.subItems) {
                                    if (sub.id == item.id) {
                                        sub.selected = true;
                                    } else {
                                        sub.selected = false;
                                    }
                                }
                            }
                            if (onCreateMenuListener != null &&
                                    onCreateMenuListener.onMenuClicked(PlayerMenu.this, item.id)) {
                                hide();
                            }
                        }
                    }
                }
            });
            listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.i(TAG, "onItemSelected:" + position);
                    if (view != null){
                        view.setBackgroundResource(R.color._ff9c3c);
                        if (lastSelectMenu != null && view != lastSelectMenu) {
                            lastSelectMenu.setBackgroundResource(android.R.color.transparent);
                        }
                        lastSelectMenu = view;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Log.i(TAG, "onNothingSelected");
                }

            });
        }
    }

    public void showQuality(int click_position){
        if (!visible && (subItems != null) && (subItems.size() != 0)) {
            visible = true;
            menuStackTop = 0;
            create();
            loadMenu(this, true, -1);
            listView.setVisibility(View.VISIBLE);
            listView.startAnimation(showAnimation);
            listView.setItemsCanFocus(true);
            listView.requestFocus();
            listView.performItemClick(listView.getChildAt(click_position), click_position, listView.getItemIdAtPosition(click_position));
        }
    }

    public void show() {
        if (!visible && (subItems != null) && (subItems.size() != 0)) {
            visible = true;
            menuStackTop = 0;
            create();
            loadMenu(this, true, -1);
            listView.setVisibility(View.VISIBLE);
            listView.startAnimation(showAnimation);
            listView.setItemsCanFocus(true);
            listView.requestFocus();
        }
    }

    public void pop() {
        if (!visible)
            return;
        if (menuStackTop > 0) {
            menuStackTop--;
            PlayerMenuItem curr = getCurrMenu();
            if (curr != null) {
                loadMenu(curr, false, -1);
            } else {
                hide();
            }
        }
    }

    public void hide() {
        if (visible) {
            visible = false;
            listView.startAnimation(hideAnimation);
            listView.setVisibility(View.GONE);
            listView.setSelection(-1);
            if (onCreateMenuListener != null) {
                onCreateMenuListener.onMenuCloseed(PlayerMenu.this);
            }
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void enable_scroll(boolean enable_scroll) {
        listView.setVerticalScrollBarEnabled(enable_scroll);
    }
}
