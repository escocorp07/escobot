package main.java.appeals;

public enum AppealStatus {
    denied("denied"),
    waiting("waiting"),
    approved("approved");

    public String status;
    AppealStatus(String status) {
        this.status=status;
    }
    public static AppealStatus parseStatus(String name) {
        for(AppealStatus s : AppealStatus.values()) {
            if(s.toString().equals(name))
                return s;
        }
        return waiting;
    }
}
