package com.moaazelneshawy.mywallet.presentation.ui

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.moaazelneshawy.mywallet.R
import com.moaazelneshawy.mywallet.database.model.TRANSACTION_STATE
import com.moaazelneshawy.mywallet.database.model.Transaction
import com.moaazelneshawy.mywallet.databinding.ItemMoneyBinding

/**
Created by Moaaz Elneshawy
on 30,March,2021
 **/


class MoneyAdapter(
    private val list: List<Transaction>,
    private val listener: OnMoneyActionsListener
) :
    RecyclerView.Adapter<MoneyAdapter.MoneyViewHolder>() {

    inner class MoneyViewHolder(private val itemMoneyBinding: ItemMoneyBinding) :
        RecyclerView.ViewHolder(itemMoneyBinding.root) {
        fun bindViews(transaction: Transaction) {

            if (transaction.creditorOrDebtor == TRANSACTION_STATE.CREDITOR.name) {
                itemMoneyBinding.nameTV.setTextColor(
                    ColorStateList.valueOf(
                        itemMoneyBinding.root.context.getColor(
                            android.R.color.holo_green_dark
                        )
                    )
                )
            } else {
                itemMoneyBinding.nameTV.setTextColor(
                    ColorStateList.valueOf(
                        itemMoneyBinding.root.context.getColor(
                            android.R.color.holo_red_dark
                        )
                    )
                )
            }
            itemMoneyBinding.nameTV.text = transaction.name
            itemMoneyBinding.reasonTV.text = transaction.reason
            itemMoneyBinding.cashOnDateTV.text = itemMoneyBinding.root.context.getString(
                R.string.cash_on_date, transaction.cash.toString(), transaction.date
            )
            itemMoneyBinding.deleteIV.setOnClickListener { listener.onDelete(transaction) }
            itemMoneyBinding.editIV.setOnClickListener { listener.onEdit(transaction) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoneyViewHolder {
        return MoneyViewHolder(
            ItemMoneyBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MoneyViewHolder, position: Int) {
        holder.bindViews(list[position])
    }

    override fun getItemCount() = list.size
}

interface OnMoneyActionsListener {
    fun onDelete(transaction: Transaction)
    fun onEdit(transaction: Transaction)
}