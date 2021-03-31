package com.moaazelneshawy.mywallet.presentation

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.moaazelneshawy.mywallet.database.model.TRANSACTION_STATE
import com.moaazelneshawy.mywallet.database.model.Transaction
import com.moaazelneshawy.mywallet.databinding.ItemTransactionsBinding

/**
Created by Moaaz Elneshawy
on 31,March,2021
 **/


class TransactionsAdapter(
    private val list: List<Transaction>,
    private val listener: OnTransactionDeleteListener
) :
    RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(private val itemBinding: ItemTransactionsBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bindViews(transaction: Transaction) {

            if (transaction.creditorOrDebtor == TRANSACTION_STATE.CREDITOR.name) {
                itemBinding.cashOnDateTV.setTextColor(
                    ColorStateList.valueOf(
                        itemBinding.root.context.getColor(
                            android.R.color.holo_green_dark
                        )
                    )
                )
            } else {
                itemBinding.cashOnDateTV.setTextColor(
                    ColorStateList.valueOf(
                        itemBinding.root.context.getColor(
                            android.R.color.holo_red_dark
                        )
                    )
                )
            }
            itemBinding.reasonTV.text = transaction.reason
            itemBinding.cashOnDateTV.text = "${transaction.cash} - ${transaction.date}"
            itemBinding.deleteIV.setOnClickListener { listener.onDelete(transaction) }
            itemBinding.editIV.setOnClickListener { listener.onEdit(transaction) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        return TransactionViewHolder(
            ItemTransactionsBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bindViews(list[position])
    }

    override fun getItemCount() = list.size
}

interface OnTransactionDeleteListener {
    fun onDelete(transaction: Transaction)
    fun onEdit(transaction: Transaction)
}