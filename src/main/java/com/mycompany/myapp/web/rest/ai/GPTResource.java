package com.mycompany.myapp.web.rest.ai;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import com.mycompany.myapp.service.ai.GPTService;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class GPTResource {

    private final GPTService gptService;

    public GPTResource(GPTService gptService) {
        this.gptService = gptService;
    }

    @PostMapping("/ask")
    public Mono<String> askGPT(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        return gptService.askGPT(prompt);
    }
}