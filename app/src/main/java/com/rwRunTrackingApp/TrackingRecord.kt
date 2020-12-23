package com.rwRunTrackingApp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TrackingRecord(
    @PrimaryKey val timestamp: Long,
    @ColumnInfo val latitude: Double, // These are the information of the record
    @ColumnInfo val longitude: Double
)