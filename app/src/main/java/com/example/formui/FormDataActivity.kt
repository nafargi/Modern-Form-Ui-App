package com.example.formui

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.formui.databinding.ActivityFormDataBinding

class FormDataActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFormDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from intent
        val fullName = intent.getStringExtra("fullName") ?: ""
        val email = intent.getStringExtra("email") ?: ""
        val phone = intent.getStringExtra("phone") ?: ""
        val birthDate = intent.getStringExtra("birthDate") ?: ""
        val gender = intent.getStringExtra("gender") ?: ""
        val imageUriString = intent.getStringExtra("imageUri")

        // Display the data
        binding.fullNameTextView.text = "${getString(R.string.full_name)}: $fullName"
        binding.emailTextView.text = "${getString(R.string.email)}: $email"
        binding.phoneTextView.text = "${getString(R.string.phone)}: $phone"
        binding.birthDateTextView.text = "${getString(R.string.birth_date)}: $birthDate"
        binding.genderTextView.text = "${getString(R.string.gender)}: $gender"

        // Display the image if available
        imageUriString?.let {
            binding.profileImageView.setImageURI(Uri.parse(it))
        }
    }
}