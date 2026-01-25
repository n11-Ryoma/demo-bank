package com.example.ebank.publicnotices.dto;

import java.time.OffsetDateTime;

public record PublicNoticeItem(
        long id,
        String category,
        String title,
        String summary,
        String severity,
        String statusLabel,
        OffsetDateTime publishFrom,
        OffsetDateTime publishUntil,
        String renderProfile,
        String renderData
) {}
