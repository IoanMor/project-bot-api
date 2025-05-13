package me.ivanmorozov.common.exception;

public class StockAlreadyExist extends RuntimeException{
    public StockAlreadyExist(String msg) {
        super(msg);
    }
}
