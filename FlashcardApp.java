import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.HashSet;
import java.nio.file.Paths;

public class FlashcardApp {
    private static final String DATA_FILE = "flashcards.csv";

    public static void main(String[] args) {
        Deck deck = new Deck();
        // try load
        try {
            deck.loadFromFile(DATA_FILE);
            System.out.println("Loaded " + deck.size() + " cards from " + Paths.get(DATA_FILE).toAbsolutePath());
        } catch (IOException e) {
            System.out.println("No saved deck found. Starting fresh. (Will save to " + Paths.get(DATA_FILE).toAbsolutePath() + ")");
        }

        Scanner sc = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n--- Flashcard App ---");
            System.out.println("1) Add card");
            System.out.println("2) Study");
            System.out.println("3) List cards");
            System.out.println("4) Stats");
            System.out.println("5) Save & Exit");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.print("Front: ");
                    String front = sc.nextLine().trim();
                    System.out.print("Back: ");
                    String back = sc.nextLine().trim();
                    deck.addCard(new Flashcard(front, back));
                    System.out.println("Added.");
                    // auto-save immediately after add
                    try {
                        deck.saveToFile(DATA_FILE);
                        System.out.println("Auto-saved to " + Paths.get(DATA_FILE).toAbsolutePath());
                    } catch (IOException e) {
                        System.err.println("Auto-save failed: " + e.getMessage());
                    }
                    break;
                case "2":
                    studySession(deck, sc);
                    break;
                case "3":
                    int i = 1;
                    for (Flashcard c : deck.getAll()) {
                        System.out.println(i++ + ") [" + c.getBox() + "] " + c.getFront() + " -> " + c.getBack());
                    }
                    break;
                case "4":
                    Map<Integer, Long> stats = deck.statsByBox();
                    for (int b = 1; b <= 5; b++) {
                        System.out.println("Box " + b + ": " + stats.getOrDefault(b, 0L));
                    }
                    break;
                case "5":
                    try {
                        deck.saveToFile(DATA_FILE);
                        System.out.println("Saved to " + Paths.get(DATA_FILE).toAbsolutePath());
                    } catch (IOException e) {
                        System.err.println("Failed to save: " + e.getMessage());
                    }
                    running = false;
                    break;
                default:
                    System.out.println("Unknown choice.");
            }
        }
        sc.close();
    }

    // studySession: shows each card once and then returns to menu
    private static void studySession(Deck deck, Scanner sc) {
        if (deck.size() == 0) {
            System.out.println("No cards. Add some first.");
            return;
        }
        System.out.println("Study mode — press Enter to show answer, then y/n if you got it right.");
        System.out.println("You can also press 'q' after seeing answer to quit early.");

        Set<Flashcard> seenThisCycle = new HashSet<>();
        boolean keepStudying = true;

        while (keepStudying) {
            Flashcard card = deck.pickCardForStudyExcluding(seenThisCycle);

            if (card == null) {
                System.out.println("\nCycle complete. Returning to main menu.");
                break;
            }

            seenThisCycle.add(card);

            System.out.println("\nFront: " + card.getFront());
            System.out.print("(press Enter to see answer) ");
            sc.nextLine();
            System.out.println("Back: " + card.getBack());
            System.out.print("Did you answer correctly? (y/n or q to quit study): ");
            String ans = sc.nextLine().trim().toLowerCase();
            if (ans.equals("y")) {
                card.markCorrect();
                System.out.println("Great! Moved to box " + card.getBox());
            } else if (ans.equals("n")) {
                card.markIncorrect();
                System.out.println("No worries. Moved to box " + card.getBox());
            } else if (ans.equals("q")) {
                System.out.println("Quitting study early. Returning to main menu.");
                break;
            } else {
                System.out.println("Unknown input, returning to menu.");
                break;
            }
        }

        // auto-save progress after study
        try {
            deck.saveToFile(DATA_FILE);
            System.out.println("Progress auto-saved to " + Paths.get(DATA_FILE).toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Auto-save after study failed: " + e.getMessage());
        }
    }
}
