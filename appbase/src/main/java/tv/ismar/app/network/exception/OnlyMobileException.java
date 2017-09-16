package tv.ismar.app.network.exception;

import java.io.IOException;

/** Created by huibin on 8/25/16. */
public class OnlyMobileException extends IOException {

    public OnlyMobileException(String url) {
        super("only mobile access network!!! ===> " + url);
    }
}
