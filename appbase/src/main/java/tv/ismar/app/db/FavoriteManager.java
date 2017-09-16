package tv.ismar.app.db;

import java.util.ArrayList;

import tv.ismar.app.entity.Favorite;

public interface FavoriteManager {
    /**
     * Add a favorite object to persistence.
     *
     * @param favorite ,this object should guaranty all fields not null.
     */
    public void addFavorite(Favorite favorite, String isnet);

    @Deprecated
    public void addFavorite(String title, String url, String content_model, String isnet);

    public Favorite getFavoriteByUrl(String url, String isnet);

    public ArrayList<Favorite> getAllFavorites(String isnet);

    public void deleteFavoriteByUrl(String url, String isnet);

    public void deleteAll(String isnet);
}
