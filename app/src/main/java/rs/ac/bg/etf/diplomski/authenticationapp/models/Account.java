package rs.ac.bg.etf.diplomski.authenticationapp.models;

import java.util.List;

public class Account {
    private String number;
    private double balance;
    private String type;
    private boolean status;
    private List<Transaction> transactions;

    public Account(String number, double balance, String type, boolean status, List<Transaction> transactions) {
        this.number = number;
        this.balance = balance;
        this.type = type;
        this.status = status;
        this.transactions = transactions;
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

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}