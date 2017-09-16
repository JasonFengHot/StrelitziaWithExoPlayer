package tv.ismar.app.network.entity;

import java.util.ArrayList;
import java.util.List;

/** Created by huaijie on 10/22/15. */
public class VersionInfoV2Entity {
    private List<ApplicationEntity> upgrades;
    private String version;
    private String homepage;

    public List<ApplicationEntity> getUpgrades() {
        return upgrades;
    }

    public void setUpgrades(List<ApplicationEntity> upgrades) {
        this.upgrades = upgrades;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public static class ApplicationEntity {
        private boolean checkUpgrade = true;
        private String product;
        private String name;
        private String screenshot;
        private String url;
        private ArrayList<String> update;
        private String summary;
        private String version;
        private String md5;

        public boolean isCheckUpgrade() {
            return checkUpgrade;
        }

        public void setCheckUpgrade(boolean checkUpgrade) {
            this.checkUpgrade = checkUpgrade;
        }

        public String getProduct() {
            return product;
        }

        public void setProduct(String product) {
            this.product = product;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getScreenshot() {
            return screenshot;
        }

        public void setScreenshot(String screenshot) {
            this.screenshot = screenshot;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public ArrayList<String> getUpdate() {
            return update;
        }

        public void setUpdate(ArrayList<String> update) {
            this.update = update;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getMd5() {
            return md5;
        }

        public void setMd5(String md5) {
            this.md5 = md5;
        }
    }
}
