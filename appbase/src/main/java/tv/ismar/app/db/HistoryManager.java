package tv.ismar.app.db;

import java.util.ArrayList;

import tv.ismar.app.entity.DBQuality;
import tv.ismar.app.entity.History;

/**
 * An History Manager.
 *
 * @author bob
 */
public interface HistoryManager {

    /**
     * Add a history.
     *
     * @param history
     */
    public void addHistory(History history, String isnet, int completePosition);

    /**
     * Add a history. Called by player when user exits player.Note that url may be an field of ItemEntity.
     * Both {@link tv.ismar.app.network.entity.ItemEntity} and {@link tv.ismar.app.network.entity.ItemEntity} is OK. This depends on the category of you ItemEntity object. Commonly, an subitem contains url field,
     * but a item contains item_url field.
     * This method guarantees if target url exists in history. it will auto update its status instead of adding a duplicated one.
     *
     * @param title           the title of current playing item.
     * @param url             the url representing the item.
     * @param currentPosition the played position when user exits player. if playback is finished. pass 0 instead of the played position.
     */
    @Deprecated
    public void addHistory(String title, String url, long currentPosition, String isnet);

    /**
     * Get a history object according given url.
     *
     * @param url, the url which can get an item contain this.
     * @return a {@link History} object.
     */
    public History getHistoryByUrl(String url, String isnet);

    /**
     * Get all histories.
     *
     * @return an ArrayList of History.
     */
    public ArrayList<History> getAllHistories(String isnet);

    /**
     * Delete a history entry in database by url
     *
     * @param url
     */
    public void deleteHistory(String url, String isnet);

    /**
     * Delete all histories in database
     */
    public void deleteAll(String isnet);


    /**
     * Add or update only one DBQuality
     */
    public void addOrUpdateQuality(DBQuality quality);

    /**
     * get user DBQuality
     */
    public DBQuality getQuality();

}
