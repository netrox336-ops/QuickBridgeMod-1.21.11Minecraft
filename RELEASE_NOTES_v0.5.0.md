# QuickBridge v0.5.0

Версия 0.5.0 развивает Server Learning и добавляет оценку качества целых bridge-циклов.

## Главное

- Cycle Learning для полного state-machine cycle;
- quality score и cycle success-rate;
- Network Condition: STABLE / DELAYED / UNSTABLE;
- Learning Checkpoints;
- автоматический rollback learned-параметров после устойчивого ухудшения;
- Training Mode без записи новых samples в постоянный профиль;
- расширенный HUD и экран статистики;
- сохранение cycle-quality, checkpoint и rollback-counter в learning store.

Ручная калибровка не перезаписывается Auto Learning. Training Mode также не изменяет постоянный профиль сервера.

Требования: Minecraft 1.21.11, Forge 61.x, Java 21.
