package rs.ac.bg.etf.diplomski.authenticationapp.models;

import java.util.Date;

public class Transaction {
    private Date date;
    private double amount;
    private String payer;
    private String recipient;
    private TRANSACTION_TYPE type;

    public Transaction() {
    }

    public Transaction(Date date, double amount, String payer, String recipient, TRANSACTION_TYPE type) {
        this.date = date;
        this.amount = amount;
        this.payer = payer;
        this.recipient = recipient;
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

    public String getPayer() {
        return payer;
    }

    public void setPayer(String payer) {
        this.payer = payer;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public TRANSACTION_TYPE getType() {
        return type;
    }

    public void setType(TRANSACTION_TYPE type) {
        this.type = type;
    }
}
