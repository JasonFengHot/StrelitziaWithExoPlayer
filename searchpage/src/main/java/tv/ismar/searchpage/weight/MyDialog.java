package tv.ismar.searchpage.weight;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import tv.ismar.searchpage.R;


/**
 * Created by admin on 2016/2/26.
 */
public class MyDialog extends Dialog implements
        View.OnClickListener, View.OnHoverListener {
    private String text;
    public static Button confirm;
    private Context context;


    public MyDialog(Context context, String text) {
        super(context, R.style.MyDialog);
        this.text = text;
        this.context=context;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (text == "") {
            setContentView(R.layout.loading);
        } else {
            setContentView(R.layout.dialog);
            TextView txt = (TextView) findViewById(R.id.tv_text1);
            View view = findViewById(R.id.view);
            view.setOnHoverListener(this);
            txt.setText(text);
            confirm = (Button) findViewById(R.id.dialog_button);
            confirm.setOnClickListener(this);
            confirm.setOnHoverListener(this);
        }

    }


    @Override
    public void onClick(View v) {
        MyDialog.this.dismiss();
        if (text.startsWith(context.getResources().getString(R.string.network_error))) {
            System.exit(0);
        }
    }


    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
                break;
            case MotionEvent.ACTION_HOVER_MOVE:
                v.requestFocus();
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                break;

        }
        return false;
    }
}
