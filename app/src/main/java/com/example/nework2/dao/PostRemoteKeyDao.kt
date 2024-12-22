package com.example.nework2.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nework2.entity.PostRemoteKeyEntity

//описание таблицы БД и запросов в эту таблицу
@Dao
interface PostRemoteKeyDao {
    //получение макс id из БД, который вернет самый новый пост
    @Query("SELECT max(`key`) FROM PostRemoteKeyEntity")
    suspend fun max(): Long?

    //получение мин id из БД, который вернет самый старый пост
    @Query("SELECT min(`key`) FROM PostRemoteKeyEntity")
    suspend fun min(): Long?

    //в случае возникновения конфликта будут перезаписываться данные в таблице
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(postRemoteKeyEntity: PostRemoteKeyEntity)

    //записывает список из данных эксземпляров
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(postRemoteKeyEntity: List<PostRemoteKeyEntity>)

    //очистка таблицы
    @Query("DELETE FROM PostRemoteKeyEntity")
    suspend fun clear()
}