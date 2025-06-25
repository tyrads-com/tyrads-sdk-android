package com.example.example_java;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.tyrads.sdk.Tyrads;
import com.tyrads.sdk.acmo.modules.dashboard.TopPremiumOffersView;

public class MainActivity extends AppCompatActivity {

    // UI Components
    private EditText apiKeyInput;
    private EditText apiSecretInput;
    private EditText userIdInput;
    private Button showOffersButton;
    private ProgressBar loadingIndicator;
    private ProgressBar initializingIndicator;
    private TopPremiumOffersView topOffersView; // Changed to specific type

    // State flags
    private boolean isLoadingOffers = false;
    private boolean isTyradsInitialized = false;

    // SharedPreferences
    private SharedPreferences sharedPreferences;

    // Default values (should be in config or build config)
    private static final String DEFAULT_API_KEY = "4f0eaa99e38e49b8b52804116e638a41";
    private static final String DEFAULT_API_SECRET = "cd3c34a52a3b75a3fdd928774615d4e142dd2e6a8ce9da14df4205c7cc812ce81d3656e3dc2c0c58ed05c75c57f87a3431fed62725bb0286f9461521b6c9997a";
    private static final String DEFAULT_USER_ID = "6";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializeSharedPreferences();
        loadSavedValues();
        initializeTyrads();

        showOffersButton.setOnClickListener(v -> handleButtonClick());
    }

    private void initializeViews() {
        apiKeyInput = findViewById(R.id.apiKeyInput);
        apiSecretInput = findViewById(R.id.apiSecretInput);
        userIdInput = findViewById(R.id.userIdInput);
        showOffersButton = findViewById(R.id.showOffersButton);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        initializingIndicator = findViewById(R.id.initializingIndicator);
        topOffersView = findViewById(R.id.topOffersView);

        // Configure the TopOffersView with default values
        topOffersView.setConfig(true, true, false, 2);
    }

    private void initializeSharedPreferences() {
        sharedPreferences = getSharedPreferences("TyradsPrefs", Context.MODE_PRIVATE);
    }

    private void loadSavedValues() {
        apiKeyInput.setText(sharedPreferences.getString("apiKey", ""));
        apiSecretInput.setText(sharedPreferences.getString("apiSecret", ""));
        userIdInput.setText(sharedPreferences.getString("userId", DEFAULT_USER_ID));
    }

    private void initializeTyrads() {
        new Thread(() -> {
            try {
                // Initialize with default values
                Tyrads.getInstance().init(
                        this,
                        DEFAULT_API_KEY,
                        DEFAULT_API_SECRET,
                        true
                );

                // Login with default user
                Tyrads.getInstance().loginUser(DEFAULT_USER_ID);

                runOnUiThread(() -> {
                    isTyradsInitialized = true;
                    initializingIndicator.setVisibility(View.GONE);
                    topOffersView.setVisibility(View.VISIBLE);
                });

            } catch (Exception e) {
                Log.e("TyradsInit", "Error initializing Tyrads", e);
                runOnUiThread(() -> {
                    initializingIndicator.setVisibility(View.GONE);
                    showErrorToast("Error initializing Tyrads");
                });
            }
        }).start();
    }

    private void handleButtonClick() {
        if (isLoadingOffers) return;

        isLoadingOffers = true;
        updateLoadingState();

        new Thread(() -> {
            try {
                String apiKey = getInputValue(apiKeyInput, DEFAULT_API_KEY);
                String apiSecret = getInputValue(apiSecretInput, DEFAULT_API_SECRET);
                String userId = getInputValue(userIdInput, DEFAULT_USER_ID);

                saveToSharedPreferences(apiKey, apiSecret, userId);

                // Reinitialize with new values
                Tyrads.getInstance().init(this, apiKey, apiSecret, true);
                Tyrads.getInstance().loginUser(userId);

                // Show offers in the embedded view
                runOnUiThread(() -> {
                    topOffersView.setConfig(true, true, false, 2);
                    topOffersView.setVisibility(View.VISIBLE);
                });

            } catch (Exception e) {
                Log.e("ShowOffers", "Error showing offers", e);
                runOnUiThread(() -> showErrorToast("Error showing offers"));
            } finally {
                runOnUiThread(() -> {
                    isLoadingOffers = false;
                    updateLoadingState();
                });
            }
        }).start();
    }

    private String getInputValue(EditText input, String defaultValue) {
        String value = input.getText().toString().trim();
        return value.isEmpty() ? defaultValue : value;
    }

    private void saveToSharedPreferences(String apiKey, String apiSecret, String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("apiKey", apiKey);
        editor.putString("apiSecret", apiSecret);
        editor.putString("userId", userId);
        editor.apply();
    }

    private void updateLoadingState() {
        showOffersButton.setText(isLoadingOffers ? "Loading..." : "Show Offers");
        showOffersButton.setEnabled(!isLoadingOffers);
        loadingIndicator.setVisibility(isLoadingOffers ? View.VISIBLE : View.GONE);
        loadingIndicator.setMax(16);
    }

    private void showErrorToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}