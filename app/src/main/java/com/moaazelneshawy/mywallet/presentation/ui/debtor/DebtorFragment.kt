package com.moaazelneshawy.mywallet.presentation.ui.debtor

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.moaazelneshawy.mywallet.R
import com.moaazelneshawy.mywallet.database.model.Person
import com.moaazelneshawy.mywallet.database.model.TRANSACTION_STATE
import com.moaazelneshawy.mywallet.database.model.Transaction
import com.moaazelneshawy.mywallet.databinding.DialogAddNewTransactionBinding
import com.moaazelneshawy.mywallet.databinding.DialogAddPersonBinding
import com.moaazelneshawy.mywallet.databinding.FragmentDebtorBinding
import com.moaazelneshawy.mywallet.presentation.MyViewModel
import com.moaazelneshawy.mywallet.presentation.ui.MoneyAdapter
import com.moaazelneshawy.mywallet.presentation.ui.OnMoneyActionsListener
import com.moaazelneshawy.mywallet.utils.*
import gun0912.tedbottompicker.TedBottomPicker
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*

class DebtorFragment : Fragment(), OnMoneyActionsListener {

    private lateinit var viewModel: MyViewModel
    private lateinit var binding: FragmentDebtorBinding

    private lateinit var addTransactionDialog: AppCompatDialog
    private lateinit var addTransactionBinding: DialogAddNewTransactionBinding
    private lateinit var creditorAdapter: MoneyAdapter

    private lateinit var addPersonDialog: AppCompatDialog
    private lateinit var addPersonBinding: DialogAddPersonBinding

    private var editedTransaction: Transaction? = null
    private var selectedPerson: Person? = null
    private val allPersons = ArrayList<Person>()

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
        binding = FragmentDebtorBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewClicks()
        observeViewModel()
    }

    private fun onViewClicks() {
        binding.addTransactionBTN.setOnClickListener {
            binding.fabsGroup.close(true)
            if (allPersons.isNotEmpty())
                showCashDialog(false)
            else binding.root.snackbar(getString(R.string.add_persons_first))
        }
        binding.addPersonBTN.setOnClickListener {
            binding.fabsGroup.close(true)
            showAddPersonDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.debtors?.observe(viewLifecycleOwner) {
            it?.let {
                setTransactions(it)
            }
        }
        viewModel.allPersons?.observe(viewLifecycleOwner) {
            it?.let {
                with(allPersons) {
                    clear()
                    addAll(it)
                }
            }
        }
    }

    private fun setTransactions(list: List<Transaction>) {
        if (list.isEmpty()) {
            binding.noDataTV.visible()
            binding.debtorFragmentRV.gone()
        } else {
            binding.noDataTV.gone()
            creditorAdapter = MoneyAdapter(list, this)
            binding.debtorFragmentRV.apply {
                linearLayoutManager()
                visible()
                adapter = creditorAdapter
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun showCashDialog(isEdit: Boolean) {
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
        if (isEdit) {
            editedTransaction?.let {
                addTransactionBinding.dateET.setText(it.date)
                addTransactionBinding.reasonET.setText(it.reason)
                addTransactionBinding.cashET.setText(it.cash.toString())
                date = it.date
            }
            addTransactionBinding.addMoneyBTN.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_edit))
        } else {
            addTransactionBinding.dateET.clearText()
            addTransactionBinding.reasonET.clearText()
            addTransactionBinding.cashET.clearText()
            date = ""
            addTransactionBinding.addMoneyBTN.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_add))
        }
        setPersonsSP()
        onEditChangesAtTransactionDialog()
        onDialogClicksAtTransactionDialog(isEdit)
        addTransactionDialog.show()
    }

    private fun setPersonsSP() {
        val titles = ArrayList<String>()
        allPersons.forEach { titles.add(it.name) }
        val arrayAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            titles
        )
        addTransactionBinding.personsSP.apply {
            adapter = arrayAdapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedPerson = allPersons[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
        }
    }

    private fun onDialogClicksAtTransactionDialog(isEdit: Boolean) {
        addTransactionBinding.addMoneyBTN.setOnClickListener { addMoney(isEdit) }
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

    private fun addMoney(isEdit: Boolean) {
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

        if (isEdit) {
            editedTransaction?.let {
                viewModel.updateTransactionWallet(
                    Transaction(
                        id = it.id,
                        mobileNumber = it.mobileNumber,
                        name = it.name,
                        cash = cash.toDouble(),
                        reason = reason,
                        creditorOrDebtor = TRANSACTION_STATE.DEBTOR.name,
                        date = date
                    )
                )
            }
            creditorAdapter.notifyDataSetChanged()
        } else viewModel.addToWallet(
            Transaction(
                mobileNumber = selectedPerson!!.mobileNumber,
                name = selectedPerson!!.name,
                cash = cash.toDouble(),
                reason = reason,
                creditorOrDebtor = TRANSACTION_STATE.DEBTOR.name,
                date = date
            )
        )
        addTransactionDialog.dismiss()
    }

    private fun onEditChangesAtTransactionDialog() {
        addTransactionBinding.cashET.addTextChangedListener { addTransactionBinding.cashInput.clearError() }
        addTransactionBinding.dateET.addTextChangedListener { addTransactionBinding.dateInput.clearError() }
        addTransactionBinding.reasonET.addTextChangedListener { addTransactionBinding.reasonInput.clearError() }
    }

    private fun addImage() {
        TedBottomPicker.with(this.requireActivity())
            .show {
                val imageUri: Uri = it
                val imageStream: InputStream? =
                    requireActivity().contentResolver.openInputStream(imageUri)
                val selectedImage = BitmapFactory.decodeStream(imageStream)
                image = encodeImage(selectedImage) ?: ""
                addPersonBinding.imageNameTV.text = getString(R.string.image_selected)
            }
    }

    private fun encodeImage(bm: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    override fun onDelete(transaction: Transaction) {
        viewModel.deleteFromWallet(transaction)
        creditorAdapter.notifyDataSetChanged()
    }

    override fun onEdit(transaction: Transaction) {
        editedTransaction = transaction
        showCashDialog(true)
    }

    private fun showAddPersonDialog() {
        if (::addPersonDialog.isInitialized.not()) {
            addPersonBinding = DialogAddPersonBinding.inflate(layoutInflater)
            addPersonDialog = AppCompatDialog(requireContext())
            addPersonDialog.apply {
                setContentView(addPersonBinding.root)
                setCancelable(true)
                setCanceledOnTouchOutside(true)
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
        addPersonBinding.imageNameTV.text = getString(R.string.image)
        addPersonBinding.nameET.clearText()
        addPersonBinding.mobileET.clearText()
        image = ""

        onEditChangesAtPersonDialog()
        onDialogClicksAtPersonDialog()
        addPersonBinding.mobileET.clearText()
        addPersonBinding.nameET.clearText()
        addPersonDialog.show()
    }

    private fun onDialogClicksAtPersonDialog() {
        addPersonBinding.closeIV.setOnClickListener { addPersonDialog.dismiss() }
        addPersonBinding.addImageBTN.setOnClickListener {
            TedPermission.with(requireContext())
                .setPermissionListener(object : PermissionListener {
                    override fun onPermissionGranted() {
                        addImage()
                    }

                    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {

                    }
                })
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .check()
        }
    }

    private fun onEditChangesAtPersonDialog() {
        addPersonBinding.nameET.addTextChangedListener { addPersonBinding.nameInput.clearError() }
        addPersonBinding.mobileET.addTextChangedListener { addPersonBinding.mobileInput.clearError() }
        addPersonBinding.addPersonBTN.setOnClickListener { addPerson() }
    }

    private fun addPerson() {
        val name = addPersonBinding.nameET.getTextValue()
        val mobile = addPersonBinding.mobileET.getTextValue()
        if (name.isEmpty()) {
            addPersonBinding.nameInput.setErrorMessage(getString(R.string.error_empty_field))
            return
        }
        if (mobile.isEmpty()) {
            addPersonBinding.mobileInput.setErrorMessage(getString(R.string.error_empty_field))
            return
        }
        val l = viewModel.addPersonAsync(Person(mobile, name, image))
        addPersonDialog.dismiss()
    }
}