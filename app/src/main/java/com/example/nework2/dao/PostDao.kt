package com.example.nework2.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nework2.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getPagingSourse(): PagingSource<Int, PostEntity>

    @Query("SELECT * FROM PostEntity ORDER BY id COLLATE NOCASE ASC")
    fun getPosts(): PagingSource<Int, PostEntity>

    @Query("SELECT * FROM PostEntity WHERE hidden = 0 ORDER BY id DESC")
    fun getAllVisible(): Flow<List<PostEntity>>

    @Query("SELECT COUNT(*) FROM PostEntity WHERE hidden = 1")
    fun getHiddenCount(): Flow<Int>

    @Query(
        """
        UPDATE PostEntity
        SET hidden = CASE WHEN hidden = 1 THEN 0 ELSE hidden END
        """
    )
    suspend fun changeHiddenPosts()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("UPDATE PostEntity SET content = :content WHERE id = :id")
    suspend fun updateContentById(id: Long, content: String)

//    suspend fun save(post: PostEntity) =
//        if (post.id == 0L) insert(post) else updateContentById(post.id, post.content)

    @Query(
        """
        UPDATE PostEntity SET
        likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id
        """
    )
    suspend fun likeById(id: Long)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM PostEntity")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM PostEntity")
    suspend fun count(): Int
}