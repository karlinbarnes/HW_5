import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

// Author: Kris Bosco
// Java class to hold all methods to make the game functional
public class GameMethods {
    //Updated max rounds to 12 to account for the now 12 patterns.
    public static final int MAX_ROUNDS = 12;//update to 12 after all patterns are finished.
    private static int correctGuessesCount = 0;
    private static int roundCount;
    private static char highestRank = '2';
    private static List<String> correctChoices = new ArrayList<>();
    private static String soundEffectAbsPath = "/congrats.wav";

    // Author: Kris Bosco
    // Main game method to check user's selection with current rounds pattern
    public static void checkPattern(List<String> selectedCards, JPanel cardPanel, Map<String, ImageIcon> cardImages) {

        TextFileMethods.logExecution("Current Round: " + roundCount);

        // Input validation to check if user has selected any cards
        // If user selects no cards, then exit method and start over
        if (selectedCards.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No cards have been selected. Click Select Cards to begin.");
            GuiMethods.clearCardPanel(cardPanel);
            return;
        }

        // Write users selected cards to CardsDealt.txt
        TextFileMethods.logSelectedCards(roundCount, selectedCards, "FILE1");

        if (roundComplete(selectedCards)) {

            correctGuessesCount++;

            // Check if user has completed two successful guesses per round
            // Proceed to next round if all requirements are satisfied
            if (correctGuessesCount == 2) {
                roundCount++;
                correctGuessesCount = 0;

                // Increment through rounds while round count is less than 6
                // Prompt user to continue through rounds or exit when a round is complete
                if (roundCount <= MAX_ROUNDS) {
                    if (soldTwoGroups(selectedCards)) {
                        winningMessages(roundCount - 1);
                        TextFileMethods.updateLastRoundWon(roundCount - 1);
                        TextFileMethods.logSelectedCards(roundCount, correctChoices, "FILE4");
                        int option = JOptionPane.showConfirmDialog(null, "Do you want to continue to the next round?", "Continue?", JOptionPane.YES_NO_OPTION);

                        // Exit program if user chooses not to continue to next round or exits out of the pop-up
                        if (option == JOptionPane.NO_OPTION || option == JOptionPane.CLOSED_OPTION) {
                            JOptionPane.showMessageDialog(null, "Thank you for playing! Have a nice day!");
                            System.exit(0);
                        }
                    } else {
                        GuiMethods.clearCardPanel(cardPanel);
                        return;
                    }
                } else {

                    // Else statement for when user has completed the last round
                    // LastWon.txt value updates to 6 and user is prompted if they would like to start over or exit
                    JOptionPane.showMessageDialog(null, "Congratulations!! You have completed all the rounds. The Art Dealer is quite happy with his new collection.");
                    TextFileMethods.logSelectedCards(roundCount, correctChoices, "FILE4");
                    int option = JOptionPane.showConfirmDialog(null, "Do you want to play again?", "Play Again?", JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.NO_OPTION || option == JOptionPane.CLOSED_OPTION) {
                        TextFileMethods.updateLastRoundWon(MAX_ROUNDS);
                        JOptionPane.showMessageDialog(null, "Thank you for playing! Have a nice day!");
                        System.exit(0);
                    } else {
                        TextFileMethods.updateLastRoundWon(0);
                        roundCount = 1;
                    }
                }
            } else {
                // Message for user to have them guess another round
                JOptionPane.showMessageDialog(null, "Well done! You guessed correctly. Try to guess correctly one more time to proceed to the next round.");
                TextFileMethods.logSelectedCards(roundCount, correctChoices, "FILE4");
            }
        } else {
            // Message for user if they have selected cards that do not match the pattern for the current round
            JOptionPane.showMessageDialog(null, "Incorrect cards selected. The Art Dealer isn't happy with the current selection. Maybe what he does like can be used as a hint?");
            TextFileMethods.logSelectedCards(roundCount, correctChoices, "FILE4");
        }

        // Clear the cardPanel before displaying new cards
        GuiMethods.clearCardPanel(cardPanel);

        correctChoices.clear();

        // Display selected cards as user inputs their selection
        for (String card : selectedCards) {
            GuiMethods.displayCardInPanel(card, cardImages, cardPanel, selectedCards, roundCount);
        }
    }

    // Author: Kris Bosco
    // Method to check if the users selected cards match the current pattern and if the user has sold two groups of correct cards to the "Art Dealer"
    // Method will return true if user has satisfied the patterns conditions
    public static boolean correctCards(int roundCount, List<String> selectedCards) {
        boolean isCorrect = false;
        switch (roundCount) {
            case 1:
                isCorrect = allRedCards(selectedCards) && soldTwoGroups(selectedCards);
                break;
            case 2:
                isCorrect = allClubs(selectedCards) && soldTwoGroups(selectedCards);
                break;
            case 3:
                isCorrect = allFaceCards(selectedCards) && soldTwoGroups(selectedCards);
                break;
            case 4:
                isCorrect = allSingleDigits(selectedCards) && soldTwoGroups(selectedCards);
                break;
            case 5:
                isCorrect = allSingleDigitPrimes(selectedCards) && soldTwoGroups(selectedCards);
                break;
            case 6:
                isCorrect = highestRank(selectedCards) && soldTwoGroups(selectedCards);
                break;
            case 7:
                isCorrect = risingRunSameSuit(selectedCards) && soldTwoGroups(selectedCards);
                break;
            case 8:
                isCorrect = skippingBy2AnySuit(selectedCards) && soldTwoGroups(selectedCards);
                break;
            case 9:
                isCorrect = addsToEleven(selectedCards) && soldTwoGroups(selectedCards);
                break;
            case 10:
                isCorrect = acesAndEights(selectedCards) && soldTwoGroups(selectedCards);
                break;
            case 11:
                isCorrect = royalFlush(selectedCards) && soldTwoGroups(selectedCards);
                break;
            case 12:
                isCorrect = twoBlackJackCombos(selectedCards) && soldTwoGroups(selectedCards);
                break;
            default:
                isCorrect = false;
                break;
        }

        return isCorrect;
    }

    // Author: Kris Bosco
    // Method to display the winning messages per round completion
    private static void winningMessages(int round) {
        String message = "";
        //String soundPath = "/Users/karlinbarnes/IdeaProjects/HW 5/src/congrats.wav";
        switch (round) {
            case 1:
                message = "USER HAS WON PATTERN: 1";
                playSound(soundEffectAbsPath);//insert sound effect here
                JOptionPane.showMessageDialog(null, "Congratulations! The Art Dealer is quite pleased with all these red cards!");
                break;
            case 2:
                message = "USER HAS WON PATTERN: 2";
                playSound(soundEffectAbsPath);//insert sound effect here
                JOptionPane.showMessageDialog(null, "Congratulations! The Art Dealer is going clubbin now!");
                break;
            case 3:
                message = "USER HAS WON PATTERN: 3";
                playSound(soundEffectAbsPath);//insert sound effect here
                JOptionPane.showMessageDialog(null, "Congratulations! The Art Dealer has made some new friends!");
                break;
            case 4:
                message = "USER HAS WON PATTERN: 4";
                playSound(soundEffectAbsPath);//insert sound effect here
                JOptionPane.showMessageDialog(null, "Congratulations! The Art Dealer is happy you singled out the good cards for him");
                break;
            case 5:
                message = "USER HAS WON PATTERN: 5";
                playSound(soundEffectAbsPath);//insert sound effect here
                JOptionPane.showMessageDialog(null, "Congratulations! The Art Dealer is at his prime now!");
                break;
            case 6:
                message = "USER HAS WON PATTERN: 6";
                playSound(soundEffectAbsPath);//insert sound effect here
                JOptionPane.showMessageDialog(null, "Congratulations! The Art Dealer only wanted the best you had to offer!");
                break;
            case 7:
                message = "USER HAS WON PATTERN: 7";
                playSound(soundEffectAbsPath);//insert sound effect here
                JOptionPane.showMessageDialog(null, "Congratulations! The Art Dealer is impressed with this rising run!");
                break;
            case 8:
                message = "USER HAS WON PATTERN: 8";
                playSound(soundEffectAbsPath);//insert sound effect here
                JOptionPane.showMessageDialog(null, "Congratulations! The Art Dealer skips in joy 2 feet at a time!");
                break;
            case 9:
                message = "USER HAS WON PATTERN: 9";
                playSound(soundEffectAbsPath);//insert sound effect here
                JOptionPane.showMessageDialog(null, "Congratulations! The Art Dealer accepts the sum of 11s!");
                break;
            case 10:
                message = "USER HAS WON PATTERN: 10";
                playSound(soundEffectAbsPath);//insert sound effect here
                JOptionPane.showMessageDialog(null, "Congratulations! The Art Dealer noticed you, Ace... and all of those eights!");
                break;
            case 11:
                message = "USER HAS WON PATTERN: 11";
                playSound(soundEffectAbsPath);//insert sound effect here
                JOptionPane.showMessageDialog(null, "Congratulations! The Art Dealer pays top dollar for this Royal Flush!");
                break;
            case 12:
                message = "USER HAS WON PATTERN: 12";
                playSound(soundEffectAbsPath);//insert sound effect here
                JOptionPane.showMessageDialog(null, "Congratulations! The Art Dealer pulls out the wallet for those Black Jacks!");
                break;
            default:
                JOptionPane.showMessageDialog(null, "Something went wrong.");
                break;
        }

        JOptionPane.showMessageDialog(null, message);
        TextFileMethods.userHasWonPattern(message);
    }

    // Author: Kris Bosco
    // Method to check if user has completed the round
    public static boolean roundComplete(List<String> selectedCards) {
        return correctCards(roundCount, selectedCards);
    }

    // Author: Kris Bosco
    // Method to return current round count. Used in Card Select button in GUI
    public static int getRoundCount() {
        return roundCount;
    }

    // Author: Kris Bosco
    // Method to determine the starting round of the game and set current round count per value stored in LastWon.txt
    // Method will return current round count used to determine what pattern the user is on when making guesses
    public static int determineStartingRound() {
        int startingRound = 1;

        // Create LastWon.txt if it does not exist
        File lastWonFile = new File("LastWon.txt");
        if (!lastWonFile.exists()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(lastWonFile))) {
                writer.write("0");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(lastWonFile))) {
            String line = reader.readLine();
            if (line != null && !line.isEmpty()) {
                int lastWonRound = Integer.parseInt(line.trim());
                if (lastWonRound == MAX_ROUNDS) {

                    // Inform user that they have won all patterns if LastWon.txt has a value of 6
                    int option = JOptionPane.showConfirmDialog(null, "You have won all the rounds already. Would you like to play again?", "Game Over", JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION) {

                        // If user wants to play again, update LastWon.txt to have a value of 0 and start the game over
                        try (FileWriter writer = new FileWriter(lastWonFile)) {
                            writer.write("0");
                        }
                        startingRound = 1;
                    } else {

                        // If user doesn't want to play again, exit the program
                        JOptionPane.showMessageDialog(null, "Thank you for playing! Have a nice day!");
                        System.exit(0);
                    }
                } else {

                    // If LastWon.txt is not equal to MAX_ROUNDS, set startingRound to (lastWonRound + 1)
                    startingRound = lastWonRound + 1;
                }
            } else {

                // If LastWon.txt is empty, start from the first round
                startingRound = 1;
            }
        } catch (IOException e) {

            // Handle file IO exception
            e.printStackTrace();
            startingRound = 1;
        } catch (NumberFormatException e) {
            // Handle parsing error if the content of LastWon.txt is not a valid integer
            e.printStackTrace();
            startingRound = 1;
        }

        roundCount = startingRound;

        // Set round count to 1 if on a new game
        if (startingRound == 1) {
            roundCount = 1;
        }

        return roundCount;
    }



    // Author: Kris Bosco
    // Method to apply asterisks to the users input to provide a visual indicator of the correct cards of the round
    private static void builder(String card, boolean correct) {

        StringBuilder newCard = new StringBuilder();

        if ((correctChoices.size() < 4) && correct) {
            newCard.append("*").append(card).append("*");
        } else if ((correctChoices.size() == 4) && correct) {
            newCard.append("*").append(card).append("*");
        } else if ((correctChoices.size() < 4) && !correct) {
            newCard.append(card);
        }

        correctChoices.add(newCard.toString());

        TextFileMethods.logExecution("In builder: " + correctChoices);
    }

    // Author: Kris Bosco
    // Method to only allow the card suits hearts or diamonds as correct choices when user is making selections
    // Will return true if only hearts or diamonds are selected in any combination of choices
    private static boolean allRedCards(List<String> selectedCards) {
        int hearts = 0;
        int diamonds = 0;


        for (String card : selectedCards) {
            if (card.contains("H")) {
                builder(card, true);
                hearts++;
            } else if (card.contains("D")) {
                builder(card, true);
                diamonds++;
            } else {
                builder(card, false);
            }
        }

        return (hearts == 4 || diamonds == 4) ||
                (hearts == 1 && diamonds == 3) ||
                (hearts == 3 && diamonds == 1) ||
                (hearts == 2 && diamonds == 2);
    }

    // Author: Kris Bosco
    // Method to only allow the card suit clubs as correct choices when user is making selections
    // Will return true if only clubs are selected by the user
    private static boolean allClubs(List<String> selectedCards) {
        int clubs = 0;

        for (String card : selectedCards) {
            if (card.contains("C")) {
                builder(card, true);
                clubs++;
            } else {
                builder(card, false);
            }
        }

        return clubs == 4;
    }

    // Author: Kris Bosco
    // Method to only allow face cards as correct choices when user is making selections
    // Will return true if only Jacks, Queens, or Kings are selected in any combination of choices
    private static boolean allFaceCards(List<String> selectedCards) {
        int faceCards = 0;

        for (String card : selectedCards) {
            if (card.contains("K") || card.contains("Q") || card.contains("J")) {
                builder(card, true);
                faceCards++;
            } else {
                builder(card, false);
            }
        }

        return faceCards == 4;
    }

    // Author: Kris Bosco
    // Method to only allow single digit cards as correct choices when user is making selections
    // Will return true if only cards between 2-9 of any suit are selection in any combination of choices
    private static boolean allSingleDigits(List<String> selectedCards) {
        int singleDigits = 0;

        for (String card : selectedCards) {
            int rank = getCardNumericValue(card.charAt(0));// incorporated method here for edge case at '10'.
            if (10 > rank && 1 < rank) {
                builder(card, true);
                singleDigits++;
            } else {
                builder(card, false);
            }
        }
// if (Character.isDigit(rank)) -- the original if statement.
        return singleDigits == 4;
    }

    // Author: Kris Bosco
    // Method to only allow single digit prime cards as correct choices when user is making selections
    // Will return true if cards of any suit that contain 3, 5, 7 are selected in any combination of choices
    private static boolean allSingleDigitPrimes(List<String> selectedCards) {
        int primeDigits = 0;

        for (String card : selectedCards) {
            char rank = card.charAt(0);
            if (rank == '2' || rank == '3' || rank == '5' || rank == '7') {
                builder(card, true);
                primeDigits++;
            } else {
                builder(card, false);
            }
        }

        return primeDigits == 4;
    }

    // Author: Kris Bosco
    // Method to only allow the highest rank card of the current group of cards selected by the user to be correct when user is making selections
    // Will return true if user selects the same card for all suits and is the highest rank entered during the round.
    private static boolean highestRank(List<String> selectedCards) {

        for (String card : selectedCards) {
            char rank = card.charAt(0);
            if (rank > highestRank) {
                highestRank = rank;
            }
        }

        int count = 0;
        for (String card : selectedCards) {
            if (card.charAt(0) == highestRank) {
                builder(card, true);
                count++;
            } else {
                builder(card, false);
            }
        }

        return count == 4;
    }

    //Author: Karlin Clabon-Barnes
    //Method to check if a hand of cards has a rising sequence of ranks - all with the same suit.
    //Returns true if a rising run within the same suit is detected.
    private static boolean risingRunSameSuit(List<String> selectedCards){
        int risingRun = 1;

        char sameSuit = selectedCards.get(0).charAt(1);//suit of first card
        for (int i = 1; i < selectedCards.size(); i++) {
            char rank = selectedCards.get(i).charAt(0);//rank of card at i
            char suit = selectedCards.get(i).charAt(1);//suit of card at i
            char previousCardRank = selectedCards.get(i-1).charAt(0);//rank of previous card to i
            if (rank > previousCardRank && suit == sameSuit) {
                risingRun++;
            }
        }
            for(String card : selectedCards){
                if(risingRun == 4){// if program detects a rising run
                    builder(card,true);// highlight all cards for Art Dealer
                } else{
                    builder(card,false);// else highlight no cards for Art Dealer
                }//Implementing builder later to accept either all cards or no cards at all.
            }




     return risingRun == 4;
    }

    //Author: Karlin Clabon-Barnes
    //Method that checks for combinations of cards that add to 11.
    // Returns true if at least one instance is found.
    private static boolean addsToEleven(List<String> selectedCards) {

        List<List<String>> validCombinations = new ArrayList<>();
        findCombinations(selectedCards, validCombinations, new ArrayList<>(), 0, 11);
        Set<String> usedCards = new HashSet<>(); // To track which cards have been used in valid combinations no duplicates

        for (List<String> combination : validCombinations) {
            for(String card : combination){
                builder(card,true);
                usedCards.add(card);
            }
        }

        // Check for any cards not used in valid combinations and mark them as false
        for (String card : selectedCards) {
            if (!usedCards.contains(card)) {
                builder(card, false);
            }
        }

        return !validCombinations.isEmpty();
    }
    //Author: Karlin Clabon-Barnes
    // Recursive method that parses through all possibilities of card combinations.
    private static void findCombinations(List<String> selectedCards, List<List<String>> validCombinations, List<String> current, int start, int targetSum) {
        if (current.size() > 1 && sumOfCards(current) == targetSum) {
            validCombinations.add(new ArrayList<>(current));
        }
        if (current.size() == 4) {
            return; // Stop if the combination size is 4 (maximum allowed)
        }
        for (int i = start; i < selectedCards.size(); i++) {//Starting with the first card to the last
            current.add(selectedCards.get(i));// Add a card starting with the first
            findCombinations(selectedCards, validCombinations, current, i + 1, targetSum);// recursive call - adding +1 cards
            current.remove(current.size() - 1); // Backtrack to try the next possibility -- removes last element in ArrayList
        }
    }

    //Author: Karlin Clabon-Barnes
    //Method to sum ranks of cards.
    private static int sumOfCards(List<String> cards) {
        int sum = 0;
        for (String card : cards) {
            char rank = card.charAt(0); // Assume card format is "RankSuit" (e.g., "2H")
            sum += getCardNumericValue(rank);
        }
        return sum;
    }
    //Author: Karlin Clabon-Barnes
    //Method that tracks a pattern of skipping by two (2,4,6,8; 3,5,7,9) within the same suit.
    //Returns true if this pattern is found.
    private static boolean skippingBy2AnySuit(List<String> selectedCards){
        int skippingBy2 = 1;//initialize the variable to one, so we can check the final three cards.
        //char firstRank = selectedCards.get(0).charAt(0);

        for (int i = 1; i < selectedCards.size(); i++) {//increment or decrement .size() ? -> Neither.
            char rank = selectedCards.get(i).charAt(0);//rank of card at i
            char previousCardRank = selectedCards.get(i-1).charAt(0);//rank of previous card to i
            if (rank == previousCardRank + 2) {
                skippingBy2++;
            }
        }
            for(String card : selectedCards){
                if(skippingBy2 == 4){// if program detects a rising run
                    builder(card,true);// highlight all cards for Art Dealer
                } else{
                    builder(card,false);// else highlight no cards for Art Dealer
                }//Implementing builder later to accept either all cards or no cards at all.
            }

        return skippingBy2 == 4;
    }

    //Author: Karlin Clabon-Barnes
    //Method to check if only aces and eights cards have been selected.
    //Returns true if only aces and eights cards are found.
    private static boolean acesAndEights(List<String> selectedCards) {
        int aces = 0;
        int eights = 0;


        for (String card : selectedCards) {
            if (card.contains("8")) {
                eights++;
            } else if (card.contains("A")) {
                aces++;
            }
        }

        for (String card : selectedCards) {
            if (aces == 2 && eights == 2) {
                builder(card, true);
            } else {
                builder(card, false);
            }
        }
        return (aces == 2 && eights == 2);
    }

    //Author: Karlin Clabon-Barnes
    //Method to detect if only face cards within the same suit.
    //Returns true if only face cards within the same suit have been selected.
    private static boolean royalFlush(List<String> selectedCards){
        char sameSuit = selectedCards.get(0).charAt(1);//suit of first card
        // variables that allow tracking of various types of cards
        int royalFlush = 0;
        int aces = 0;
        int kings = 0;
        int queens = 0;
        int jokers = 0;

        for (String card : selectedCards) {
            if (card.charAt(0) == 'A'&& card.charAt(1) == sameSuit) {
                aces++;
                royalFlush++;
            } else if (card.charAt(0) == 'K'&& card.charAt(1) == sameSuit) {
                kings++;
                royalFlush++;
            } else if (card.charAt(0) == 'Q' && card.charAt(1) == sameSuit) {
                queens++;
               royalFlush++;
            } else if (card.charAt(0) == 'J'&& card.charAt(1) == sameSuit) {
                jokers++;
               royalFlush++;
            }
        }
        // if conditions hold for all 4 cards
        for(String card : selectedCards){
            if(royalFlush == 4 && aces == 1 && kings == 1 && queens == 1 && jokers == 1){
                builder(card,true);
            } else{
                builder(card,false);
            }
        }
        return royalFlush == 4;
    }
    //Author: Karlin Clabon-Barnes
    //Method detects two black jacks and two aces of any suits.
    //Returns true if only the two black jacks and two different aces are selected.
    private static boolean twoBlackJackCombos(List<String> selectedCards){
        int jackClub = 0;
        int jackSpades = 0;
        int aces = 0;

        for (String card : selectedCards) {
            if (card.charAt(0) == 'J' && card.charAt(1) == 'C') {//if jack of Clubs found
                jackClub++;
            }
            if (card.charAt(0) == 'J' && card.charAt(1) == 'S') {//if jack of Spades found
                jackSpades++;
            }
            if (card.charAt(0) == 'A'){
                aces++;
            }
        }

        for (String card : selectedCards) {
            if (aces == 2 && jackClub == 1 && jackSpades == 1) {
                builder(card, true);
            } else {
                builder(card, false);
            }
        }
        return (aces == 2 && jackClub == 1 && jackSpades == 1);
    }

    //Author: Karlin Clabon-Barnes
    //Method used in pattern finding game methods to assign and return numeric values of card ranks.
    private static int getCardNumericValue(char rank) {
        switch(rank) {
            case 'A': return 1; // Ace as 1
            case '2': return 2;
            case '3': return 3;
            case '4': return 4;
            case '5': return 5;
            case '6': return 6;
            case '7': return 7;
            case '8': return 8;
            case '9': return 9;
            case '1': return 10; //Will have to update code to account for testing cases with '10' cards. 3 chars not 2!
            default: return 0; // This will handle face cards or invalid entries
        }
    }


    // Author: Kris Bosco
    // Method to check if user has sold two groups of four to the "Art Dealer"
    // Returns false if user has only sold one group
    private static boolean soldTwoGroups(List<String> selectedCards) {
        int size = selectedCards.size();
        if (size % 2 != 0 || size < 4) {
            return false;
        } else {
            return true;
        }
    }

    //Author: Karlin Clabon-Barnes
    //Method to allow congratulatory sound effect to play upon user winning round.
    public static void playSound(String resourcePath) {
        try {
            // Obtain a URL to the resource
            URL url = GameMethods.class.getResource(resourcePath);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);

            // Get a sound clip resource
            Clip clip = AudioSystem.getClip();

            // Open audio clip and load samples from the audio input stream
            clip.open(audioIn);
            clip.start(); // Start playing the sound

        } catch (UnsupportedAudioFileException e) {
            System.err.println("Unsupported audio file format.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error reading audio file.");
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            System.err.println("Audio line unavailable.");
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("Sound file not found in resources.");
            e.printStackTrace();
        }
    }



}