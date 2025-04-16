package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.backend.BlackjackGame;
import com.example.myapplication.backend.Wrapper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class GameActivity extends AppCompatActivity {
    // Если необходима обертка для другой логики, можно оставить её (пока не используется)
    private Wrapper stack = new Wrapper(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Убираем заголовок окна
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_game);

        Button hitButton = findViewById(R.id.hit_button);
        Button standButton = findViewById(R.id.stand_button);
        Button doubleButton = findViewById(R.id.double_button);
        hitButton.setVisibility(View.GONE);
        standButton.setVisibility(View.GONE);
        doubleButton.setVisibility(View.GONE);

        final TextView title = findViewById(R.id.title);
        new Handler().postDelayed(() -> title.setVisibility(View.GONE), 5000);

        // Используем массив для хранения баланса, чтобы переменная была effectively final
        final int[] currentBalance = { readNumberFromFile() };
        TextView cStack = findViewById(R.id.current_stack);
        cStack.setText(String.valueOf(currentBalance[0]));

        Button startButton = findViewById(R.id.start_button);
        EditText betInput = findViewById(R.id.bet);

        startButton.setOnClickListener(view -> {
            String betText = betInput.getText().toString().trim();
            if (betText.isEmpty()) {
                Toast.makeText(this, "Введите ставку", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int bet = Integer.parseInt(betText);
                if (bet > currentBalance[0] || bet <= 0) {
                    Toast.makeText(this, "Ставка превышает баланс или равна нулю", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Обновляем баланс в массиве и сохраняем его в файл
                currentBalance[0] -= bet;
                saveNumberToFile(currentBalance[0]);
                startGame(bet, currentBalance);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Некорректная ставка", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startGame(int bet, final int[] currentBalance) {
        Button startButton = findViewById(R.id.start_button);
        startButton.setVisibility(View.GONE);

        TextView title = findViewById(R.id.title);
        title.setVisibility(View.GONE);

        EditText betInput = findViewById(R.id.bet);
        betInput.setVisibility(View.GONE);

        TextView cStack = findViewById(R.id.current_stack);

        // Инициализируем игру и начинаем раздачу карт
        BlackjackGame game = new BlackjackGame();
        game.startGame(bet);

        updateUI(game);

        Button hitButton = findViewById(R.id.hit_button);
        Button standButton = findViewById(R.id.stand_button);
        Button doubleButton = findViewById(R.id.double_button);

        hitButton.setOnClickListener(view -> {
            game.hit();
            updateUI(game);
        });

        standButton.setOnClickListener(view -> {
            game.stand();
            if (game.isPlayerTurnComplete()) {
                game.playDealerHand();
            }
            updateUI(game);
            showResults(game);
        });

        doubleButton.setOnClickListener(view -> {
            game.doubleDown();
            updateUI(game);
        });
    }

//    private void updateUI(BlackjackGame game) {
//        LinearLayout playerCardsLayout = findViewById(R.id.player_cards);
//        BlackjackGame.Hand currentHand = game.getCurrentHand();
//        if (currentHand == null) {
//            showResults(game);
//            return;
//        }
//        playerCardsLayout.removeAllViews();
//
//// Получаем список карт из текущей руки
//        List<BlackjackGame.Card> cards = currentHand.getCards();
//
//// Для каждой карты создаём ImageView и устанавливаем картинку
//        for (BlackjackGame.Card card : cards) {
//            ImageView cardImageView = new ImageView(this);
//
//            // Формируем имя ресурса на основе ранга и масти карты
//            // Предполагается, что изображения называются как "ace_of_spades", "2_of_hearts" и т.д.
//            String resourceName = card.getRank().toLowerCase() + "_of_" + card.getSuit().toLowerCase();
//            int resId = getResources().getIdentifier(resourceName, "drawable", getPackageName());
//
//            // Если ресурс не найден (resId==0), можно выставить изображение рубашки карты
//            if (resId == 0) {
//                resId = getResources().getIdentifier("back_of_card", "drawable", getPackageName());
//            }
//
//            cardImageView.setImageResource(resId);
//
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(60), dpToPx(90));
//            params.setMargins(dpToPx(8), 0, dpToPx(8), 0);
//            playerCardsLayout.addView(cardImageView, params);
//        }
//
//
//        TextView dealerCards = findViewById(R.id.dealers_cards);
//        if (game.getDealerHand() != null) {
//            // Если игра не окончена, скрываем первую карту дилера
//            dealerCards.setText(game.printHandInfo(game.getDealerHand(), true));
//        }
//
//        Button hitButton = findViewById(R.id.hit_button);
//        Button standButton = findViewById(R.id.stand_button);
//        Button doubleButton = findViewById(R.id.double_button);
//
//        hitButton.setVisibility(View.VISIBLE);
//        standButton.setVisibility(View.VISIBLE);
//        doubleButton.setVisibility(game.canDouble() ? View.VISIBLE : View.INVISIBLE);
//    }

    private void showResults(BlackjackGame game) {
        // Получаем контейнер для отображения рук игрока
        LinearLayout playerCardsLayout = findViewById(R.id.player_cards);


        if (playerCardsLayout == null) {
            Log.e("GameActivity", "player_cards layout не найден! Проверь ID в XML");
        }



        // Проходим по всем рукам игрока
        List<BlackjackGame.Hand> hands = game.getPlayerHands();
        for (int i = 0; i < hands.size(); i++) {
            // Для каждой руки отображаем заголовок с номером руки
            String header = "Рука " + (i + 1) + ":";

        }

        LinearLayout dealerCardsLayout = findViewById(R.id.dealer_cards);

        if (dealerCardsLayout == null) {
            Log.e("GameActivity", "dealer_cards layout не найден! Проверь ID в XML");
        }

        if (game.getDealerHand() != null) {
            displayHand(dealerCardsLayout, game.getDealerHand(), "");
        }

        // Далее – обработка результатов игры и обновление баланса (код без изменений)
        List<BlackjackGame.GameResult> results = game.getResults();
        StringBuilder resultsStr = new StringBuilder();
        int currentBalance = readNumberFromFile();
        for (BlackjackGame.GameResult result : results) {
            switch (result) {
                case WIN:
                    resultsStr.append("Вы выиграли!\n");
                    currentBalance += 2 * game.getCurrentBet();
                    break;
                case LOSE:
                    resultsStr.append("Вы проиграли.\n");
                    break;
                case PUSH:
                    resultsStr.append("Ничья.\n");
                    currentBalance += game.getCurrentBet();
                    break;
                case BLACKJACK:
                    resultsStr.append("Блэкджек!\n");
                    currentBalance += (5 * game.getCurrentBet()) / 2;
                    break;
            }
        }
        saveNumberToFile(currentBalance);

        TextView resultsView = findViewById(R.id.results_view);
        resultsView.setText(resultsStr.toString());
        resultsView.setVisibility(View.VISIBLE);

        Button returnButton = findViewById(R.id.return_button);
        returnButton.setVisibility(View.VISIBLE);
        returnButton.setOnClickListener(view -> {
            Intent intent = new Intent(GameActivity.this, GameActivity.class);
            startActivity(intent);
            finish();
        });
    }


    private void saveNumberToFile(int number) {
        try (FileOutputStream fos = openFileOutput("number.txt", MODE_PRIVATE)) {
            fos.write(String.valueOf(number).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() ->
                    Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private int readNumberFromFile() {
        try (FileInputStream fis = openFileInput("number.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            String line = reader.readLine();
            return (line != null) ? Integer.parseInt(line) : 0;
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            runOnUiThread(() ->
                    Toast.makeText(this, "Ошибка чтения", Toast.LENGTH_SHORT).show()
            );
            return 0;
        }
    }
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void displayHand(LinearLayout container, BlackjackGame.Hand hand, String header) {
        if (container == null) {
            Log.e("GameActivity", "container null — проверьте ID в XML");
            return;
        }

        container.removeAllViews();

        if (!header.isEmpty()) {
            TextView handHeader = new TextView(this);
            handHeader.setText(header);
            handHeader.setTextColor(getResources().getColor(R.color.white));
            handHeader.setTextSize(18);
            handHeader.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(4));
            container.addView(handHeader);
        }

        LinearLayout handLayout = new LinearLayout(this);
        handLayout.setOrientation(LinearLayout.HORIZONTAL);
        container.addView(handLayout);

        for (BlackjackGame.Card card : hand.getCards()) {
            ImageView iv = new ImageView(this);

            // убрали префикс "a"
            String resourceName = card.getRank().toLowerCase()
                    + "_of_"
                    + card.getSuit().toLowerCase();
            int resId = getResources().getIdentifier(
                    resourceName, "drawable", getPackageName());
            if (resId == 0) {
                // fallback на рубашку
                resId = getResources().getIdentifier(
                        "back_of_card", "drawable", getPackageName());
            }

            Log.d("GameActivity", "loading drawable "
                    + resourceName + " -> resId=" + resId);

            iv.setImageResource(resId);
            // для отладки можно раскомментировать:
            // iv.setBackgroundColor(Color.RED);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(dpToPx(60), dpToPx(90));
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            iv.setLayoutParams(params);

            handLayout.addView(iv);
        }
    }


    private void updateUI(BlackjackGame game) {
        LinearLayout playerCardsLayout = findViewById(R.id.player_cards);
        playerCardsLayout.removeAllViews();

        BlackjackGame.Hand currentHand = game.getCurrentHand();
        if (currentHand == null) {
            showResults(game);
            return;
        }
        displayHand(playerCardsLayout, currentHand, "");

        LinearLayout dealerCardsLayout = findViewById(R.id.dealer_cards);
        dealerCardsLayout.removeAllViews();
        if (game.getDealerHand() != null) {
            displayHand(dealerCardsLayout, game.getDealerHand(), "");
        }

        findViewById(R.id.hit_button).setVisibility(View.VISIBLE);
        findViewById(R.id.stand_button).setVisibility(View.VISIBLE);
        findViewById(R.id.double_button)
                .setVisibility(game.canDouble() ? View.VISIBLE : View.INVISIBLE);
    }

}
