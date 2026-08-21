# QuickBridge — профили техник v0.3.0

QuickBridge использует единый world-aware runtime, но каждая техника имеет базовый профиль, state-machine поведение и отдельную пользовательскую калибровку.

| Профиль | Статус | Movement | Runtime v0.3 |
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

Сложные профили больше не определяют всё поведение только через `tick % cycle`. `TechniqueStateMachine` хранит нормализованный прогресс цикла и переводит его в фазу техники.

Для Telly / Speed Telly / Blink / Andromeda используются фазы:

- `RUNUP` — разгон;
- `JUMP` — импульс прыжка;
- `TURN` — подготовка Rotation Engine;
- `BURST` — placement window;
- `RESET` — возвращение к следующему циклу.

Breezily / Witchly / Moonwalk используют `STRAFE L` и `STRAFE R`, поэтому movement pattern и yaw oscillation синхронизированы одним состоянием.

## Adaptive Cadence

`AdaptiveCadence` учитывает:

- фактическую горизонтальную скорость игрока;
- число confirmed / failed placements;
- среднее время появления подтвержденного блока в клиентском мире;
- пользовательский `cadenceBias`.

Результат — `cadenceFactor`. При хорошей скорости и стабильном подтверждении профиль может идти быстрее; при высокой задержке или failures движок автоматически снижает темп.

## Per-Technique Tuning

В v0.3 пользователь может отдельно для каждого профиля менять:

- `cycleScale` — 70–140% базовой длины state-machine cycle;
- `leadOffset` — ручную поправку точки placement;
- `rotationScale` — 55–160% базовой скорости Rotation Engine;
- `cadenceBias` — ручную поправку к Adaptive Cadence.

Параметры сохраняются в `config/quickbridge.properties` с ключами `tuning.<TECHNIQUE>.*`.

## Placement confirmation и Recovery Hold

Попытка `useItemOn` считается успешной только после фактического появления блока в клиентском мире. Если блок не подтвержден за `confirmationTicks`, выполняется ограниченный recovery.

Начиная с v0.3 при recovery state machine переходит в `RECOVERY`, QuickBridge кратко снимает движение и удерживает sneak. Это уменьшает риск продолжить движение по позиции, где сервер не подтвердил блок.

## World-direction movement

Rotation Engine может повернуть камеру на 90°/180°, но движение рассчитывается относительно исходного мирового вектора техники и преобразуется обратно в W/A/S/D относительно текущего yaw. В v0.3 alternating movement получает направление strafe напрямую из state machine.

`[EXP]` означает необходимость дальнейшей практической калибровки под серверную физику и задержку. Это не режим обхода античита.
