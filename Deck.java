import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;

public class Deck {
    private List<Flashcard> cards = new ArrayList<>();
    private Random random = new Random();

    public void addCard(Flashcard card) {
        if (!cards.contains(card)) cards.add(card);
    }

    public void removeCard(Flashcard card) {
        cards.remove(card);
    }

    public int size() { return cards.size(); }

    public Map<Integer, Long> statsByBox() {
        return cards.stream().collect(Collectors.groupingBy(Flashcard::getBox, Collectors.counting()));
    }

    public List<Flashcard> getCardsInBox(int box) {
        return cards.stream().filter(c -> c.getBox() == box).collect(Collectors.toList());
    }

    // Weighted pick (lower box = higher chance)
    public Flashcard pickCardForStudy() {
        if (cards.isEmpty()) return null;
        List<Flashcard> weighted = new ArrayList<>();
        for (Flashcard c : cards) {
            int weight = 6 - c.getBox();
            for (int i = 0; i < weight; i++) weighted.add(c);
        }
        return weighted.get(random.nextInt(weighted.size()));
    }

    // pick excluding seen set
    public Flashcard pickCardForStudyExcluding(Set<Flashcard> exclude) {
        if (cards.isEmpty()) return null;
        List<Flashcard> weighted = new ArrayList<>();
        for (Flashcard c : cards) {
            if (exclude != null && exclude.contains(c)) continue;
            int weight = 6 - c.getBox();
            for (int i = 0; i < weight; i++) weighted.add(c);
        }
        if (weighted.isEmpty()) return null;
        return weighted.get(random.nextInt(weighted.size()));
    }

    // Robust save: create parent folder, use TRUNCATE_EXISTING and flush
    public void saveToFile(String path) throws IOException {
        Path p = Paths.get(path).toAbsolutePath();
        // ensure parent exists
        Path parent = p.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        // Open writer with create + truncate
        try (BufferedWriter writer = Files.newBufferedWriter(p,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {
            for (Flashcard c : cards) {
                writer.write(c.toCSVLine());
                writer.newLine();
            }
            writer.flush();
        }
    }

    public void loadFromFile(String path) throws IOException {
        Path p = Paths.get(path).toAbsolutePath();
        cards.clear();
        if (!Files.exists(p)) return;
        try (BufferedReader br = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                Flashcard c = Flashcard.fromCSVLine(line);
                if (c != null) cards.add(c);
            }
        }
    }

    public List<Flashcard> getAll() { return new ArrayList<>(cards); }
}
