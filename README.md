# QuickBridge — Minecraft 1.21.11

**QuickBridge** — client-side Forge-мод с world-aware движком бриджинга. Он управляет движением игрока, камерой и постановкой блоков, ориентируясь на реальное состояние мира, а не только на фиксированные AHK-таймеры.

> Важно: автоматизированный bridging может нарушать правила отдельных серверов. QuickBridge не содержит функций обхода античита, маскировки или античит-эвейжна. Используйте мод только там, где такие функции разрешены.

## Что нового в v0.2.0

- **Rotation Engine** — плавные yaw/pitch профили для техник, которым требуется разворот камеры;
- **world-direction movement** — камера может поворачиваться, а желаемое направление движения сохраняется;
- фазовая логика **Telly / Speed Telly / Blink**: разбег → прыжок → разворот → placement window;
- отдельные oscillation-профили для **Breezily / Witchly / Andromeda / Moonwalk**;
- новый **Edge Detector**, который сканирует реальную опору впереди траектории игрока;
- **placement confirmation** — попытка считается подтвержденной только когда блок реально появился в мире;
- **placement recovery** — неподтвержденная установка ограниченно повторяется;
- **Fail-stop** — автоматическая остановка после серии placement failures;
- **Restore View** — возврат исходного yaw/pitch после завершения;
- расширенный HUD с фазой движка, confirmed/retry/fail и edge distance;
- Auto Select Blocks теперь предпочитает самый большой стак блоков;
- исправлена синхронизация Toggle после автоматического safety-stop.

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

Профиль каждой техники задаёт movement style, placement cadence, jump cadence, rotation mode, cycle, placement window, placement lead, pitch, rotation speed, edge threshold и oscillation. Техники с `[EXP]` требуют дальнейшей практической калибровки под серверную физику и задержку.

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

Движок хранит очередь pending placements. Если сервер не подтвердил блок за заданное окно, QuickBridge может повторить установку. После исчерпания повторов placement отмечается как failed; несколько последовательных failures могут автоматически остановить bridging.

## HUD

HUD показывает:

- текущую технику;
- `ACTIVE / RUN / PLACE / EDGE / RECOVERY / OFF`;
- количество блоков в хотбаре;
- режим Hold/Toggle;
- количество подтвержденных установок;
- число recovery attempts;
- failures;
- оценку расстояния до края/отсутствия опоры.

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

- `BridgeTechnique` — каталог техник и tuning-параметры;
- `BridgeEngine` — runtime/state machine, movement и pending placements;
- `RotationEngine` — управление yaw/pitch;
- `EdgeDetector` — оценка края по состоянию мира;
- `PlacementHelper` — выбор блока, поиск валидной грани и отправка placement;
- `BridgeConfig` — пользовательские настройки;
- `QuickBridgeHudRenderer` — HUD;
- `QuickBridgeScreen` — меню конфигурации.

Подробная таблица профилей находится в `docs/TECHNIQUES.md`.
