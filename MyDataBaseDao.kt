package com.bignerdranch.android.application_practica2.ui.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MyDataBaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(myData: MyData)

    @Update
    fun update(myData: MyData)

    @Query("SELECT id,image,name,surname,`group` FROM mydata LIMIT 10")
    fun query(): Flow<List<MyData>>

    @Query("SELECT * FROM mydata WHERE id = :id")
    fun getById(id:Int):MyData?

}