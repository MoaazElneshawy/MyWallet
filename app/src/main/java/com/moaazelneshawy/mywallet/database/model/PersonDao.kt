package com.moaazelneshawy.mywallet.database.model

import androidx.lifecycle.LiveData
import androidx.room.*

/**
Created by Moaaz Elneshawy
on 30,March,2021
 **/

@Dao
interface PersonDao {

    @Update
    fun updatePerson(person: Person): Int

    @Insert
    fun addPerson(person: Person): Long

    @Delete
    fun deletePerson(person: Person): Int

    @Query("SELECT * FROM persons")
    fun getAllPersons(): LiveData<List<Person>>

    @Query("SELECT * FROM persons where mobile_number=:mobileNumber")
    fun getPersonDetails(mobileNumber: String): LiveData<Person>
}