# QuickBridge — профили техник v0.7.0

QuickBridge использует единый world-aware runtime, но каждая техника имеет собственный базовый профиль, state-machine поведение, ручную калибровку, learned-поправки и начиная с v0.7 — отдельный execution profile.

| Профиль | Статус | Movement | Runtime |
| --- | --- | --- | --- |
| Ninja | stable | backward | cruise + world edge scan |
| Diagonal Ninja | stable | diagonal backward | cruise + edge scan |
| Breezily | stable | alternating backward | STRAFE L / STRAFE R + execution window |
| Witchly | stable | alternating backward | STRAFE L / STRAFE R + yaw alignment |
| God Bridge | stable | backward-right | cruise + jump cadence |
| Jump God | experimental | backward-right | fast jump cadence |
| Telly | experimental | forward | RUNUP → JUMP → TURN → BURST → RESET + execution guard |
| Speed Telly | experimental | forward | ускоренный Telly cycle + tighter rotation gate |
| Side Bridge | stable | side-right | world-direction side movement |
| Time Bridge | experimental | backward | cadence 2t + adaptive correction |
| Andromeda | experimental | backward-right | RUNUP → JUMP → TURN → BURST → RESET + execution guard |
| Blink Bridge | experimental | forward | короткий runup + длинный adaptive burst window |
| Slope Bridging | stable | backward | cruise + jump cadence + edge scan |
| Moonwalk | experimental | alternating backward | STRAFE L / STRAFE R + execution window |

## Execution Profiles

В v0.7 сложная техника хранит не только обычный `BridgeTechnique`, но и отдельный `TechniqueExecutionProfile`.

Профиль задаёт:

- границы фаз;
- базовое начало/конец placement window;
- минимальную скорость для burst;
- допустимую ошибку yaw;
- допустимую ошибку pitch;
- safe re-entry progress после recovery;
- необходимость rotation/motion gate;
- предпочтение airborne burst для Telly-подобных техник.

Это позволяет отделить смысл техники от серверного обучения. Auto Learning может слегка поправлять cycle/lead/cadence, но не меняет базовую последовательность Telly или Andromeda.

## Technique State Machine

Telly / Speed Telly / Blink / Andromeda используют последовательность:

- `RUNUP` — разгон;
- `JUMP` — импульс прыжка;
- `TURN` — подготовка камеры;
- `BURST` — окно постановки;
- `RESET` — переход к следующему циклу.

Breezily / Witchly / Moonwalk используют `STRAFE L` и `STRAFE R`.

В v0.7 скорость продвижения по фазе может немного замедляться, если сложная техника ещё физически не готова перейти дальше. Например, JUMP-фаза не должна мгновенно проскочить, пока игрок фактически остаётся на земле.

## Execution Guard

Execution Guard проверяет placement непосредственно перед выдачей команды постановки.

Для сложной техники учитываются:

1. правильная state-machine фаза;
2. adaptive placement window;
3. motion score;
4. rotation alignment.

Если placement cadence уже наступил, но guard считает действие преждевременным, попытка не отправляется. Accumulator при этом ограничивается, чтобы после открытия окна мод не выпустил длинную накопленную очередь кликов.

Execution Guard можно отключить в меню.

## Adaptive Placement Window

Базовое окно берётся из execution profile, после чего в небольших пределах корректируется runtime-данными:

- более высокая скорость немного упреждает начало окна;
- повышенный ACK может добавить небольшую раннюю поправку;
- плохая placement reliability подрезает поздний край окна.

Коррекция специально ограничена небольшим диапазоном, чтобы адаптация не переписывала саму геометрию техники.

## Rotation Alignment

Rotation Engine теперь после каждого шага сообщает реальную ошибку относительно целевого yaw/pitch.

Для профилей с rotation gate placement считается готовым только когда камера попадает в индивидуальный tolerance выбранной техники. HUD показывает текущие yaw/pitch errors.

## Recovery State Machine и Phase Resync

Если placement не подтверждён за confirmation window, обычная state machine приостанавливается:

`HOLD → REALIGN → RETRY → RESUME`

После завершения `RESUME` v0.7 может запросить phase resync. Для Telly, Speed Telly, Blink, Andromeda и других профилей с safe re-entry progress state machine:

- сбрасывает placement accumulator;
- возвращается в безопасную точку цикла;
- продолжает технику уже из синхронизированной фазы.

Это устраняет ситуацию, когда recovery занял несколько тиков, а основной Telly-cycle за это время логически ушёл вперёд.

## Condition-Aware Learning

Сохраняется архитектура v0.6:

- global server baseline;
- отдельные condition buckets `STABLE / DELAYED / UNSTABLE`;
- hysteresis при переключении;
- плавный blend профилей;
- Network Guard;
- Learning Checkpoints и Auto Rollback.

Старые learning-файлы остаются совместимыми.

## Cycle Learning

`CycleEvaluator` оценивает полный state-machine cycle и учитывает:

- confirmed blocks;
- failed blocks;
- recovery attempts;
- quality score;
- успешность цикла;
- Network Condition.

Cycle feedback дополняет placement feedback и используется для server learning.

## Training Mode

Training Mode запускает обычный Bridge Engine, Execution Guard, state machine и recovery, но не пишет новые samples в persistent Server Learning.

Во время тренировки отдельно считаются cycles, success-rate, EMA качества и последнее сетевое состояние.

## Ручная калибровка

Для каждого профиля отдельно сохраняются:

- `cycleScale`;
- `leadOffset`;
- `rotationScale`;
- `cadenceBias`.

Редактор v0.7 показывает manual/learned/effective tuning и readonly execution profile выбранной техники.

## Placement confirmation

Попытка `useItemOn` не считается успехом сама по себе. QuickBridge ждёт фактического появления блока в клиентском мире. Только после этого placement считается confirmed.

`[EXP]` означает необходимость практической калибровки под конкретную физику и задержку сервера. Это не режим обхода античита.
