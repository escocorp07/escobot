package main.java.appeals;

public enum AppealStatus {
    denied("Denied"),
    accepted("Accepted"),
    waiting("Waiting.");

    public String status;
    AppealStatus(String status) {
        this.status=status;
    }
}
