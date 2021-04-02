package com.moaazelneshawy.mywallet.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.moaazelneshawy.mywallet.database.data.DatabaseRepo
import com.moaazelneshawy.mywallet.database.model.Person
import com.moaazelneshawy.mywallet.database.model.TRANSACTION_STATE
import com.moaazelneshawy.mywallet.database.model.Transaction
import kotlinx.coroutines.runBlocking

/**
Created by Moaaz Elneshawy
on 30,March,2021
 **/


class MyViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = DatabaseRepo(application)

    // creditors
    fun addToWallet(transaction: Transaction) =
        runBlocking { repo.addMoneyToMyWallet(transaction) }


    fun deleteFromWallet(transaction: Transaction) =
        runBlocking { repo.deleteMoneyFromWallet(transaction) }

    fun updateTransactionWallet(transaction: Transaction) =
        runBlocking { repo.updateTransaction(transaction) }


    fun deletePerson(person: Person) =
        runBlocking { repo.deletePerson(person) }

    fun addPerson(person: Person) =
        runBlocking { repo.addPerson(person) }

    fun updatePerson(person: Person) =
        runBlocking { repo.updatePerson(person) }

    val creditors: LiveData<List<Transaction>>?
        get() {
            return repo.getAllCreditorDebtorMoney(TRANSACTION_STATE.CREDITOR.name)
        }

    val transactoins: LiveData<List<Transaction>>?
        get() = repo.getAllTransactions()

    val debtors: LiveData<List<Transaction>>?
        get() {
            return repo.getAllCreditorDebtorMoney(TRANSACTION_STATE.DEBTOR.name)
        }

    val allPersons: LiveData<List<Person>>?
        get() = repo.getAllPersons()

    fun getPersonMoney(mobileNumber: String): LiveData<List<Transaction>>? {
        return repo.getPersonMoney(mobileNumber)
    }

    fun getPersonDetails(mobileNumber: String): LiveData<Person>? {
        return repo.getPersonDetails(mobileNumber)
    }

}