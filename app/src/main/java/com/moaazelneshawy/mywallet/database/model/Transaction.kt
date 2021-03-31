package com.moaazelneshawy.mywallet.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
Created by Moaaz Elneshawy
on 30,March,2021
 **/

@Entity(tableName = "transactions")
class Transaction(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: Int = System.currentTimeMillis().toInt(),
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "mobile_number") val mobileNumber: String,
    @ColumnInfo(name = "reason") val reason: String,
    @ColumnInfo(name = "creditor_debtor") val creditorOrDebtor: String,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "cash") val cash: Double
)