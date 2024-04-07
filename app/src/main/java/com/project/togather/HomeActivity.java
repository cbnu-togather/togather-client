package com.project.togather;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.project.togather.databinding.ActivityHomeBinding;
import com.project.togather.databinding.ActivitySignUpBinding;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}