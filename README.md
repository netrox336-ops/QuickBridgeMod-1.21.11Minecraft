# QuickBridge — Minecraft 1.21.11

**QuickBridge** — client-side Forge-мод с world-aware движком бриджинга. Он управляет движением игрока, камерой и постановкой блоков, ориентируясь на реальное состояние мира и серверные подтверждения, а не только на фиксированные AHK-таймеры.

> Важно: автоматизированный bridging может нарушать правила отдельных серверов. QuickBridge не содержит функций обхода античита, маскировки или античит-эвейжна. Используйте мод только там, где такие функции разрешены.

## Что нового в v0.3.0

- **Technique State Machine** — сложные техники больше не завязаны только на `tick % cycle`;
- Telly / Speed Telly / Blink / Andromeda работают по фазам `RUNUP → JUMP → TURN → BURST → RESET`;
- Witchly / Breezily / Moonwalk получили отдельные левую и правую strafe-фазы;
- **Adaptive Cadence** оценивает горизонтальную скорость игрока, успешность placement и задержку серверных подтверждений;
- при нестабильных подтверждениях cadence автоматически замедляется, вместо того чтобы продолжать проигрывать слишком быстрый паттерн;
- **Recovery Hold**: при повторной установке QuickBridge кратко удерживает игрока и sneak, чтобы не продолжать движение по неподтвержденному мосту;
- placement lead автоматически получает небольшую поправку от фактической скорости игрока;
- **Per-Technique Tuning** — для каждого профиля отдельно сохраняются длина цикла, placement lead, скорость вращения и cadence bias;
- добавлен отдельный экран **Калибровка техники**;
- diagnostic HUD теперь показывает скорость, cadence multiplier, среднюю задержку ACK блока и reliability placements.

## Техники

QuickBridge содержит 14 профилей:

- Ninja
- Diagonal Ninja
- Breezily
- Witchly
- God Bridge
- Jump God
- Telly
- Speed Telly
- Side Bridge
- Time Bridge
- Andromeda
- Blink Bridge
- Slope Bridging
- Moonwalk

Техники с `[EXP]` требуют дальнейшей практической калибровки под серверную физику и задержку. Начиная с v0.3.0 калибровку можно менять прямо в игре отдельно для каждого профиля.

## Калибровка техники

Для каждой техники доступны четыре независимых параметра:

- **Длина цикла** — растягивает или сжимает state-machine cycle;
- **Упреждение блока** — сдвигает прогнозируемую точку постановки вперед/назад;
- **Скорость поворота** — масштабирует Rotation Engine;
- **Cadence bias** — ручная поправка к адаптивной скорости выполнения профиля.

Настройки сохраняются в `config/quickbridge.properties` отдельно для каждого `BridgeTechnique`.

## Управление по умолчанию

| Клавиша | Действие |
| --- | --- |
| `B` | запуск/удержание QuickBridge |
| `N` | следующая техника |
| `K` | аварийный STOP |
| `O` | настройки |

Все клавиши можно переназначить через стандартное меню Minecraft **Controls**.

## Safety / Recovery

В настройках доступны:

- Auto Select Blocks;
- Sneak Assist;
- Stop on Fall;
- Stop on GUI;
- Placement Recovery;
- число повторных placement attempts;
- confirmation window в тиках;
- Fail-stop;
- Restore View;
- Diagnostic HUD.

Движок хранит очередь pending placements. Если сервер не подтвердил блок за заданное окно, QuickBridge может повторить установку. Во время recovery state машина кратко останавливает движение. После исчерпания повторов placement отмечается как failed; несколько последовательных failures могут автоматически остановить bridging.

## HUD

HUD показывает:

- текущую технику и state-machine phase;
- количество блоков в хотбаре;
- режим Hold/Toggle;
- confirmed / recovery / failed placements;
- edge distance;
- горизонтальную скорость игрока;
- текущий adaptive cadence multiplier;
- среднее время подтверждения блока в тиках;
- reliability подтверждений.

## Требования

- Minecraft Java **1.21.11**
- Forge **61.x** (проект собирается на 61.2.0)
- Java **21**

## Установка

1. Установить Forge для Minecraft 1.21.11.
2. Скачать JAR из GitHub Releases.
3. Поместить JAR в `.minecraft/mods`.
4. Запустить Minecraft с профилем Forge.

## Архитектура

- `BridgeTechnique` — базовый каталог профилей;
- `TechniqueStateMachine` — фазы и runtime-переходы техник;
- `AdaptiveCadence` — адаптация к скорости и server ACK;
- `TechniqueTuning` — пользовательская калибровка профиля;
- `BridgeEngine` — movement, placement queue и recovery;
- `RotationEngine` — управление yaw/pitch по фазам state machine;
- `EdgeDetector` — оценка края по состоянию мира;
- `PlacementHelper` — выбор блока, поиск валидной грани и отправка placement;
- `BridgeConfig` — настройки и per-technique tuning;
- `TechniqueEditorScreen` — редактор калибровки;
- `QuickBridgeHudRenderer` — HUD;
- `QuickBridgeScreen` — главное меню конфигурации.

Подробная таблица профилей находится в `docs/TECHNIQUES.md`.
