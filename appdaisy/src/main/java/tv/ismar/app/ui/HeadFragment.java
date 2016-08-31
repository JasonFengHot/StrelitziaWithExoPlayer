package tv.ismar.app.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tv.ismar.app.R;

public class HeadFragment extends Fragment {

    private TextView head_title;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_head, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        head_title = (TextView) view.findViewById(R.id.head_title);

    }

    public void setHeadTitle(String title) {
        if (head_title != null) {
            head_title.setVisibility(View.VISIBLE);
            head_title.setText(title);
        }
    }
}
