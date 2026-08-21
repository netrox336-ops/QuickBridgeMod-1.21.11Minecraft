# Changelog

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
