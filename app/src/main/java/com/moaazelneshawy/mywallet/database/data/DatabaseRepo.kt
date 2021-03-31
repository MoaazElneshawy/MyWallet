package com.moaazelneshawy.mywallet.database.data

import android.app.Application
import androidx.lifecycle.LiveData
import com.moaazelneshawy.mywallet.database.config.MyWalletDB
import com.moaazelneshawy.mywallet.database.model.Person
import com.moaazelneshawy.mywallet.database.model.PersonDao
import com.moaazelneshawy.mywallet.database.model.Transaction
import com.moaazelneshawy.mywallet.database.model.TransactionDao
import kotlinx.coroutines.*

/**
Created by Moaaz Elneshawy
on 30,March,2021
 **/


class DatabaseRepo(application: Application) {

    private var transactionDao: TransactionDao? = null
    private var personDao: PersonDao? = null

    init {
        val myWalletDB = MyWalletDB.getInstance(application)
        if (myWalletDB != null) {
            transactionDao = myWalletDB.transactionDao()
            personDao = myWalletDB.personDao()
        }
    }

    // get all money depends on state
    fun getAllCreditorDebtorMoney(creditorOrDebtor: String): LiveData<List<Transaction>>? {
        return transactionDao?.getAllCreditorDebtorMoney(creditorOrDebtor)
    }

    // add new
    suspend fun addMoneyToMyWallet(transaction: Transaction) =
        CoroutineScope(Dispatchers.IO).async {
            return@async transactionDao?.addMoney(transaction)!!
        }.await()


    suspend fun deleteMoneyFromWallet(transaction: Transaction) =
        CoroutineScope(Dispatchers.IO).async {
            return@async transactionDao?.deleteMoney(transaction)!!
        }.await()

    fun getPersonMoney(mobileNumber: String): LiveData<List<Transaction>>? {
        return transactionDao?.getPersonMoney(mobileNumber)
    }

    fun getAllPersons(): LiveData<List<Person>>? {
        return personDao?.getAllPersons()
    }

    fun getPersonDetails(mobileNumber: String): LiveData<Person>? {
        return personDao?.getPersonDetails(mobileNumber)
    }

    suspend fun deletePerson(person: Person) = CoroutineScope(Dispatchers.IO).async {
        transactionDao?.deletePersonMoney(person.mobileNumber)
        return@async personDao?.deletePerson(person)!!
    }.await()

    suspend fun addPerson(person: Person) =
        CoroutineScope(Dispatchers.IO).async {
            return@async personDao?.addPerson(person)!!
        }.await()

    suspend fun updatePerson(person: Person) = CoroutineScope(Dispatchers.IO).async {
        return@async personDao?.updatePerson(person)!!
    }.await()


    suspend fun updateTransaction(transaction: Transaction) = CoroutineScope(Dispatchers.IO).async {
        return@async transactionDao?.updateTransaction(transaction)!!
    }.await()

}