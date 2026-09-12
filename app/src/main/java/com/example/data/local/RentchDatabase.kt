package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ApartmentEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.UserProfileEntity

@Database(
    entities = [
        ApartmentEntity::class,
        UserProfileEntity::class,
        ChatMessageEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RentchDatabase : RoomDatabase() {
    abstract fun rentchDao(): RentchDao

    companion object {
        @Volatile
        private var INSTANCE: RentchDatabase? = null

        fun getInstance(context: Context): RentchDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RentchDatabase::class.java,
                    "rentch_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
