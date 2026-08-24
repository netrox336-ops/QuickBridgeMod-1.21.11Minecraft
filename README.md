# QuickBridge — Minecraft 1.21.11

**QuickBridge** — клиентский Forge-мод для автоматизированного бриджинга. Он управляет движением, камерой и постановкой блоков, ориентируясь на состояние мира, реальные подтверждения сервера и траекторию игрока, а не просто проигрывая фиксированный AHK-таймер.

> Автоматизированный bridging может быть запрещён правилами отдельных серверов. В QuickBridge нет обхода античита, маскировки или функций скрытия автоматизации. Используйте мод только там, где такие функции разрешены.

## Что нового в v0.9.0

Главное изменение — **Route Stability Layer**. QuickBridge теперь фиксирует центральную ось моста при запуске техники и отслеживает накопленный боковой дрейф игрока относительно этой линии.

### Route Stability

Route Stability работает поверх обычного movement-профиля и не заменяет механику самой техники. Для Breezily, Witchly и Moonwalk центральная ось считается отдельно от их штатного strafe-паттерна, поэтому коррекция не пытается убрать саму технику.

Во время движения мод считает:

- текущий боковой drift игрока;
- сглаженный drift;
- пройденное расстояние вдоль оси;
- силу текущей коррекции;
- отклонение последнего подтверждённого блока;
- средний placement spread;
- количество re-anchor событий.

Коррекция применяется плавно к мировому вектору движения. Резкого snap-поворота камеры или мгновенной смены направления нет.

### Режимы коррекции

Доступны три режима:

- **Мягко** — минимальное вмешательство;
- **Нормально** — стандартный режим;
- **Сильно** — более узкая линия и более заметная коррекция.

При сильном отклонении QuickBridge усиливает возвращение к линии и, если включён Sneak Assist, удерживает sneak до стабилизации траектории.

### Lane-aware Smart Placement

Smart Placement Planner из v0.8 теперь учитывает Route Stability. Кандидат, который технически можно поставить, но который заметно расширяет мост или уводит его от центральной линии, получает дополнительный штраф к score.

Таким образом planner учитывает одновременно:

- placement lead;
- attachment-соседей;
- reach;
- направление движения;
- текущую скорость и reliability;
- боковое отклонение target от маршрута.

Pending placements всё так же исключаются до получения server ACK.

### Re-anchor

Маршрут автоматически переякоривается при смене техники или большом скачке позиции, например после телепорта. Это не даёт старой оси продолжать тянуть игрока к уже неактуальной линии.

## Smart Placement и Path Guard

`PlacementPlanner` рассматривает несколько продольных и боковых target-кандидатов и выбирает лучший по score. `PathProbe` анализирует ближайший участок по направлению движения, а `Path Guard` останавливает автоматическое движение перед настоящим препятствием. Обычный void впереди не считается препятствием.

## Execution Guard

Для Telly, Speed Telly, Blink и Andromeda используются отдельные execution-профили с фазами, placement window, минимальной burst-speed и допустимой ошибкой yaw/pitch.

Execution Guard разрешает placement только когда состояние техники действительно готово к постановке. После recovery сложная техника возвращается в безопасную точку цикла.

## Condition-Aware Learning

QuickBridge использует двухслойное обучение:

1. общий server baseline;
2. отдельный learned-профиль для `STABLE`, `DELAYED` или `UNSTABLE`.

Network Profile Selector использует hysteresis, а изменения между профилями смешиваются плавно. Ручная калибровка пользователя не перезаписывается Auto Learning.

## Training Mode

Training Mode позволяет тестировать технику без изменения постоянного learning-профиля. Bridging, Route Stability, planner, Execution Guard и recovery продолжают работать, а persistent Server Learning не получает новые samples.

## Техники

Доступно 14 профилей:

- Ninja
- Diagonal Ninja
- Breezily
- Witchly
- God Bridge
- Jump God
- Telly
- Speed Telly
- Side Bridge
- Time Bridge
- Andromeda
- Blink Bridge
- Slope Bridging
- Moonwalk

Профили с `[EXP]` остаются экспериментальными и требуют практической калибровки под конкретную физику сервера и задержку соединения.

## Управление по умолчанию

| Клавиша | Действие |
| --- | --- |
| `B` | запуск/удержание QuickBridge |
| `N` | следующая техника |
| `K` | аварийный STOP |
| `O` | настройки |

Все клавиши можно переназначить через стандартное меню Minecraft **Controls**.

## Основные настройки

- Smart Placement;
- Route Stability;
- режим Route Correction;
- Path Guard;
- Execution Guard;
- Auto Select Blocks;
- Sneak Assist;
- Placement Recovery;
- число recovery attempts;
- confirmation window;
- Fail-stop;
- Restore View;
- Diagnostic HUD;
- Auto Learning;
- Learning HUD;
- Training Mode;
- Network Profiles;
- Network Guard.

## Требования

- Minecraft Java **1.21.11**
- Forge **61.x** — проект собирается на **61.2.0**
- Java **21**

## Установка

1. Установите Forge для Minecraft 1.21.11.
2. Скачайте JAR из GitHub Releases.
3. Поместите JAR в `.minecraft/mods`.
4. Запустите Minecraft с профилем Forge.

Подробности: `docs/TECHNIQUES.md`, `docs/PLANNER.md` и `docs/ROUTE_STABILITY.md`.
