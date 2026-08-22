# QuickBridge v0.6.0

Версия 0.6.0 добавляет condition-aware learning для Minecraft 1.21.11.

## Главное

- отдельные learned-профили для `STABLE`, `DELAYED` и `UNSTABLE`;
- сохранение старого server-learning как общего baseline;
- hysteresis при переключении сетевого профиля;
- плавный blend между старым и новым профилем;
- Network Guard для более осторожного runtime при задержках;
- condition-level samples, cycles, confidence, checkpoints и rollback;
- отдельный сброс активного network bucket;
- расширенный HUD и экран статистики.

## Совместимость

- Minecraft Java 1.21.11
- Forge 61.x
- Java 21

Learning-файлы от v0.4/v0.5 сохраняются и продолжают использоваться как общий baseline.
