package com.example.trabalhocmu.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(entities = [Post::class], version = 2)
@TypeConverters(Converters::class)
abstract class PostDatabase : RoomDatabase() {

    abstract fun getPostDao():PostDao

    companion object{
        private var INSTANCE:PostDatabase?=null

        fun getDatabase(context: Context):PostDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PostDatabase::class.java,
                    "post-database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
