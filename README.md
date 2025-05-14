# Vardoger

**Vardoger** is a Minecraft (Paper) plugin that tracks where players look — unlocking commands and triggering progression by watching signs.

![Vardoger demo](https://raw.githubusercontent.com/cutelilreno/Vardoger/main/.github/assets/vardoger-demo.gif)

---

## ✨ Features

* Tracks players' gaze using ray tracing
* Measures how long a sign is looked at
* Stores progress per player in `playerdata/<uuid>.json`
* Organises signs by groups
* Executes a defined list of commands when individual signs or full groups are completed
* Optional mod visibility when players have seen enough of a group (`spyThreshold`)
* Supports MiniMessage via internal `@print` for colourful, rich feedback

---

## 🎮 Use Cases

* Gamified rule walls
* Lore-based unlocks and attention puzzles
* Passive quest progression
* Staff tools to quietly track attention (e.g., rules or instructions)

---

## ⚙ How It Works

Tracked signs and groups are defined in a single file: `groups.yml`.

Each group can define:

* `requiredDuration`: How long to look at each sign (in ticks)
* `onComplete`: The command list that's run when completed
* `signCooldown`: Optional cooldown before retriggering a sign's commands (in seconds)
* `spyThreshold`: Optional — sends a message to staff when a player reaches this threshold of signs seen

Note: `onComplete` can be set for both groups and individual signs. It runs once when completed — except for signs with `signCooldown`, which can re-trigger after the cooldown.

---

## 📄 Example `groups.yml`

```yaml
rules:
  requiredDuration: 5
  signCooldown: 60
  spyThreshold: 0.3
  onComplete:
    - 'tellraw {player} {"text":"Thank you for reading the rules! ❤","italic":true,"color":"yellow"}'
  signs:
    rule1:
      world: world
      x: -63
      y: 112
      z: 86
      onComplete:
        - 'tell {player} hey'
    rule2:
      world: world
      x: -63
      y: 112
      z: 85
      onComplete:
        - '@print <pride>be gay; do crime'
```
Example spy message, when the threshold is met for a group:
```
🟨 [vg] squigglyblimp has looked at rules.
```

> `@print` is an internal command that sends **MiniMessage-formatted** text directly to the player who triggered the sign - ideal for gradients, styling, hover or click events.

---

## 🔧 Commands

```shell
/vg                      - Show plugin help
/vg addgroup <id>        - Create a group (saved in groups.yml after restart)
/vg addsign <id> <group> - Register the sign you're looking at
```

ℹ️ Signs only start tracking after a restart. Define them in-game, then fine-tune `groups.yml` manually if needed.

---

## 🔐 Permissions

| Node             | Description                                                                    |
| ---------------- | ------------------------------------------------------------------------------ |
| `vardoger.admin` | See internal debug messages (e.g. invalid `@` commands)                        |
| `vardoger.spy`   | Be notified when a player reaches a group's `spyThreshold` (e.g. 30% complete) |

---

## 🧐 Developer Notes

* Built on the **Paper API** (not spigot)
* Uses **CommandAPI** for command registration
* Asynchronous autosave of player data with atomic .tmp → .json file writes
* Players are removed from memory when offline
* Ray tracing is throttled and scoped to loaded chunks for performance

---

## 💬 Credits

* Concept by [lordpipe](https://github.com/lordofpipes)
* Code by [cutelilreno](https://github.com/cutelilreno)