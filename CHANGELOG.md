# Changelog

## 0.9.0 — 2026-08-24

Девятая версия QuickBridge добавляет стабилизацию общей траектории моста и связывает её со Smart Placement Planner.

### Добавлено
- `RouteStabilityController` с фиксированной центральной осью маршрута;
- расчёт raw/smoothed lateral drift относительно оси;
- плавная коррекция мирового movement-вектора без snap-поворота камеры;
- режимы коррекции `GENTLE / NORMAL / STRONG`;
- hard-drift состояние `ROUTE CORRECT` с поддержкой Sneak Assist;
- отдельная центральная ось для alternating-профилей Breezily / Witchly / Moonwalk;
- учёт подтверждённых сервером блоков и placement spread EMA;
- автоматический re-anchor после смены техники или большого скачка позиции;
- lane-aware penalty в Smart Placement Planner;
- route error выбранного placement-кандидата в planner diagnostics;
- HUD-метрики drift / correction / spread / route progress / re-anchor;
- настройка Route Stability и силы коррекции в игровом меню;
- документация `docs/ROUTE_STABILITY.md`.

### Изменено
- Planner теперь предпочитает кандидаты ближе к центральной линии моста при включённом Route Stability;
- Path Probe и Edge Detector получают уже скорректированный world-direction vector;
- confirmed placement дополнительно обновляет геометрию маршрута;
- при сильном дрейфе мод корректирует траекторию постепенно вместо резкого изменения направления;
- старые конфиги v0.8 продолжают работать: Route Stability по умолчанию включён в режиме NORMAL.

## 0.8.0 — 2026-08-23

Восьмая версия QuickBridge перерабатывает выбор placement target и добавляет прогноз ближайшего участка моста.

### Добавлено
- `PlacementPlanner` с ранжированием нескольких продольных и боковых target-кандидатов;
- оценка attachment-соседей, дистанции до клика, соответствия линии движения и placement lead;
- небольшая коррекция planner по текущей скорости и reliability;
- `PathProbe` с прогнозом участка до 2.4 блока впереди;
- расстояние до первого gap и препятствия;
- support ratio и длина непрерывного gap;
- `Path Guard`, который приостанавливает движение перед настоящим препятствием;
- `BridgePathDiagnostics` с candidate count, chosen rank/score и planner misses;
- отдельные настройки Smart Placement и Path Guard;
- новая planner/path диагностика в HUD.

### Изменено
- обычные placement-попытки больше не ограничены тремя жёсткими target-позициями при включённом Smart Placement;
- pending targets исключаются из повторного выбора до получения серверного подтверждения;
- recovery по-прежнему повторяет конкретный проблемный target и не подменяется новым planner-планом;
- ожидаемый void впереди не считается препятствием Path Guard;
- конфигурация и HUD обновлены до версии 0.8.0.

## 0.7.0 — 2026-08-22

Седьмая версия QuickBridge добавляет отдельный execution-слой для контроля реального исполнения сложных техник.

### Добавлено
- `TechniqueExecutionProfile` с отдельными phase boundaries для сложных техник;
- индивидуальные placement windows, min burst speed и yaw/pitch tolerance;
- `Execution Guard`, который проверяет фазу, движение и rotation alignment перед placement;
- adaptive placement window по горизонтальной скорости, ACK и reliability;
- `ExecutionDiagnostics` с motion score, rotation errors, blocked placements и phase-resync counter;
- Rotation Engine feedback по фактической ошибке yaw/pitch;
- safe re-entry progress после recovery;
- phase resync после `RECOVERY RESUME`;
- отображение execution profile в редакторе техники;
- расширенная execution-диагностика HUD.

### Изменено
- Telly / Speed Telly / Blink / Andromeda больше не используют BURST как единственное условие постановки;
- JUMP/TURN progression может слегка замедляться, если физическое состояние игрока ещё не соответствует следующей фазе;
- поздняя часть placement window автоматически сокращается при низкой reliability;
- placement accumulator ограничивается во время заблокированной попытки, чтобы после открытия окна не выпускать накопленную очередь;
- recovery теперь возвращает сложную технику в синхронизированную точку цикла вместо продолжения старого progress;
- Execution Guard можно отключить отдельно от Network Guard и Auto Learning.

## 0.6.0 — 2026-08-22

Шестая версия QuickBridge разделяет обучение по состоянию соединения и добавляет плавное переключение runtime-профилей.

### Добавлено
- condition-aware learning buckets для `STABLE / DELAYED / UNSTABLE`;
- двухслойная модель `server baseline + network condition profile`;
- `NetworkProfileSelector` с hysteresis и защитой от переключения после единичного плохого цикла;
- более быстрое переключение при ухудшении сети и более осторожный возврат к стабильному профилю;
- плавный blend параметров старого и нового condition-профиля;
- отдельный `Network Guard` с консервативными runtime-поправками при DELAYED/UNSTABLE;
- независимые condition samples / cycles / confidence / checkpoints / rollback;
- сброс только активного network-profile без удаления общего server baseline;
- HUD-диагностика active/candidate profile, transition blend и switch counter.

### Изменено
- learning-файл v0.5 полностью сохраняет обратную совместимость и используется как global baseline;
- placement feedback теперь обучает как общий профиль, так и активный condition bucket;
- законченный cycle обучает общий профиль и bucket фактически наблюдаемого Network Condition;
- экран статистики разделяет GLOBAL и активный network bucket;
- Network Profiles и Network Guard можно отключать независимо от Auto Learning.

## 0.5.0 — 2026-08-22

Пятая версия QuickBridge переводит обучение с оценки отдельных placements на оценку целых bridge-циклов и добавляет автоматический откат неудачных learned-параметров.

### Добавлено
- Cycle Learning: confirmed / failed / recovery учитываются на уровне полного state-machine cycle;
- quality score и success-rate целых циклов;
- Network Condition `STABLE / DELAYED / UNSTABLE` по фактическому ACK, reliability и recovery-rate;
- Learning Checkpoints для удачных learned-состояний;
- Auto Rollback после устойчивого ухудшения качества относительно checkpoint;
- сохранение cycle statistics, best quality, checkpoint и rollback counter;
- Training Mode с отдельной временной статистикой без записи в Server Learning;
- HUD-метрики cycle quality, network condition, rollback и training session;
- расширенный экран статистики обучения.

### Изменено
- confidence теперь учитывает и placement samples, и количество полноценных cycles;
- скорость обучения уменьшается при нестабильных серверных подтверждениях;
- placement-learning в Training Mode полностью отключён;
- Server Learning Store расширен с обратной совместимостью со старыми learning-файлами.

## 0.4.0 — 2026-08-22

Четвёртая версия QuickBridge добавляет постоянное обучение профилей по конкретным серверам и отдельную recovery state machine.

### Добавлено
- Server Learning с отдельной статистикой для каждой пары сервер + техника;
- локальное хранение learning-профилей в `config/quickbridge-learning.properties`;
- накопление samples, success/failure, recovery success и ACK EMA;
- learned-corrections для cycle scale, placement lead, rotation scale и cadence bias;
- confidence, который ограничивает силу learned-поправок до накопления достаточной статистики;
- отдельный Recovery State Machine: HOLD → REALIGN → RETRY → RESUME;
- усиленный realign/recovery для Telly, Speed Telly, Andromeda и Blink;
- отдельный экран статистики текущего сервера;
- независимый сброс обучения выбранной техники или всего текущего сервера;
- Learning HUD с persistent reliability/confidence;
- отображение manual / learned / effective параметров в редакторе техники.

### Изменено
- ручная калибровка больше не изменяется автообучением: learned-слой накладывается отдельно;
- Technique State Machine больше не содержит собственную recovery-заглушку;
- placement recovery полностью управляется отдельной state machine;
- learning data автоматически сохраняется при остановке движка;
- главное меню переработано под Auto Learning и статистику сервера.

## 0.3.0 — 2026-08-21

Третья версия QuickBridge переводит сложные техники на адаптивные state machines.

### Добавлено
- Technique State Machine для фазового исполнения сложных техник;
- отдельные фазы RUNUP / JUMP / TURN / BURST / RESET для Telly, Speed Telly, Blink и Andromeda;
- отдельные STRAFE L / STRAFE R фазы для Breezily, Witchly и Moonwalk;
- Adaptive Cadence по горизонтальной скорости, reliability placements и задержке server ACK;
- автоматическое замедление cadence при нестабильных подтверждениях;
- Recovery Hold с временной остановкой движения и sneak во время повторной установки;
- учет фактической скорости игрока в placement lead;
- per-technique tuning: cycle scale, lead offset, rotation scale, cadence bias;
- отдельный экран калибровки выбранной техники;
- сохранение индивидуальной калибровки каждого профиля в конфиг;
- HUD-метрики Speed / Cadence / ACK latency / Reliability.

### Изменено
- Rotation Engine теперь получает текущую фазу state machine;
- world-direction movement для alternating-профилей синхронизирован со strafe-фазой;
- recovery больше не продолжает движение в момент повторной установки;
- диагностический HUD расширен для анализа реального поведения техники на сервере.

## 0.2.0 — 2026-08-21

Вторая версия QuickBridge с переработанным runtime движком.

### Добавлено
- Rotation Engine с отдельными профилями для сложных техник;
- world-direction movement: движение сохраняет заданное направление при повороте камеры;
- фазовая логика Telly/Speed Telly/Blink;
- новый edge detector с проверкой реальной опоры впереди игрока;
- подтверждение placement по фактическому появлению блока в мире;
- очередь pending placements;
- ограниченный placement recovery с повторными попытками;
- fail-stop после серии неподтвержденных установок;
- восстановление исходного yaw/pitch после остановки;
- diagnostic HUD: confirmed/retries/failures/edge distance;
- дополнительные настройки Recovery, Fail-stop, Restore View и confirmation window;
- исправлена синхронизация Toggle после автоматической остановки движка.

### Изменено
- перекалиброваны профили Ninja, Breezily, Witchly, God Bridge, Telly, Speed Telly, Side Bridge, Time Bridge, Andromeda, Blink Bridge, Slope Bridging и Moonwalk;
- Auto Select Blocks теперь выбирает самый большой доступный стак блоков;
- PlacementHelper выбирает ближайшую валидную грань соседнего блока.

## 0.1.0 — 2026-08-21

Первая публичная версия QuickBridge для Minecraft 1.21.11.

### Добавлено
- базовый world-aware Bridge Engine;
- 14 профилей bridging-техник;
- Hold/Toggle управление;
- автоматический выбор блоков;
- safety controller;
- HUD;
- экран настроек;
- конфигурация на диске;
- GitHub Actions для сборки и автоматической публикации Release.
