package tv.ismar.app.db;

import java.util.ArrayList;

import tv.ismar.app.entity.Favorite;

public interface FavoriteManager {
    /**
     * Add a favorite object to persistence.
     *
     * @param favorite ,this object should guaranty all fields not null.
     */
    void addFavorite(Favorite favorite, String isnet);

    @Deprecated
    void addFavorite(String title, String url, String content_model, String isnet);

    Favorite getFavoriteByUrl(String url, String isnet);

    ArrayList<Favorite> getAllFavorites(String isnet);

    void deleteFavoriteByUrl(String url, String isnet);

    void deleteAll(String isnet);
}
