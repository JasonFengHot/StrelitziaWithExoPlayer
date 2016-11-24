package tv.ismar.app.models;

import java.util.List;

/**
 * Created by huaijie on 1/20/16.
 */
public class SemanticSearchResponseEntity {
    private List<Facet> facet;

    public List<Facet> getFacet() {
        return facet;
    }

    public void setFacet(List<Facet> facet) {
        this.facet = facet;
    }

    public class Facet {
        private int count;
        private int total_count;
        private List<SemantichObjectEntity> objects;
        private String content_type;
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getTotal_count() {
            return total_count;
        }

        public void setTotal_count(int total_count) {
            this.total_count = total_count;
        }

        public List<SemantichObjectEntity> getObjects() {
            return objects;
        }

        public void setObjects(List<SemantichObjectEntity> objects) {
            this.objects = objects;
        }

        public String getContent_type() {
            return content_type;
        }

        public void setContent_type(String content_type) {
            this.content_type = content_type;
        }
    }

}
