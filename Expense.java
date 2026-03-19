public class Expense {

    private String title;
    private String category;
    private double amount;
    private String date;
    private String description;

    public Expense(String title, String category, double amount, String date, String description) {
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }
}