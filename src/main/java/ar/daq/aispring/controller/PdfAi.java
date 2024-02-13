package ar.daq.aispring.controller;

import ar.daq.aispring.request.ChatRequest;
import ar.daq.aispring.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PdfAi {
    private ChatService chatService;

    @GetMapping("/chat")
    public ResponseEntity pdf(@RequestBody ChatRequest request) {

        Boolean coupon = this.checkCoupon(request.getCoupon());
        return ResponseEntity.ok(chatService.chat(request.getPrompt(), coupon));
    }

    private Boolean checkCoupon(String coupon) {
        return true;
    }
}
