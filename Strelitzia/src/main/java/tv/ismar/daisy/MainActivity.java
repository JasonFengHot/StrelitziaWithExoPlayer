package tv.ismar.daisy;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntentInterface;
import tv.ismar.detailpage.view.DetailPageActivity;
import tv.ismar.homepage.view.HomePageActivity;
import tv.ismar.player.view.PlayerActivity;

import static tv.ismar.app.core.PageIntentInterface.EXTRA_MODEL;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_PK;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_TYPE;

public class MainActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        SyncUtils.CreateSyncAccount(this);

        Button demo_detail_btn1 = (Button) findViewById(R.id.demo_detail_btn1);
        demo_detail_btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DetailPageActivity.class);
                intent.putExtra(EXTRA_MODEL, "movie");
                intent.putExtra(EXTRA_TYPE, PageIntentInterface.DETAIL_TYPE_ITEM);
                intent.putExtra(EXTRA_PK, 81025);
                startActivity(intent);
            }
        });

        Button demo_detail_btn2 = (Button) findViewById(R.id.demo_detail_btn2);
        demo_detail_btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DetailPageActivity.class);
                intent.putExtra(EXTRA_MODEL, "television");
                intent.putExtra(EXTRA_TYPE, PageIntentInterface.DETAIL_TYPE_ITEM);
                intent.putExtra(EXTRA_PK, 692464);
                startActivity(intent);
            }
        });

        Button demo_detail_btn3 = (Button) findViewById(R.id.demo_detail_btn3);
        demo_detail_btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DetailPageActivity.class);
                intent.putExtra(EXTRA_MODEL, "variety");
                intent.putExtra(EXTRA_TYPE, PageIntentInterface.DETAIL_TYPE_ITEM);
                intent.putExtra(EXTRA_PK, 688464);
                startActivity(intent);
            }
        });

        Button launcher = (Button) findViewById(R.id.launcher);
        launcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HomePageActivity.class);
                startActivity(intent);
            }
        });
        Button channel= (Button) findViewById(R.id.channel);
        channel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setAction("tv.ismar.daisy.Channel");
                intent.putExtra("channel", "chinesemovie");
                intent.putExtra("url", "http://sky.tvxio.bestv.com.cn/v3_0/SKY2/tou0/api/tv/sections/chinesemovie/");
                intent.putExtra("title", "华语电影");
                intent.putExtra("portraitflag", 2);
                startActivity(intent);
            }
        });
        Button history= (Button) findViewById(R.id.history);
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setAction("tv.ismar.daisy.Channel");
                intent.putExtra("channel", "histories");
                startActivity(intent);
            }
        });
        Button favorite= (Button) findViewById(R.id.favorit);
        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setAction("tv.ismar.daisy.Channel");
                intent.putExtra("channel", "$bookmarks");
                startActivity(intent);
            }
        });
        Button search= (Button) findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setAction("tv.ismar.searchpage.search");
                startActivity(intent);
            }
        });
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;  // 屏幕宽度（像素）
        int height = metric.heightPixels;  // 屏幕高度（像素）
        float density = metric.density;  // 屏幕密度（0.75 / 1.0 / 1.5）
        int densityDpi = metric.densityDpi;  // 屏幕密度DPI（120 / 160 / 240）
        float scaledDensity = metric.scaledDensity;
        Log.i("LH/", "width:" + width + " widthDp:" + px2dip(width) +
                "\nheight:" + height + " heightDp:" + px2dip(height) +
                "\ndensity:" + density + "\ndensityDpi" + densityDpi +
                "\nscaledDensity:" + scaledDensity);

//        width:2560
//        height:1504
//        density:2.0
//        densityDpi320
//        scaledDensity:2.0

//        width:1800
//        height:1080
//        density:2.75
//        densityDpi440
//        scaledDensity:2.75

//        width:1920 widthDp:1280
//        height:1080 heightDp:720
//        density:1.5
//        densityDpi240
//        scaledDensity:1.5


    }

    public int px2dip(float pxValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public void onMovieClick(View view) {
        Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
        intent.putExtra(PageIntentInterface.EXTRA_PK, 81025);
        startActivity(intent);

    }

    public void onTelevisionClick(View view) {
        Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
        intent.putExtra(PageIntentInterface.EXTRA_PK, 710394);
//        intent.putExtra(PageIntentInterface.EXTRA_SUBITEM_PK, 409844);
        startActivity(intent);
    }

    public void onPayMovieClick(View view) {
        Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
        intent.putExtra(PageIntentInterface.EXTRA_PK, 706220);
        startActivity(intent);
    }

    public void userCenter(View view) {
    }

    public void onAdClick(View view){
        Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
        intent.putExtra(PageIntentInterface.EXTRA_PK, 706318);// 马兰谣
        startActivity(intent);

    }

}
