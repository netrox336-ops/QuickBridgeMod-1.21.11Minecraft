# Changelog

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
