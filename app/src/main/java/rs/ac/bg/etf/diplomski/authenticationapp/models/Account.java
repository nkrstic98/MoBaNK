package rs.ac.bg.etf.diplomski.authenticationapp.models;

import java.util.List;

public class Account {
    private String number;
    private double balance;
    private String currency;
    private boolean status;
    private List<Transaction> transactions;

    public Account() {
    }

    public Account(String number, double balance, String currency, boolean status, List<Transaction> transactions) {
        this.number = number;
        this.balance = balance;
        this.currency = currency;
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean getStatus() {
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