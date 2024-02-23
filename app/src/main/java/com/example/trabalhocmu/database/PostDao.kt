package com.example.trabalhocmu.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PostDao {

    @Query("select * from Post")
    fun getPosts(): LiveData<List<Post>>

    @Query("select * from Post where Id = :id")
    fun getOnePost(id:String): Post

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(post:Post) : Long

    @Update
    fun update(post:Post)

    @Delete
    fun delete(post:Post) : Int
}
