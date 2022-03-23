import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Hangman {

	private static int correct;
	private static int incorrect;
	private static int left;
	private static int count;
	private static int score;
	private static int gameCount;
	private static int finalscore;
	private static int bonus;
	private final static int COLS = 40;
	private final static int MAX_GUESSES = 7;
	private static boolean hanged = false;
	private static boolean saved = false;
	private static boolean stop = false;
	private static boolean end = false;
	private static String word;
	private static String guess;
	private static String option;
	private static String name;
	private static final String DICTIONARY = "res/dictionary.txt";
	private static final String HIGHSCORES = "res/highscores.txt";
	private static ArrayList<String> letters = new ArrayList<>();
	private static ArrayList<String> guesses = new ArrayList<>();
	private static ArrayList<String> matches = new ArrayList<>();
	private static ArrayList<Integer> scores = new ArrayList<>();
	private static ArrayList<String> dictionary = new ArrayList<>();
	private static ArrayList<String> leaderboard = new ArrayList<>();
	private static HashSet<String> uLetters;
	private static Scanner input;

	public static void main(String[] args) {
		try {
			File file = new File(DICTIONARY);
			input = new Scanner(file);
			System.out.println("Loading dictionary...");
			do {
				dictionary.add(input.nextLine());
			} while (input.hasNextLine());
			input = new Scanner(System.in);
			gameCount = 1;
			do {
				newGame();
				gameCount++;
				scores.add(score);
				end = (stop || hanged);
			} while(!end);
			for (Integer s: scores) finalscore += s;
			System.out.println("\n--------------------------------------\n");
			System.out.println("Your final score is: " + finalscore);
			if (finalscore > 0) leaderboard();
			System.out.println("\nThank you for playing hangman.\n");
		} catch (FileNotFoundException e) {
			System.out.println("File " + DICTIONARY + " not found!");
		}
	}

	public static void newGame() {
		letters.clear();
		guesses.clear();
		matches.clear();
		saved = false;
		option = "N";
		correct = 0;
		incorrect = 0;
		score = 0;
		left = MAX_GUESSES;
		System.out.println();
		System.out.println("-------------- hangman! --------------");
		System.out.println();
		System.out.println(gameCount > 1 ? "      This is your game number " + gameCount + "\n" : "        First game, good luck!\n");

		int random = (int)(Math.random() * dictionary.size());
		word = dictionary.get(random).toUpperCase();
		for (int i = 0; i < word.length(); i++) {
			letters.add(Character.toString(word.charAt(i)));
		}
		uLetters = new HashSet<String>(letters);
		System.out.println("The hidden word has " + letters.size() + " letters.");

		do {
			play();
			end = (saved || hanged);
		} while(!end);

		if (gameCount > 0 && !hanged) {
			System.out.print("\nDo you want to keep playing? (y/n) ");
			option = input.nextLine().toUpperCase();
			if (!option.matches("[YN]")) throw new IllegalArgumentException("\nPlease select an option! (y/n)");
			stop = option.equals("N");
			clearscreen();
		}
	}

	public static void play() {
		try {
			correct = matches.size();
			incorrect = guesses.size();
			left = MAX_GUESSES - incorrect;
			System.out.println();
			hang();
			System.out.println();	
			System.out.print("HIDDEN WORD: ");
			for (String l: letters) {
				if (matches.contains(l)) {
					System.out.print(l + " ");
				} else {
					System.out.print("_ ");
				}
			}
			// System.out.println("\n <" + word + ">"); // HEY, NO CHEATING!!!
			System.out.println("\n");
			System.out.println("Correct guesses: " + correct);
			System.out.println("Incorrect guesses: " + incorrect + " " + guesses.toString());
			System.out.println("Guesses left: " + left);
			System.out.println("Score: " + score);
			System.out.println("\n--------------------------------------");

			if (matches.size() > 0 && (correct == uLetters.size())) {
				score += (100 + 30 * left);
				saved = true;
				hang();
				System.out.println("\nYOU GOT IT! The word was: " + word);
				System.out.println("Hooray, you scored: " + score);
			} else if (left == 0) {
				hanged = true;
				System.out.println("\nYOU HANGED! The word was: " + word);
				System.out.println("Too bad, your score was: " + score);
				score = 0;
			} else if (correct > 0 && (correct == uLetters.size() - 1)) {
				System.out.print("\nONE LETTER TO GO! Do you want to try and guess the word? (y/n) ");
				guessWord();
			} else if (incorrect == MAX_GUESSES - 1) {
				System.out.print("\nLAST GUESS! Do you want to try your luck and guess the word? (y/n) ");
				guessWord();
			}  else guessLetter();
		} catch (InputMismatchException e) {
			System.out.println("Enter a letter.");
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void guessLetter() {
		count = 0;
		System.out.println();
		System.out.print("Enter next guess: ");
		guess = input.nextLine().toUpperCase();
		if (guess.length() > 1) throw new IllegalArgumentException("\n*** You have to enter a single letter to guess ***");
		if (!guess.matches("[A-Z]")) throw new IllegalArgumentException("\n*** You have to enter a letter to guess ***");
		if (guesses.contains(guess) || matches.contains(guess)) {
			System.out.println("\nYou have already guessed the letter " + guess);
		} else {
			for (String l: letters) {
				if (guess.equals(l)) {
					if (!matches.contains(guess)) matches.add(l);
					count++;
				}
			}
		} 
		if (!matches.contains(guess)) {
			System.out.println("\nNope, there are no " + guess + "'s here.");
			if (!guesses.contains(guess)) {
				if (guesses.size() > 0 && guess.compareTo(guesses.get(0)) < 0) {
					guesses.add(0, guess);
				} else guesses.add(guess);
			}
		}
		score += (count * 10);
	}

	public static void guessWord() {
		option = input.nextLine().toUpperCase();
		if (!option.matches("[YN]")) throw new IllegalArgumentException("\n*** You have to select an option! (y/n) ***");
		if (option.equals("Y")) {
			System.out.println();
			System.out.print("WHAT IS THE WORD? ");
			guess = input.nextLine().toUpperCase();
			if (guess.equals(word)) {
				score += (100 + 30 * left);
				bonus = 50 * left;
				saved = true;
				score += bonus;
				hang();
				System.out.println("\nYOU GOT IT! The word was: " + word);
				System.out.println("\nYou guessed the word and you deserve a bonus!");
				System.out.println("Adding " + bonus + " points to your score.");
				System.out.println("Hooray, you scored: " + score);

			} else {
				hanged = true;
				score = 0;
				System.out.println("\nYOU HANGED! The word was: " + word);
			}
		} else guessLetter();
	}

	public static void leaderboard() {
		try {
			File file = new File(HIGHSCORES);
			boolean isEmpty = false;
			if (!file.exists()) {
				file.createNewFile();
				isEmpty = true;
			}

			input = new Scanner(file);
			String line, leadername;
			int leaderscore;
			int center;
			boolean highscore = false;
			System.out.println("\n ============= Leaderboard =============\n");

			if (input.hasNextLine()) {
				do {
					line = input.nextLine();
					center = (COLS + line.length())/2;
					leadername = line.substring(0, line.indexOf(":"));
					leaderscore = Integer.parseInt(line.substring(line.indexOf(":") + 2, line.length()));
					leaderboard.add(leadername + ": " + leaderscore);
					if (finalscore > leaderscore) {
						highscore = true;
					}
					System.out.printf("%" + center + "s\n", line);
				} while (input.hasNextLine());
			} else {
				isEmpty = true;
				line = "NO HIGH SCORES";
				center = (COLS + line.length())/2;
				System.out.printf("%" + center + "s\n", line);
			}

			System.out.println("\n =======================================\n");

			int highscores = leaderboard.size();
			if (highscore || highscores < 5 || isEmpty) {

				System.out.println("Congratulations! You achieved a high score.");
				System.out.println("Now you are on the Leaderboard!\n");
				input = new Scanner(System.in);
				System.out.print("Please enter your name: ");
				name = input.nextLine();

				int i = 0;
				if (name.equals("")) throw new IllegalArgumentException("Please enter a name.");
				if (isEmpty) leaderboard.add(name + ": " + finalscore);
				else {
					for (String s: leaderboard) {
						leaderscore = Integer.parseInt(s.substring(s.indexOf(":") + 2, s.length()));
						if (finalscore > leaderscore || i == highscores - 1) {
							leaderboard.add(i, name + ": " + finalscore);
							break;
						}
						i++;
					}
				}

				highscores = leaderboard.size();
				if (highscores > 5) leaderboard.remove(highscores - 1);

				PrintWriter output = new PrintWriter(file);
				for (String s: leaderboard) {
					output.println(s);
				}
				output.close();

			} else {
				System.out.println("You did not make it to the top five.");
				System.out.println("Keep playing to become immortal!");
				System.out.println("No new record written.\n");
			}

			input = new Scanner(file);
			System.out.println("\n ============= Leaderboard =============\n");

			while (input.hasNextLine()) {
				line = input.nextLine();	
				center = (COLS + line.length())/2;
				System.out.printf("%" + center + "s\n", line);
			} 

			System.out.println("\n =======================================\n");

			input.close();
		} catch (FileNotFoundException e) {
			System.out.println("\nFile " + HIGHSCORES + " not found!");
		} catch (IOException e) {
			System.out.println("\nInput/output error!");
		} catch (NoSuchElementException e) {
			System.out.println("\nArray element not found!");
		} catch (NumberFormatException e) {
			System.out.println("\nInvalid number!");
		} catch (IndexOutOfBoundsException e) {
			System.out.println("\nInvalid item!");
			System.out.println(e.getMessage());
		}
	}

	public static void hang() {
		if (saved == true) {
			System.out.printf("%4s\n%-4s%1s\n%-4s\n%-3s%1s\n%-4s%1s\n%-3s%1s", "___", "|", "|", "|", "|", "\\Ô/", "|", "|", "|", "/ˆ\\\n");
		} else if (left == 6) {
			System.out.printf("%4s\n%-4s%1s\n%-4s\n%-4s\n%-4s\n%-4s", "___", "|", "|", "|", "|", "|", "|\n");
		} else if (left == 5) {
			System.out.printf("%4s\n%-4s%1s\n%-4s%1s\n%-4s\n%-4s\n%-4s", "___", "|", "|", "|", "O", "|", "|", "|\n");
		} else if (left == 4) {
			System.out.printf("%4s\n%-4s%1s\n%-4s%1s\n%-4s%1s\n%-4s\n%-4s", "___", "|", "|", "|", "O", "|", "|", "|", "|\n");
		} else if (left == 3) {
			System.out.printf("%4s\n%-4s%1s\n%-4s%1s\n%-3s%1s\n%-4s\n%-4s", "___", "|", "|", "|", "O", "|", "/|", "|", "|\n");
		} else if (left == 2) {
			System.out.printf("%4s\n%-4s%1s\n%-4s%1s\n%-3s%1s\n%-4s\n%-4s", "___", "|", "|", "|", "O", "|", "/|\\", "|", "|\n");
		} else if (left == 1) {
			System.out.printf("%4s\n%-4s%1s\n%-4s%1s\n%-3s%1s\n%-3s%1s\n%-4s", "___", "|", "|", "|", "O", "|", "/|\\", "|", "/", "|\n");
		} else if (left == 0) {
			System.out.printf("%4s\n%-4s%1s\n%-4s%1s\n%-3s%1s\n%-3s%1s\n%-4s", "___", "|", "|", "|", "Q", "|", "/|\\", "|", "/ˆ\\", "|\n");
		} else {
			System.out.printf("%4s\n%-4s\n%-4s\n%-4s\n%-4s", "___", "|", "|", "|", "|\n");
		} 
	}

	public static void clearscreen() {
		//System.out.print("\033[H\033[2J");
		//System.out.print("\033\143");
		//System.out.print('\f');
		//for (int i = 0; i < 50; ++i) System.out.println();
		System.out.flush();
	}
}