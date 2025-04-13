package com.example.myapplication.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlackjackGame {
    private Deck deck;
    private List<Hand> playerHands;
    private Hand dealerHand;
    private int currentBet;
    private int currentHandIndex;
    private boolean insuranceAvailable;


    public int getCurrentBet(){
        return (currentBet);
    }

    public BlackjackGame() {
        initializeGame();
    }
    public List<Hand> getPlayerHands() {
        return playerHands;
    }


    public void startGame(int bet) {
        initializeGame();
        this.currentBet = bet;
        initialDeal();
    }

    private void initializeGame() {
        deck = new Deck();
        playerHands = new ArrayList<>();
        playerHands.add(new Hand());
        dealerHand = new Hand();
        currentHandIndex = 0;
        insuranceAvailable = true;
    }

    private void initialDeal() {
        deck.shuffle();

        // Раздача первых двух карт
        playerHands.get(0).addCard(deck.draw());
        dealerHand.addCard(deck.draw());
        playerHands.get(0).addCard(deck.draw());
        dealerHand.addCard(deck.draw());
    }

    /**
     * Возвращает текущую руку игрока, если она существует.
     * Если все руки обработаны, возвращает null.
     */
    public Hand getCurrentHand() {
        if (currentHandIndex >= playerHands.size()) {
            return null;
        }
        return playerHands.get(currentHandIndex);
    }

    public void hit() {
        if (isPlayerTurnComplete()) return;
        Hand currentHand = getCurrentHand();
        if (currentHand == null) return;
        currentHand.addCard(deck.draw());
        if (currentHand.isBusted()) {
            stand();
        }
    }

    public void stand() {
        currentHandIndex++;
        if (isPlayerTurnComplete()) {
            playDealerHand();
        }
    }

    public void doubleDown() {
        if (isPlayerTurnComplete()) return;
        Hand currentHand = getCurrentHand();
        if (currentHand == null) return;
        currentBet *= 2;
        currentHand.addCard(deck.draw());
        stand();
    }

    public void split() {
        if (isPlayerTurnComplete()) return;
        Hand currentHand = getCurrentHand();
        if (currentHand == null || !currentHand.canSplit()) return;

        List<Card> cards = currentHand.getCards();

        Hand newHand1 = new Hand();
        newHand1.addCard(cards.get(0));
        newHand1.addCard(deck.draw());

        Hand newHand2 = new Hand();
        newHand2.addCard(cards.get(1));
        newHand2.addCard(deck.draw());

        // Заменяем текущую руку на две новые
        playerHands.remove(currentHandIndex);
        playerHands.add(currentHandIndex, newHand1);
        playerHands.add(currentHandIndex + 1, newHand2);
    }

    public void playDealerHand() {
        // Пока общая сумма меньше 17, либо равна 17, но рука мягкая (soft 17)
        while (dealerHand.getBestValue() < 17 ||
                (dealerHand.getBestValue() == 17 && dealerHand.isSoft17())) {
            dealerHand.addCard(deck.draw());
        }
    }


    public List<GameResult> getResults() {
        List<GameResult> results = new ArrayList<>();
        int dealerValue = dealerHand.getBestValue();
        boolean dealerBust = dealerHand.isBusted();

        for (int i = 0; i < playerHands.size(); i++) {
            if(playerHands.get(i) == null) break;
            Hand hand = playerHands.get(i);
            if (hand.isBusted()) {
                results.add(GameResult.LOSE);
            } else if (hand.isBlackjack()) {
                if (dealerHand.isBlackjack()) {
                    results.add(GameResult.PUSH);
                } else {
                    results.add(GameResult.BLACKJACK);
                }
            } else if (dealerBust) {
                results.add(GameResult.WIN);
            } else {
                int playerValue = hand.getBestValue();
                if (playerValue > dealerValue) {
                    results.add(GameResult.WIN);
                } else if (playerValue == dealerValue) {
                    results.add(GameResult.PUSH);
                } else {
                    results.add(GameResult.LOSE);
                }
            }
        }
        return results;
    }

    public boolean canSplit() {
        Hand currentHand = getCurrentHand();
        return currentHand != null && currentHand.canSplit() && playerHands.size() < 4;
    }

    public boolean canDouble() {
        Hand currentHand = getCurrentHand();
        return currentHand != null && currentHand.getCards().size() == 2;
    }

    public boolean isPlayerTurnComplete() {
        return currentHandIndex >= playerHands.size();
    }

    public Hand getDealerHand() {
        return dealerHand;
    }

    public enum GameResult {
        WIN, LOSE, PUSH, BLACKJACK
    }

    // --- Вложенные классы ---

    private static class Deck {
        private List<Card> cards;

        public Deck() {
            initialize();
        }

        private void initialize() {
            cards = new ArrayList<>();
            for (Suit suit : Suit.values()) {
                for (Rank rank : Rank.values()) {
                    cards.add(new Card(rank, suit));
                }
            }
            shuffle();
        }

        public void shuffle() {
            Collections.shuffle(cards);
        }

        public Card draw() {
            if (cards.isEmpty()) {
                initialize();
            }
            return cards.remove(0);
        }
    }

    public static class Hand {
        private List<Card> cards;

        public Hand() {
            cards = new ArrayList<>();
        }
        public boolean isSoft17() {
            int total = 0;
            int aces = 0;
            // Считаем все карты, туз считается как 11
            for (Card card : cards) {
                if (card.rank == Rank.ACE) {
                    total += 11;
                    aces++;
                } else {
                    total += card.rank.getValue();
                }
            }
            // Если сумма больше 21, уменьшаем значение за счет тузов
            while (total > 21 && aces > 0) {
                total -= 10;
                aces--;
            }
            // Если итоговая сумма равна 17 и остался хотя бы один туз, считающийся как 11, значит рука мягкая
            return total == 17 && aces > 0;
        }


        public void addCard(Card card) {
            cards.add(card);
        }

        public int getBestValue() {
            int value = 0;
            int aces = 0;

            for (Card card : cards) {
                int cardValue = card.rank.getValue();
                value += cardValue;
                if (card.rank == Rank.ACE) {
                    aces++;
                }
            }

            while (value > 21 && aces > 0) {
                value -= 10;
                aces--;
            }
            return value;
        }

        public boolean isBusted() {
            return getBestValue() > 21;
        }

        public boolean isBlackjack() {
            return cards.size() == 2 && getBestValue() == 21;
        }

        public boolean canSplit() {
            return cards.size() == 2 &&
                    cards.get(0).rank == cards.get(1).rank &&
                    cards.get(0).rank != Rank.ACE;
        }

        public List<Card> getCards() {
            return new ArrayList<>(cards);
        }
    }

    private enum Suit {
        HEARTS, DIAMONDS, CLUBS, SPADES
    }

    private enum Rank {
        TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7),
        EIGHT(8), NINE(9), TEN(10), JACK(10), QUEEN(10),
        KING(10), ACE(11);

        private final int value;

        Rank(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static class Card {
        private final Rank rank;
        private final Suit suit;

        public Card(Rank rank, Suit suit) {
            this.rank = rank;
            this.suit = suit;
        }

        public String getSuit() {
            return String.valueOf(suit);
        }

        public String getRank() {
            return String.valueOf(rank);
        }
    }

    static String formatCards(List<Card> cards) {
        String fCards = "";
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                fCards += "\n";
            }
            fCards += cards.get(i).getRank() + " of " + cards.get(i).getSuit();
        }
        return fCards;
    }

    static String formatCards(List<Card> cards, boolean d) {
        // При показе дилера показываем только первую карту
        return cards.get(0).getRank() + " of " + cards.get(0).getSuit();
    }

    public static String printHandInfo(Hand hand, boolean hidden) {
        return formatCards(hand.getCards(), hidden);
    }

    public static String printHandInfo(Hand hand) {
        return formatCards(hand.getCards());
    }
}
