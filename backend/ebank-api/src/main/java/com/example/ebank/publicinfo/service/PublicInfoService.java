package com.example.ebank.publicinfo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.ebank.publicinfo.dto.AtmLocation;
import com.example.ebank.publicinfo.dto.AtmSearchResponse;
import com.example.ebank.publicinfo.dto.FaqItem;
import com.example.ebank.publicinfo.dto.FaqSearchItem;
import com.example.ebank.publicinfo.dto.FaqSearchResponse;
import com.example.ebank.publicinfo.dto.FeeItem;
import com.example.ebank.publicinfo.dto.FeesResponse;
import com.example.ebank.publicinfo.dto.FxRateItem;
import com.example.ebank.publicinfo.dto.FxRatesResponse;
import com.example.ebank.publicinfo.dto.InterestCalcResponse;
import com.example.ebank.publicinfo.dto.LoanCalcResponse;
import com.example.ebank.publicinfo.dto.NewsDetail;
import com.example.ebank.publicinfo.dto.NewsItem;
import com.example.ebank.publicinfo.dto.NewsListResponse;
import com.example.ebank.publicinfo.dto.RateItem;
import com.example.ebank.publicinfo.dto.RatesResponse;
import com.example.ebank.publicinfo.dto.SecurityAlertItem;
import com.example.ebank.publicinfo.dto.SecurityAlertsResponse;
import com.example.ebank.publicinfo.repository.jdbc.AtmRepositoryJdbc;
import com.example.ebank.publicinfo.repository.jdbc.FaqRepositoryJdbc;
import com.example.ebank.publicinfo.repository.jdbc.FeesRepositoryJdbc;
import com.example.ebank.publicinfo.repository.jdbc.FxRepositoryJdbc;
import com.example.ebank.publicinfo.repository.jdbc.NewsRepositoryJdbc;
import com.example.ebank.publicinfo.repository.jdbc.RatesRepositoryJdbc;
import com.example.ebank.publicinfo.repository.jdbc.SecurityAlertRepositoryJdbc;

@Service
public class PublicInfoService {

    private final AtmRepositoryJdbc atmRepository;
    private final RatesRepositoryJdbc ratesRepository;
    private final FeesRepositoryJdbc feesRepository;
    private final FxRepositoryJdbc fxRepository;
    private final NewsRepositoryJdbc newsRepository;
    private final SecurityAlertRepositoryJdbc securityRepository;
    private final FaqRepositoryJdbc faqRepository;

    public PublicInfoService(AtmRepositoryJdbc atmRepository,
                             RatesRepositoryJdbc ratesRepository,
                             FeesRepositoryJdbc feesRepository,
                             FxRepositoryJdbc fxRepository,
                             NewsRepositoryJdbc newsRepository,
                             SecurityAlertRepositoryJdbc securityRepository,
                             FaqRepositoryJdbc faqRepository) {
        this.atmRepository = atmRepository;
        this.ratesRepository = ratesRepository;
        this.feesRepository = feesRepository;
        this.fxRepository = fxRepository;
        this.newsRepository = newsRepository;
        this.securityRepository = securityRepository;
        this.faqRepository = faqRepository;
    }

    public AtmSearchResponse searchAtms(String pref, boolean openNow, boolean cash, String q,
                                        String service, String sort, String order, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = clamp(size, 1, 100);

        AtmRepositoryJdbc.SearchResult result = atmRepository.search(
                pref, openNow, cash, q, service, sort, order, safePage, safeSize);

        for (AtmLocation item : result.getItems()) {
            item.setMapLink(mapLink(item.getLat(), item.getLng()));
        }

        String sortToken = (sort == null || sort.isBlank()) ? "name" : sort.toLowerCase(Locale.ROOT);
        if (order != null && !order.isBlank()) {
            sortToken = sortToken + ":" + order.toLowerCase(Locale.ROOT);
        }

        return new AtmSearchResponse(result.getItems(), safePage, safeSize, result.getTotal(), sortToken);
    }

    public RatesResponse getRates(String category) {
        List<RateItem> items = ratesRepository.findRates(category);
        return new RatesResponse(nowIso(), items);
    }

    public FeesResponse getFees(String service) {
        List<FeeItem> items = feesRepository.findFees(service);
        return new FeesResponse(nowIso(), items);
    }

    public FxRatesResponse getFxRates(String base) {
        Map<String, Double> fxToJpy = fxRepository.loadFxToJpy();
        String normalizedBase = (base == null || base.isBlank()) ? "JPY" : base.toUpperCase(Locale.ROOT);
        if (!fxToJpy.containsKey(normalizedBase)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unsupported base currency: " + normalizedBase);
        }

        double baseToJpy = fxToJpy.get(normalizedBase);
        List<FxRateItem> items = new ArrayList<>();
        for (Map.Entry<String, Double> entry : fxToJpy.entrySet()) {
            String quote = entry.getKey();
            if (quote.equals(normalizedBase)) {
                continue;
            }
            double quoteToJpy = entry.getValue();
            double rate = quoteToJpy / baseToJpy;
            items.add(new FxRateItem(normalizedBase, quote, round(rate, 6)));
        }

        items.sort(Comparator.comparing(FxRateItem::getQuote));
        return new FxRatesResponse(nowIso(), normalizedBase, items);
    }

    public NewsListResponse listNews(String category, String q, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = clamp(size, 1, 50);

        NewsRepositoryJdbc.SearchResult result = newsRepository.list(category, q, safePage, safeSize);
        List<NewsItem> pageItems = result.getItems().stream()
                .map(item -> new NewsItem(item.getId(), item.getCategory(), item.getTitle(),
                        item.getSummary(), item.getPublishedAt(), item.getUpdatedAt()))
                .toList();

        return new NewsListResponse(pageItems, safePage, safeSize, result.getTotal());
    }

    public NewsDetail getNewsDetail(String id) {
        NewsDetail detail = newsRepository.findById(id);
        if (detail == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "news not found");
        }
        return detail;
    }

    public SecurityAlertsResponse listSecurityAlerts(String tag, int limit) {
        int safeLimit = clamp(limit, 1, 50);
        List<SecurityAlertItem> items = securityRepository.list(tag, safeLimit);
        return new SecurityAlertsResponse(nowIso(), items);
    }

    public FaqSearchResponse searchFaq(String q, String category, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = clamp(size, 1, 50);
        String query = q == null ? "" : q.trim();

        FaqRepositoryJdbc.SearchResult result = faqRepository.search(query, category, safePage, safeSize);
        List<FaqSearchItem> pageItems = result.getItems().stream()
                .map(item -> new FaqSearchItem(
                        item.getId(),
                        item.getCategory(),
                        item.getQuestion(),
                        item.getAnswer(),
                        buildHighlights(item, query)))
                .toList();

        return new FaqSearchResponse(pageItems, safePage, safeSize, result.getTotal());
    }

    public InterestCalcResponse calcInterest(long principal, double ratePercent, int days) {
        if (principal <= 0 || ratePercent < 0 || days <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "principal, rate, days must be positive");
        }
        double interest = principal * (ratePercent / 100.0) * (days / 365.0);
        double total = principal + interest;
        return new InterestCalcResponse(principal, ratePercent, days, round(interest, 2), round(total, 2));
    }

    public LoanCalcResponse calcLoan(long amount, double ratePercent, int months) {
        if (amount <= 0 || ratePercent < 0 || months <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "amount, rate, months must be positive");
        }

        double monthlyRate = ratePercent / 100.0 / 12.0;
        double monthlyPayment;
        if (monthlyRate == 0) {
            monthlyPayment = amount / (double) months;
        } else {
            double pow = Math.pow(1 + monthlyRate, months);
            monthlyPayment = amount * (monthlyRate * pow) / (pow - 1);
        }

        double totalPayment = monthlyPayment * months;
        double totalInterest = totalPayment - amount;
        return new LoanCalcResponse(amount, ratePercent, months,
                round(monthlyPayment, 2), round(totalPayment, 2), round(totalInterest, 2));
    }

    private boolean containsIgnoreCase(String text, String term) {
        if (text == null || term == null) {
            return false;
        }
        return text.toLowerCase(Locale.ROOT).contains(term.toLowerCase(Locale.ROOT));
    }

    private List<String> buildHighlights(FaqItem item, String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        List<String> highlights = new ArrayList<>();
        String term = query.toLowerCase(Locale.ROOT);
        if (containsIgnoreCase(item.getQuestion(), term)) {
            highlights.add(snippet(item.getQuestion(), term));
        }
        if (containsIgnoreCase(item.getAnswer(), term)) {
            highlights.add(snippet(item.getAnswer(), term));
        }
        return highlights;
    }

    private String snippet(String text, String term) {
        String lower = text.toLowerCase(Locale.ROOT);
        int idx = lower.indexOf(term.toLowerCase(Locale.ROOT));
        if (idx < 0) {
            return text;
        }
        int start = Math.max(0, idx - 8);
        int end = Math.min(text.length(), idx + term.length() + 8);
        return text.substring(start, end);
    }

    private int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private double round(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    private String nowIso() {
        return OffsetDateTime.now().toString();
    }

    private String mapLink(double lat, double lng) {
        return "https://maps.google.com/?q=" + lat + "," + lng;
    }
}
