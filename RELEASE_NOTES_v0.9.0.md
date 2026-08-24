# QuickBridge 0.9.0 — Route Stability

Версия 0.9.0 добавляет отдельный слой стабилизации общей траектории моста.

### Главное

- новая центральная ось маршрута, которая фиксируется при запуске техники;
- расчёт и сглаживание бокового drift игрока;
- плавная коррекция мирового movement-вектора;
- три режима силы коррекции: **Мягко / Нормально / Сильно**;
- отдельная обработка alternating-профилей Breezily / Witchly / Moonwalk;
- состояние `ROUTE CORRECT` при сильном отклонении;
- автоматический re-anchor при смене техники и большом скачке позиции;
- confirmed placements теперь участвуют в расчёте фактической ширины моста;
- Smart Placement Planner стал lane-aware и штрафует targets, которые уводят мост от центральной линии;
- в HUD добавлены drift, correction, placement spread, route progress, lane error и re-anchor counter;
- Route Stability можно полностью отключить, оставив поведение v0.8.

Существующие настройки, Server Learning, Network Profiles, Training Mode, Execution Guard, Recovery, Smart Placement и Path Guard сохранены.

Требования: Minecraft Java 1.21.11, Forge 61.x, Java 21.
