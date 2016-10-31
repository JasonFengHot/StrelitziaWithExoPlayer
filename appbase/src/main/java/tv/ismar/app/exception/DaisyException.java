package tv.ismar.app.exception;

public class DaisyException extends Exception {
	
	private static final long serialVersionUID = -5500277806647175487L;

	private String url;
	
	public DaisyException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DaisyException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

	public DaisyException(String url, String detailMessage) {
		super(detailMessage);
		this.url = url;
	}

	public DaisyException(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}
