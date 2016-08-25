package tv.ismar.app.network.exception;

import java.io.IOException;

/**
 * Created by huibin on 8/25/16.
 */
public class OnlyWifiException extends IOException {

    private static final String TAG = OnlyWifiException.class.getSimpleName();

    public OnlyWifiException(String url) {
        super("only wifi access network!!!  ===> " + url);

    }
}
