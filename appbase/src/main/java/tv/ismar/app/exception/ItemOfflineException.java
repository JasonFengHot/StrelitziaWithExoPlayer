package tv.ismar.app.exception;

public class ItemOfflineException extends DaisyException {

    private static final long serialVersionUID = 6871944141330908916L;

    private static final String message = "ERROR 404, target url is not available";

    public ItemOfflineException(String url) {
        super(url, message);
        // TODO Auto-generated constructor stub
    }
}
