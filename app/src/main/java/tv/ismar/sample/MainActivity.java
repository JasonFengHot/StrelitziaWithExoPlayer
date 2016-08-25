package tv.ismar.sample;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import tv.ismar.account.SyncUtils;
import tv.ismar.detailpage.view.DetailPageActivity;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button demo_detail_btn = (Button) findViewById(R.id.demo_detail_btn);
        demo_detail_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DetailPageActivity.class);
                startActivity(intent);
            }
        });


        SyncUtils.CreateSyncAccount(this);
    }


}
