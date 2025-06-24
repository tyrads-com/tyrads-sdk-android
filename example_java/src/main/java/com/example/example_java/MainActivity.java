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
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.Dispatchers;

public class MainActivity extends AppCompatActivity {

    private EditText apiKeyInput;
    private EditText apiSecretInput;
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
        String savedUserId = sharedPreferences.getString("userId", "1");

        apiKeyInput.setText(savedApiKey != null ? savedApiKey : "");
        apiSecretInput.setText(savedApiSecret != null ? savedApiSecret : "");
        userIdInput.setText(savedUserId != null ? savedUserId : "");
    }

    private void initializeTyrads() {
        new Thread(() -> {
            try {
                Tyrads.getInstance().init(
                        this,
                        "4f0eaa99e38e49b8b52804116e638a41",
                        "cd3c34a52a3b75a3fdd928774615d4e142dd2e6a8ce9da14df4205c7cc812ce81d3656e3dc2c0c58ed05c75c57f87a3431fed62725bb0286f9461521b6c9997a",
                        true
                );

             Tyrads.getInstance().loginUser("6");

                Thread.sleep(1500);

                runOnUiThread(() -> {
                    isTyradsInitialized = true;
                    initializingIndicator.setVisibility(View.GONE);
                    topOffersView.setVisibility(View.VISIBLE);
                });

            } catch (Exception e) {
                Log.e("TyradsInit", "Error initializing Tyrads", e);
                runOnUiThread(() -> {
                    initializingIndicator.setVisibility(View.GONE);
                    Toast.makeText(this, "Error initializing Tyrads", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void handleButtonClick() {
        String apiKey = apiKeyInput.getText().toString().trim();
        String apiSecret = apiSecretInput.getText().toString().trim();
        String userId = userIdInput.getText().toString().trim();

        // Commented out validation as in original code
        // if (apiKey.isEmpty() || apiSecret.isEmpty() || userId.isEmpty()) {
        //     Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        //     return;
        // }

        isLoadingOffers = true;
        updateLoadingState();

        new Thread(() -> {
            try {
                saveToSharedPreferences(apiKey, apiSecret, userId);

                String finalApiKey = apiKey.isEmpty() ? "4f0eaa99e38e49b8b52804116e638a41" : apiKey;
                String finalApiSecret = apiSecret.isEmpty() ? "cd3c34a52a3b75a3fdd928774615d4e142dd2e6a8ce9da14df4205c7cc812ce81d3656e3dc2c0c58ed05c75c57f87a3431fed62725bb0286f9461521b6c9997a" : apiSecret;
                String finalUserId = userId.isEmpty() ? "6" : userId;

                Tyrads.getInstance().init(
                        this,
                        finalApiKey,
                        finalApiSecret,
                        true
                );

                Tyrads.getInstance().loginUser(finalUserId);
                Tyrads.getInstance().showOffers(null,null);

                runOnUiThread(() -> {
                    isLoadingOffers = false;
                    updateLoadingState();
                });

            } catch (Exception e) {
                Log.e("ShowOffers", "Error showing offers", e);
                runOnUiThread(() -> {
                    isLoadingOffers = false;
                    updateLoadingState();
                    Toast.makeText(this, "Error showing offers", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }



    private void saveToSharedPreferences(String apiKey, String apiSecret, String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("apiKey", apiKey);
        editor.putString("apiSecret", apiSecret);
        editor.putString("userId", userId);
        editor.apply();
    }

    private void updateLoadingState() {
        if (isLoadingOffers) {
            showOffersButton.setText("Loading...");
            showOffersButton.setEnabled(false);
            loadingIndicator.setMax(16);
            loadingIndicator.setVisibility(View.VISIBLE);
        } else {
            showOffersButton.setText("Show Offers");
            showOffersButton.setEnabled(true);
            loadingIndicator.setVisibility(View.GONE);
        }
    }
}