package com.example.myapplication.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlackjackBackup {
    private Deck deck;
    private List<Hand> playerHands;
    private Hand dealerHand;
    private int currentBet;
    private int currentHandIndex;
    private boolean insuranceAvailable;
    Wrapper stack;

    private int bet;

    public BlackjackBackup(Wrapper stack) {
        this.stack = stack;
        initializeGame();
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

    public void hit() {
        Hand currentHand = getCurrentHand();
        currentHand.addCard(deck.draw());

        if (currentHand.isBusted()) {
            stand();
        }
    }

    public void stand() {
        currentHandIndex++;
    }

    public void doubleDown() {
        Hand currentHand = getCurrentHand();
        currentBet *= 2;
        currentHand.addCard(deck.draw());
        stand();
    }

    public void split() {
        Hand currentHand = getCurrentHand();
        if (currentHand.canSplit()) {
            List<Card> cards = currentHand.getCards();

            Hand newHand1 = new Hand();
            newHand1.addCard(cards.get(0));
            newHand1.addCard(deck.draw());

            Hand newHand2 = new Hand();
            newHand2.addCard(cards.get(1));
            newHand2.addCard(deck.draw());

            playerHands.remove(currentHandIndex);
            playerHands.add(currentHandIndex, newHand1);
            playerHands.add(currentHandIndex + 1, newHand2);
        }
    }

    public void playDealerHand() {
        while (dealerHand.getBestValue() < 17) {
            dealerHand.addCard(deck.draw());
        }
    }

    public List<GameResult> getResults() {
        List<GameResult> results = new ArrayList<>();
        int dealerValue = dealerHand.getBestValue();
        boolean dealerBust = dealerHand.isBusted();

        for (int i = 0; i < playerHands.size(); i++) {
            if (playerHands.get(i).isBusted()) {
                results.add(GameResult.LOSE);
                stack.value -= currentBet;
            } else if (playerHands.get(i).isBlackjack()) {
                if(dealerHand.isBlackjack()) results.add(GameResult.PUSH);
                else {
                    results.add(GameResult.BLACKJACK);
                    stack.value += currentBet*3/2;
                }
            } else if (dealerBust) {
                results.add(GameResult.WIN);
                stack.value += currentBet;
            } else {
                int playerValue = playerHands.get(i).getBestValue();
                if (playerValue > dealerValue) {
                    results.add(GameResult.WIN);
                    stack.value += currentBet;
                } else if (playerValue == dealerValue) {
                    results.add(GameResult.PUSH);
                } else {
                    results.add(GameResult.LOSE);
                    stack.value -= currentBet;
                }
            }
        }
        return results;
    }

    public Hand getCurrentHand() {
        return playerHands.get(currentHandIndex);
    }

    public boolean canSplit() {
        return getCurrentHand().canSplit() && playerHands.size() < 4;
    }

    public boolean canDouble() {
        return getCurrentHand().getCards().size() == 2;
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

    static class Hand {
        private List<Card> cards;

        public Hand() {
            cards = new ArrayList<>();
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
                    cards.get(0).rank == cards.get(1).rank && // Сравниваем ранги
                    cards.get(0).rank != Rank.ACE; // Запрещаем сплит тузов
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
        public String getSuit(){
            return String.valueOf((this.suit));
        }
        public String getRank(){
            return String.valueOf((this.rank));
        }
    }
    private static String formatCards(List<BlackjackGame.Card> cards) {
        String fCards = "";
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                fCards += "\n";
            }
            fCards += cards.get(i).getRank() +" of " + cards.get(i).getSuit();
        }
        return fCards;
    }
    private static String formatCards(List<BlackjackGame.Card> cards, boolean d) {
        return cards.get(0).getRank() +" of " + cards.get(0).getSuit();
    }
    public static String printHandInfo(BlackjackGame.Hand hand, boolean h) {
        return(BlackjackGame.formatCards(hand.getCards(), true));
    }
    public static String printHandInfo(BlackjackGame.Hand hand) {
        return (BlackjackGame.formatCards(hand.getCards()));
    }
}