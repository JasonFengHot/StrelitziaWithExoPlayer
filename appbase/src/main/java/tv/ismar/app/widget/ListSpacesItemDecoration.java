package tv.ismar.app.widget;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/** Created by mac on 15/10/28. */
public class ListSpacesItemDecoration extends RecyclerView.ItemDecoration {

    private int space;

    public ListSpacesItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(
            Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = 0;
        } else {
            outRect.top = space;
        }
        outRect.left = 0;
        outRect.right = 0;
        outRect.bottom = 0;
    }
}
