# Zombie Apocalypse Simulation — Java / Swing

A tile-based simulation of humans surviving a zombie outbreak, built to match
the full design spec: 4-directional A* pathfinding, day/night zombie freezing,
sleep-meter-driven shelter seeking, a fixed cure supply, and a play/pause +
speed-cycling HUD.

## Requirements

- JDK 17 or newer (uses only `javax.swing` / `java.awt` — no external
  dependencies, nothing to download).

## How to compile & run

From the project root:

```bash
javac -d bin src/com/zombiesim/*.java
java -cp bin com.zombiesim.Main
```

A window opens and the simulation starts running immediately (unpaused, as
specified).

## Adding the icons

The spec calls for six 1000×1000px transparent PNGs from Canva. Drop them
into `resources/icons/` with these exact filenames and they'll be loaded and
pre-scaled to tile size automatically:

| Purpose         | File               |
|------------------|--------------------|
| Human            | `Human_Icon.png`   |
| Zombie           | `Zombie_Icon.png`  |
| Cure pickup      | `Syringe_Icon.png` |
| Shelter          | `House_Icon.png`   |
| Blocked terrain  | `Blocked_Icon.png` |
| Dead marker      | `Dead_Icon.png`    |

If a file is missing, `IconManager` just returns `null` for it and
`SimulationPanel` falls back to a simple colored circle/square, so the sim
runs fine with none, some, or all of the icons in place — nothing needs to
change in code.

## Project layout

```
src/com/zombiesim/
  Position.java        grid coordinate
  Terrain.java          LAND / SHELTER / BLOCKED
  Grid.java              2D terrain array + dead-marker overlay + shelter occupancy
  AStar.java             4-directional A*, Manhattan heuristic, per-species passability
  Agent.java             shared position/path/movement base class
  Human.java             wander / flee / seek-shelter / sleep / cure-hunt state machine
  Zombie.java             frozen (day) / wander / chase state machine
  Cure.java, CureManager.java   fixed-supply cure spawning, pickup, consumption
  SimClock.java           sim time, day/night phase, 0.5x-4x speed cycling, pause
  SimulationEngine.java   ties it all together: tick loop, contact resolution, win check
  IconManager.java        loads + pre-scales the PNG icons once
  PhaseIndicator.java     small sun/moon glyph for the HUD
  SimulationPanel.java    rendering + the real-time game loop (Swing Timer)
  HUDPanel.java           clock, day/night indicator, population counters, status bar
  ControlPanel.java       Play/Pause and Speed buttons
  MainFrame.java          assembles everything; grid size / population / cure counts
                          are configurable constants at the top of this file
  Main.java               entry point
```

## Tuning

All the "configurable variables" from the spec live as named constants at the
top of `MainFrame.java` (grid rows/cols, starting human/zombie counts, total
cure supply, blocked-tile count, shelter cluster count) and
`SimulationEngine.java` (movement speeds, sleep-drain rates, vision radii, day
and night length). Nothing needs code restructuring to adjust — just change
the constant values.

## Notes on a couple of judgment calls

The spec doesn't fully pin down two edge cases, so here's what this
implementation does:

- **Cure contact ambiguity** — when a cure-carrying human and a zombie land on
  the same tile in the same tick, "who reached whom first" is inferred from
  the zombie's state that tick: if it was actively `CHASE`-ing, it's treated
  as having reached the human (human self-cures); otherwise the human (who
  was in its own hunting state) is treated as having reached the zombie (the
  zombie is cured).
- **Cures remaining counter** — counts total supply minus cures actually
  *used*, so a cure sitting on the ground still counts as "remaining" until
  someone uses it, which matches how the HUD counter is described.
