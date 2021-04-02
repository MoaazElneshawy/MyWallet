package com.moaazelneshawy.mywallet.database.model

import androidx.lifecycle.LiveData
import androidx.room.*

/**
Created by Moaaz Elneshawy
on 30,March,2021
 **/

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions where creditor_debtor=:creditorOrDebtor")
    fun getAllCreditorDebtorMoney(creditorOrDebtor: String): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions")
    fun getAllTransactions(): LiveData<List<Transaction>>

    @Insert
    fun addMoney(transaction: Transaction): Long

    @Update
    fun updateMoney(transaction: Transaction): Int

    @Delete
    fun deleteMoney(transaction: Transaction): Int

    @Query("SELECT * FROM transactions where mobile_number=:mobileNumber")
    fun getPersonMoney(mobileNumber: String): LiveData<List<Transaction>>

    @Query("DELETE FROM transactions where mobile_number=:mobileNumber")
    fun deletePersonMoney(mobileNumber: String)

    @Update
    fun updateTransaction(transaction: Transaction): Int

}