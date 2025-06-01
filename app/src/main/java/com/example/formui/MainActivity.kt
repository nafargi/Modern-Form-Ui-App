package com.example.formui

import com.example.formui.R
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.formui.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var selectedImageUri: Uri? = null
    private var selectedDate: String = ""
    private var currentLanguage = "en"

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.profileImageView.setImageURI(it)
        }
    }
    private fun setLocale(langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config: Configuration = Configuration()
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)

        val refresh = Intent(this, MainActivity::class.java)
        startActivity(refresh)
        finish()
    }

    override fun attachBaseContext(newBase: Context) {
        val lang = newBase.getSharedPreferences("Settings", MODE_PRIVATE)
            .getString("App_Lang", "en") ?: "en"
        val context = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("Settings", MODE_PRIVATE)
        val lang = prefs.getString("App_Lang", "en") ?: "en"
        LocaleHelper.setLocale(this, lang)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        registerForContextMenu(binding.profileImageView)

        binding.selectImageButton.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.datePickerButton.setOnClickListener {
            showDatePickerDialog()
        }

        binding.submitButton.setOnClickListener {
            if (validateForm()) {
                submitForm()
            }
        }

        binding.resetButton.setOnClickListener {
            resetForm()
        }

        if (savedInstanceState != null) {
            currentLanguage = savedInstanceState.getString("currentLanguage", "en")
            selectedDate = savedInstanceState.getString("selectedDate", "")
            selectedImageUri = savedInstanceState.getString("selectedImageUri")?.let { Uri.parse(it) }
        }
    }

    private fun changeLanguage(langCode: String) {
        val prefs = getSharedPreferences("Settings", MODE_PRIVATE)
        prefs.edit().putString("App_Lang", langCode).apply()

        recreate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_language_english -> {
                setLocale("en")
                true
            }
            R.id.menu_language_amharic -> {
                setLocale("am")
                true
            }
            R.id.menu_settings -> {
                true
            }
            R.id.menu_help -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setLocaleAndRestart(languageCode: String) {
        val prefs = getSharedPreferences("Settings", MODE_PRIVATE)
        prefs.edit() { putString("App_Lang", languageCode) }
        recreate()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("currentLanguage", currentLanguage)
        outState.putString("selectedDate", selectedDate)
        selectedImageUri?.let {
            outState.putString("selectedImageUri", it.toString())
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                selectedDate = dateFormat.format(calendar.time)
                binding.birthDateTextView.text = "${getString(R.string.birth_date)}: $selectedDate"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun validateForm(): Boolean {
        if (binding.fullNameEditText.text.isNullOrEmpty()) {
            binding.fullNameEditText.error = getString(R.string.error_full_name)
            return false
        }

        if (binding.emailEditText.text.isNullOrEmpty()) {
            binding.emailEditText.error = getString(R.string.error_email)
            return false
        }

        if (binding.phoneEditText.text.isNullOrEmpty()) {
            binding.phoneEditText.error = getString(R.string.error_phone)
            return false
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_birth_date), Toast.LENGTH_SHORT).show()
            return false
        }

        if (binding.genderRadioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(this, getString(R.string.error_gender), Toast.LENGTH_SHORT).show()
            return false
        }


        return true
    }

    private fun submitForm() {
        val fullName = binding.fullNameEditText.text.toString()
        val email = binding.emailEditText.text.toString()
        val phone = binding.phoneEditText.text.toString()

        val gender = when (binding.genderRadioGroup.checkedRadioButtonId) {
            R.id.maleRadioButton -> getString(R.string.male)
            R.id.femaleRadioButton -> getString(R.string.female)
            else -> ""
        }

        val intent = Intent(this, FormDataActivity::class.java).apply {
            putExtra("fullName", fullName)
            putExtra("email", email)
            putExtra("phone", phone)
            putExtra("birthDate", selectedDate)
            putExtra("gender", gender)
            selectedImageUri?.let { putExtra("imageUri", it.toString()) }
        }
        startActivity(intent)
    }

    private fun resetForm() {
        binding.fullNameEditText.text?.clear()
        binding.emailEditText.text?.clear()
        binding.phoneEditText.text?.clear()
        binding.genderRadioGroup.clearCheck()
        selectedDate = ""
        binding.birthDateTextView.text = getString(R.string.birth_date)
        selectedImageUri = null
        binding.profileImageView.setImageResource(android.R.drawable.ic_menu_gallery)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.context_edit -> {
                val intent = Intent(Intent.ACTION_PICK).apply {
                    type = "image/*"
                }
                startActivityForResult(intent, PICK_IMAGE_REQUEST)
                true
            }
            R.id.context_delete -> {
                selectedImageUri = null
                binding.profileImageView.setImageResource(android.R.drawable.ic_menu_gallery)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            binding.profileImageView.setImageURI(selectedImageUri)
        }
    }


    companion object {
        private const val PICK_IMAGE_REQUEST = 100
    }
}
