package com.example.trabalhocmu.database

import androidx.lifecycle.LiveData


class PostRepo(private val postDatabase: PostDao) : PostDao {

    /**
     * Retorna um LiveData contendo a lista de todos os posts.
     *
     * @return LiveData contendo a lista de todos os posts.
     */
    override fun getPosts(): LiveData<List<Post>> {
        return postDatabase.getPosts()
    }

    /**
     * Retorna um post específico com o ID fornecido.
     *
     * @param id ID do post a ser retornado.
     * @return O post correspondente ao ID fornecido.
     */
    override fun getOnePost(id: String): Post {
        return postDatabase.getOnePost(id)
    }

    /**
     * Insere um novo post na base de dados ROOM.
     *
     * @param post O post a ser inserido.
     * @return O ID do post inserido.
     */
    override fun insert(post: Post): Long {
        return postDatabase.insert(post)
    }

    /**
     * Atualiza um post existente na base de dados ROOM.
     *
     * @param post O post a ser atualizado.
     */
    override fun update(post: Post) {
        return postDatabase.update(post)
    }

    /**
     * Exclui um post existente da base de dados ROOM.
     *
     * @param post O post a ser excluído.
     * @return O número de linhas afetadas pela exclusão (deve ser 1 se o post for excluído com
     * sucesso, 0 caso contrário).
     */
    override fun delete(post: Post): Int {
        return postDatabase.delete(post)
    }
}