package kz.railan.villain_lair_api.auth.service;

import kz.railan.villain_lair_api.auth.dto.RegisterRequest;
import kz.railan.villain_lair_api.auth.dto.RegisterResponse;
import kz.railan.villain_lair_api.common.config.KafkaTopicConfig;
import kz.railan.villain_lair_api.common.exception.ConflictException;
import kz.railan.villain_lair_api.common.security.JwtService;
import kz.railan.villain_lair_api.economy.service.EconomyService;
import kz.railan.villain_lair_api.event.KafkaEventPublisher;
import kz.railan.villain_lair_api.hero.entity.HeroProfile;
import kz.railan.villain_lair_api.hero.entity.Weapon;
import kz.railan.villain_lair_api.hero.repository.HeroInventoryItemRepository;
import kz.railan.villain_lair_api.hero.repository.HeroProfileRepository;
import kz.railan.villain_lair_api.hero.repository.WeaponRepository;
import kz.railan.villain_lair_api.lair.repository.GuardRepository;
import kz.railan.villain_lair_api.lair.repository.LairRepository;
import kz.railan.villain_lair_api.lair.repository.TrapRepository;
import kz.railan.villain_lair_api.user.entity.Role;
import kz.railan.villain_lair_api.user.entity.User;
import kz.railan.villain_lair_api.user.repository.UserRepository;
import kz.railan.villain_lair_api.villain.repository.VillainProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserRepository userRepository;
    @Mock HeroProfileRepository heroProfileRepository;
    @Mock WeaponRepository weaponRepository;
    @Mock HeroInventoryItemRepository heroInventoryItemRepository;
    @Mock VillainProfileRepository villainProfileRepository;
    @Mock LairRepository lairRepository;
    @Mock GuardRepository guardRepository;
    @Mock TrapRepository trapRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtService jwtService;
    @Mock EconomyService economyService;
    @Mock KafkaEventPublisher eventPublisher;

    @InjectMocks
    AuthService authService;

    @Test
    void register_heroRole_createsHeroProfile() {
        RegisterRequest request = new RegisterRequest("hero1", "hero1@test.com", "password1", Role.HERO, null);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("hero1");
        savedUser.setRole(Role.HERO);
        savedUser.setCoins(0);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        HeroProfile savedProfile = new HeroProfile();
        savedProfile.setId(1L);
        when(heroProfileRepository.save(any(HeroProfile.class))).thenReturn(savedProfile);

        Weapon weapon = new Weapon();
        weapon.setDurability(25);
        when(weaponRepository.findByCode("BASIC_SWORD")).thenReturn(Optional.of(weapon));

        RegisterResponse result = authService.register(request);

        assertEquals(Role.HERO, result.role());
        assertEquals("hero1", result.username());
        verify(heroProfileRepository).save(any(HeroProfile.class));
        verify(lairRepository, never()).save(any());
        verify(eventPublisher).publish(eq(KafkaTopicConfig.USER_REGISTERED), anyString(), any());
    }

    @Test
    void register_villainRole_createsVillainProfileAndLair() {
        RegisterRequest request = new RegisterRequest("villain1", "villain1@test.com", "password1", Role.VILLAIN, "Dark Lair");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("villain1");
        savedUser.setRole(Role.VILLAIN);
        savedUser.setCoins(0);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        when(lairRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegisterResponse result = authService.register(request);

        assertEquals(Role.VILLAIN, result.role());
        verify(villainProfileRepository).save(any());
        verify(lairRepository).save(any());
        verify(heroProfileRepository, never()).save(any());
        verify(eventPublisher).publish(eq(KafkaTopicConfig.USER_REGISTERED), anyString(), any());
    }

    @Test
    void register_duplicateEmail_throwsConflict() {
        RegisterRequest request = new RegisterRequest("hero1", "dup@test.com", "password1", Role.HERO, null);
        when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
    }

    @Test
    void register_duplicateUsername_throwsConflict() {
        RegisterRequest request = new RegisterRequest("taken", "new@test.com", "password1", Role.HERO, null);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
    }
}
