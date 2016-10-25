package tv.ismar.app.exception;

public class NetworkException extends DaisyException {

	private static final long serialVersionUID = -9178809832449533772L;

	private static final String message = "network exception";
	
	public NetworkException(String url) {
		super(url, message);
		// TODO Auto-generated constructor stub
	}

}
