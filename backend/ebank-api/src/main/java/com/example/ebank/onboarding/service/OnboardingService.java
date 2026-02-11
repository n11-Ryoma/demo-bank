package com.example.ebank.onboarding.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ebank.accounts.repository.jdbc.AccountRepositoryJdbc;
import com.example.ebank.auth.repository.jdbc.UserRepositoryJdbc;
import com.example.ebank.model.TransactionType;
import com.example.ebank.user.entity.UserProfile;
import com.example.ebank.onboarding.dto.OpenAccountRequest;
import com.example.ebank.onboarding.dto.OpenAccountResponse;
import com.example.ebank.user.repository.jdbc.UserProfileRepositoryJdbc;

@Service
public class OnboardingService {

    private final UserRepositoryJdbc userRepository;
    private final UserProfileRepositoryJdbc profileRepository;
    private final AccountRepositoryJdbc accountRepository;

    public OnboardingService(
            UserRepositoryJdbc userRepository,
            UserProfileRepositoryJdbc profileRepository,
            AccountRepositoryJdbc accountRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public OpenAccountResponse openAccount(OpenAccountRequest req) {
        // 1) username繝ｦ繝九・繧ｯ繝√ぉ繝・け
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username already exists: " + req.getUsername());
        }

        // 2) users INSERT 竊・userId蜿門ｾ・
        Long userId = userRepository.createUser(req.getUsername(), req.getPassword(), req.getEmail());

        // 3) user_profile INSERT
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setNameKanji(req.getNameKanji());
        profile.setNameKana(req.getNameKana());
        profile.setBirthDate(req.getBirthDate());
        profile.setGender(req.getGender());
        profile.setPhone(req.getPhone());
        profile.setPostalCode(req.getPostalCode());
        profile.setAddress(req.getAddress());
        profile.setMyNumber(req.getMyNumber());
        profileRepository.insert(profile);


        // 4) accounts INSERT 竊・accountId/蜿｣蠎ｧ逡ｪ蜿ｷ/謾ｯ蠎励さ繝ｼ繝芽ｿ斐☆
        var account = accountRepository.createMainAccountForUser(userId); // Account霑泌唆迚医′讌ｽ

        // 5) 髢玖ｨｭ縺ｮ蜿門ｼ募ｱ･豁ｴ繧・莉ｶ蜈･繧後ｋ・磯橿陦後▲縺ｽ縺包ｼ・        // type 縺ｯ varchar(16) 縺ｪ縺ｮ縺ｧ "OPEN" 縺ｪ縺ｩ遏ｭ縺・枚蟄怜・縺ｫ縺吶ｋ
        accountRepository.insertTransaction(
                account.getId(),
                TransactionType.OPEN,
                0L,
                account.getBalance(),
                null,
                "蜿｣蠎ｧ髢玖ｨｭ"
        );

        // 6) 繝ｬ繧ｹ繝昴Φ繧ｹ
        OpenAccountResponse res = new OpenAccountResponse();
        res.setUserId(userId);
        res.setAccountId(account.getId());
        res.setBranchCode(account.getBranchCode());
        res.setAccountNumber(account.getAccountNumber());
        res.setBalance(account.getBalance());
        res.setMessage("Account opened");
        return res;
    }
}




