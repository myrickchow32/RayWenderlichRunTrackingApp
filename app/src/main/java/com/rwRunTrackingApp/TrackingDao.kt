package com.rwRunTrackingApp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TrackingDao {
    @Query("SELECT * FROM trackingrecord")
    fun getAll(): List<TrackingRecord>

    @Insert
    fun insert(trackingRecord: TrackingRecord)

    @Query("DELETE FROM trackingrecord")
    fun delete()
}