package tv.ismar.app.entity;


public class AdElement {
     
	private int root_retcode;
	private String root_retmsg;
	private int retcode;
	private String retmsg;
	private String title;
	private int media_id;
	private String description;
	private String media_url;
	private String media_type;
	private int serial;
	private int start;
	private int end;
	private String report_url;
	private String md5;

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public String getReport_url() {
		return report_url;
	}

	public void setReport_url(String report_url) {
		this.report_url = report_url;
	}

	public int getRoot_retcode() {
		return root_retcode;
	}

	public void setRoot_retcode(int root_retcode) {
		this.root_retcode = root_retcode;
	}

	public String getRoot_retmsg() {
		return root_retmsg;
	}

	public void setRoot_retmsg(String root_retmsg) {
		this.root_retmsg = root_retmsg;
	}

	private int duration;

	public int getRetcode() {
		return retcode;
	}

	public void setRetcode(int retcode) {
		this.retcode = retcode;
	}

	public String getRetmsg() {
		return retmsg;
	}

	public void setRetmsg(String retmsg) {
		this.retmsg = retmsg;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getMedia_id() {
		return media_id;
	}

	public void setMedia_id(int media_id) {
		this.media_id = media_id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMedia_url() {
		return media_url;
	}

	public void setMedia_url(String media_url) {
		this.media_url = media_url;
	}

	public String getMedia_type() {
		return media_type;
	}

	public void setMedia_type(String media_type) {
		this.media_type = media_type;
	}

	public int getSerial() {
		return serial;
	}

	public void setSerial(int serial) {
		this.serial = serial;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

}
