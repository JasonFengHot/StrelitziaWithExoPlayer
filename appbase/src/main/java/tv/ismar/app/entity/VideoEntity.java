package tv.ismar.app.entity;

import java.util.List;

/**
 * Created by <huaijiefeng@gmail.com> on 9/2/14.
 */
public class VideoEntity {
    private int count;
    private List<Objects> objects;
    private int num_pages;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Objects> getObjects() {
        return objects;
    }

    public void setObjects(List<Objects> objects) {
        this.objects = objects;
    }

    public int getNum_pages() {
        return num_pages;
    }

    public void setNum_pages(int num_pages) {
        this.num_pages = num_pages;
    }

    public static class Objects {
        private String image;
        private String title;
        private String item_url;
        private boolean is_complex;
        private String content_model;

        public String getContent_model(){
            return content_model;
        }
        public void setContent_model(String content_model){
            this.content_model = content_model;
        }
        public boolean isIs_complex() {
			return is_complex;
		}

		public void setIs_complex(boolean is_complex) {
			this.is_complex = is_complex;
		}

		public String getItem_url() {
            return item_url;
        }

        public void setItem_url(String item_url) {
            this.item_url = item_url;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
