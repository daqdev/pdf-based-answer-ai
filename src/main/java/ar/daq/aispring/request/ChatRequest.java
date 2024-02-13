package ar.daq.aispring.request;


import lombok.Data;

@Data
public class ChatRequest {

    String prompt;
    String key;
    String coupon;

}

