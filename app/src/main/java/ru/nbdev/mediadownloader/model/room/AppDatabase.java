package ru.nbdev.mediadownloader.model.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import ru.nbdev.mediadownloader.model.room.entity.DbPhoto;
import ru.nbdev.mediadownloader.model.room.entity.DbSearchRequest;
import ru.nbdev.mediadownloader.model.room.entity.DbJoinRequestAndPhotos;

@Database(entities = {DbPhoto.class, DbSearchRequest.class, DbJoinRequestAndPhotos.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract PhotosDao photosDao();

    public abstract PhotoRepositoryCacheDao photoRepositoryCacheDao();
}
