package ar.daq.aispring.service;

import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private PdfChatClient client;
    private OpenAiChatOptions options;

    public String chat(String prompt, Boolean coupon) {
        if(coupon){
            return client.chat(prompt);
        }else{
            return "Coupon invalid";
        }
    }
}
