package com.rikkeibankproject.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "kyc_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    private String address;

    @Column(nullable = false, unique = true)
    private String identityCard;

    @Column(nullable = false)
    private String frontCardUrl;

    @Column(nullable = false)
    private String backCardUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus status;

    @OneToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;
}
