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
import com.tyrads.sdk.TyradsCallback;
import com.tyrads.sdk.TyradsLoginCallback;

public class MainActivity extends AppCompatActivity {

    private EditText apiKeyInput;
    private EditText apiSecretInput;
    private EditText encryptionKeyInput;
    private EditText userIdInput;
    private Button showOffersButton;
    private ProgressBar loadingIndicator;
    private ProgressBar initializingIndicator;
    private View topOffersView;

    private boolean isLoadingOffers = false;
    private boolean isTyradsInitialized = false;

    private SharedPreferences sharedPreferences;

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
        encryptionKeyInput = findViewById(R.id.encryptionKeyInput);
        userIdInput = findViewById(R.id.userIdInput);
        showOffersButton = findViewById(R.id.showOffersButton);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        initializingIndicator = findViewById(R.id.initializingIndicator);
        topOffersView = findViewById(R.id.topOffersView);
    }

    private void initializeSharedPreferences() {
        sharedPreferences = getSharedPreferences("TyradsPrefs", Context.MODE_PRIVATE);
    }

    private void loadSavedValues() {
        String savedApiKey = sharedPreferences.getString("apiKey", "");
        String savedApiSecret = sharedPreferences.getString("apiSecret", "");
        String savedEncryptionKey = sharedPreferences.getString("encryptionKey", "");
        String savedUserId = sharedPreferences.getString("userId", "1");

        apiKeyInput.setText(savedApiKey != null ? savedApiKey : "");
        apiSecretInput.setText(savedApiSecret != null ? savedApiSecret : "");
        encryptionKeyInput.setText(savedEncryptionKey != null ? savedEncryptionKey : "");
        userIdInput.setText(savedUserId != null ? savedUserId : "");
    }

    private void initializeTyrads() {
        // Show initializing indicator
        initializingIndicator.setVisibility(View.VISIBLE);
        topOffersView.setVisibility(View.GONE);

        // Initialize SDK with callback
        Tyrads.getInstance().init(
                this,
                "4f0eaa99e38e49b8b52804116e638a41",
                "cd3c34a52a3b75a3fdd928774615d4e142dd2e6a8ce9da14df4205c7cc812ce81d3656e3dc2c0c58ed05c75c57f87a3431fed62725bb0286f9461521b6c9997a",
                "dKWuxV#Ab9pBXNvg3UFrQPmk8aCn5SDL", // encryption key
                true, // debug mode
                new TyradsCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d("TyradsInit", "SDK initialized successfully");

                        // Login user after successful initialization
                        Tyrads.getInstance().loginUser("14560", new TyradsLoginCallback() {
                            @Override
                            public void onSuccess(boolean isNewUser) {
                                Log.d("TyradsLogin", "User logged in. New user: " + isNewUser);

                                isTyradsInitialized = true;
                                initializingIndicator.setVisibility(View.GONE);
                                topOffersView.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onFailure(String error) {
                                Log.e("TyradsLogin", "Login failed: " + error);
                                initializingIndicator.setVisibility(View.GONE);
                                Toast.makeText(MainActivity.this, "Login failed: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e("TyradsInit", "Initialization failed: " + error);
                        initializingIndicator.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Error initializing Tyrads: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void handleButtonClick() {
        String apiKey = apiKeyInput.getText().toString().trim();
        String apiSecret = apiSecretInput.getText().toString().trim();
        String encryptionKey = encryptionKeyInput.getText().toString().trim();
        String userId = userIdInput.getText().toString().trim();

        // Optional validation (commented out as in Kotlin version)
        // if (apiKey.isEmpty() || apiSecret.isEmpty() || userId.isEmpty()) {
        //     Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        //     return;
        // }

        isLoadingOffers = true;
        updateLoadingState();

        // Use default values if fields are blank (matching Kotlin version)
        String finalApiKey = apiKey.isEmpty() ? "4f0eaa99e38e49b8b52804116e638a41" : apiKey;
        String finalApiSecret = apiSecret.isEmpty() ? "cd3c34a52a3b75a3fdd928774615d4e142dd2e6a8ce9da14df4205c7cc812ce81d3656e3dc2c0c58ed05c75c57f87a3431fed62725bb0286f9461521b6c9997a" : apiSecret;
        String finalEncryptionKey = encryptionKey.isEmpty() ? "dKWuxV#Ab9pBXNvg3UFrQPmk8aCn5SDL" : encryptionKey;
        String finalUserId = userId.isEmpty() ? "6" : userId;

        // Save to SharedPreferences
        saveToSharedPreferences(apiKey, apiSecret, encryptionKey, userId);

        // Initialize SDK with callback
        Tyrads.getInstance().init(
                this,
                finalApiKey,
                finalApiSecret,
                finalEncryptionKey,
                false, // debug mode off for user-triggered actions
                new TyradsCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d("TyradsInit", "SDK re-initialized successfully");

                        // Login user after successful initialization
                        Tyrads.getInstance().loginUser(finalUserId, new TyradsLoginCallback() {
                            @Override
                            public void onSuccess(boolean isNewUser) {
                                Log.d("TyradsLogin", "User logged in. New user: " + isNewUser);

                                // Show offers after successful login
                                Tyrads.getInstance().showOffers(null, null, new TyradsCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d("TyradsOffers", "Offers shown successfully");
                                        isLoadingOffers = false;
                                        updateLoadingState();
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        Log.e("TyradsOffers", "Failed to show offers: " + error);
                                        isLoadingOffers = false;
                                        updateLoadingState();
                                        Toast.makeText(MainActivity.this, "Error showing offers: " + error, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onFailure(String error) {
                                Log.e("TyradsLogin", "Login failed: " + error);
                                isLoadingOffers = false;
                                updateLoadingState();
                                Toast.makeText(MainActivity.this, "Login failed: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e("TyradsInit", "Initialization failed: " + error);
                        isLoadingOffers = false;
                        updateLoadingState();
                        Toast.makeText(MainActivity.this, "Error initializing: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void saveToSharedPreferences(String apiKey, String apiSecret, String encryptionKey, String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("apiKey", apiKey);
        editor.putString("apiSecret", apiSecret);
        editor.putString("encryptionKey", encryptionKey);
        editor.putString("userId", userId);
        editor.apply();
    }

    private void updateLoadingState() {
        if (isLoadingOffers) {
            showOffersButton.setText("Loading...");
            showOffersButton.setEnabled(false);
            loadingIndicator.setVisibility(View.VISIBLE);
        } else {
            showOffersButton.setText("Show Offers");
            showOffersButton.setEnabled(true);
            loadingIndicator.setVisibility(View.GONE);
        }
    }
}