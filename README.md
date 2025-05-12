# Vardoger
A Spigot plugin for tracking player attention — unlock commands and progress by looking at signs.

![Vardoger Demo](vardogerdemo.gif)

## Features
- Detects when players are looking at signs
- Tracks time spent focusing per sign
- Defines groups of signs with goal durations
- Executes commands when a group is completed

## Use Cases
- Rule walls that players must *actually* read
- Educational worlds or quests
- Unlockable achievements based on observation

## How it works
Signs are defined in `groups.yml`. Each group has sign IDs and a required gaze duration.
The plugin monitors player gaze using ray tracing and updates progress.

## Example groups.yml

```yaml
rules:
  requiredDuration: 5
  onComplete:
    - 'tellraw {player} {"text":"Thank you for reading the rules! \u2006\u2764","italic":true,"color":"yellow"}'
    - tell {player} completed groups can also run multiple commands!
  signs:
    rule1:
      world: world
      x: -63
      y: 112
      z: 86
    rule2:
      world: world
      x: -63
      y: 112
      z: 85
```