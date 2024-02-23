package com.example.trabalhocmu.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Post(
    @PrimaryKey
    val Id:String,
    val title: String,
    val description: String,
    val photo: String?,
    val address: String?,
    val lat: Double,
    val lon: Double,
    val lifespan: Int = 30,
    val category: PostCategory,
    var upvotes: Int = 0,
    var downvotes: Int = 0,
    var userUp : List<String>,
    var userDown : List<String>,
    var ratings: List<Int>,
    var userRate: List<String>,
    var comments: List<String>
){


    constructor() : this("", "", "", null, null, 0.0,0.0, 30, PostCategory.NORMAL, 0, 0, emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
}

/*data class PostLifespan(
    val category: PostCategory,
    val duration: Duration = Duration.ofDays(30)
)*/

enum class PostCategory {
    NORMAL,
    PERIGO,
    EVENTO
}

