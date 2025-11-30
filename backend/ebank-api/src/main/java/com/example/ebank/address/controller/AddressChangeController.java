package com.example.ebank.address.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ebank.address.dto.AddressChangeCommitRequest;
import com.example.ebank.address.dto.AddressChangeResponse;
import com.example.ebank.address.dto.CurrentAddressResponse;
import com.example.ebank.address.service.AddressChangeService;
import com.example.ebank.address.service.AddressQueryService;
import com.example.ebank.auth.jwt.JwtUtil;

@RestController
@RequestMapping("/api/address-change")
public class AddressChangeController {

    private final AddressChangeService service;
    private final AddressQueryService queryService;
    private final JwtUtil jwtUtil;

    public AddressChangeController(AddressChangeService service,
                                   AddressQueryService queryService,
                                   JwtUtil jwtUtil) {
        this.service = service;
        this.queryService = queryService;
        this.jwtUtil = jwtUtil;
    }
    
    @GetMapping("/current")
    public ResponseEntity<CurrentAddressResponse> getCurrentAddress(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        CurrentAddressResponse current = queryService.getCurrentAddress(userId);
        if (current == null) {
            // まだ住所が登録されていない場合
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(current);
    }
    
    @PostMapping("/commit")
    public ResponseEntity<AddressChangeResponse> commit(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody AddressChangeCommitRequest req) {

        // Authorization チェック
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // JWT 抽出
        String token = authHeader.substring(7);

        // userId を JWT から取り出す (必要なので必須)
        Long userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Service に投げる
        AddressChangeResponse res = service.commit(userId, req);

        return ResponseEntity.ok(res);
    }
}
