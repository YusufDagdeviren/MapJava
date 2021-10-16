package com.yusufdagdeviren.mapjava.room;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.yusufdagdeviren.mapjava.model.Place;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface PlaceDao {

    @Query("SELECT * FROM Place")
    Flowable<List<Place>> getAll();
    /*
    @Query("SELECT * FROM Place WHERE name = :nameInput")
    List<Place> getAll(String nameInput );
     */

    @Insert
    Completable insert(Place place);

    @Delete
    Completable delete(Place place);

}
