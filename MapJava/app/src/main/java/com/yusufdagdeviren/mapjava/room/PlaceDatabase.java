package com.yusufdagdeviren.mapjava.room;


import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.yusufdagdeviren.mapjava.model.Place;

@Database(entities = {Place.class},version = 1)
public abstract class PlaceDatabase extends RoomDatabase {

    public abstract PlaceDao placeDao();


}
