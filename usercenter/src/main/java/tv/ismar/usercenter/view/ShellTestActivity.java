package tv.ismar.usercenter.view;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.utils.ShellUtils;

import tv.ismar.usercenter.R;

/** Created by huibin on 1/10/2017. */
public class ShellTestActivity extends Activity {

    private EditText withoutRootShellEdit;
    private EditText rootShellEdit;

    private TextView successText;
    private TextView failedText;
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shell_test);
        successText = (TextView) findViewById(R.id.success_text);
        failedText = (TextView) findViewById(R.id.failed_text);
        resultText = (TextView) findViewById(R.id.result_text);

        withoutRootShellEdit = (EditText) findViewById(R.id.without_root_shell_edit);
        rootShellEdit = (EditText) findViewById(R.id.root_shell_edit);
    }

    public void withoutRootShell(View view) {
        String shell = withoutRootShellEdit.getText().toString();
        if (TextUtils.isEmpty(shell)) {
            Toast.makeText(this, "text not allow empty!!!", Toast.LENGTH_SHORT).show();
        } else {
            ShellUtils.CommandResult commandResult = ShellUtils.execCmd(shell, false, true);
            successText.setText("success msg: " + commandResult.successMsg);
            failedText.setText("failed msg: " + commandResult.errorMsg);
            resultText.setText("result code: " + commandResult.result);
        }
    }

    public void rootShell(View view) {
        String shell = rootShellEdit.getText().toString();
        if (TextUtils.isEmpty(shell)) {
            Toast.makeText(this, "text not allow empty!!!", Toast.LENGTH_SHORT).show();
        } else {
            ShellUtils.CommandResult commandResult = ShellUtils.execCmd(shell, true, true);
            successText.setText("success msg: " + commandResult.successMsg);
            failedText.setText("failed msg: " + commandResult.errorMsg);
            resultText.setText("result code: " + commandResult.result);
        }
    }
}
