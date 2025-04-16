package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
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
    private Wrapper stack = new Wrapper(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_game);
        Button hit_button = findViewById(R.id.hit_button);
        Button stand_button = findViewById(R.id.stand_button);
        Button double_button = findViewById(R.id.double_button);
        hit_button.setVisibility(View.INVISIBLE);
        double_button.setVisibility(View.INVISIBLE);
        stand_button.setVisibility(View.INVISIBLE);
        TextView title = findViewById(R.id.title);
        // title.setVisibility(View.VISIBLE);
        // Установите задержку на 3 секунды (3000 миллисекунд)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                title.setVisibility(View.GONE); // Скрыть текст
            }
        }, 5000);
        // Инициализация UI элементов
        TextView c_stack = findViewById(R.id.current_stack);
        c_stack.setText(String.valueOf(String.valueOf(readNumberFromFile())));

        Button start_button = findViewById(R.id.start_button);
        EditText editText = findViewById(R.id.bet);

        // Обработка нажатия на кнопку
        start_button.setOnClickListener(view -> {
            try {
                // Получаем значение из EditText
                String betText = editText.getText().toString();
                if (betText.isEmpty()) {
                    Toast.makeText(this, "Введите ставку", Toast.LENGTH_SHORT).show();
                    return;
                }

                int bet = Integer.parseInt(betText); // Преобразуем в число
                if(bet > readNumberFromFile()){
                    Integer.parseInt("abc");
                }
                int a = readNumberFromFile();
                a -= bet;
                saveNumberToFile(a);
                start_game(bet); // Запуск игры
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Некорректная ставка", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void start_game(int bet) {
        // Скрываем кнопку и поле ввода
        Button start_button = findViewById(R.id.start_button);
        start_button.setVisibility(View.GONE);

        TextView title = findViewById(R.id.title);
        title.setVisibility(View.INVISIBLE);

        EditText editText = findViewById(R.id.bet);
        editText.setVisibility(View.GONE);

        TextView c_stack = findViewById(R.id.current_stack);
        c_stack.setVisibility(View.GONE);

        // Инициализируем игру и начинаем раздачу
        BlackjackGame game = new BlackjackGame();
        game.startGame(bet);

        // Первоначальное обновление UI
        update_UI(game);

        // Инициализация кнопок действий
        Button hit_button = findViewById(R.id.hit_button);
        Button stand_button = findViewById(R.id.stand_button);
        Button double_button = findViewById(R.id.double_button);

        // Обработчик для кнопки "Hit"
        hit_button.setOnClickListener(view -> {
            game.hit();
            update_UI(game);
        });

        // Обработчик для кнопки "Stand"
        stand_button.setOnClickListener(view -> {
            game.stand();
            // Если игрок закончил ход (все руки обработаны), дилер играет
            if (game.isPlayerTurnComplete()) {
                game.playDealerHand();
            }
            update_UI(game);
        });

        // Обработчик для кнопки "Double Down"
        double_button.setOnClickListener(view -> {
            game.doubleDown();
            update_UI(game);
        });
    }

    private void update_UI(BlackjackGame game) {
        TextView player_cards = findViewById(R.id.players_cards);
        // Получаем текущую руку игрока
        BlackjackGame.Hand currentHand = game.getCurrentHand();
        // Если рука отсутствует — значит, ход игрока завершён
        if (currentHand == null) {
            res_out(game); // Выводим итоговые результаты или выполняем другое действие
            return;        // Завершаем выполнение метода, чтобы не передавать null в printHandInfo
        }
        // Обновляем отображение руки игрока
        player_cards.setText(game.printHandInfo(currentHand));

        // Обновляем отображение руки дилера (если она не null)
        TextView dealers_cards = findViewById(R.id.dealers_cards);
        if (game.getDealerHand() != null) {
            dealers_cards.setText(game.printHandInfo(game.getDealerHand(), true));
        }

        // Обновляем видимость кнопок
        Button hit_button = findViewById(R.id.hit_button);
        Button stand_button = findViewById(R.id.stand_button);
        Button double_button = findViewById(R.id.double_button);

        stand_button.setVisibility(View.VISIBLE);
        if (game.canDouble()) {
            double_button.setVisibility(View.VISIBLE);
        }
        hit_button.setVisibility(View.VISIBLE);
    }
    private void res_out(BlackjackGame game) {
        // Отображаем итоговую информацию по всем рукам игрока
        List<BlackjackGame.Hand> hands = game.getPlayerHands();
        StringBuilder playerHandsString = new StringBuilder();
        for (int i = 0; i < hands.size(); i++) {
            playerHandsString.append("Рука ").append(i + 1).append(":\n")
                    .append(game.printHandInfo(hands.get(i)))
                    .append("\n\n");
        }
        TextView player_cards = findViewById(R.id.players_cards);
        player_cards.setText(playerHandsString.toString());

        // Обновляем отображение карты дилера
        TextView dealers_cards = findViewById(R.id.dealers_cards);
        if (game.getDealerHand() != null) {
            dealers_cards.setText(game.printHandInfo(game.getDealerHand()));
        }

        // Формируем строку с результатами игры
        List<BlackjackGame.GameResult> results = game.getResults();
        StringBuilder resultsString = new StringBuilder();
        for (BlackjackGame.GameResult result : results) {
            int a = readNumberFromFile();
            Log.d("MyTag", String.valueOf(a));
            switch(result) {
                case WIN:
                    resultsString.append("Вы выиграли!\n");
                    a += 2 * game.getCurrentBet();
                    saveNumberToFile(a);
                    Log.d("MyTag", String.valueOf(a));
                    break;
                case LOSE:
                    resultsString.append("Вы проиграли.\n");
                    break;
                case PUSH:
                    resultsString.append("Ничья.\n");
                    a += game.getCurrentBet();
                    saveNumberToFile(a);
                    Log.d("MyTag", String.valueOf(a));
                    break;
                case BLACKJACK:
                    resultsString.append("Блэкджек!\n");
                    a += (5 * game.getCurrentBet())/2;
                    saveNumberToFile(a);
                    break;
            }
        }
        TextView resultsView = findViewById(R.id.results_view);
        resultsView.setText(resultsString.toString());
        resultsView.setVisibility(View.VISIBLE);

        // Кнопка для возврата к начальному состоянию Activity
        Button button = findViewById(R.id.return_button);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GameActivity.this, GameActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }


    private void saveNumberToFile(int number) {
        try (FileOutputStream fos = openFileOutput("number.txt", MODE_PRIVATE)) {
            fos.write(String.valueOf(number).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show());
        }
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

    private void displayCards(BlackjackGame game) {
        // Получаем текущую руку игрока
        BlackjackGame.Hand currentHand = game.getCurrentHand();

        TextView player_cards = findViewById(R.id.players_cards);
        if (currentHand != null) {
            player_cards.setText(game.printHandInfo(currentHand));
        } else {
            player_cards.setText("Ход игрока завершён");
        }

        // Отображаем карты дилера (если уже открыты)
        TextView dealers_cards = findViewById(R.id.dealers_cards);
        if (game.getDealerHand() != null) {
            // true — скрыть первую карту (если ещё идёт игра), false — показать все
            boolean hideFirstCard = !game.isPlayerTurnComplete();
            dealers_cards.setText(game.printHandInfo(game.getDealerHand(), hideFirstCard));
        }
    }

}