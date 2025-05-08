package com.mycompany.myapp.web.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class GoldPriceProxyResource {

    private static final String EXTERNAL_API_URL = "https://edge-api.pnj.io/ecom-frontend/v1/get-gold-price";

    @GetMapping("/gold-price")
    public ResponseEntity<String> proxyGoldPrice(@RequestParam String zone) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = EXTERNAL_API_URL + "?zone=" + zone;
            String response = restTemplate.getForObject(url, String.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error proxying gold price API: " + e.getMessage());
        }
    }
}
