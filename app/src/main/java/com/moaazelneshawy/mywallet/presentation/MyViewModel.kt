package com.moaazelneshawy.mywallet.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.moaazelneshawy.mywallet.database.data.DatabaseRepo
import com.moaazelneshawy.mywallet.database.model.Person
import com.moaazelneshawy.mywallet.database.model.TRANSACTION_STATE
import com.moaazelneshawy.mywallet.database.model.Transaction
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

/**
Created by Moaaz Elneshawy
on 30,March,2021
 **/


class MyViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = DatabaseRepo(application)

    // creditors
    internal fun addToWallet(transaction: Transaction) {
        GlobalScope.async { repo.addMoneyToMyWallet(transaction) }
    }

    internal fun deleteFromWallet(transaction: Transaction) {
        GlobalScope.async { repo.deleteMoneyFromWallet(transaction) }
    }

    internal fun updateTransactionWallet(transaction: Transaction) {
        GlobalScope.async { repo.updateTransaction(transaction) }
    }

    fun deletePerson(person: Person) {
        GlobalScope.async { repo.deletePerson(person) }
    }

    fun addPersonAsync(person: Person) = GlobalScope.async { repo.addPerson(person) }

    fun updatePerson(person: Person) {
        GlobalScope.async {
            repo.updatePerson(person)
        }
    }

    val creditors: LiveData<List<Transaction>>?
        get() {
            return repo.getAllCreditorDebtorMoney(TRANSACTION_STATE.CREDITOR.name)
        }

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