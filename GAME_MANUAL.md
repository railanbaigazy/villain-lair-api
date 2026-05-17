# Villain Lair: Game Manual

Heroes plunder villain lairs for coins. Villains fortify their lairs and survive the raids. Every action costs or earns coins; every battle is permanent.

Base URL: `http://localhost:8080`  
Interactive docs: `http://localhost:8080/swagger-ui.html`

---

## Table of Contents

1. [Getting Started](#1-getting-started)
2. [Authentication](#2-authentication)
3. [Playing as a Hero](#3-playing-as-a-hero)
4. [Playing as a Villain](#4-playing-as-a-villain)
5. [Combat: How Battles Work](#5-combat--how-battles-work)
6. [Shop](#6-shop)
7. [Lairs: Public Information](#7-lairs--public-information)
8. [Event Log](#8-event-log)
9. [Economy Reference](#9-economy-reference)
10. [Quick-Reference Card](#10-quick-reference-card)

---

## 1. Getting Started

### Pick a Role

| Role    | Goal                                                    |
| ------- | ------------------------------------------------------- |
| HERO    | Attack villain lairs, loot coins, level up weapons      |
| VILLAIN | Build a fortress, fend off heroes, grow your reputation |

You cannot switch roles. Choose carefully.

### Register an Account

```
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "ironclad",
  "email": "ironclad@example.com",
  "password": "secret123",
  "role": "HERO"
}
```

For villains, you may optionally name your lair:

```json
{
  "username": "doomvault",
  "email": "doom@example.com",
  "password": "secret123",
  "role": "VILLAIN",
  "lairName": "Citadel of Doom"
}
```

If `lairName` is omitted, it defaults to `"<username>'s Lair"`.

### Starting State

Every new account receives **100 coins** as a starter bonus.

**Hero starting state:**

- Base attack: **25**
- Health: **100**
- Inventory: Basic Sword equipped (attack bonus +10, durability 25)

**Villain starting state:**

- Lair level: **1**, health: **100/100**, security level: **15**
- Defense units: Starter Guard (power 10) + Spike Trap (power 8, durability 4)
- Reputation: 0

---

## 2. Authentication

All endpoints except `/api/v1/auth/**` and Swagger require a `Bearer` token.

### Login

```
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "ironclad@example.com",
  "password": "secret123"
}
```

Response:

```json
{
  "token": "eyJhbGci...",
  "type": "Bearer",
  "role": "HERO"
}
```

### Using the Token

Include the token in every subsequent request:

```
Authorization: Bearer eyJhbGci...
```

Tokens expire after 60 minutes by default. Log in again to get a fresh token.

---

## 3. Playing as a Hero

### Check Your Profile

```
GET /api/v1/heroes/me
Authorization: Bearer <token>
```

Returns your username, coin balance, base attack, health, and equipped weapon.

### View Your Inventory

```
GET /api/v1/heroes/me/inventory
Authorization: Bearer <token>
```

Lists all weapons you own with their current durability and equipped status.

### Equip a Weapon

You must have a working weapon equipped before you can attack.

```
POST /api/v1/heroes/me/inventory/{inventoryItemId}/equip
Authorization: Bearer <token>
```

- Only one weapon can be equipped at a time. Equipping a new one automatically unequips the current one.
- A weapon with 0 durability cannot be equipped. Buy a replacement from the shop.

### Attack a Lair

```
POST /api/v1/heroes/attacks
Authorization: Bearer <token>
Content-Type: application/json

{
  "targetLairId": 42
}
```

The target lair must have ACTIVE status and health > 0.

**What happens:**

1. Your attack power and the lair's defense are calculated (see [Combat](#5-combat--how-battles-work)).
2. If you win: you earn coins and the lair takes damage.
3. If you lose: the villain earns coins, you gain nothing, and **you take damage**.
4. Your weapon loses 1 durability regardless of outcome.
5. All active traps in the lair lose 1 durability.

**Battle response (hero win example):**

```json
{
  "battleId": 17,
  "result": "HERO_WIN",
  "finalHeroPower": 38,
  "finalLairDefense": 31,
  "damageDealt": 17,
  "coinsRewardedToHero": 50,
  "coinsRewardedToVillain": 0,
  "lairHealthAfter": 83,
  "heroDamageReceived": 0,
  "heroHealthAfter": 100,
  "message": "Attack succeeded and the lair took damage."
}
```

**Battle response (hero loss example):**

```json
{
  "battleId": 18,
  "result": "VILLAIN_WIN",
  "finalHeroPower": 31,
  "finalLairDefense": 40,
  "damageDealt": 0,
  "coinsRewardedToHero": 0,
  "coinsRewardedToVillain": 30,
  "lairHealthAfter": 100,
  "heroDamageReceived": 14,
  "heroHealthAfter": 86,
  "message": "Attack failed and the villain held the line."
}
```

### View Your Consumables (Potions)

```
GET /api/v1/heroes/me/inventory/consumables
Authorization: Bearer <token>
```

Lists all consumable items you own. Each entry includes the consumable `id` you need for the use endpoint, the item name, heal amount, and remaining quantity.

### Use a Consumable (Heal)

```
POST /api/v1/heroes/me/inventory/consumables/{consumableItemId}/use
Authorization: Bearer <token>
```

Restores HP equal to the item's heal amount (Health Potion: +30 HP), capped at 100. Subtracts 1 quantity; the entry is removed from inventory when quantity hits 0. Cannot be used at full health.

Response:

```json
{
  "consumableId": 3,
  "itemName": "Health Potion",
  "healAmount": 30,
  "heroHealthAfter": 100
}
```

### View Your Battle History

```
GET /api/v1/heroes/me/attacks
Authorization: Bearer <token>
```

Returns all battles you have participated in, newest first.

### View a Specific Battle

```
GET /api/v1/battles/{battleId}
Authorization: Bearer <token>
```

Only participants (hero or villain) can view a battle's details.

### Hero Strategy Tips

- **Always keep a spare weapon.** When your equipped weapon hits 0 durability during an attack, it is automatically unequipped and you cannot attack again until you equip another one.
- **Stock potions before tough raids.** Losing a battle now costs you health. Buy a Health Potion (40 coins, +30 HP) from the shop before attacking heavily fortified lairs.
- **Scout before attacking.** Use `GET /api/v1/lairs/{lairId}` to see a lair's security level and defense units before committing.
- **Target low-level lairs first.** Higher lair levels give better coin rewards but also have stronger defenses.
- **Upgrade your weapon.** The Basic Sword (+10) will struggle against fortified lairs. Spend coins on an Iron Sword or Plasma Blade from the shop.

---

## 4. Playing as a Villain

### Check Your Profile

```
GET /api/v1/villains/me
Authorization: Bearer <token>
```

Returns your username, coin balance, reputation, and lair summary.

### View Your Lair (Full Detail)

```
GET /api/v1/villains/me/lair
Authorization: Bearer <token>
```

Shows health, max health, security level, status, and all defense units (guards, traps, defense weapons) with their active state.

### Rename Your Lair

```
PATCH /api/v1/villains/me/lair
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "The Black Fortress"
}
```

### Upgrade Security

Increases your lair's security level by 1. Security level contributes directly to your defense score.

```
POST /api/v1/villains/me/lair/upgrade-security
Authorization: Bearer <token>
```

- Cost: `25 + (lairLevel × 10)` coins
- Maximum security level: **20**
- Also increments your lair's level by 1

| Lair Level | Upgrade Cost |
| ---------- | ------------ |
| 1          | 35 coins     |
| 2          | 45 coins     |
| 5          | 75 coins     |
| 10         | 125 coins    |

### Repair Your Lair

Restores 25 HP to your lair. Required after successful hero attacks.

```
POST /api/v1/villains/me/lair/repair
Authorization: Bearer <token>
```

- Cost: `15 + (lairLevel × 5)` coins
- Restores exactly **25 health** (capped at max health)
- If your lair was BREACHED (health = 0), repairing it restores its ACTIVE status

| Lair Level | Repair Cost |
| ---------- | ----------- |
| 1          | 20 coins    |
| 2          | 25 coins    |
| 5          | 40 coins    |
| 10         | 65 coins    |

### Buy Defense Units from the Shop

See [Shop](#6-shop) for villain items. Purchasing a guard, trap, or defense weapon automatically adds it to your lair.

### View Your Defense History

```
GET /api/v1/villains/me/lair/attacks
Authorization: Bearer <token>
```

Lists all incoming attacks on your lair, newest first.

### Villain Strategy Tips

- **Defense score = security level + sum of all active guard power + sum of all active trap power + sum of all active defense weapon power.**
- **Traps wear out.** Each trap loses 1 durability per incoming attack. Buy replacements from the shop before yours hit 0.
- **Guards never expire.** They are a permanent, stackable defense investment.
- **Repair immediately after a breach.** A BREACHED lair (health = 0) cannot be attacked, but you also earn no defense rewards until it is ACTIVE again.
- **Stack before upgrading security.** Buying multiple guards is often cheaper per power point than paying for security upgrades at higher levels.
- **Defense Turrets are the best single unit.** 14 power for 90 coins vs Guard's 8 power for 50 coins or Trap's 7 power for 45 coins - turrets win on raw power per coin, but they have limited durability (6 uses).

---

## 5. Combat: How Battles Work

### Power Calculation

```
Hero Power  = heroBaseAttack + equippedWeapon.attackBonus + random(-3..+3)
Lair Defense = securityLevel + Σ(activeGuardPower) + Σ(activeTrapPower) + Σ(activeDefenseWeaponPower) + random(-3..+3)
```

Both values are floored at 0. The random modifier is drawn independently for hero and lair each battle - there is no way to predict or control it.

### Outcome

| Condition                  | Result      |
| -------------------------- | ----------- |
| Hero Power >= Lair Defense | HERO_WIN    |
| Hero Power < Lair Defense  | VILLAIN_WIN |

### Rewards

**Hero wins:**

- Hero earns: `40 + (lairLevel × 10)` coins
- Lair takes damage: `10 + max(0, finalHeroPower - finalLairDefense)` HP
- Villain earns: 0 coins

**Villain wins (hero loses):**

- Villain earns: `20 + (lairLevel × 5)` coins
- No damage dealt to lair
- Hero earns: 0 coins
- Hero takes damage: `5 + max(0, finalLairDefense - finalHeroPower)` HP

### Durability Wear

- Hero's equipped weapon: loses 1 durability per attack (regardless of outcome)
- Villain's active traps: each loses 1 durability per incoming attack (regardless of outcome)
- When weapon or trap durability reaches 0: it is deactivated (weapon is unequipped; trap is set inactive)

### Lair Breach

When lair health reaches 0, the lair becomes **BREACHED**:

- Status changes from ACTIVE to BREACHED
- Breached lairs cannot be attacked
- The villain earns no defense rewards while breached
- Repair the lair to restore ACTIVE status

---

## 6. Shop

### Browse Items

```
GET /api/v1/shop/items
Authorization: Bearer <token>
```

Returns all active shop items filtered to your role. Heroes see weapons; villains see defense units.

### Buy an Item

```
POST /api/v1/shop/items/{itemId}/buy
Authorization: Bearer <token>
```

- Deducts the item's price from your coin balance
- Insufficient coins → 402 error
- **Heroes (weapon):** added to weapon inventory; auto-equipped if nothing usable is currently equipped
- **Heroes (potion):** added to consumable inventory; stacks if you already own the same item
- **Villains:** defense unit is automatically added to your lair and set to active

### Hero Weapons

| Item         | Price     | Attack Bonus | Durability | Notes                      |
| ------------ | --------- | ------------ | ---------- | -------------------------- |
| Basic Sword  | 0 coins   | +10          | 25 uses    | Given free at signup       |
| Iron Sword   | 60 coins  | +18          | 40 uses    | Best mid-game option       |
| Plasma Blade | 140 coins | +30          | 55 uses    | Strongest weapon available |

### Hero Consumables

| Item          | Price    | Heal Amount | Notes                           |
| ------------- | -------- | ----------- | ------------------------------- |
| Health Potion | 40 coins | +30 HP      | Single use; stacks in inventory |

Use via `POST /api/v1/heroes/me/inventory/consumables/{id}/use`. Cannot be used at full health (100 HP).

### Villain Defense Units

| Item           | Price    | Defense Power | Durability | Notes                        |
| -------------- | -------- | ------------- | ---------- | ---------------------------- |
| Guard          | 50 coins | +8            | Permanent  | Never expires; stacks freely |
| Trap           | 45 coins | +7            | 4 uses     | Expires; needs replacement   |
| Defense Turret | 90 coins | +14           | 6 uses     | High power; limited uses     |

---

## 7. Lairs: Public Information

Anyone (hero or villain) can browse lairs to scout targets or check the leaderboard.

### List All Lairs

```
GET /api/v1/lairs
Authorization: Bearer <token>
```

Returns all lairs with their owner, level, security level, and status. BREACHED lairs are shown but cannot be attacked.

### View a Specific Lair

```
GET /api/v1/lairs/{lairId}
Authorization: Bearer <token>
```

Returns the public lair detail: name, owner, level, health, security level, status, and a list of defense units (type and power are visible - heroes can plan their attack accordingly).

---

## 8. Event Log

The game records every significant action as an event. You can query recent events to see game history.

```
GET /api/v1/events
Authorization: Bearer <token>
```

Optional filter:

```
GET /api/v1/events?type=AttackCompletedEvent
```

Returns the last 100 events. Available event types:

| Type                        | Triggered by                   |
| --------------------------- | ------------------------------ |
| `UserRegisteredEvent`       | New account created            |
| `ItemPurchasedEvent`        | Shop purchase completed        |
| `WeaponEquippedEvent`       | Hero equips a weapon           |
| `LairSecurityUpgradedEvent` | Villain upgrades lair security |
| `LairRepairedEvent`         | Villain repairs lair damage    |
| `AttackCompletedEvent`      | Battle resolved                |

---

## 9. Economy Reference

### How Coins Work

- Coins are awarded and deducted atomically - you can never go below 0.
- All transactions are recorded and visible in the event log.
- There is no daily reward or passive income; coins come from battle victories and must be spent wisely.

### Transaction Types

| Type               | Direction | Trigger                         |
| ------------------ | --------- | ------------------------------- |
| STARTER_BONUS      | Credit    | Account registration            |
| ITEM_PURCHASE      | Debit     | Shop purchase                   |
| ATTACK_WIN_REWARD  | Credit    | Hero wins a battle              |
| DEFENSE_WIN_REWARD | Credit    | Villain successfully defends    |
| LAIR_UPGRADE       | Debit     | Security upgrade or lair repair |

### Coin Flows at a Glance

```
Hero wins:
  Hero  +40 + (lairLevel × 10)
  Villain  0

Villain wins:
  Hero     0
  Villain  +20 + (lairLevel × 5)

Security upgrade:
  Villain  −(25 + lairLevel × 10)

Lair repair:
  Villain  −(15 + lairLevel × 5)   →  +25 HP
```

---

## 10. Quick-Reference Card

### Hero Cheat Sheet

```
Register           POST /api/v1/auth/register             { role: "HERO" }
Login              POST /api/v1/auth/login
My profile         GET  /api/v1/heroes/me
My weapons         GET  /api/v1/heroes/me/inventory
Equip weapon       POST /api/v1/heroes/me/inventory/{id}/equip
My consumables     GET  /api/v1/heroes/me/inventory/consumables
Use consumable     POST /api/v1/heroes/me/inventory/consumables/{id}/use
Scout a lair       GET  /api/v1/lairs/{lairId}
List lairs         GET  /api/v1/lairs
Attack             POST /api/v1/battles/attacks           { targetLairId: N }
My battles         GET  /api/v1/heroes/me/attacks
Battle detail      GET  /api/v1/battles/{battleId}
Browse shop        GET  /api/v1/shop/items
Buy item           POST /api/v1/shop/items/{id}/buy
Event log          GET  /api/v1/events
```

### Villain Cheat Sheet

```
Register         POST /api/v1/auth/register       { role: "VILLAIN", lairName: "..." }
Login            POST /api/v1/auth/login
My profile       GET  /api/v1/villains/me
My lair          GET  /api/v1/villains/me/lair
Rename lair      PUT  /api/v1/villains/me/lair    { name: "..." }
Upgrade security POST /api/v1/villains/me/lair/upgrade-security
Repair lair      POST /api/v1/villains/me/lair/repair
Incoming attacks GET  /api/v1/villains/me/lair/attacks
Browse shop      GET  /api/v1/shop/items
Buy defense unit POST /api/v1/shop/items/{id}/buy
Event log        GET  /api/v1/events
```

### At-a-Glance Formulas

```
Hero Power        = 25 (base) + weapon bonus + rand(-3..+3)
Lair Defense      = securityLevel + guardPower + trapPower + turretPower + rand(-3..+3)
Hero Win          = heroPower >= lairDefense

Hero reward       = 40 + (lairLevel × 10) coins   (on win)
Villain reward    = 20 + (lairLevel × 5) coins    (on win)
Lair damage       = 10 + max(0, heroPower − lairDefense) HP  (on hero win)
Hero damage       = 5 + max(0, lairDefense − heroPower) HP   (on hero loss)

Health Potion     = +30 HP, max 100, 40 coins

Upgrade cost      = 25 + (lairLevel × 10) coins  → +1 security, +1 level
Repair cost       = 15 + (lairLevel × 5) coins   → +25 HP
```
