package com.moaazelneshawy.mywallet.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
Created by Moaaz Elneshawy
on 30,March,2021
 **/

@Entity(tableName = "persons")
class Person(
    @PrimaryKey
    @ColumnInfo(name = "mobile_number") val mobileNumber: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "image") val image: String = ""
)