# QuickBridge — профили техник v0.8.0

QuickBridge использует единый world-aware runtime, но каждая техника имеет собственный базовый профиль, state-machine поведение, ручную калибровку, learned-поправки и execution profile. Начиная с v0.8 обычная постановка также проходит через общий Smart Placement Planner.

| Профиль | Статус | Movement | Runtime |
| --- | --- | --- | --- |
| Ninja | stable | backward | cruise + edge scan + planner |
| Diagonal Ninja | stable | diagonal backward | cruise + edge scan + planner |
| Breezily | stable | alternating backward | STRAFE L / STRAFE R + execution window |
| Witchly | stable | alternating backward | STRAFE L / STRAFE R + yaw alignment |
| God Bridge | stable | backward-right | cruise + jump cadence + planner |
| Jump God | experimental | backward-right | fast jump cadence + planner |
| Telly | experimental | forward | RUNUP → JUMP → TURN → BURST → RESET + execution guard + planner |
| Speed Telly | experimental | forward | ускоренный Telly cycle + tighter rotation gate + planner |
| Side Bridge | stable | side-right | world-direction side movement + planner |
| Time Bridge | experimental | backward | cadence 2t + adaptive correction + planner |
| Andromeda | experimental | backward-right | RUNUP → JUMP → TURN → BURST → RESET + execution guard + planner |
| Blink Bridge | experimental | forward | короткий runup + adaptive burst window + planner |
| Slope Bridging | stable | backward | cruise + jump cadence + edge scan |
| Moonwalk | experimental | alternating backward | STRAFE L / STRAFE R + execution window |

## Smart Placement Planner

До v0.8 Bridge Engine пробовал небольшой фиксированный набор target-позиций. Теперь `PlacementPlanner` строит набор кандидатов вдоль направления движения и добавляет несколько небольших боковых альтернатив.

Кандидат получает score с учётом:

- текущего placement lead;
- фактической скорости игрока;
- reliability;
- расстояния от ожидаемой точки моста;
- бокового отклонения;
- числа доступных соседних блоков для attachment;
- дистанции от глаз игрока до потенциальной точки клика.

Кандидаты сортируются по score. Pending target, который уже ждёт server ACK, пропускается и не выбирается повторно.

Recovery не использует новый план: если конкретный block placement уже сорвался, recovery повторяет именно исходный target.

## Path Probe и Path Guard

`PathProbe` просматривает участок примерно до 2.4 блока по направлению текущего движения.

Он считает:

- distance до первого gap;
- distance до первого препятствия на уровне ног/головы;
- support ratio;
- максимальную длину непрерывного gap.

Void сам по себе не является ошибкой — мод предназначен именно для построения моста над пустотой.

`Path Guard` реагирует только на настоящее препятствие непосредственно по направлению движения. В этом случае автоматическое движение приостанавливается и при включённом Sneak Assist удерживается sneak.

Smart Placement и Path Guard можно отключить отдельно.

## Execution Profiles

Сложная техника хранит отдельный `TechniqueExecutionProfile`.

Профиль задаёт:

- границы фаз;
- базовое начало/конец placement window;
- минимальную скорость burst;
- допустимую ошибку yaw/pitch;
- safe re-entry progress после recovery;
- необходимость rotation/motion gate.

Это отделяет смысл техники от server learning: Auto Learning может слегка поправлять cycle/lead/cadence, но не меняет базовую последовательность Telly или Andromeda.

## Technique State Machine

Telly / Speed Telly / Blink / Andromeda используют последовательность:

`RUNUP → JUMP → TURN → BURST → RESET`

Breezily / Witchly / Moonwalk используют `STRAFE L` и `STRAFE R`.

Продвижение по фазе может немного замедляться, если физическое состояние игрока ещё не соответствует следующей фазе.

## Execution Guard

Execution Guard проверяет placement непосредственно перед отправкой команды постановки.

Учитываются:

1. правильная state-machine фаза;
2. adaptive placement window;
3. motion score;
4. rotation alignment.

Если placement cadence наступил слишком рано, команда не отправляется. Accumulator ограничивается, чтобы после открытия окна не выпускать накопленную очередь placements.

## Adaptive Placement Window

Базовое окно берётся из execution profile и немного корректируется runtime-данными:

- высокая скорость слегка упреждает начало окна;
- повышенный ACK допускает небольшую раннюю поправку;
- низкая reliability сокращает позднюю часть окна.

## Rotation Alignment

Rotation Engine сообщает реальную ошибку относительно целевого yaw/pitch. Для профилей с rotation gate placement считается готовым только когда камера попадает в tolerance выбранной техники.

## Recovery State Machine и Phase Resync

Если placement не подтверждён за confirmation window, обычная state machine приостанавливается:

`HOLD → REALIGN → RETRY → RESUME`

После `RESUME` сложная техника может запросить phase resync. State machine сбрасывает placement accumulator и возвращается в безопасную точку цикла.

## Condition-Aware Learning

Сохраняется архитектура:

- global server baseline;
- condition buckets `STABLE / DELAYED / UNSTABLE`;
- hysteresis при переключении;
- плавный blend профилей;
- Network Guard;
- Learning Checkpoints и Auto Rollback.

Старые learning-файлы остаются совместимыми.

## Cycle Learning

`CycleEvaluator` оценивает полный state-machine cycle и учитывает confirmed blocks, failed blocks, recovery attempts, quality score и Network Condition.

## Training Mode

Training Mode запускает обычный Bridge Engine, Placement Planner, Execution Guard, state machine и recovery, но не пишет новые samples в persistent Server Learning.

## Ручная калибровка

Для каждого профиля отдельно сохраняются:

- `cycleScale`;
- `leadOffset`;
- `rotationScale`;
- `cadenceBias`.

## Placement confirmation

Попытка `useItemOn` не считается успехом сама по себе. QuickBridge ждёт фактического появления блока в клиентском мире. Только после этого placement считается confirmed.

`[EXP]` означает необходимость практической калибровки под конкретную физику и задержку сервера. Это не режим обхода античита.
