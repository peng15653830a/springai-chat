package com.example.ordersystem.dto;

public class NaturalLanguageRequest {
    private String instruction;

    public NaturalLanguageRequest() {}

    public NaturalLanguageRequest(String instruction) {
        this.instruction = instruction;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }
}