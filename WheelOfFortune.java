import java.util.Scanner;
import java.io.File;
import java.io.PrintWriter;

public class WheelOfFortune {
	// Main scanner is initialized here so that it can be used in functions
	static Scanner sc = new Scanner(System.in);
	// Input options that are used in various places
	static final char[] YES_NO = {'y', 'n'};
	static final char[] NEXT_MOVE = {'s', 'v', 'g'};
	
	public static void main(String[] args) throws Exception {
		// Initialize constants
		// Instructions that are optionally printed to the user
		final String TUTORIAL = "- You will be competing against an AI to guess a unique phrase.\n- Each turn, you spin a wheel with different cash amounts and guess a consonant that may be in the phrase.\n- If you guess correctly, all positions of that letter are revealed and you receive the value of your spin multiplied by the number of letters that were revealed.\n- You may also buy all positions of a vowel for $250.\n- But beware, the AI is also trying to guess the phrase, just as you are.\n- The player who guesses the phrase first wins! Good luck!";
		// Values on the wheel, one of which is chosen randomly on each spin (simulating a real spin)
		// Each element has extra whitespace so that they will format nicely on the wheel that is printed to the user (explained in more detail in the print wheel method)
		final String[] WHEEL_VALUES = {"  $100      ", "  $250      ", "  $500      ", "  $750      ", "  $1000     ", "  $2000     ", " Lose a turn", "Bankrupt    "};
		
		// Initialize the scanner for the phrases database file
		// Each line in this file has a category and a unique phrase that are separated by a comma
		File phrasesFile = new File("phrases.txt");
		Scanner phrasesSC = new Scanner(phrasesFile);
		
		// Greet the user and ask for their username
		System.out.println("Please use full screen for the best experience!");
		String username = getInput("Enter your username: ", new char[0]);
		// In case the user presses enter without entering a username
		if (username.length() == 0) username = "Anonymous";
		System.out.println("Welcome, " + username + ", to Wheel of Fortune!");
		
		// Show the tutorial
		char showTutorial = getInput("Show tutorial? [y/n] ", YES_NO).charAt(0);
		if (showTutorial == 'y') {
			System.out.println(TUTORIAL);
			pause("Ready?");
		}
		
		// Determine the number of lines in the phrases file which will be the upper limit of the random function
		// Simply increments a counter by 1 after the scanner moves to the next line
		int lineCount = 0;
		while (phrasesSC.hasNextLine()) {
			phrasesSC.nextLine();
			lineCount++;
		}

		// Number of turns completed so far, incremented by 1 after each turn
		int turns = 0;
		
		// Keep playing the game while the user wants to (asked whether to play again at the end of the game)
		boolean keepPlaying = true;
		while (keepPlaying) {
			// Choose a random phrase
			// Need another scanner because the file has to be run though again (previous scanner would already be at the end of the file)
			Scanner phrasesSC2 = new Scanner(phrasesFile);
			// Choose a random line based on the number of lines that was found
			int randLine = (int)(Math.random() * lineCount + 1);
			// Keep moving scanner to the next line until it reaches the random line
			for (int i = 0; i < randLine - 1; i++) {
				phrasesSC2.nextLine();
			}
			// Obtain the category and phrase on the random line
			String categoryAnswer = phrasesSC2.nextLine();
			// Using the first comma specifically to separate the category and phrase is important because there might be another comma in the phrase itself
			String category = categoryAnswer.substring(0, categoryAnswer.indexOf(","));
			String answer = categoryAnswer.substring(categoryAnswer.indexOf(",") + 1);
			// Print the answer for debugging purposes
			//System.out.println("Answer: " + answer);
			
			// Initialize variables
			// User and AI scores
			int userScore = 0;
			int aiScore = 0;
			// Consonants and vowels that were already used
			// Uses strings for variable length (increases length once letters start being guessed)
			String consonants = "";
			String vowels = "";
			// Consonants and vowels that can still be guessed
			// Any consonants and vowel can be guessed at the start of the game
			String availConsonants = "BCDFGHJKLMNPQRSTVWXYZ";
			String availVowels = "AEIOU";
			// 0 means nobody won yet, 1 means the user won, 2 means the AI won
			int gameOver = 0;
			// Generate the actual phrase that the user sees (unguessed letters replaced with underscores)
			String phrase = generatePhrase(answer, consonants, vowels);
			
			// Game loop
			while (gameOver == 0) {
				// New line on each turn
				System.out.println();
				// Print the game summary at the start of each turn (category, phrase (with underscores), scores, letters guessed so far, letters remaining)
				gameSummary(category, phrase, userScore, aiScore, consonants, vowels, availConsonants, availVowels);
				
				// User goes on even turns
				// AI only goes first if they were the winner of the previous game
				// User (first) parameter is true if the current turn number is divisible by 2
				String[] newStats = playTurn(turns % 2 == 0, phrase, answer, gameOver, userScore, aiScore, consonants, vowels, availConsonants, availVowels, WHEEL_VALUES);
				
				// Update all game stats
				// Phrase that the user sees (with underscores)
				phrase = newStats[0];
				// Whether the game has ended yet
				gameOver = Integer.parseInt(newStats[1]);
				// Scores
				userScore = Integer.parseInt(newStats[2]);
				aiScore = Integer.parseInt(newStats[3]);
				// Letters that were already guessed
				consonants = newStats[4];
				vowels = newStats[5];
				// Letters that are not guessed yet
				availConsonants = newStats[6];
				availVowels = newStats[7];
				
				// Increment the turn number at the end of each turn
				turns++;
			}
			// At the end of the game:
			
			// Convert the leaderboard database file to a string
			// Each line in this file has a username and a score that are separated by a comma
			// AI's username is AI
			File lbFile = new File("leaderboard.txt");
			lbFile.createNewFile();
			Scanner lbSC = new Scanner(lbFile);
			String lbString = "";
			while (lbSC.hasNextLine()) {
				lbString += (lbSC.nextLine() + "\n");
			}
			
			// Add the winner to the leaderboard
			// Check whether the winner is the user or the AI
			if (gameOver == 1) {
				lbString += (username + "," + userScore);
			} else if (gameOver == 2) {
				lbString += ("AI," + aiScore);
			}
			
			// Sort the leaderboard using bubble sort
			// Convert the leaderboard string to an array (easier for sorting)
			String[] lbArr = lbString.split("\n");
			for (int i = 1; i < lbArr.length; i++) {
				for (int j = 0; j < lbArr.length - i; j++) {
					// Substring a certain string element to keep everything after the last comma, then convert it into an int
					// Using the last comma is important because the username might have commas in it, but the score will never have a comma
					int a = Integer.parseInt(lbArr[j].substring(lbArr[j].lastIndexOf(",") + 1));
					int b = Integer.parseInt(lbArr[j + 1].substring(lbArr[j + 1].lastIndexOf(",") + 1));
					// Swap elements if needed
					if (a < b) {
						String temp = lbArr[j];
						lbArr[j] = lbArr[j + 1];
						lbArr[j + 1] = temp;
					}
				}
			}
			
			// Determine the maximum username length which will be used for formatting
			// Loop through the leaderboard array and update a max variable based on the length of the substring before the last comma in each element
			int maxName = 0;
			for (int i = 0; i < lbArr.length; i++) {
				int name = lbArr[i].substring(0, lbArr[i].lastIndexOf(",")).length();
				if (name > maxName) {
					maxName = name;
				}
			}
			
			// Save and print the leaderboard
			PrintWriter lbPW = new PrintWriter("leaderboard.txt");
			System.out.println();
			System.out.println("Leaderboard:");
			// Loop through each element in the leaderboard array
			for (int i = 0; i < lbArr.length; i++) {
				lbPW.println(lbArr[i]);
				// Seperate each username and its correlating score using the last comma as a separator
				String name = lbArr[i].substring(0, lbArr[i].lastIndexOf(","));
				String score = lbArr[i].substring(lbArr[i].lastIndexOf(",") + 1);
				// The leaderboard is printed with a number at the start of each line to indicate that line's position on the leaderboard
				// If that number is two digits, there must be 1 less space between the username and the score to ensure that the scores still line up
				int twoDigits = 0;
				if ((i + 1) / 10 > 0) {
					twoDigits = 1;
				}
				// The gap between each username and its score starts at 5 spaces
				// Increased by the difference between the maximum username and the current username
				// Decreased by 1 if the position on the leaderboard is two digits
				// The whole objective is to ensure that all scores line up in the console, regardless of username length
				System.out.println((i + 1) + ". " + name + repeat(' ', 5 + maxName - name.length() - twoDigits) + "$" + score);
			}
			
			// Close the leaderboard file print writer
			lbPW.close();
			
			// Ask the user if they would like to play again
			System.out.println();
			if (getInput("Would you like to play again? [y/n] ", YES_NO).equals("n")) {
				// End the while loop if the user does not want to play again 
				keepPlaying = false;
				// Print a goodbye message to the user
				System.out.println("See you next time!");
			} else {
				// Reset number of turns
				// Winner goes first
				if (gameOver == 1) {
					turns = 0;
				} else if (gameOver == 2) {
					turns = 1;
				}
			}
			
			// Close the leaderboard file scanner
			lbSC.close();
			// Close the second phrases file scanner
			phrasesSC2.close();
			// Closed inside of the while loop because they are reset if the user wants to play again
		}
		
		// Close the main scanner
		sc.close();
		// Close the first phrases file scanner
		phrasesSC.close();
	}
	
	// Main logic for each turn in the game
	// Same function is used for both user and A
	public static String[] playTurn(boolean user, String phrase, String answer, int gameOver, int userScore, int aiScore, String consonants, String vowels, String availConsonants, String availVowels, String[] WHEEL_VALUES) {
		// Print whose turn it is
		if (user) {
			System.out.println("Your turn!");
		} else {
			pause("AI's turn.");
		}
		
		// Set variables which reset each turn
		
		// Whether the current turn is over
		// Function ends once this turns true
		boolean turnOver = false;
		// Whether it's the AI's first time playing a move
		// Only used for printing a message to the console
		boolean firstTime = true;
		// The current choice that the user or AI has made
		// s: spinning the wheel
		// v: buying a vowel
		// g: guessing the full phrase
		// Starts at s because players spin the wheel at the start of their turn
		char choice = 's';
		
		// Skip spinning the wheel if there are no consonants left to guess
		if (availConsonants.length() == 0) {
			char[] options = {'v', 'g'};
			if (user) {
				// Ask the user which option they would like to choose instead
				choice = getInput("Would you like to buy a vowel [v] or guess the phrase [g]? ", options).charAt(0);
			} else {
				// AI chooses a random option from the remaining available choices
				choice = options[(int)(Math.random() * options.length)];
			}
		}
		
		// Player can keep making moves while they are able to
		while (!turnOver) {
			System.out.println();
			if (choice == 's') { // Guessing a consonant
				// Not printed if it's the AI's first time spinning because they're not spinning "again"
				if (!user && !firstTime) {
					System.out.println("AI chooses to spin again.");
				}
				firstTime = false;
				
				// Spin the wheel
				if (user) {
					pause("Spin the wheel!");
				} else {
					System.out.println("AI is spinning...");
					
				}
				int spinNum = (int)(Math.random() * WHEEL_VALUES.length);
				// Lose a turn and Bankrupt have a 5x lower chance
				// If the random spin lands on either of those, another random number between 0 and 4 is chosen
				// If the new random number is specifically 0, the spin remains
				// If it isn't, the wheel spins again excluding Lose a turn and Bankrupt
				if (spinNum == 6 || spinNum == 7) {
					if ((int)(Math.random() * 5) != 0) {
						// Lose a turn and Bankrupt are the last two elements in the wheel values array
						// They can be excluded by pretending that the max array length does not reach them
						spinNum = (int)(Math.random() * (WHEEL_VALUES.length - 2));
					}
				}
				
				// Print the wheel
				String spin = WHEEL_VALUES[spinNum].trim();
				printWheel(WHEEL_VALUES, spinNum);
				if (user) {
					System.out.println("You land on " + spin + ".");
				} else {
					System.out.println("AI lands on " + spin + ".");
				}
				
				// Check if the player lands on an "unlucky" option
				if (spin.equals("Lose a turn")) {
					// End the player's turn if they land on Lose a turn
					turnOver = true;
				} else if (spin.equals("Bankrupt")) {
					// End the player's turn and reset their money if they land on Bankrupt
					if (user) {
						userScore = 0;
						System.out.println("Oh no! You lose all of your money!");
					} else {
						aiScore = 0;
						System.out.println("Oh no! AI loses all of their money!");
					}
					turnOver = true;
				} else {
					// Convert the value of the spin to an int by removing the $ at the start
					int spinValue = Integer.parseInt(spin.substring(1, spin.length()));
					
					// Player guesses a consonant
					char consonant = ' ';
					if (user) {
						// Ask the user to guess a consonant
						// Valid input is any of the unguessed consonants, both in uppercase and lowercase
						consonant = getInput("Guess a consonant: ", (availConsonants + availConsonants.toLowerCase()).toCharArray()).toUpperCase().charAt(0);
					} else {
						// AI guesses the most common letters first
						String commonLetters = "RSTLN";
						String commonAvailLetters = "";
						// Check which common letters have not been guessed yet by looping through them and the available consonants variable
						for (int i = 0; i < commonLetters.length(); i++) {
							char letter = commonLetters.charAt(i);
							if (availConsonants.indexOf(letter) > -1) {
								commonAvailLetters += letter;
							}
						}
						if (commonAvailLetters.length() == 0) {
							// Pick another random letter if all the common letters are already guessed
							consonant = availConsonants.charAt((int)(Math.random() * availConsonants.length()));
							// Uncommon letters have about a twice lower chance
							// Uses the same random logic as Lose a turn and Bankrupt having a x5 lower chance
							// However, they have *about* a twice lower chance because they could still be selected again if the AI chooses a random letter again
							if ("JQVXZ".indexOf(consonant) > -1) {
								if (Math.random() * 2 == 0) {
									consonant = availConsonants.charAt((int)(Math.random() * availConsonants.length()));
								}
							}
						} else {
							// If there is an available common letter, select a random one
							consonant = commonAvailLetters.charAt((int)(Math.random() * commonAvailLetters.length()));
						}
						System.out.println("AI guesses " + consonant + ".");
					}
					// Handle guessing a letter
					// In its own function because the code is exactly the same for both the user and AI (no user variable passed on)
					// The code is also the same for both consonants and vowels
					String[] newData = guessLetter(consonant, answer, consonants, vowels);
					
					// Update the game stats
					// If a consonant is guessed correctly, the player receives the value of their spin multiplied by the number of that consonant in the phrase
					int scoreIncrease = Integer.parseInt(newData[0]) * spinValue;
					phrase = newData[1];
					consonants = newData[2];
					
					// Remove the consonant that was just guessed from the list of available choices
					availConsonants = replace(availConsonants, availConsonants.indexOf(consonant), "");
					
					// Check whether the player's turn is over or they can continue going
					// They can continue going if their score increased (if they successfully guessed a consonant)
					if (scoreIncrease > 0) {
						if (user) {
							userScore += scoreIncrease;
							System.out.println("You earn $" + scoreIncrease + " and your new score is $" + userScore + ".");
						} else {
							aiScore += scoreIncrease;
							pause("AI earns $" + scoreIncrease + " and AI's new score is $" + aiScore + ".");
						}
						
						// Check if the full answer was already revealed
						if (phrase.equals(answer)) {
							if (user) {
								System.out.println("Wait a minute, the full phrase is revealed! You win the game!");
								// End the game
								gameOver = 1;
							} else {
								System.out.println("Wait a minute, the full phrase is revealed! AI wins the game!");
								gameOver = 2;
							}
							turnOver = true;
						} else {
							// Player picks their next move
							if (user) {
								// List available options to the user
								String prompt;
								char[] options;
								if (availVowels.length() == 0 || userScore < 250) {
									// If there are no vowels left to guess or if the user's score is less than 250, the user cannot buy a vowel
									// However, they can still spin again or guess the full phrase
									prompt = "Would you like to spin again [s] or guess the full phrase [g]? ";
									options = new char[]{'s', 'g'};
								} else if (availConsonants.length() == 0) {
									// No consonants left to guess
									// The user has no choice but to either buy a vowel or guess the full phrase
									// Though if all the consonants have been guessed already, there is an extremely likely chance that the user's score is over 250 anyway
									System.out.println("There are no consonants left to guess.");
									prompt = "Would you like to buy a vowel [v] or guess the full phrase [g]? ";
									options = new char[]{'v', 'g'};
								} else {
									// No extra conditions, the user can make any move
									prompt = "Would you like to spin again [s], buy a vowel [v], or guess the full phrase [g]? ";
									options = NEXT_MOVE;
								}
								// Get the user's choice based on the available options
								choice = getInput(prompt, options).charAt(0);
							} else {
								// Determine how many letters are in the answer and how many letters are in the phrase variable (how many letters were revealed so far)
								// The number of letters in the answer is not simply the length of the answer because it could contain other characters as well
								int answerLetters = 0;
								int phraseLetters = 0;
								for (int i = 0; i < phrase.length(); i++) {
									String letters = "QWERTYUIOPASDFGHJKLZXCVBNM";
									if (letters.indexOf(answer.charAt(i)) > -1) {
										answerLetters++;
									}
									if (letters.indexOf(phrase.charAt(i)) > -1) {
										phraseLetters++;
									}
								}
								
								String optionsString = "";
								if ((availVowels.length() > 0 && aiScore >= 250) || availConsonants.length() == 0) {
									// AI can buy a vowel if its score is above 250 and there are vowels left to guess
									// However, if there are no consonants left to guess, the AI must be able to buy vowels just like the user
									optionsString += "v";
									
								}
								if (availConsonants.length() > 0) {
									// AI can guess a consonant if there are consonants left to guess
									optionsString += "s";
								} else {
									// Print that there are no consonants left to guess if they just ran out
									System.out.println("There are no consonants left to guess.");
								}
								if (phraseLetters > answerLetters / 2) {
									// AI only begins attempting to guess after over half of the letters are revealed
									// AI is supposed to act like a real player who does not know the answer already
									// This was the purpose of the previous calculations
									// AI is also more likely to guess when over half of the letters are revealed (1/2 chance instead of 1/3 chance)
									optionsString += "gg";
								}
								
								// AI chooses a random option
								char[] options = optionsString.toCharArray();
								choice = options[(int)(Math.random() * options.length)];						
							}
						}
					} else {
						// If the player guessed a consonant unsuccessfully, their turn ends
						turnOver = true;
					}
				}
			} else if (choice == 'v') { // Buying a vowel
				if (!user) {
					System.out.println("AI chooses to buy a vowel.");
				}
				
				// Decrease player score
				if (user) {
					userScore -= 250;
				} else {
					aiScore -= 250;
				}
				
				// Player guesses a vowel
				char vowel;
				if (user) {
					vowel = getInput("Guess a vowel: ", (availVowels + availVowels.toLowerCase()).toCharArray()).toUpperCase().charAt(0);
				} else {
					// AI gueses a random vowel fairly
					vowel = availVowels.charAt((int)(Math.random() * availVowels.length()));
					System.out.println("AI guesses " + vowel + ".");
				}
				
				// Same function is used for both guessing consonants and vowels 
				String[] newData = guessLetter(vowel, answer, consonants, vowels);
				
				// Update the game stats
				phrase = newData[1];
				vowels = newData[3];
				
				// Remove the vowel that was just guessed from the list of available choices
				availVowels = replace(availVowels, availVowels.indexOf(vowel), "");
				
				// Check if the full answer was already revealed
				if (phrase.equals(answer)) {
					if (user) {
						System.out.println("Wait a minute, the full phrase is revealed! You win the game!");
						// End the game
						gameOver = 1;
					} else {
						System.out.println("Wait a minute, the full phrase is revealed! AI wins the game!");
						gameOver = 2;
					}
				}
				
				// End turn
				turnOver = true;
			} else if (choice == 'g') { // Guessing the whole phrase
				if (!user) {
					System.out.println("AI chooses to guess the full phrase.");
				}
				
				// Player guesses the phrase
				String guess;
				if (user) {
					guess = getInput("Guess the phrase: ", new char[0]);
					
					// Check if the guess equals the answer
					if (guess.toLowerCase().equals(answer.toLowerCase())) {
						System.out.println("That is correct! You win the game!");
						// Print the full phrase
						System.out.println("The full phrase was:");
						printPhrase(answer, "", true);
						// End the game
						gameOver = 1;
					} else {
						System.out.println("That is incorrect.");
					}
				} else {
					// AI has a 1 in 2 chance of being correct
					// This is on top of the 1 in 2 chance of choosing to the guess the full phrase in the first place
					// And that is on top of only having the option to guess the full phrase once over half of the letters are revealed
					if ((int)(Math.random() * 2) == 0) {
						System.out.println("AI guesses correctly! AI wins the game!");
						// Print the full phrase if the AI guesses correctly
						System.out.println("The full phrase was:");
						printPhrase(answer, "", true);
						// End the game
						gameOver = 2;
					} else {
						System.out.println("AI guesses incorrectly.");
					}
				}
				
				// End the player's turn in either case
				turnOver = true;
			}
		}
		
		// Print that the player's turn is over
		if (gameOver == 0) {
			if (user) {
				pause("Your turn is over.");
			} else {
				pause("AI's turn is over.");
			}
		} else if (gameOver == 1) {
			// Print a congratulations message if the user won
			pause("Congratulations on being a Wheel of Fortune master!");
		} else {
			// Print a supportive message if the user lost
			pause("Better luck next time!");
		}
		
		// Return updated game stats in one large array
		// Everything gets converted to a string because every element in the array must be the same type
		String[] returnArr = {phrase, Integer.toString(gameOver), Integer.toString(userScore), Integer.toString(aiScore), consonants, vowels, availConsonants, availVowels};
		return returnArr;
	}
	
	// Print the game summary at the start of each turn (category, phrase (with underscores), scores, letters guessed so far, letters remaining)
	public static void gameSummary(String category, String phrase, int userScore, int aiScore, String consonants, String vowels, String availConsonants, String availVowels) {
		// Keep track of the last line that was printed
		String lastString = "| Category: " + category + " |";
		// Print many dashes between lines to separate information
		System.out.println(repeat('-', lastString.length()));
		// Print category
		System.out.println(lastString);
		
		// Print the formatted phrase (each character is in its own box)
		lastString = printPhrase(phrase, lastString, false);
		
		// Print scores
		// Finish strings function adds extra spaces at the end of lines so that the border at the end can line up regardless of line length
		String[] scoreArr = finishStrings("| Your score: $" + userScore, "| AI's score: $" + aiScore);
		// Print line separator, which is the same length as the longer line either directly below or directly above
		// That ensures that there are no gaps in the borders
		// This is why the last printed string is stored
		System.out.println(repeat('-', Math.max(lastString.length(), scoreArr[0].length())));
		lastString = scoreArr[0];
		System.out.println(scoreArr[0]);
		System.out.println(scoreArr[1]);
		
		// Print letters guessed
		// Steps are the same as for printing scores
		String[] lettersArr = finishStrings("| Consonants guessed: " + stringToList(consonants), "| Vowels guessed: " + stringToList(vowels));
		System.out.println(repeat('-', Math.max(lastString.length(), lettersArr[0].length())));
		lastString = lettersArr[0];
		System.out.println(lettersArr[0]);
		System.out.println(lettersArr[1]);
		
		// Print available letters
		String[] availArr = finishStrings("| Consonants available: " + stringToList(availConsonants), "| Vowels available: " + stringToList(availVowels));
		System.out.println(repeat('-', Math.max(lastString.length(), availArr[0].length())));
		lastString = availArr[0];
		System.out.println(availArr[0]);
		System.out.println(availArr[1]);
		System.out.println(repeat('-', lastString.length()));
	}
	
	// Format and print the phrase that the user sees (underscores + each letter is in its own box)
	public static String printPhrase(String phrase, String lastString, boolean bottom) {
		String line = "";
		// Loop through each character in the phrase
		for (int i = 0; i < phrase.length(); i++) {
			line += phrase.charAt(i);
			// Each formatted line must have a minimum of 6 characters from the phrase and end in a space
			// This is so multiple short words in a row appear on the same line
			// There are no such restrictions for the last line because there will not be enough characters left in the phrase to satisfy the conditions
			if ((phrase.charAt(i) == ' ' && line.length() > 6) || i == phrase.length() - 1) {
				String formattedLine = "";
				// Each character from the phrase has a pipe between them
				for (int j = 0; j < line.length(); j++) {
					formattedLine += ("| " + line.charAt(j) + " ");
				}
				// One extra pipe at the end
				formattedLine += "|";
				// Line separator between each line
				// Combined with the pipes, makes it look like each character is in its own box
				System.out.println(repeat('-', Math.max(lastString.length(), formattedLine.length())));
				System.out.println(formattedLine);
				// Must store the last printed string because each line separator takes the max length between the line above it and the line below it
				// Again, this ensures that there are no gaps in the overall outer border
				lastString = formattedLine;
				line = "";
			}
		}
		// When the phrase is printed as part of the game summary, the last line separator is not printed in this function because it is already printed in the game summary function
		// When the phrase is printed on its own (such as right after a letter is successfully guessed), the last line separator must be printed here
		if (bottom) {
			System.out.println(repeat('-', lastString.length()));
		}
		return lastString;
	}
	
	// Print the actual wheel on each spin, always showing the value that was landed on at the top 
	public static void printWheel(String[] WHEEL_VALUES, int spinNum) {
		// Wheel art with numbers as placeholders so that the position of each wheel value can be updated
		String wheel = "             ******||||*****\n"
			+ "          @@@@*     ||     *@@@\n"
			+ "        @@    @*          *@   @@@\n"
			+ "      @@      @* 11111111*@       @\n"
			+ "    @@         @*  1111  *@        @@\n"
			+ "   @@  88888888 @*      *@ 22222222  @\n"
			+ "  @@@@@@@  8888  @*    *@    2222 @@@@@\n"
			+ " ********@@@     @*   *@     @@@@@******\n"
			+ " *       ***@@@@@@*****@@@@@@*****     *\n"
			+ " *          ******     ******          *\n"
			+ " * 777777777777 *       * 333333333333 *\n"
			+ " *         *******     ******          *\n"
			+ " *     ****@@@@@@@*****@@@@@@****      *\n"
			+ "  *****@@@@      @*   *@     @@@@******\n"
			+ "   @@@@          @*    *@        @@@@@@\n"
			+ "   @@  66666666 @*     *@ 44444444   @\n"
			+ "     @   6666  @*       *@  4444   @@\n"
			+ "      @@      @*55555555 *@      @@\n"
			+ "        @@   @*   5555    *@   @@\n"
			+ "          @@@*            *@@@@\n"
			+ "            ***************";
		
		String[] newWheelValues = new String[8];
		int j = 0;
		// Transfers the wheel values array to the new wheel values array, except changing the order so that the value that was landed on is at index 0
		// Loop starts off at the value that was landed on
		for (int i = spinNum; i < WHEEL_VALUES.length; i++) {
			newWheelValues[j] = WHEEL_VALUES[i];
			j++;
		}
		// When the end of the wheel values array is reached, come back to the start and stop right before the value that was landed on
		for (int i = 0; i < spinNum; i++) {
			newWheelValues[j] = WHEEL_VALUES[i];
			j++;
		}
		
		// Each numbered placeholder on the wheel art contains 8 characters in the top line and 4 characters in the bottom line
		// Each value in the wheel values array has different whitespace surrounding it and the same length of 12 (8 + 4)
		// This is so that they can always align nicely within the placeholders
		// However, upon testing, the case where there is a money value on the wedge with placeholder 3 does not look very nice
		// The code below updates the whitespace in that specific condition so that the money value will align closer to the middle of the wedge
		if (newWheelValues[2].trim().length() == 4) {
			newWheelValues[2] = "    " + newWheelValues[2].trim() + "    ";
		} else if (newWheelValues[2].trim().length() == 5) {
			newWheelValues[2] = "    " + newWheelValues[2].trim() + "   ";
		}
		
		// Loop through every character in the wheel art
		// When the current character is a number (placeholder), replace that character with a single character from the corresponding wheel value
		// The positions array keeps track of the next character from each wheel value that will be inserted when another character from its corresponding placeholder is located
		int[] positions = new int[8];
		for (int i = 0; i < wheel.length(); i++) {
			char c = wheel.charAt(i);
			if ("12345678".indexOf(c) > -1) {
				int num = Integer.parseInt(Character.toString(c));
				wheel = replace(wheel, i, Character.toString(newWheelValues[num - 1].charAt(positions[num - 1])));
				positions[num - 1]++;
			}
		}
		
		// Print the newly formatted wheel
		System.out.println(wheel);
	}
	
	// Add extra spaces at the end of two lines so that they will have the same line length
	// Then, add a pipe at the end
	// Used in the game summary function to ensure that all formatted borders are consistent
	public static String[] finishStrings(String one, String two) {
		// Find which string is longer
		int max = Math.max(one.length(), two.length());
		// Add as many spaces at the end of each string as the difference between the current string and the longest string
		// The longer string does not get any extra spaces
		one += (repeat(' ', max - one.length()) + " |");
		two += (repeat(' ', max - two.length()) + " |");
		// Return the strings
		String[] strings = {one, two};
		return strings;
	}
	
	// Insert a comma and a space between every character in a string
	// Used to print list of guessed consonant and vowels in the game summary
	public static String stringToList(String str) {
		String newStr = "";
		// Loop through each character in the string
		for (int i = 0; i < str.length(); i++) {
			// Add the current character to the new string as well as a comma and space at the end
			newStr += str.charAt(i);
			if (i != str.length() - 1) {
				newStr += ", ";
			}
		}
		// Return the new string
		return newStr;
	}

	// Replace the character at a specific index in a string with a new character
	// newChar is technically a string so that it can have length 0 (removal of the character)
	// Used to remove guessed letters from the availConsonants and availVowels variables
	// Also used to replace the number placeholders in the wheel art
	public static String replace(String str, int pos, String newChar) {
		return str.substring(0, pos) + newChar + str.substring(pos + 1);
	}

	// Wait for the user to press any key
	public static void pause(String message) {
		getInput(message + " [enter]", new char[0]);
	}
	
	// Get input from the user, but only accepting specific inputs
	public static String getInput(String prompt, char[] options) {
		String input = "";
		boolean valid = false;
		
		// Keep asking the user for input while their previous input was invalid
		while (!valid) {
			// Don't print "Invalid input" the first time the user enters input
			if (input.length() > 0) {
				System.out.println("Invalid input. Please try again.");
			}
			
			// Print prompt and get input
			System.out.print(prompt);
			input = sc.nextLine();
			
			// If there are no options, accept any input
			if (options.length == 0) {
				valid = true;
			}
			
			// Loop through the valid options and check if the input equals any of them
			for (int i = 0; i < options.length; i++) {
				if (input.length() == 1 && input.charAt(0) == options[i]) {
					valid = true;
					break;
				}
			}
			
		}
		
		return input;
	}
	
	// Handle guessing a letter in the phrase, either from the user or AI, and either a consonant or a vowel
	public static String[] guessLetter(char guess, String answer, String consonants, String vowels) {
		// Update the game stats based on whether the guessed letter is a consonant or a vowel
		if ("AEIOU".indexOf(guess) > -1) {
			vowels += guess;
		} else {
			consonants += guess;
		}
		
		// Find how many of the guessed letter are in the phrase
		int count = 0;
		for (int i = 0; i < answer.length(); i++) {
			if (answer.charAt(i) == guess) {
				count++;
			}
		}
		
		// Determine the new phrase using the new list of guessed letters
		String phrase = generatePhrase(answer, consonants, vowels);
		
		// Print how many of the guessed letter are in the phrase
		if (count > 0) {
			if (count == 1) {
				System.out.println("There is 1 " + guess + " in the phrase!");
			} else {
				System.out.println("There are " + count + " " + guess + "'s in the phrase!");
			}
			printPhrase(phrase, "", true);
		} else {
			System.out.println("There are no " + guess + "'s in the phrase.");
		}
		
		// Return the count of the guessed letter in the phrase, the new phrase itself, and the new lists of guessed consonants and vowels (only one of those is actually updated at a time)
		String[] newData = {Integer.toString(count), phrase, consonants, vowels};
		return newData;
	}
	
	// Replace unguessed letters in the answer with underscores
	public static String generatePhrase(String answer, String consonants, String vowels) {
		String phrase = "";
		
		// Loop through the characters in the phrase and check which ones are letters
		String letters = "QWERTYUIOPASDFGHJKLZXCVBNM";
		for (int i = 0; i < answer.length(); i++) {
			// If the current character is a letter and not already guessed, replace it with an underscore
			char letter = answer.charAt(i);
			if (letters.indexOf(letter) > -1 && consonants.indexOf(letter) == -1 && vowels.indexOf(letter) == -1) {
				phrase += "_";
			} else {
				// If the current character is not a letter or already guessed, transfer it to the new string normally
				phrase += answer.charAt(i);
			}
		}
		
		// Return the phrase that the user sees
		return phrase;
	}
	
	// Repeat a character for a certain number of times
	// Used for formatting the game summary
	public static String repeat(char symbol, int times) {
		String repeated = "";
		// Add the inputted symbol to a string for a specific number of times
		for (int i = 0; i < times; i++) {
			repeated += symbol;
		}
		
		// Return the string
		return repeated;
	}
}
