# QuickBridge 0.7.0 — Execution Profiles

Версия 0.7.0 улучшает именно исполнение сложных bridging-техник.

### Главное
- отдельные `TechniqueExecutionProfile` для сложных техник;
- индивидуальные phase boundaries и placement windows;
- **Execution Guard** для проверки фазы, движения и камеры перед placement;
- adaptive placement window по speed / ACK / reliability;
- Rotation Engine теперь измеряет yaw/pitch error относительно целевого положения;
- отдельные yaw/pitch tolerance для техник;
- motion score для burst-фаз;
- Telly/Speed Telly/Blink/Andromeda больше не обязаны ставить блок только потому, что формальный timer дошёл до BURST;
- после recovery добавлен **Phase Resync** с возвратом в безопасную точку цикла;
- placement accumulator сбрасывается после phase-resync;
- HUD показывает execution score, rotation errors, window state, guard state, blocked placements и resync counter;
- редактор техники показывает readonly execution-параметры;
- Execution Guard можно отключить отдельно.

### Совместимость
- Minecraft Java 1.21.11
- Forge 61.2.0
- Java 21
- learning-файлы и ручные настройки прошлых версий сохраняются.

В моде нет функций обхода античита или скрытия автоматизации. Используйте автоматизированный bridging только на серверах, где это разрешено правилами.
