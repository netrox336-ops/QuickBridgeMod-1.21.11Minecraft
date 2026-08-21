# QuickBridge 0.4.0 — Minecraft 1.21.11

В этой версии QuickBridge начинает запоминать реальное поведение техник на конкретных серверах и постепенно подстраивать профиль без изменения ручной калибровки.

### Главное
- Server Learning отдельно для каждого сервера и каждой техники;
- persistent samples, reliability, ACK latency и recovery statistics;
- learned-corrections для cycle / lead / rotation / cadence;
- confidence ограничивает влияние обучения, пока статистики мало;
- отдельный `quickbridge-learning.properties`;
- Recovery State Machine: HOLD → REALIGN → RETRY → RESUME;
- отдельная recovery-настройка сложных Telly / Speed Telly / Andromeda / Blink;
- экран статистики сервера;
- независимый сброс learned-профиля;
- Learning HUD;
- редактор показывает ручные, learned и итоговые effective-параметры.

Автообучение можно полностью отключить. В этом случае QuickBridge продолжает использовать только ручную калибровку v0.3.

QuickBridge не содержит обхода античита, маскировки или функций скрытия автоматизации.
