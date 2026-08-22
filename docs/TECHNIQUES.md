# QuickBridge — профили техник v0.5.0

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

## Cycle Learning

Начиная с v0.5 QuickBridge оценивает не только отдельные placements, но и полный state-machine cycle. `CycleEvaluator` фиксирует переход через конец цикла и считает:

- confirmed blocks;
- failed blocks;
- recovery attempts;
- итоговый quality score;
- успешность цикла;
- текущий Network Condition.

Cycle quality используется как более сильный сигнал обучения, чем одиночный placement. Это особенно важно для Telly, Andromeda и других техник, где отдельный блок может быть подтверждён, но весь цикл при этом выполнен плохо.

## Network Condition

Состояние сервера классифицируется как:

- `STABLE`;
- `DELAYED`;
- `UNSTABLE`.

Для этого используются наблюдаемые клиентом показатели: ACK блоков, reliability и recovery-rate. При нестабильных подтверждениях вес новых learned-изменений уменьшается.

## Learning Checkpoints и Auto Rollback

После серии качественных циклов текущие learned-параметры сохраняются как checkpoint. Если после него несколько циклов подряд показывают заметное устойчивое ухудшение, профиль автоматически возвращается к последнему checkpoint.

Rollback касается только learned-слоя. Ручная калибровка пользователя не меняется.

## Training Mode

Training Mode запускает обычный Bridge Engine, state machine и recovery, но не пишет новые placement/cycle samples в persistent Server Learning.

Во время тренировки отдельно считаются:

- количество циклов;
- success-rate;
- EMA качества;
- последнее сетевое состояние.

После выключения режима эта временная статистика может быть сброшена и никак не влияет на профиль сервера.

## Adaptive Cadence

Текущий cadence учитывает:

- горизонтальную скорость игрока;
- confirmed / failed placements текущего запуска;
- среднее время подтверждения блока;
- ручной `cadenceBias`;
- learned-correction сервера, если Auto Learning включён.

## Server Learning

Статистика хранится отдельно для каждой пары **сервер + техника**. Профиль накапливает placement samples, recovery success, ACK EMA, cycle statistics, quality score, checkpoint и количество rollback.

Ручные настройки в `quickbridge.properties` не перезаписываются. Learning хранится отдельно в `quickbridge-learning.properties`.

## Recovery State Machine

Если placement не подтверждён за confirmation window, обычная state machine временно приостанавливается:

`HOLD → REALIGN → RETRY → RESUME`

Для Telly, Speed Telly, Andromeda и Blink realign/resume более осторожные, чем для обычных профилей.

## Ручная калибровка

Для каждого профиля отдельно сохраняются:

- `cycleScale` — 70–140%;
- `leadOffset` — поправка placement target;
- `rotationScale` — 55–160%;
- `cadenceBias` — ручная поправка Adaptive Cadence.

Редактор показывает три слоя: manual, learned и effective.

## Placement confirmation

Попытка `useItemOn` не считается успехом сама по себе. QuickBridge ждёт фактического появления блока в клиентском мире. Только после этого placement попадает в confirmed statistics.

`[EXP]` означает необходимость дальнейшей практической калибровки под конкретную физику и задержку сервера. Это не режим обхода античита.
