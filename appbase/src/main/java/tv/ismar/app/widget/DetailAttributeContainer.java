package tv.ismar.app.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Iterator;
import java.util.LinkedHashMap;

import tv.ismar.app.R;
import tv.ismar.app.util.Utils;

/** Created by beaver on 16-8-22. */
public class DetailAttributeContainer extends LinearLayout {

    private static final String TAG = "LH/DetailAttribute";
    private final int defaultTextSize = 18;
    private final int defaultSpace = 16;
    private LinkedHashMap<String, String> mAttrName; // 设置详情页影片信息
    private LinkedHashMap<String, String> mAttrValue; // 设置详情页影片信息
    private Context mContext;

    private int textSize;
    private int textColor;
    private int space;

    public DetailAttributeContainer(Context context) {
        this(context, null);
    }

    public DetailAttributeContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DetailAttributeContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setOrientation(LinearLayout.VERTICAL);
        TypedArray typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.DetailAttributeContainer);
        textSize =
                typedArray.getDimensionPixelSize(
                        R.styleable.DetailAttributeContainer_dac_textSize, defaultTextSize);
        textColor =
                typedArray.getColor(
                        R.styleable.DetailAttributeContainer_dac_textColor, Color.WHITE);
        space =
                typedArray.getDimensionPixelSize(
                        R.styleable.DetailAttributeContainer_dac_space, defaultSpace);
        typedArray.recycle();
    }

    public void showAttributes(
            LinkedHashMap<String, String> attrName, LinkedHashMap<String, String> attrValue) {
        Iterator<java.util.Map.Entry<String, String>> iter = attrValue.entrySet().iterator();
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(getWidth(), LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = space;
        while (iter.hasNext()) {
            java.util.Map.Entry<String, String> entry = iter.next();
            String key = entry.getKey();
            String value = entry.getValue();
            if (Utils.isEmptyText(value) || Utils.isEmptyText(key)) {
                continue;
            }
            String name = attrName.get(key);

            TextView nameValue = new TextView(mContext);
            nameValue.setLayoutParams(layoutParams);
            nameValue.setFocusable(false);
            nameValue.setFocusableInTouchMode(false);
            nameValue.setSingleLine(true);
            nameValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            nameValue.setTextColor(textColor);
            nameValue.setText(name + ":" + value);

            addView(nameValue);
        }
    }
}
