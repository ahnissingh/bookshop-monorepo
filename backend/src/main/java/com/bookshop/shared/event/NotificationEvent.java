package com.bookshop.shared.event;

import lombok.Builder;

import java.util.Map;

@Builder
public record NotificationEvent(
        String eventId,
        String eventType,
        String recipient,
        Map<String, Object> payload
) {}