package tv.ismar.app;

import android.app.Application;
import android.content.Context;

import rx.Scheduler;
import rx.schedulers.Schedulers;
import tv.ismar.app.network.SkyService;

/**
 * Created by beaver on 16-8-19.
 */
public class VodApplication extends Application {


    private SkyService skyService;
    private Scheduler defaultSubscribeScheduler;

    public static VodApplication get(Context context) {
        return (VodApplication) context.getApplicationContext();
    }

    public SkyService getGithubService() {
        if (skyService == null) {
            skyService= SkyService.Factory.create("");
        }
        return skyService;
    }

    //For setting mocks during testing
    public void setGithubService(SkyService githubService) {
        this.skyService = githubService;
    }

    public Scheduler defaultSubscribeScheduler() {
        if (defaultSubscribeScheduler == null) {
            defaultSubscribeScheduler = Schedulers.io();
        }
        return defaultSubscribeScheduler;
    }

    //User to change scheduler from tests
    public void setDefaultSubscribeScheduler(Scheduler scheduler) {
        this.defaultSubscribeScheduler = scheduler;
    }
}
