package com.sovereign.domain.user.entity;

import com.sovereign.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
public class UserSettings extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String currency = "USD";

    @Column(nullable = false)
    private String locale = "en-US";

    @Column(nullable = false)
    private String timezone = "UTC";

    @Column(nullable = false)
    private String theme = "system";

    @Column(nullable = false)
    private boolean notificationsEnabled = true;
}