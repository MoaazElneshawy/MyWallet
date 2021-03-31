package com.moaazelneshawy.mywallet.presentation

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
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialog
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.moaazelneshawy.mywallet.R
import com.moaazelneshawy.mywallet.database.model.Person
import com.moaazelneshawy.mywallet.database.model.TRANSACTION_STATE
import com.moaazelneshawy.mywallet.database.model.Transaction
import com.moaazelneshawy.mywallet.databinding.ActivityPersonTransactionsBinding
import com.moaazelneshawy.mywallet.databinding.DialogAddNewTransactionBinding
import com.moaazelneshawy.mywallet.databinding.DialogAddPersonBinding
import com.moaazelneshawy.mywallet.utils.*
import gun0912.tedbottompicker.TedBottomPicker
import org.jetbrains.anko.alert
import org.jetbrains.anko.makeCall
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*

class PersonTransactionsActivity : AppCompatActivity(), OnTransactionDeleteListener {

    private lateinit var binding: ActivityPersonTransactionsBinding
    private lateinit var viewModel: MyViewModel
    private lateinit var transactionsAdapter: TransactionsAdapter
    private var total = MutableLiveData(0)
    private var date = ""

    private lateinit var addPersonDialog: AppCompatDialog
    private lateinit var addPersonBinding: DialogAddPersonBinding

    private var image = ""

    private lateinit var addTransactionDialog: AppCompatDialog
    private lateinit var addTransactionBinding: DialogAddNewTransactionBinding

    private var person: Person? = null
    private var editedTransaction: Transaction? = null
    private val calendar = Calendar.getInstance()
    private val currentYear = calendar.get(Calendar.YEAR)
    private val currentMonth = calendar.get(Calendar.MONTH)
    private val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        viewModel = ViewModelProvider(this)[MyViewModel::class.java]
        onViewClicks()
        intent?.extras?.let {
            it.getString("mobileNumber")?.let { it1 ->
                getPersonTransactions(it1)
                getPersonDetails(it1)
            }
        }
        total.observe(this) {
            it?.let {
                binding.totalTV.text = getString(R.string.total, it)
                if (it > 0 || it == 0) binding.totalTV.setTextColor(getColor(android.R.color.holo_green_dark))
                else binding.totalTV.setTextColor(getColor(android.R.color.holo_red_dark))
            }
        }
    }

    private fun getPersonDetails(mobileNumber: String) {
        viewModel.getPersonDetails(mobileNumber)?.observe(this) {
            it?.let {
                person = it
                binding.nameTV.text = it.name
                if (it.image.isNotEmpty()) {
                    val decodedString: ByteArray = Base64.decode(it.image, Base64.DEFAULT)
                    val decodedByte =
                        BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    binding.imageIV.setImageBitmap(decodedByte)
                }
            }
        }
    }

    private fun getPersonTransactions(mobileNumber: String) {
        viewModel.getPersonMoney(mobileNumber)?.observe(this) {
            it?.let { list ->
                if (list.isEmpty()) {
                    binding.noDataTV.visible()
                    binding.transactionsRV.gone()
                    binding.totalTV.gone()
                } else {
                    binding.noDataTV.gone()
                    total.value = 0
                    list.forEach {
                        if (it.creditorOrDebtor == TRANSACTION_STATE.CREDITOR.name) {
                            total.value = total.value!! + it.cash
                        } else {
                            total.value = total.value!! - it.cash
                        }
                    }
                    binding.totalTV.visible()
                    transactionsAdapter = TransactionsAdapter(list, this)
                    binding.transactionsRV.apply {
                        linearLayoutManager()
                        visible()
                        adapter = transactionsAdapter
                    }
                }

            }
        }
    }

    private fun onViewClicks() {
        binding.backIV.setOnClickListener { onBackPressed() }
        binding.callBTN.setOnClickListener {
            binding.fabsGroup.close(true)
            TedPermission.with(this)
                .setPermissionListener(object : PermissionListener {
                    override fun onPermissionGranted() {
                        person?.mobileNumber?.let { it1 -> makeCall(it1) }
                    }

                    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {

                    }

                })
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(
                    Manifest.permission.CALL_PHONE
                )
                .check()
        }
        binding.deleteBTN.setOnClickListener {
            binding.fabsGroup.close(true)
            alert {
                title = getString(R.string.delete_person_title)
                positiveButton(getString(R.string.delete)) {
                    person?.let { it1 -> viewModel.deletePerson(it1) }
                    this@PersonTransactionsActivity.finish()
                }
                negativeButton(getString(R.string.cancel)) { it.dismiss() }
            }.show()
        }
        binding.addTransactionBTN.setOnClickListener {
            binding.fabsGroup.close(true)
            showCashDialog(false)
        }
        binding.editBTN.setOnClickListener {
            binding.fabsGroup.close(true)
            showAddPersonDialog()
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun showCashDialog(isEdit: Boolean) {
        if (::addTransactionDialog.isInitialized.not()) {
            addTransactionDialog = AppCompatDialog(this)
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

        addTransactionBinding.personsSP.gone()
        addTransactionBinding.roleRG.visible()
        if (isEdit) {
            editedTransaction?.let {
                addTransactionBinding.dateET.setText(it.date)
                addTransactionBinding.reasonET.setText(it.reason)
                addTransactionBinding.cashET.setText(it.cash.toString())
                date = it.date
                addTransactionBinding.creditorRB.isChecked =
                    it.creditorOrDebtor == TRANSACTION_STATE.CREDITOR.name

                addTransactionBinding.debtorRB.isChecked =
                    it.creditorOrDebtor == TRANSACTION_STATE.DEBTOR.name
            }
            addTransactionBinding.addMoneyBTN.setImageDrawable(getDrawable(R.drawable.ic_edit))
        } else {
            addTransactionBinding.dateET.clearText()
            addTransactionBinding.reasonET.clearText()
            addTransactionBinding.cashET.clearText()
            date = ""
            addTransactionBinding.addMoneyBTN.setImageDrawable(getDrawable(R.drawable.ic_add))
        }
        onEditChangesAtTransactionDialog()
        onDialogClicksAtTransactionDialog(isEdit)
        addTransactionDialog.show()
    }


    private fun onEditChangesAtTransactionDialog() {
        addTransactionBinding.cashET.addTextChangedListener { addTransactionBinding.cashInput.clearError() }
        addTransactionBinding.dateET.addTextChangedListener { addTransactionBinding.dateInput.clearError() }
        addTransactionBinding.reasonET.addTextChangedListener { addTransactionBinding.reasonInput.clearError() }
    }


    private fun onDialogClicksAtTransactionDialog(isEdit: Boolean) {
        addTransactionBinding.addMoneyBTN.setOnClickListener { addMoney(isEdit) }
        addTransactionBinding.dateET.setOnClickListener { selectDate() }
        addTransactionBinding.closeIV.setOnClickListener { addTransactionDialog.dismiss() }
    }

    private fun selectDate() {
        val dpd = DatePickerDialog(
            this, { _, year, monthOfYear, dayOfMonth ->
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

        var state = TRANSACTION_STATE.CREDITOR
        if (addTransactionBinding.debtorRB.isChecked) state = TRANSACTION_STATE.DEBTOR

        if (isEdit) {
            editedTransaction?.let {
                viewModel.updateTransactionWallet(
                    Transaction(
                        id = it.id,
                        mobileNumber = person!!.mobileNumber,
                        name = person!!.name,
                        cash = cash.toInt(),
                        reason = reason,
                        creditorOrDebtor = state.name,
                        date = date
                    )
                )
            }
            binding.root.snackbar(getString(R.string.updated_successfully))
        } else {
            viewModel.addToWallet(
                Transaction(
                    mobileNumber = person!!.mobileNumber,
                    name = person!!.name,
                    cash = cash.toInt(),
                    reason = reason,
                    creditorOrDebtor = state.name,
                    date = date
                )
            )
            binding.root.snackbar(getString(R.string.added_successfully))
        }
        if (::transactionsAdapter.isInitialized) transactionsAdapter.notifyDataSetChanged()
        addTransactionDialog.dismiss()
    }

    override fun onDelete(transaction: Transaction) {
        alert {
            title = getString(R.string.delete_transaction_title)
            positiveButton(getString(R.string.delete)) {
                viewModel.deleteFromWallet(transaction)
                transactionsAdapter.notifyDataSetChanged()
            }
            negativeButton(getString(R.string.cancel)) { it.dismiss() }
        }.show()
    }

    override fun onEdit(transaction: Transaction) {
        editedTransaction = transaction
        showCashDialog(true)
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private fun showAddPersonDialog() {
        if (::addPersonDialog.isInitialized.not()) {
            addPersonBinding = DialogAddPersonBinding.inflate(layoutInflater)
            addPersonDialog = AppCompatDialog(this)
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

        onEditChanges()
        onDialogClicks()
        addPersonBinding.mobileET.setText(person!!.mobileNumber)
        image = person!!.image
        addPersonBinding.nameET.setText(person!!.name)
        addPersonBinding.addPersonBTN.setImageDrawable(getDrawable(R.drawable.ic_edit))
        addPersonDialog.show()
    }

    private fun onDialogClicks() {
        addPersonBinding.closeIV.setOnClickListener { addPersonDialog.dismiss() }
        addPersonBinding.addImageBTN.setOnClickListener {
            TedPermission.with(this)
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

    private fun onEditChanges() {
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
        viewModel.updatePerson(Person(mobile, name, image))
        addPersonDialog.dismiss()
    }

    private fun addImage() {
        TedBottomPicker.with(this)
            .show {
                val imageUri: Uri = it
                val imageStream: InputStream? =
                    this.contentResolver.openInputStream(imageUri)
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

}