package com.moaazelneshawy.mywallet.presentation.ui.search

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialog
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.moaazelneshawy.mywallet.R
import com.moaazelneshawy.mywallet.database.model.TRANSACTION_STATE
import com.moaazelneshawy.mywallet.database.model.Transaction
import com.moaazelneshawy.mywallet.databinding.DialogAddNewTransactionBinding
import com.moaazelneshawy.mywallet.databinding.FragmentSearchBinding
import com.moaazelneshawy.mywallet.presentation.MyViewModel
import com.moaazelneshawy.mywallet.presentation.ui.MoneyAdapter
import com.moaazelneshawy.mywallet.presentation.ui.OnMoneyActionsListener
import com.moaazelneshawy.mywallet.utils.*
import org.jetbrains.anko.support.v4.alert
import java.util.*

class SearchFragment : Fragment(), OnMoneyActionsListener {

    private lateinit var viewModel: MyViewModel
    private lateinit var binding: FragmentSearchBinding
    private lateinit var moneyAdapter: MoneyAdapter

    private lateinit var addTransactionDialog: AppCompatDialog
    private lateinit var addTransactionBinding: DialogAddNewTransactionBinding
    private var editedTransaction: Transaction? = null

    private var total = MutableLiveData(0)

    private var image = ""
    private var date = ""
    private val calendar = Calendar.getInstance()
    private val currentYear = calendar.get(Calendar.YEAR)
    private val currentMonth = calendar.get(Calendar.MONTH)
    private val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel =
            ViewModelProvider(requireActivity()).get(MyViewModel::class.java)
        binding = FragmentSearchBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewClicks()
        observeViewModel()
    }

    private fun observeViewModel() {
        total.observe(viewLifecycleOwner) {
            it?.let {
                binding.totalTV.text = getString(R.string.total, it)
                if (it > 0) binding.totalTV.setTextColor(requireActivity().getColor(android.R.color.holo_green_dark))
                else binding.totalTV.setTextColor(requireActivity().getColor(android.R.color.holo_red_dark))
            }
        }
    }

    private fun onViewClicks() {
        binding.searchFragmentSearchBTN.setOnClickListener {
            search()
        }
        with(binding.mobileET) {
            doAfterTextChanged { binding.mobileInput.clearError() }
            setActionOnSearch {
                search()
            }
        }

    }

    private fun search() {
        val mobile = binding.mobileET.getTextValue()

        if (mobile.isEmpty()) {
            binding.mobileInput.setErrorMessage(getString(R.string.error_empty_field))
            return
        }

        hideKeyboardFrom(requireContext(), binding.root)

        viewModel.getPersonMoney(mobile)?.observe(viewLifecycleOwner) {
            it?.let { list ->
                if (list.isEmpty()) {
                    binding.searchFragmentRV.gone()
                    binding.totalTV.gone()
                    binding.root.snackbar("$mobile غير موجود ")
                } else {
                    total.value = 0
                    list.forEach {
                        if (it.creditorOrDebtor == TRANSACTION_STATE.CREDITOR.name) {
                            total.value = total.value!! + it.cash
                        } else {
                            total.value = total.value!! - it.cash
                        }
                    }
                    binding.totalTV.visible()
                    moneyAdapter = MoneyAdapter(list, this)
                    binding.searchFragmentRV.apply {
                        linearLayoutManager()
                        visible()
                        adapter = moneyAdapter
                    }
                }

            }
        }
    }

    override fun onDelete(transaction: Transaction) {
        alert {
            title = getString(R.string.delete_transaction_title)
            positiveButton(getString(R.string.delete)) {
                viewModel.deleteFromWallet(transaction)
                moneyAdapter.notifyDataSetChanged()
            }
            negativeButton(getString(R.string.cancel)) { it.dismiss() }
        }.show()
    }

    override fun onEdit(transaction: Transaction) {
        editedTransaction = transaction
        showCashDialog()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun showCashDialog() {
        if (::addTransactionDialog.isInitialized.not()) {
            addTransactionDialog = AppCompatDialog(requireContext())
            addTransactionBinding = DialogAddNewTransactionBinding.inflate(layoutInflater)
            addTransactionDialog.apply {
                setCancelable(true)
                setCanceledOnTouchOutside(true)
                setContentView(addTransactionBinding.root)
                window?.apply {
                    setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    setGravity(Gravity.BOTTOM)
                }
            }
        }


        addTransactionBinding.roleRG.gone()
        addTransactionBinding.personsSP.gone()
        editedTransaction?.let {
            addTransactionBinding.dateET.setText(it.date)
            addTransactionBinding.reasonET.setText(it.reason)
            addTransactionBinding.cashET.setText(it.cash.toString())
            date = it.date
        }
        addTransactionBinding.addMoneyBTN.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_edit))

        onEditChangesAtTransactionDialog()
        onDialogClicksAtTransactionDialog()
        addTransactionDialog.show()
    }

    private fun onDialogClicksAtTransactionDialog() {
        addTransactionBinding.addMoneyBTN.setOnClickListener { addMoney() }
        addTransactionBinding.dateET.setOnClickListener { selectDate() }
        addTransactionBinding.closeIV.setOnClickListener { addTransactionDialog.dismiss() }
    }

    private fun selectDate() {
        val dpd = DatePickerDialog(
            requireContext(), { _, year, monthOfYear, dayOfMonth ->
                date = refactorDatePickerFormat(year, monthOfYear, dayOfMonth)
                addTransactionBinding.dateET.setText(date)
            },
            currentYear,
            currentMonth,
            currentDay
        )
        dpd.datePicker.maxDate = Date().time
        dpd.show()
    }


    private fun addMoney() {
        val cash = addTransactionBinding.cashET.getTextValue()
        val reason = addTransactionBinding.reasonET.getTextValue()
        if (cash.isEmpty()) {
            addTransactionBinding.cashInput.setErrorMessage(getString(R.string.error_empty_field))
            return
        }
        if (reason.isEmpty()) {
            addTransactionBinding.reasonInput.setErrorMessage(getString(R.string.error_empty_field))
            return
        }
        if (date.isEmpty()) {
            addTransactionBinding.dateInput.setErrorMessage(getString(R.string.error_empty_field))
            return
        }

        editedTransaction?.let {
            viewModel.updateTransactionWallet(
                Transaction(
                    id = it.id,
                    mobileNumber = it.mobileNumber,
                    name = it.name,
                    cash = cash.toInt(),
                    reason = reason,
                    creditorOrDebtor = it.creditorOrDebtor,
                    date = date
                )
            )
            binding.root.snackbar(getString(R.string.updated_successfully))
        }
        if (::moneyAdapter.isInitialized) moneyAdapter.notifyDataSetChanged()
        addTransactionDialog.dismiss()
    }

    private fun onEditChangesAtTransactionDialog() {
        addTransactionBinding.cashET.addTextChangedListener { addTransactionBinding.cashInput.clearError() }
        addTransactionBinding.dateET.addTextChangedListener { addTransactionBinding.dateInput.clearError() }
        addTransactionBinding.reasonET.addTextChangedListener { addTransactionBinding.reasonInput.clearError() }
    }
}