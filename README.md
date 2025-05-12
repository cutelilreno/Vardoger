# Vardoger

> **Prototype plugin!** — do your own checks before using in production!!

A Spigot plugin for tracking player attention — unlock commands and progress by looking at signs.

Original concept by **lordpipe**.

![Vardoger demo](https://raw.githubusercontent.com/cutelilreno/Vardoger/main/.github/assets/vardoger-demo.gif)

---

## ✨ Features

* Detects when players are looking at specific signs
* Tracks time spent focusing per sign
* Groups signs into collections with a required gaze duration
* Executes commands when a group is completed

---

## 🎮 Use Cases

* Gamified rule walls
* Quests and educational content
* Unlockable rewards or progression triggers

---

## ⚙ How It Works

Signs are grouped in `groups.yml`, with:

* A required duration (in ticks)
* A list of signs with coordinates
* A command list to run once the group is completed

The plugin uses raytracing to detect what signs players are looking at and updates their progress accordingly.

---

## 📟 Example `groups.yml`

```yaml
rules:
  requiredDuration: 5
  onComplete:
    - 'tellraw {player} {"text":"Thank you for reading the rules! \u2006\u2764","italic":true,"color":"yellow"}'
    - tell {player} Completed groups can also run multiple commands!
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

---

## 🔧 Usage

```shell
/vg addgroup <group>
/vg addsign <id> <group>
```

📝 *Signs won't track until the next reboot.*

It's recommended to define groups  and signs in-game, then fine-tune `groups.yml` to add custom commands.

---

## 🧠 Credits

Built by [cutelilreno](https://github.com/cutelilreno)
Concept by [lordpipe](https://github.com/lordofpipes)
