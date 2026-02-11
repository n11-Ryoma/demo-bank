package com.example.ebank.publicinfo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ebank.publicinfo.dto.AtmSearchResponse;
import com.example.ebank.publicinfo.dto.FaqSearchResponse;
import com.example.ebank.publicinfo.dto.FeesResponse;
import com.example.ebank.publicinfo.dto.FxRatesResponse;
import com.example.ebank.publicinfo.dto.InterestCalcResponse;
import com.example.ebank.publicinfo.dto.LoanCalcResponse;
import com.example.ebank.publicinfo.dto.NewsDetail;
import com.example.ebank.publicinfo.dto.NewsListResponse;
import com.example.ebank.publicinfo.dto.RatesResponse;
import com.example.ebank.publicinfo.dto.SecurityAlertsResponse;
import com.example.ebank.publicinfo.service.PublicInfoService;

@RestController
@RequestMapping("/api")
public class PublicInfoController {

    private final PublicInfoService service;

    public PublicInfoController(PublicInfoService service) {
        this.service = service;
    }

    @GetMapping("/atm")
    public AtmSearchResponse searchAtm(
            @RequestParam(required = false) String pref,
            @RequestParam(name = "open_now", defaultValue = "0") int openNow,
            @RequestParam(name = "cash", defaultValue = "0") int cash,
            @RequestParam(required = false) String q,
            @RequestParam(required = false, name = "service") String serviceName,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        boolean openNowFlag = openNow == 1;
        boolean cashFlag = cash == 1;
        return service.searchAtms(pref, openNowFlag, cashFlag, q, serviceName, sort, order, page, size);
    }

    @GetMapping("/rates")
    public RatesResponse getRates(@RequestParam(required = false) String category) {
        return service.getRates(category);
    }

    @GetMapping("/fees")
    public FeesResponse getFees(@RequestParam(required = false, name = "service") String serviceName) {
        return service.getFees(serviceName);
    }

    @GetMapping("/fx")
    public FxRatesResponse getFx(@RequestParam(required = false) String base) {
        return service.getFxRates(base);
    }

    @GetMapping("/news")
    public NewsListResponse listNews(
            @RequestParam(required = false) String category,
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.listNews(category, query, page, size);
    }

    @GetMapping("/news/{id}")
    public NewsDetail getNewsDetail(@PathVariable String id) {
        return service.getNewsDetail(id);
    }

    @GetMapping("/security-alerts")
    public SecurityAlertsResponse listSecurityAlerts(
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "10") int limit) {
        return service.listSecurityAlerts(tag, limit);
    }

    @GetMapping("/faq")
    public FaqSearchResponse searchFaq(
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.searchFaq(query, category, page, size);
    }

    @GetMapping("/calc/interest")
    public InterestCalcResponse calcInterest(
            @RequestParam long principal,
            @RequestParam double rate,
            @RequestParam int days) {
        return service.calcInterest(principal, rate, days);
    }

    @GetMapping("/calc/loan")
    public LoanCalcResponse calcLoan(
            @RequestParam long amount,
            @RequestParam double rate,
            @RequestParam int months) {
        return service.calcLoan(amount, rate, months);
    }
}


