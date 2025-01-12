package com.example.nework2.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nework2.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM UserEntity  ORDER BY id DESC")
    fun getAll(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    @Query("SELECT * FROM UserEntity ORDER BY id DESC")
    fun pagingSource(): PagingSource<Int, UserEntity>

    @Query("DELETE FROM UserEntity")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) == 0 FROM UserEntity")
    suspend fun isEmpty(): Boolean
}