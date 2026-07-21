package com.automobileproject.eap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "shop_pages")
@Getter
@Setter
@ToString(exclude = {"shop"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopPage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false, unique = true)
    private Shop shop;

    @Column(length = 200)
    private String heroTitle;

    @Column(length = 500)
    private String heroSubtitle;

    @Column(columnDefinition = "TEXT")
    private String aboutText;

    @Column(length = 200)
    @Builder.Default
    private String servicesTitle = "Our Services";

    @Column(length = 500)
    private String facebookUrl;

    @Column(length = 500)
    private String instagramUrl;

    @Column(length = 20)
    private String whatsappNumber;

    @Column(nullable = false)
    @Builder.Default
    private boolean published = false;
}
