# Zombie vs Human Simulation

A Java Swing simulation matching the mockup: humans wander a map dotted
with buildings (obstacles) and safe zones; at night, zombies wake up and
chase any human within their visibility radius using grid-based BFS
pathfinding to route around buildings. During the day zombies freeze.
A cure tool lets you click a zombie that's adjacent to a human to try to
convert it back.

## How to run

Requires a JDK (21 recommended, but 11+ should work).

```bash
cd src
javac *.java
java Main
```

## Class overview

| Class | Role |
|---|---|
| `Entity` | Abstract superclass — position, radius, `update()`, `getColor()` |
| `Human` | Wanders; flees toward nearest safe zone if a zombie is visible at night |
| `Zombie` | Frozen by day; at night, chases the nearest visible human via `Pathfinder` |
| `Building` | Rectangular obstacle — blocks movement and pathfinding |
| `SafeZone` | Rectangular area humans flee toward |
| `Pathfinder` | Grid-based BFS pathfinding around buildings |
| `Simulation` | Owns all entities/state, advances the world each tick, exposes query methods (`isWalkable`, `findNearestHuman`, etc.) |
| `SimulationPanel` | JPanel — draws the world, runs the animation `Timer`, handles cure-tool mouse clicks |
| `ControlPanel` | Sidebar — live stats, cycle indicator, cure tool button, play/pause, speed cycling |
| `Main` | Assembles the JFrame (title bar, sidebar, canvas, status bar) |

## Controls

- **Play / Pause** — starts or stops the simulation clock.
- **Speed: 1x / 2x / 4x** — cycles simulation speed.
- **Arm cure tool** then click a zombie that has a human nearby — attempts a cure.

## Things you could extend

- Zombies currently "infect" nothing — add a chance for a zombie to convert
  a human it touches, to make Night phases actually threaten the human count.
- The day/night length, visibility radius, and cure radius are constants
  at the top of `Simulation.java` — easy to tune.
- Swap the BFS pathfinder for A* (add a heuristic) if you want zombies to
  path around larger obstacle fields more efficiently.
