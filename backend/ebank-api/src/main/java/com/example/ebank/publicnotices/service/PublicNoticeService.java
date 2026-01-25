package com.example.ebank.publicnotices.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.ebank.publicnotices.dto.PublicNoticeDetailResponse;
import com.example.ebank.publicnotices.dto.PublicNoticeItem;
import com.example.ebank.publicnotices.dto.PublicNoticeListResponse;
import com.example.ebank.publicnotices.entity.PublicNotice;
import com.example.ebank.publicnotices.repository.jdbc.PublicNoticeRepositoryJdbc;

@Service
public class PublicNoticeService {

    private final PublicNoticeRepositoryJdbc repo;

    public PublicNoticeService(PublicNoticeRepositoryJdbc repo) {
        this.repo = repo;
    }

    public PublicNoticeListResponse list(Optional<String> category, int limit) {
        List<PublicNoticeItem> items = repo.findVisible(category, limit).stream()
                .map(PublicNoticeService::toItem)
                .toList();
        return new PublicNoticeListResponse(items);
    }

    public PublicNoticeDetailResponse detail(long id) {
        PublicNotice n = repo.findVisibleById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found"));
        return toDetail(n);
    }

    private static PublicNoticeItem toItem(PublicNotice n) {
        return new PublicNoticeItem(
                n.getId(),
                n.getCategory(),
                n.getTitle(),
                n.getSummary(),
                n.getSeverity(),
                n.getStatusLabel(),
                n.getPublishFrom(),
                n.getPublishUntil(),
                n.getRenderProfile(),
                n.getRenderData()
        );
    }

    private static PublicNoticeDetailResponse toDetail(PublicNotice n) {
        return new PublicNoticeDetailResponse(
                n.getId(),
                n.getCategory(),
                n.getTitle(),
                n.getSummary(),
                n.getBodyMarkdown(),
                n.getBodyHtml(),
                n.getSeverity(),
                n.getStatusLabel(),
                n.getPublishFrom(),
                n.getPublishUntil(),
                n.getRenderProfile(),
                n.getRenderData()
        );
    }
}
