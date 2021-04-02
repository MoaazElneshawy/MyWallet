package com.moaazelneshawy.mywallet.presentation.ui.balance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.moaazelneshawy.mywallet.database.model.TRANSACTION_STATE
import com.moaazelneshawy.mywallet.databinding.FragmentBalanceBinding
import com.moaazelneshawy.mywallet.presentation.MyViewModel

class BalanceFragment : Fragment() {

    private lateinit var viewModel: MyViewModel
    private lateinit var binding: FragmentBalanceBinding

    private val debt = MutableLiveData(0)
    private val owned = MutableLiveData(0)
    private val balance = MutableLiveData(0)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity()).get(MyViewModel::class.java)
        binding = FragmentBalanceBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.transactoins?.observe(viewLifecycleOwner) {
            it?.let { transacitions ->
                debt.value = 0
                balance.value = 0
                owned.value = 0
                if (transacitions.isNotEmpty())
                    transacitions.forEach {
                        if (it.creditorOrDebtor == TRANSACTION_STATE.CREDITOR.name)
                            owned.value = owned.value!! + it.cash
                        else debt.value = debt.value!! + it.cash

                        balance.value = owned.value!! - debt.value!!
                    }
            }
        }
        observe()
    }

    private fun observe() {
        debt.observe(viewLifecycleOwner) {
            it?.let { binding.debtTV.text = it.toString() }
        }
        owned.observe(viewLifecycleOwner) {
            it?.let { binding.ownedTV.text = it.toString() }
        }
        balance.observe(viewLifecycleOwner) {
            it?.let {
                binding.balanceTV.text = it.toString()
                if (it > 0 || it == 0) binding.balanceTV.setTextColor(
                    requireContext().getColor(
                        android.R.color.holo_green_dark
                    )
                )
                else binding.balanceTV.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
            }
        }

    }


}