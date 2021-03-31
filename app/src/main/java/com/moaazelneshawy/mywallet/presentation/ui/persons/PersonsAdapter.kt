package com.moaazelneshawy.mywallet.presentation.ui.persons

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.moaazelneshawy.mywallet.database.model.Person
import com.moaazelneshawy.mywallet.databinding.ItemPersonBinding

/**
Created by Moaaz Elneshawy
on 30,March,2021
 **/


class PersonsAdapter(
    private val persons: List<Person>,
    private val listener: OnPersonActionsListener
) : RecyclerView.Adapter<PersonsAdapter.PersonViewHolder>() {

    inner class PersonViewHolder(private val itemPersonBinding: ItemPersonBinding) :
        RecyclerView.ViewHolder(itemPersonBinding.root) {
        fun bind(person: Person) {
            if (person.image.isNotEmpty()) {
                val decodedString: ByteArray = Base64.decode(person.image, Base64.DEFAULT)
                val decodedByte =
                    BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                itemPersonBinding.imageIV.setImageBitmap(decodedByte)
            }
            itemPersonBinding.nameTV.text = person.name
            itemPersonBinding.callIV.setOnClickListener { listener.onCallPerson(person) }
            itemPersonBinding.deleteIV.setOnClickListener { listener.onDeletePerson(person) }
            itemPersonBinding.root.setOnClickListener { listener.onGetPersonTransactions(person) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        return PersonViewHolder(
            ItemPersonBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        holder.bind(persons[position])
    }

    override fun getItemCount() = persons.size
}

interface OnPersonActionsListener {
    fun onCallPerson(person: Person)
    fun onDeletePerson(person: Person)
    fun onGetPersonTransactions(person: Person)
}