package com.example.trabalhocmu

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.trabalhocmu.database.Post
import com.example.trabalhocmu.database.PostCategory
import com.example.trabalhocmu.database.PostDatabase
import com.example.trabalhocmu.database.PostRepo
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FirestorePostViewModel(application: Application) : AndroidViewModel(application) {

    private val db: FirebaseFirestore
    private val postRepoRoom: PostRepo
    private val collectionName: String
    val postsLiveData: MutableLiveData<List<Post>>
    val postsRoomLiveData: LiveData<List<Post>>


    init {
        db = Firebase.firestore
        collectionName = "POSTS"
        postsLiveData = MutableLiveData(listOf())
        val postDatabaseRoom = PostDatabase.getDatabase(application)
        postRepoRoom = PostRepo(postDatabaseRoom.getPostDao())
        getPostsLive { posts ->
            postsLiveData.postValue(posts)
        }
        postsRoomLiveData = getPosts()
        syncRoomWithFirestore()

    }

    /**
     * Insere um novo post na Firestore.
     *
     * @param post O objeto Post a ser inserido na base de dados.
     */
    fun insertPost(post: Post) {
        viewModelScope.launch {
            val postToSave = hashMapOf(
                "post" to post
            )
            db.collection(collectionName).document(post.Id).set(postToSave)
        }
    }

    /**
     * Atualiza um post existente na Firestore.
     *
     * @param post O objeto Post alterado a ser atualizado na base de dados.
     */
    fun update(post: Post) {
        viewModelScope.launch {
            val postToUpdate = hashMapOf(
                "post" to post
            )
            db.collection(collectionName).document(post.Id).set(postToUpdate, SetOptions.merge())
        }
    }

    /**
     * Obtém uma lista de posts em tempo real da Firestore
     *
     * @param callback Uma função callback que recebe a lista de posts.
     */
    private fun getPostsLive(callback: (List<Post>) -> Unit) {
        viewModelScope.launch {
            val ref = db.collection(collectionName)
            ref.addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    val list = mutableListOf<Post>()
                    for (document in snapshot.documents) {
                        val x = document.data
                        val post = x?.get("post") as? Map<*, *>
                        if (post != null) {
                            val id = post["id"] as String ?: ""
                            val title = post["title"] as? String ?: ""
                            val description = post["description"] as? String ?: ""
                            val photo = post["photo"] as? String ?: ""
                            val address = post["address"] as? String ?: ""
                            val lat = post["lat"] as? Double ?: 0.0
                            val lon = post["lon"] as? Double ?: 0.0


                            val lifespan = (post["lifespan"] as? Long ?: 0).toInt()

                            val categoryString =
                                post["category"] as? String ?: PostCategory.NORMAL.name
                            val category = PostCategory.valueOf(categoryString)

                            val upvotes = (post["upvotes"] as? Long ?: 0).toInt()
                            val downvotes = (post["downvotes"] as? Long ?: 0).toInt()
                            val userUp = post["userUp"] as? List<String> ?: emptyList()
                            val userDown = post["userDown"] as? List<String> ?: emptyList()
                            val ratings = post["ratings"] as? List<Int> ?: emptyList()
                            val userRate = post["userRate"] as? List<String> ?: emptyList()
                            val comments = post["userDown"] as? List<String> ?: emptyList()


                            val post = Post(
                                id,
                                title,
                                description,
                                photo,
                                address,
                                lat,
                                lon,
                                lifespan,
                                category,
                                upvotes,
                                downvotes,
                                userUp,
                                userDown,
                                ratings,
                                userRate,
                                comments
                            )
                            list.add(post)
                        }
                    }
                    postsLiveData.postValue(list)
                    callback(list)
                }
            }
        }
    }

    /**
     * Obtém uma lista de posts da base de dados ROOM.
     *
     * @return Um LiveData que contém a lista de posts.
     */
    fun getPosts(): LiveData<List<Post>> {
        return postRepoRoom.getPosts()
    }

    /**
     * Obtém um único post da base de dados ROOM com o ID fornecido.
     *
     * @param id O ID do post a ser obtido.
     * @return O objeto Post correspondente ao ID fornecido.
     */
    suspend fun getOnePost(id: String): Post {
        return withContext(Dispatchers.IO) {
            postRepoRoom.getOnePost(id)
        }
    }

    /**
     * Insere um novo post na base de dados ROOM.
     *
     * @param post O objeto Post a ser inserido na base de dados ROOM.
     */
    fun insert(post: Post) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                postRepoRoom.insert(post)
            }
        }
    }

    /**
     * Atualiza um post existente na base de dados ROOM.
     *
     * @param post O objeto Post alterado a ser atualizado na base de dados ROOM.
     */
    fun updateRoom(post: Post) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                postRepoRoom.update(post)
            }
        }
    }

    /**
     * Elimina um post da base de dados ROOM.
     *
     * @param post O objeto Post a ser eliminado da base de dados ROOM.
     */
    fun delete(post: Post) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                postRepoRoom.delete(post)
            }
        }
    }

    /**
     * Sincroniza os posts armazenados localmente no Room com os documentos correspondentes na Firestore.
     * Verifica se cada post no Room existe na Firestore e, se não, insere-o na Firestore.
     */
    private fun syncRoomWithFirestore() {
        postsRoomLiveData.value?.let { posts ->
            if (posts.isNotEmpty()) {
                posts.forEach { post ->
                    val firestoreDocument = db.collection(collectionName).document(post.Id)

                    firestoreDocument.get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (!documentSnapshot.exists()) {
                                firestoreDocument.set(post)
                                    .addOnSuccessListener {
                                        Log.d(
                                            "FirestorePostViewModel",
                                            "Post ${post.Id} sincronizado com sucesso com o Firestore."
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(
                                            "FirestorePostViewModel",
                                            "Erro ao inserir o post ${post.Id} no Firestore: ${e.message}",
                                            e
                                        )
                                    }
                            } else {
                                Log.d(
                                    "FirestorePostViewModel",
                                    "O post ${post.Id} já existe no Firestore."
                                )
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(
                                "FirestorePostViewModel",
                                "Erro ao verificar a existência do post ${post.Id} no Firestore: ${e.message}",
                                e
                            )
                        }
                }
            } else {
                Log.d("FirestorePostViewModel", "A lista de posts do Room está vazia.")
            }
        } ?: Log.d("FirestorePostViewModel", "A lista de posts do Room é nula.")
    }

    /**
     * Incrementa o número de upvotes de um post e atualiza na Firestore e no Room.
     *
     * @param post O post a ser upvoted.
     * @param viewModel O ViewModel associado a esta operação.
     */
    fun upvotePost(post: Post, viewModel: FirestorePostViewModel) {
        val user = com.google.firebase.Firebase.auth.currentUser

        if (user != null) {
            if (!post.userUp.contains(user.email)) {
                post.upvotes++
                val mutableUserUpList = post.userUp.toMutableList()
                user.email?.let { mutableUserUpList.add(it) }
                post.userUp = mutableUserUpList.toList()
                if (post.userDown.contains(user.email)) {
                    post.downvotes--
                    val mutableUserDownList = post.userDown.toMutableList()
                    user.email?.let { mutableUserDownList.remove(it) }
                    post.userDown = mutableUserDownList.toList()
                }
                viewModel.update(post)
                viewModel.updateRoom(post)
            }
        }
    }

    /**
     * Incrementa o número de downvotes de um post e atualiza na Firestore e no Room.
     *
     * @param post O post a ser downvoted.
     * @param viewModel O ViewModel associado a esta operação.
     */
    fun downvotePost(post: Post, viewModel: FirestorePostViewModel) {

        val user = com.google.firebase.Firebase.auth.currentUser

        if (user != null) {
            if (!post.userDown.contains(user.email)) {
                post.downvotes++
                val mutableUserDownList = post.userDown.toMutableList()
                user.email?.let { mutableUserDownList.add(it) }
                post.userDown = mutableUserDownList.toList()
                if (post.userUp.contains(user.email)) {
                    post.upvotes--
                    val mutableUserUpList = post.userUp.toMutableList()
                    user.email?.let { mutableUserUpList.remove(it) }
                    post.userUp = mutableUserUpList.toList()
                }
                viewModel.update(post)
                viewModel.updateRoom(post)
            }
        }
    }

    /**
     * Remove o upvote de um post e atualiza na Firestore e no Room.
     *
     * @param post O post do qual o upvote será removido.
     * @param viewModel O ViewModel associado a esta operação.
     */
    fun removeUpvotePost(post: Post, viewModel: FirestorePostViewModel) {
        val user = com.google.firebase.Firebase.auth.currentUser

        if (user != null) {
            if (post.userUp.contains(user.email)) {
                post.upvotes--
                val mutableUserUpList = post.userUp.toMutableList()
                user.email?.let { mutableUserUpList.remove(it) }
                post.userUp = mutableUserUpList.toList()

                viewModel.update(post)
                viewModel.updateRoom(post)
            }
        }
    }

    /**
     * Remove o downvote de um post e atualiza na Firestore e no Room.
     *
     * @param post O post do qual o downvote será removido.
     * @param viewModel O ViewModel associado a esta operação.
     */
    fun removeDownvotePost(post: Post, viewModel: FirestorePostViewModel) {

        val user = com.google.firebase.Firebase.auth.currentUser

        if (user != null) {
            if (post.userDown.contains(user.email)) {
                post.downvotes--
                val mutableUserDownList = post.userDown.toMutableList()
                user.email?.let { mutableUserDownList.remove(it) }
                post.userDown = mutableUserDownList.toList()

                viewModel.update(post)
                viewModel.updateRoom(post)
            }
        }
    }

    /**
     * Adiciona um comentário a um post e atualiza na Firestore e no Room.
     *
     * @param post O post ao qual o comentário será adicionado.
     * @param comment O comentário a ser adicionado.
     * @param viewModel O ViewModel associado a esta operação.
     */
    fun addComent(post: Post, comment: String, viewModel: FirestorePostViewModel) {
        val user = com.google.firebase.Firebase.auth.currentUser
        if (user != null) {
            val mutableUserCommentsList = post.comments.toMutableList()
            var commentToSave = "${user.email}: ${comment}"
            mutableUserCommentsList.add(commentToSave)
            post.comments = mutableUserCommentsList.toList()

            viewModel.update(post)
            viewModel.updateRoom(post)
        }
    }

    /**
     * Adiciona um rating a um post e atualiza na Firestore e no Room.
     *
     * @param post O post ao qual a avaliação será adicionada.
     * @param rating A avaliação a ser adicionada.
     * @param viewModel O ViewModel associado a esta operação.
     */
    fun ratePost(post: Post, rating: Int, viewModel: FirestorePostViewModel){
        val user = com.google.firebase.Firebase.auth.currentUser
        if (user != null) {
            val mutableRatingsList = post.ratings.toMutableList()
            mutableRatingsList.add(rating)
            post.ratings = mutableRatingsList.toList()
            val mutableUserRatingsList = post.userRate.toMutableList()
            user.email?.let { mutableUserRatingsList.add(it) }
            post.userRate = mutableUserRatingsList.toList()

            viewModel.update(post)
            viewModel.updateRoom(post)
        }
    }
}

