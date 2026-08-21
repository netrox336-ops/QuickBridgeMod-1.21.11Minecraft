# QuickBridge — профили техник v0.4.0

QuickBridge использует единый world-aware runtime, но каждая техника имеет собственный базовый профиль, state-machine поведение, ручную калибровку и — при включённом Auto Learning — отдельную learned-поправку для текущего сервера.

| Профиль | Статус | Movement | Runtime |
| --- | --- | --- | --- |
| Ninja | stable | backward | cruise + world edge scan |
| Diagonal Ninja | stable | diagonal backward | cruise + edge scan |
| Breezily | stable | alternating backward | STRAFE L / STRAFE R |
| Witchly | stable | alternating backward | STRAFE L / STRAFE R + yaw oscillation |
| God Bridge | stable | backward-right | cruise + jump cadence |
| Jump God | experimental | backward-right | fast jump cadence |
| Telly | experimental | forward | RUNUP → JUMP → TURN → BURST → RESET |
| Speed Telly | experimental | forward | ускоренный Telly state cycle |
| Side Bridge | stable | side-right | world-direction side movement |
| Time Bridge | experimental | backward | cadence 2t + adaptive correction |
| Andromeda | experimental | backward-right | RUNUP → JUMP → TURN → BURST → RESET |
| Blink Bridge | experimental | forward | короткий runup + длинный burst window |
| Slope Bridging | stable | backward | cruise + jump cadence + edge scan |
| Moonwalk | experimental | alternating backward | STRAFE L / STRAFE R |

## Technique State Machine

Telly / Speed Telly / Blink / Andromeda используют последовательность:

- `RUNUP` — разгон;
- `JUMP` — импульс прыжка;
- `TURN` — подготовка камеры;
- `BURST` — placement window;
- `RESET` — переход к следующему циклу.

Breezily / Witchly / Moonwalk используют `STRAFE L` и `STRAFE R`, поэтому движение и yaw oscillation синхронизированы одним состоянием.

## Adaptive Cadence

Текущий cadence учитывает:

- горизонтальную скорость игрока;
- confirmed / failed placements текущего запуска;
- среднее время подтверждения блока;
- ручной `cadenceBias`;
- learned-correction сервера, если Auto Learning включён.

## Server Learning

v0.4 хранит статистику отдельно для каждой пары **сервер + техника**. Например, Telly на одном сервере и Telly на другом сервере имеют независимые learning-профили.

Профиль накапливает:

- `samples`;
- confirmed и failed placements;
- recovery success;
- EMA задержки подтверждения блока;
- learned correction для cycle / lead / rotation / cadence.

Сила learned-коррекции умножается на `confidence`. Пока samples мало, влияние обучения небольшое. По мере накопления статистики confidence плавно растёт до 100%.

Ручные настройки в `quickbridge.properties` при этом не перезаписываются. Learning хранится отдельно в `quickbridge-learning.properties`.

## Recovery State Machine

Если placement не подтверждён за confirmation window, обычная state machine временно приостанавливается и управление переходит в recovery:

`HOLD → REALIGN → RETRY → RESUME`

- `HOLD` — движение останавливается и удерживается sneak;
- `REALIGN` — камера плавно доворачивается к проблемному target;
- `RETRY` — повторяется placement;
- `RESUME` — короткая стабилизация перед возвратом к основной технике.

Для Telly, Speed Telly, Andromeda и Blink realign/resume длиннее, чем для обычных профилей.

## Ручная калибровка

Для каждого профиля отдельно сохраняются:

- `cycleScale` — 70–140%;
- `leadOffset` — поправка placement target;
- `rotationScale` — 55–160%;
- `cadenceBias` — ручная поправка Adaptive Cadence.

Редактор v0.4 показывает сразу три слоя: manual, learned и effective.

## Placement confirmation

Попытка `useItemOn` не считается успехом сама по себе. QuickBridge ждёт фактического появления блока в клиентском мире. Только после этого placement попадает в confirmed statistics и может использоваться Server Learning.

`[EXP]` означает необходимость дальнейшей практической калибровки под конкретную физику и задержку сервера. Это не режим обхода античита.
