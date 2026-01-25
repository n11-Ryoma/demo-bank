package com.example.ebank.publicnotices.dto;

import java.util.List;

public record PublicNoticeListResponse(
        List<PublicNoticeItem> items
) {}
