# Aegis

**Lightweight server-side anti-cheat.** By [Aurovion](https://aurovion.club).

Aegis is a fast, dependency-free anti-cheat foundation. It ships three heuristic
checks feeding a shared violation system with staff alerts, VL decay, and a
configurable punishment ladder.

> **Honest scope:** Aegis is heuristic and Bukkit-API only — no packet
> inspection. It reliably catches *blatant* cheating with conservative,
> false-positive-averse thresholds. It is a foundation to build on, not a
> drop-in replacement for packet-level systems like Grim or Vulcan.

## Checks

| Check | What it catches |
|---|---|
| **Speed** | Horizontal movement faster than allowed (Speed-potion & ice aware, buffered) |
| **Fly** | Sustained ascent / hovering while airborne (water, climbables, levitation & slow-fall exempt) |
| **Reach** | Melee hits beyond the eye-to-hitbox distance limit (the hit is cancelled) |

Every flag adds to a per-check **violation level**. VLs decay over time; when a
check crosses its `punish-at`, the configured punishment commands run.

## Compatibility

- **Server:** Paper / Spigot **1.20.x – 1.21.x** (`api-version: 1.20`)
- **Java:** 17+

## Install

1. Drop `Aegis-1.0.0.jar` into `plugins/`.
2. Start once to generate `plugins/Aegis/config.yml`, tune thresholds, `/aegis reload`.
3. Give staff the `aegis.alerts` permission to receive flags.

## Commands & permissions

| Command | Description |
|---|---|
| `/aegis checks` | List checks, enabled state and thresholds |
| `/aegis vl <player>` | Show a player's current violation levels |
| `/aegis alerts` | Toggle receiving alerts (per staff member) |
| `/aegis reload` | Reload the config |

Permissions: `aegis.command`, `aegis.alerts`, `aegis.bypass` (exempt from all checks).

## Tuning

All thresholds live in `config.yml`. If legitimate players are ever flagged,
raise the relevant check's limit or `punish-at`. Set `exempt-ops: true` to skip
checks for operators. `%player%` in `punishments.commands` is replaced with the
offender's name.

## Roadmap ideas

AutoClicker/CPS, KillAura (attack-angle / multi-target), NoFall, Nuker, and
ping-compensated reach — natural next checks on this framework.

## Building from source

```bash
mvn package
```

Output: `target/Aegis-1.0.0.jar`. Requires the PaperMC Maven repo for the
compile-only `paper-api` dependency.

---

© Aurovion. Provided as-is.
