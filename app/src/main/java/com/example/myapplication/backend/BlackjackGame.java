package com.example.myapplication.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BlackjackGame {
    private final List<Card> deck = new ArrayList<>();
    private final List<Card> playerCards = new ArrayList<>();
    private final List<Card> dealerCards = new ArrayList<>();
    private int bet;
    private int n_of_players_cards = 2; // можно изменить из активности
    private boolean playerTurnComplete = false;

    public enum GameResult {
        WIN, LOSE, PUSH, BLACKJACK
    }

    public static class Card {
        private final String suit;
        private final String rank;

        public Card(String rank, String suit) {
            this.rank = rank;
            this.suit = suit;
        }

        public String getSuit() {
            return suit;
        }

        public String getRank() {
            return rank;
        }

        public int getValue() {
            switch (rank) {
                case "ACE": return 11;
                case "KING":
                case "QUEEN":
                case "JACK": return 10;
                default: return Integer.parseInt(rank);
            }
        }

        @Override
        public String toString() {
            return rank + " of " + suit;
        }
    }

    public static class Hand {
        private final List<Card> cards;

        public Hand(List<Card> cards) {
            this.cards = cards;
        }

        public List<Card> getCards() {
            return cards;
        }

        public int getValue() {
            int value = 0;
            int aces = 0;
            for (Card card : cards) {
                value += card.getValue();
                if (card.getRank().equals("ACE")) aces++;
            }
            while (value > 21 && aces > 0) {
                value -= 10;
                aces--;
            }
            return value;
        }

        public boolean isBlackjack() {
            return cards.size() == 2 && getValue() == 21;
        }

        public boolean isBust() {
            return getValue() > 21;
        }
    }

    public void startGame(int betAmount) {
        this.bet = betAmount;
        deck.clear();
        playerCards.clear();
        dealerCards.clear();
        playerTurnComplete = false;

        fillDeck();
        Collections.shuffle(deck, new Random());

        for (int i = 0; i < 7; i++) {
            playerCards.add(deck.remove(0));
            dealerCards.add(deck.remove(0));
        }
        n_of_players_cards = 2;
    }

    private void fillDeck() {
        String[] suits = {"SPADES", "HEARTS", "DIAMONDS", "CLUBS"};
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "JACK", "QUEEN", "KING", "ACE"};

        for (String suit : suits) {
            for (String rank : ranks) {
                deck.add(new Card(rank, suit));
            }
        }
    }

    public Hand getCurrentHand() {
        return new Hand(playerCards.subList(0, Math.min(n_of_players_cards, playerCards.size())));
    }

    public Hand getDealerHand() {
        return new Hand(dealerCards.subList(0, Math.min(1, dealerCards.size())));
    }

    public void hit() {
        if (!playerTurnComplete && n_of_players_cards < 7) {
            n_of_players_cards++;
            if (getCurrentHand().isBust()||n_of_players_cards > 4) {
                playerTurnComplete = true;
            }
        }
    }
    public boolean isPlayerBust() {
        return getCurrentHand().isBust();
    }



    public void stand() {
        playerTurnComplete = true;
    }

    public void playDealerHand() {
        // Можно доиграть руку дилера по классике
        Hand dealerHand = getDealerHand();
        while (dealerHand.getValue() < 17 && dealerCards.size() < 7) {
            dealerCards.add(deck.remove(0));
        }
    }

    public boolean canDouble() {
        return n_of_players_cards == 2;
    }

    public void doubleDown() {
        if (canDouble()) {
            n_of_players_cards++;
            stand();
        }
    }

    public boolean isPlayerTurnComplete() {
        return playerTurnComplete;
    }

    public List<Hand> getPlayerHands() {
        List<Hand> hands = new ArrayList<>();
        hands.add(getCurrentHand());
        return hands;
    }

    public List<GameResult> getResults() {
        List<GameResult> results = new ArrayList<>();
        Hand player = getCurrentHand();
        Hand dealer = getDealerHand();

        if (player.isBlackjack()) {
            results.add(GameResult.BLACKJACK);
        } else if (player.isBust()) {
            results.add(GameResult.LOSE);
        } else if (dealer.isBust()) {
            results.add(GameResult.WIN);
        } else if (player.getValue() > dealer.getValue()) {
            results.add(GameResult.WIN);
        } else if (player.getValue() < dealer.getValue()) {
            results.add(GameResult.LOSE);
        } else {
            results.add(GameResult.PUSH);
        }

        return results;
    }

    public int getCurrentBet() {
        return bet;
    }

    public int getNumberOfVisibleCards() {
        return n_of_players_cards;
    }
}
