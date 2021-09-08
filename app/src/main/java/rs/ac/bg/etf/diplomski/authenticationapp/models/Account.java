package rs.ac.bg.etf.diplomski.authenticationapp.models;

public class Account {
    private String number;
    private double balance;
    private String type;
    private boolean status;

    public Account() {
    }

    public Account(String number, double balance, String type, boolean status) {
        this.number = number;
        this.balance = balance;
        this.type = type;
        this.status = status;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}