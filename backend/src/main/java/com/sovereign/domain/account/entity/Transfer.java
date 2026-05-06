package com.sovereign.domain.account.entity;

import com.sovereign.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "transfers")
@Getter
@Setter
public class Transfer extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "debit_transaction_id")
    private Transaction debitTransaction;

    @OneToOne
    @JoinColumn(name = "credit_transaction_id")
    private Transaction creditTransaction;

    @Column(nullable = false)
    private boolean userConfirmed = false;

    @Column(nullable = false)
    private boolean dismissed = false;

    @Column(nullable = false)
    private boolean autoDetected = true;
}