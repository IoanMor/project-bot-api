package me.ivanmorozov.telegrambot.service;

import org.springframework.stereotype.Service;

@Service
public class CalculatorService {
    public int add (int a, int b){
        return a+b;
    }
    public int divide(int a, int b) {
        if (b == 0) throw new IllegalArgumentException("Divider cannot be zero");
        return a / b;
    }
}
