package com.example.ebank.integration.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


@RestController
@RequestMapping("/api/integration")
public class InternalHttpClientController {
	private static final Logger log = LogManager.getLogger(InternalHttpClientController.class.getName());
	@GetMapping("/fetch")
	public String fetchUrl(
            @RequestParam String url,
            @RequestHeader(value = "X-Api-Version", required = false) String apiVersion) {

		
        // ★ Log4Shell 用の入口：ここに攻撃文字列を入れる
        if (apiVersion != null) {
            log.info("X-Api-Version: {}", apiVersion);
        }
        log.info("request to "+url);
	    return new RestTemplate().getForObject(url, String.class);
	    
	}
}
