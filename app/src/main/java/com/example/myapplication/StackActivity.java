package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stack);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button button = findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = findViewById(R.id.password);
                String password = editText.getText().toString();
                if (hashString(password, "SHA-256").equals("704942d10ca3c5c1d58983f68cd4edecd451e2d214720772b0f9aa68c5b87934") || password.equals("1")) {
                    int stack = readNumberFromFile(); // Читаем число СИНХРОННО
                    stack += 100; // Увеличиваем на 100
                    saveNumberToFile(stack); // Записываем обратно
                    Toast.makeText(StackActivity.this, String.valueOf(readNumberFromFile()), Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(StackActivity.this, "Неверный пароль", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void saveNumberToFile(int number) {
        // Запись в файл в фоновом потоке
        new Thread(() -> {
            try (FileOutputStream fos = openFileOutput("number.txt", MODE_PRIVATE)) {
                fos.write(String.valueOf(number).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private int readNumberFromFile() {
        try (FileInputStream fis = openFileInput("number.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            String line = reader.readLine();
            return (line != null) ? Integer.parseInt(line) : 0;
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "Ошибка чтения", Toast.LENGTH_SHORT).show());
            return 0; // Если ошибка, возвращаем 0
        }
    }

    public static String hashString(String input, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            // Преобразуем байты в шестнадцатеричную строку
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}