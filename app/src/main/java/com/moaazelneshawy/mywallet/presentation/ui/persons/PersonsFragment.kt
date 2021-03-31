package com.moaazelneshawy.mywallet.presentation.ui.persons

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.moaazelneshawy.mywallet.R
import com.moaazelneshawy.mywallet.database.model.Person
import com.moaazelneshawy.mywallet.databinding.DialogAddPersonBinding
import com.moaazelneshawy.mywallet.databinding.FragmentPersonsBinding
import com.moaazelneshawy.mywallet.presentation.MyViewModel
import com.moaazelneshawy.mywallet.presentation.PersonTransactionsActivity
import com.moaazelneshawy.mywallet.utils.*
import gun0912.tedbottompicker.TedBottomPicker
import org.jetbrains.anko.makeCall
import org.jetbrains.anko.support.v4.intentFor
import java.io.ByteArrayOutputStream
import java.io.InputStream

class PersonsFragment : Fragment(), OnPersonActionsListener {

    private lateinit var binding: FragmentPersonsBinding
    private lateinit var viewModel: MyViewModel
    private lateinit var personsAdapter: PersonsAdapter

    private lateinit var addPersonDialog: AppCompatDialog
    private lateinit var addPersonBinding: DialogAddPersonBinding

    private var image = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPersonsBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(MyViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        onViewsClicks()
    }

    private fun onViewsClicks() {
        binding.personsFragmentAddBTN.setOnClickListener { showAddPersonDialog() }
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

        onEditChanges()
        onDialogClicks()
        addPersonBinding.mobileET.clearText()
        addPersonBinding.nameET.clearText()
        addPersonDialog.show()
    }

    private fun onDialogClicks() {
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
        val job = viewModel.addPersonAsync(Person(mobile, name, image))
        Log.e("job", "$job")
        addPersonDialog.dismiss()
    }

    private fun observeViewModel() {
        viewModel.allPersons?.observe(viewLifecycleOwner) {
            it?.let {
                setPersons(it)
            }
        }
    }

    private fun setPersons(list: List<Person>) {
        if (list.isEmpty()) {
            binding.noDataTV.visible()
            binding.personsFragmentRV.gone()
        } else {
            binding.noDataTV.gone()
            personsAdapter = PersonsAdapter(list, this)
            binding.personsFragmentRV.apply {
                linearLayoutManager()
                visible()
                adapter = personsAdapter
            }
        }
    }

    override fun onCallPerson(person: Person) {
        TedPermission.with(requireContext())
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    requireContext().makeCall(person.mobileNumber)
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

    override fun onDeletePerson(person: Person) {
        viewModel.deletePerson(person)
    }

    override fun onGetPersonTransactions(person: Person) {
        startActivity(intentFor<PersonTransactionsActivity>("mobileNumber" to person.mobileNumber))
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


}