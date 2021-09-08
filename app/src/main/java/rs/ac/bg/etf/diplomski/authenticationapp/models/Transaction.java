package rs.ac.bg.etf.diplomski.authenticationapp.models;

import java.util.Date;

public class Transaction {
    private Date date;
    private double amount;
    private String executor;
    private TRANSACTION_TYPE type;

    public Transaction() {
    }

    public Transaction(Date date, double amount, String executor, TRANSACTION_TYPE type) {
        this.date = date;
        this.amount = amount;
        this.executor = executor;
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public TRANSACTION_TYPE getType() {
        return type;
    }

    public void setType(TRANSACTION_TYPE type) {
        this.type = type;
    }
}
