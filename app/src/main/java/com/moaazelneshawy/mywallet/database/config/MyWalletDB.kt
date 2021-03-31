package com.moaazelneshawy.mywallet.database.config

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.moaazelneshawy.mywallet.database.model.Transaction
import com.moaazelneshawy.mywallet.database.model.TransactionDao
import com.moaazelneshawy.mywallet.database.model.Person
import com.moaazelneshawy.mywallet.database.model.PersonDao

/**
Created by Moaaz Elneshawy
on 30,March,2021
 **/

@Database(entities = [Transaction::class, Person::class], version = 1)
abstract class MyWalletDB : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun personDao(): PersonDao

    companion object {
        private val LOCK = Any()
        private var sInstance: MyWalletDB? = null
        fun getInstance(context: Context): MyWalletDB? {
            if (sInstance == null) {
                synchronized(LOCK) {
                    sInstance = Room.databaseBuilder(
                        context.applicationContext,
                        MyWalletDB::class.java, "myWalletDB"
                    )
                        .build()
                }
            }
            return sInstance
        }
    }
}