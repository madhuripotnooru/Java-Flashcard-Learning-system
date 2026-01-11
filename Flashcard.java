import java.util.Objects;

public class Flashcard {
    private String front;
    private String back;
    private int box; // Leitner box: 1..5

    public Flashcard(String front, String back) {
        this.front = front;
        this.back = back;
        this.box = 1;
    }

    public Flashcard(String front, String back, int box) {
        this.front = front;
        this.back = back;
        this.box = Math.max(1, Math.min(5, box));
    }

    public String getFront() { return front; }
    public String getBack() { return back; }
    public int getBox() { return box; }

    public void markCorrect() {
        if (box < 5) box++;
    }

    public void markIncorrect() {
        if (box > 1) box--;
    }

    public void setFront(String front) { this.front = front; }
    public void setBack(String back) { this.back = back; }
    public void setBox(int box) { this.box = Math.max(1, Math.min(5, box)); }

    @Override
    public String toString() {
        return "Flashcard{" + "front='" + front + '\'' + ", back='" + back + '\'' + ", box=" + box + '}';
    }

    // CSV helper: escape pipe and newline if needed
    public String toCSVLine() {
        String f = front.replace("|", "¦").replace("\n", " ");
        String b = back.replace("|", "¦").replace("\n", " ");
        return f + "|" + b + "|" + box;
    }

    public static Flashcard fromCSVLine(String line) {
        String[] parts = line.split("\\|", 3);
        if (parts.length < 3) return null;
        String f = parts[0].replace("¦", "|");
        String b = parts[1].replace("¦", "|");
        int box = 1;
        try { box = Integer.parseInt(parts[2]); } catch (NumberFormatException ignored) {}
        return new Flashcard(f, b, box);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Flashcard)) return false;
        Flashcard fc = (Flashcard) o;
        return Objects.equals(front, fc.front) && Objects.equals(back, fc.back);
    }

    @Override
    public int hashCode() {
        return Objects.hash(front, back);
    }
}
