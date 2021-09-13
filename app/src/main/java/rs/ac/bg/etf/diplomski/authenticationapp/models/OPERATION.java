package rs.ac.bg.etf.diplomski.authenticationapp.models;

public enum OPERATION {
    DO_NOTHING,
    SET_EMAIL,
    SET_PASSWORD,
    SET_NEW_PIN,
    CONFIRM_NEW_PIN,
    REGISTER_NEW_PIN,
    SET_FINGERPRINT,
    DELETE_ACCOUNT,
    EXCHANGE_OFFICE,
    INTERNAL_TRANSFER,
    EXTERNAL_PAYMENT
}
