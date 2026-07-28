package com.sovereign.domain.plaid.entity;

import com.sovereign.common.entity.BaseEntity;
import com.sovereign.common.enums.SyncStatus;
import com.sovereign.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "plaid_items")
@Getter
@Setter
public class PlaidItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "item_id", nullable = false, unique = true)
    private String itemId;

    // NOTE: in production, encrypt this at rest
    @Column(name = "access_token", nullable = false)
    private String accessToken;

    private String institutionId;
    private String institutionName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SyncStatus syncStatus = SyncStatus.SUCCESS;

    private LocalDateTime lastSyncedAt;

    // cursor for incremental /transactions/sync calls
    @Column(name = "sync_cursor")
    private String syncCursor;
}