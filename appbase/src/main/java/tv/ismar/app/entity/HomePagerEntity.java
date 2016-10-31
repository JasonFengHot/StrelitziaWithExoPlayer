package tv.ismar.app.entity;

import java.util.ArrayList;

/**
 * Created by huaijie on 6/3/15.
 */
public class HomePagerEntity {
    private ArrayList<Carousel> carousels;
    private ArrayList<Poster> posters;

    public ArrayList<Carousel> getCarousels() {
        return carousels;
    }

    public void setCarousels(ArrayList<Carousel> carousels) {
        this.carousels = carousels;
    }

    public ArrayList<Poster> getPosters() {
        return posters;
    }

    public void setPosters(ArrayList<Poster> posters) {
        this.posters = posters;
    }

    public class Carousel {
        private String video_image;
        private String pause_time;
        private String url;
        private String video_url;
		private String title;
        private String introduction;
        private String thumb_image;
        private String model_name;
        private String content_model;
        private int corner;
        private boolean expense;
        private int position;
		public int getPosition() {
			return position;
		}

		public void setPosition(int position) {
			this.position = position;
		}

		public boolean isExpense() {
			return expense;
		}

		public void setExpense(boolean expense) {
			this.expense = expense;
		}

		public int getCorner() {
			return corner;
		}

		public void setCorner(int corner) {
			this.corner = corner;
		}

		public String getContent_model() {
			return content_model;
		}

		public void setContent_model(String content_model) {
			this.content_model = content_model;
		}

		public String getVideo_image() {
            return video_image;
        }

        public void setVideo_image(String video_image) {
            this.video_image = video_image;
        }

        public String getPause_time() {
            return pause_time;
        }

        public void setPause_time(String pause_time) {
            this.pause_time = pause_time;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

		public String getVideo_url() {
            return video_url;
        }

        public void setVideo_url(String video_url) {
            this.video_url = video_url;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getIntroduction() {
            return introduction;
        }

        public void setIntroduction(String introduction) {
            this.introduction = introduction;
        }

        public String getThumb_image() {
            return thumb_image;
        }

        public void setThumb_image(String thumb_image) {
            this.thumb_image = thumb_image;
        }

        public String getModel_name() {
            return model_name;
        }

        public void setModel_name(String model_name) {
            this.model_name = model_name;
        }
    }


    public class Poster {
        private String vertical_url;
        private String title;
        private String introduction;
        private String url;
        private String poster_url;
        private String model_name;
        private String custom_image;
        private String content_model;
        private int corner;
        private boolean expense;
        private int position;

		public int getPosition() {
			return position;
		}

		public void setPosition(int position) {
			this.position = position;
		}

		public boolean isExpense() {
			return expense;
		}

		public void setExpense(boolean expense) {
			this.expense = expense;
		}

		public int getCorner() {
			return corner;
		}

		public void setCorner(int corner) {
			this.corner = corner;
		}

		public String getContent_model() {
			return content_model;
		}

		public void setContent_model(String content_model) {
			this.content_model = content_model;
		}

		public String getVertical_url() {
            return vertical_url;
        }

        public void setVertical_url(String vertical_url) {
            this.vertical_url = vertical_url;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getIntroduction() {
            return introduction;
        }

        public void setIntroduction(String introduction) {
            this.introduction = introduction;
        }

        public String getUrl() {
            return url;
        }

        public String getPoster_url() {
            return poster_url;
        }

        public void setPoster_url(String poster_url) {
            this.poster_url = poster_url;
        }

        public String getModel_name() {
            return model_name;
        }

        public void setModel_name(String model_name) {
            this.model_name = model_name;
        }

        public String getCustom_image() {
            return custom_image;
        }

        public void setCustom_image(String custom_image) {
            this.custom_image = custom_image;
        }
    }

}
