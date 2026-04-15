package kz.railan.villain_lair_api.auth.service;

import kz.railan.villain_lair_api.auth.dto.LoginRequest;
import kz.railan.villain_lair_api.auth.dto.LoginResponse;
import kz.railan.villain_lair_api.auth.dto.RegisterRequest;
import kz.railan.villain_lair_api.auth.dto.RegisterResponse;
import kz.railan.villain_lair_api.common.exception.ConflictException;
import kz.railan.villain_lair_api.common.security.JwtService;
import kz.railan.villain_lair_api.hero.entity.HeroInventoryItem;
import kz.railan.villain_lair_api.hero.entity.HeroProfile;
import kz.railan.villain_lair_api.hero.entity.Weapon;
import kz.railan.villain_lair_api.hero.repository.HeroInventoryItemRepository;
import kz.railan.villain_lair_api.hero.repository.HeroProfileRepository;
import kz.railan.villain_lair_api.hero.repository.WeaponRepository;
import kz.railan.villain_lair_api.lair.entity.Guard;
import kz.railan.villain_lair_api.lair.entity.Lair;
import kz.railan.villain_lair_api.lair.entity.Trap;
import kz.railan.villain_lair_api.lair.repository.GuardRepository;
import kz.railan.villain_lair_api.lair.repository.LairRepository;
import kz.railan.villain_lair_api.lair.repository.TrapRepository;
import kz.railan.villain_lair_api.user.entity.Role;
import kz.railan.villain_lair_api.user.entity.User;
import kz.railan.villain_lair_api.user.repository.UserRepository;
import kz.railan.villain_lair_api.villain.entity.VillainProfile;
import kz.railan.villain_lair_api.villain.repository.VillainProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private static final String STARTER_WEAPON_NAME = "Rusty Blaster";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HeroProfileRepository heroProfileRepository;

    @Autowired
    private WeaponRepository weaponRepository;

    @Autowired
    private HeroInventoryItemRepository heroInventoryItemRepository;

    @Autowired
    private VillainProfileRepository villainProfileRepository;

    @Autowired
    private LairRepository lairRepository;

    @Autowired
    private GuardRepository guardRepository;

    @Autowired
    private TrapRepository trapRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email is already registered");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username is already taken");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setCoins(100);
        User savedUser = userRepository.save(user);

        if (request.role() == Role.HERO) {
            createHeroStarterState(savedUser);
        } else {
            createVillainStarterState(savedUser, request.lairName());
        }

        return new RegisterResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getRole());
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ConflictException("User was authenticated but could not be loaded"));
        return new LoginResponse(jwtService.generateToken(user), "Bearer", user.getRole());
    }

    private void createHeroStarterState(User user) {
        HeroProfile profile = new HeroProfile();
        profile.setUser(user);
        profile.setBaseAttack(25);
        profile.setHealth(100);
        HeroProfile savedProfile = heroProfileRepository.save(profile);

        Weapon weapon = weaponRepository.findByName(STARTER_WEAPON_NAME)
                .orElseThrow(() -> new IllegalStateException("Starter weapon seed is missing"));
        HeroInventoryItem item = new HeroInventoryItem();
        item.setHeroProfile(savedProfile);
        item.setWeapon(weapon);
        item.setQuantity(1);
        item.setEquipped(true);
        heroInventoryItemRepository.save(item);
    }

    private void createVillainStarterState(User user, String requestedLairName) {
        VillainProfile profile = new VillainProfile();
        profile.setUser(user);
        profile.setReputation(0);
        villainProfileRepository.save(profile);

        Lair lair = new Lair();
        lair.setOwnerUser(user);
        lair.setName((requestedLairName == null || requestedLairName.isBlank()) ? user.getUsername() + "'s Lair" : requestedLairName);
        lair.setLevel(1);
        lair.setHealth(100);
        lair.setSecurityLevel(15);
        Lair savedLair = lairRepository.save(lair);

        Guard guard = new Guard();
        guard.setLair(savedLair);
        guard.setName("Starter Guard");
        guard.setPower(10);
        guard.setActive(true);
        guardRepository.save(guard);

        Trap trap = new Trap();
        trap.setLair(savedLair);
        trap.setName("Spike Trap");
        trap.setPower(8);
        trap.setDurability(20);
        trap.setActive(true);
        trapRepository.save(trap);
    }
}
