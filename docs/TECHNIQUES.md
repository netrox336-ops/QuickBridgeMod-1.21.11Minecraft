# QuickBridge — профили техник v0.2.0

QuickBridge использует единый runtime engine, но каждая техника имеет собственный tuning-профиль. Это позволяет менять механику конкретного bridge без переписывания основного движка.

| Профиль | Статус | Movement | Rotation | Особенность v0.2 |
| --- | --- | --- | --- | --- |
| Ninja | stable | backward | none | world edge scan + sneak threshold |
| Diagonal Ninja | stable | diagonal backward | none | отдельный placement lead |
| Breezily | stable | alternating backward | oscillating backward | небольшой yaw oscillation |
| Witchly | stable | alternating backward | oscillating backward | увеличенная амплитуда yaw |
| God Bridge | stable | backward-right | backward | плавный разворот + jump cadence |
| Jump God | experimental | backward-right | backward | ускоренный jump cycle |
| Telly | experimental | forward | telly phases | run → jump → reverse-view placement |
| Speed Telly | experimental | forward | telly phases | более короткий cycle и быстрый rotation |
| Side Bridge | stable | side-right | side-right | world-direction side movement |
| Time Bridge | experimental | backward | backward | отдельный cadence 2t |
| Andromeda | experimental | backward-right | oscillating backward | быстрый rotation + multi-placement |
| Blink Bridge | experimental | forward | telly phases | ранний placement-window + burst attempts |
| Slope Bridging | stable | backward | backward | высокий edge threshold + jump cadence |
| Moonwalk | experimental | alternating backward | oscillating backward | отдельная амплитуда oscillation |

## Tuning-параметры

Каждый `BridgeTechnique` задаёт:

- `movementStyle` — желаемое движение в мировых координатах;
- `placeEveryTicks` — базовую частоту placement;
- `jumpEveryTicks` — ритм прыжков для обычных профилей;
- `extraPlacementAttempts` — дополнительные цели в одном placement window;
- `rotationMode` — NONE / BACKWARD / OSCILLATING_BACKWARD / TELLY / SIDE_RIGHT;
- `cycleTicks` — длину фазового цикла;
- `placementStartTick` — начало placement-window для TELLY-профилей;
- `placementLead` — расстояние упреждения цели относительно игрока;
- `placementPitch` — целевой pitch камеры;
- `rotationStep` — максимальный шаг плавного поворота за tick;
- `edgeSneakThreshold` — дистанцию до отсутствующей опоры для Sneak Assist;
- `oscillationDegrees` — амплитуду yaw-колебания.

## Placement confirmation

В v0.2 попытка `useItemOn` не считается автоматически успешным блоком. `BridgeEngine` сохраняет target в pending-очередь и ждёт, пока клиентский мир действительно перестанет считать эту позицию воздухом.

Если блок не появился за `confirmationTicks`, движок может повторить placement до `maxRecoveryAttempts`. После исчерпания recovery target считается failed. Несколько последовательных failures могут вызвать Fail-stop.

## World-direction movement

Rotation Engine может повернуть камеру на 90°/180°, но движение вычисляется относительно исходного мирового вектора техники. Затем QuickBridge преобразует этот вектор обратно в W/A/S/D относительно текущего yaw. Благодаря этому разворот для placement не должен сам по себе менять траекторию игрока.

`[EXP]` означает необходимость дальнейшей практической калибровки под конкретную серверную физику и задержку. Это не режим обхода античита.
