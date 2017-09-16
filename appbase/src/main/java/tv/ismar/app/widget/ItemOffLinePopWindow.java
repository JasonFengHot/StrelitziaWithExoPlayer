package tv.ismar.app.widget;

import android.content.Context;

/** Created by huibin on 11/17/16. */
public class ItemOffLinePopWindow extends ModuleMessagePopWindow {
    private static ItemOffLinePopWindow ourInstance;

    public ItemOffLinePopWindow(Context context) {
        super(context);
    }

    public static ItemOffLinePopWindow getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new ItemOffLinePopWindow(context);
        }
        return ourInstance;
    }
}
