package tv.ismar.homepage.adapter;

import android.view.MotionEvent;
import android.view.View;

/** Created by mac on 15/11/3. */
public interface OnItemActionListener {

    void onItemHoverListener(View v, MotionEvent event, int position);

    void onItemFocusListener(View v, boolean hasFocus, int position);

    void onItemClickListener(View v, int position);

    void onItemSelectedListener(int position);
}
