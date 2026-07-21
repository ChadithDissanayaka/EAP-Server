package com.automobileproject.eap.service.impl;

import com.automobileproject.eap.dto.request.ShopRegistrationRequestDTO;
import com.automobileproject.eap.dto.response.ShopResponseDTO;
import com.automobileproject.eap.entity.*;
import com.automobileproject.eap.exception.DuplicateEntryException;
import com.automobileproject.eap.exception.EntryNotFoundException;
import com.automobileproject.eap.mapper.ShopMapper;
import com.automobileproject.eap.repo.ShopPageRepo;
import com.automobileproject.eap.repo.ShopRepo;
import com.automobileproject.eap.repo.UserRepo;
import com.automobileproject.eap.service.EmailService;
import com.automobileproject.eap.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopServiceImpl implements ShopService {

    private final ShopRepo shopRepo;
    private final ShopPageRepo shopPageRepo;
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final ShopMapper shopMapper;
    private final EmailService emailService;

    @Override
    @Transactional
    public ShopResponseDTO registerShop(ShopRegistrationRequestDTO dto) {
        log.info("Registering new shop: {}", dto.getShopName());

        // Validate uniqueness
        if (userRepo.findByUsername(dto.getUsername()).isPresent()) {
            throw new DuplicateEntryException("Username already exists: " + dto.getUsername());
        }
        if (userRepo.findByEmail(dto.getEmail()).isPresent()) {
            throw new DuplicateEntryException("Email already registered: " + dto.getEmail());
        }

        // Generate slug from shop name
        String slug = generateSlug(dto.getShopName());

        // 1. Create shop owner user (without shop reference initially)
        User owner = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(ROLE_TYPES.SHOP_OWNER)
                .phoneNumber(dto.getPhoneNumber())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .emailVerified(false)
                .isActive(true)
                .build();

        User savedOwner = userRepo.save(owner);

        // 2. Create shop
        Shop shop = Shop.builder()
                .name(dto.getShopName())
                .slug(slug)
                .description(dto.getShopDescription())
                .address(dto.getShopAddress())
                .phoneNumber(dto.getShopPhoneNumber())
                .email(dto.getShopEmail())
                .subscriptionPlan(dto.getSubscriptionPlan())
                .status(SHOP_STATUS_TYPES.PENDING_APPROVAL)
                .owner(savedOwner)
                .build();

        Shop savedShop = shopRepo.save(shop);

        // 3. Link owner to shop
        savedOwner.setShop(savedShop);
        userRepo.save(savedOwner);

        // 4. Create default shop page
        ShopPage page = ShopPage.builder()
                .shop(savedShop)
                .heroTitle("Welcome to " + dto.getShopName())
                .heroSubtitle("Quality automotive service you can trust")
                .aboutText("We provide professional automotive services.")
                .servicesTitle("Our Services")
                .published(false)
                .build();
        shopPageRepo.save(page);

        // 5. Send verification email
        String verificationToken = UUID.randomUUID().toString();
        savedOwner.setVerificationToken(verificationToken);
        userRepo.save(savedOwner);

        try {
            emailService.sendVerificationEmail(savedOwner.getEmail(), verificationToken);
            log.info("Verification email sent to shop owner: {}", savedOwner.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to shop owner", e);
        }

        log.info("Shop registered successfully: {} (slug: {}, status: PENDING_APPROVAL)", savedShop.getName(), slug);
        return shopMapper.toResponseDTO(savedShop);
    }

    @Override
    @Transactional(readOnly = true)
    public ShopResponseDTO getShopById(UUID shopId) {
        Shop shop = shopRepo.findById(shopId)
                .orElseThrow(() -> new EntryNotFoundException("Shop not found with ID: " + shopId));
        return shopMapper.toResponseDTO(shop);
    }

    @Override
    @Transactional(readOnly = true)
    public ShopResponseDTO getShopBySlug(String slug) {
        Shop shop = shopRepo.findBySlug(slug)
                .orElseThrow(() -> new EntryNotFoundException("Shop not found with slug: " + slug));
        return shopMapper.toResponseDTO(shop);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopResponseDTO> getAllShops() {
        return shopRepo.findAll().stream()
                .map(shopMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopResponseDTO> getShopsByStatus(String status) {
        SHOP_STATUS_TYPES statusType = SHOP_STATUS_TYPES.valueOf(status.toUpperCase());
        return shopRepo.findByStatus(statusType).stream()
                .map(shopMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public ShopResponseDTO approveShop(UUID shopId) {
        Shop shop = shopRepo.findById(shopId)
                .orElseThrow(() -> new EntryNotFoundException("Shop not found with ID: " + shopId));

        shop.setStatus(SHOP_STATUS_TYPES.ACTIVE);
        shop.setApprovedAt(LocalDateTime.now());
        Shop saved = shopRepo.save(shop);
        log.info("Shop approved and activated: {}", shop.getName());
        return shopMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public ShopResponseDTO activateShop(UUID shopId) {
        Shop shop = shopRepo.findById(shopId)
                .orElseThrow(() -> new EntryNotFoundException("Shop not found with ID: " + shopId));

        shop.setStatus(SHOP_STATUS_TYPES.ACTIVE);
        Shop saved = shopRepo.save(shop);
        log.info("Shop activated: {}", shop.getName());
        return shopMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public ShopResponseDTO deactivateShop(UUID shopId) {
        Shop shop = shopRepo.findById(shopId)
                .orElseThrow(() -> new EntryNotFoundException("Shop not found with ID: " + shopId));

        shop.setStatus(SHOP_STATUS_TYPES.DEACTIVATED);
        Shop saved = shopRepo.save(shop);
        log.info("Shop deactivated: {}", shop.getName());
        return shopMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public ShopResponseDTO suspendShop(UUID shopId) {
        Shop shop = shopRepo.findById(shopId)
                .orElseThrow(() -> new EntryNotFoundException("Shop not found with ID: " + shopId));

        shop.setStatus(SHOP_STATUS_TYPES.SUSPENDED);
        Shop saved = shopRepo.save(shop);
        log.info("Shop suspended: {}", shop.getName());
        return shopMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public ShopResponseDTO upgradePlan(UUID shopId, String plan) {
        Shop shop = shopRepo.findById(shopId)
                .orElseThrow(() -> new EntryNotFoundException("Shop not found with ID: " + shopId));

        SUBSCRIPTION_PLAN_TYPES planType = SUBSCRIPTION_PLAN_TYPES.valueOf(plan.toUpperCase());
        shop.setSubscriptionPlan(planType);
        Shop saved = shopRepo.save(shop);
        log.info("Shop plan upgraded to {}: {}", planType, shop.getName());
        return shopMapper.toResponseDTO(saved);
    }

    @Override
    public long countByStatus(String status) {
        SHOP_STATUS_TYPES statusType = SHOP_STATUS_TYPES.valueOf(status.toUpperCase());
        return shopRepo.findByStatus(statusType).size();
    }

    @Override
    public long countAll() {
        return shopRepo.count();
    }

    private String generateSlug(String shopName) {
        String baseSlug = shopName.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        String slug = baseSlug;
        int counter = 1;
        while (shopRepo.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        return slug;
    }

    @Override
    @Transactional
    public ShopResponseDTO updateSlug(UUID shopId, String slug) {
        Shop shop = shopRepo.findById(shopId)
                .orElseThrow(() -> new EntryNotFoundException("Shop not found with ID: " + shopId));

        if (!slug.equals(shop.getSlug()) && shopRepo.existsBySlug(slug)) {
            throw new DuplicateEntryException("That portal link is already taken: " + slug);
        }

        shop.setSlug(slug);
        Shop saved = shopRepo.save(shop);
        log.info("Shop portal slug updated to '{}': {}", slug, shop.getName());
        return shopMapper.toResponseDTO(saved);
    }
}
