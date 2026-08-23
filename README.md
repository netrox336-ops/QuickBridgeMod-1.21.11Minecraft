# QuickBridge — Minecraft 1.21.11

**QuickBridge** — клиентский Forge-мод для автоматизированного бриджинга. Он управляет движением, камерой и постановкой блоков, ориентируясь на состояние мира и реальные подтверждения сервера, а не просто проигрывая фиксированный AHK-таймер.

> Автоматизированный bridging может быть запрещён правилами отдельных серверов. В QuickBridge нет обхода античита, маскировки или функций скрытия автоматизации. Используйте мод только там, где такие функции разрешены.

## Что нового в v0.8.0

Главное изменение — **Smart Placement Planner**. Раньше Bridge Engine пробовал небольшой фиксированный набор target-блоков. Теперь перед постановкой QuickBridge строит набор возможных позиций, проверяет их и ранжирует.

Planner учитывает:

- фактическое направление движения;
- placement lead выбранной техники;
- текущую скорость игрока;
- reliability текущего запуска;
- число доступных соседних блоков для attachment;
- дистанцию до точки клика;
- отклонение от ожидаемой линии моста;
- небольшие боковые альтернативы, если прямой target неудобен;
- pending placements, которые ещё ждут подтверждения сервера.

В итоге Bridge Engine сначала пробует наиболее подходящий target, а не просто первую рассчитанную координату.

### Path Probe

Новый **Path Probe** постоянно просматривает участок примерно на 2.4 блока по направлению движения и оценивает:

- расстояние до первого gap;
- расстояние до препятствия на уровне ног/головы;
- долю существующей опоры впереди;
- длину непрерывного gap.

Обычный void впереди не считается ошибкой — для bridging это ожидаемое состояние.

### Path Guard

Если Path Probe видит настоящее препятствие непосредственно по направлению движения, **Path Guard** приостанавливает автоматическое движение и удерживает sneak. Мод не продолжает слепо толкать игрока в стену или блок.

Path Guard и Smart Placement можно отключать независимо.

### Planner Diagnostics

Diagnostic HUD теперь показывает:

- сколько placement-кандидатов было просканировано;
- сколько из них признано пригодными;
- ранг выбранного target;
- score выбранного варианта;
- число planner misses;
- расстояние до первого gap/obstacle;
- support ratio;
- длину обнаруженного gap;
- состояние `CLEAR / BLOCKED`.

## Execution Guard

Execution Profiles из v0.7 сохраняются. Для Telly, Speed Telly, Blink и Andromeda отдельно задаются границы фаз, placement window, минимальная скорость burst, допустимая ошибка yaw/pitch и безопасная точка повторного входа после recovery.

Execution Guard разрешает placement только когда совпадают нужная фаза, placement window, состояние движения и допустимая точность камеры.

После recovery сложная техника возвращается к безопасной точке цикла вместо продолжения старой фазы вслепую.

## Condition-Aware Learning

QuickBridge использует двухслойное обучение:

1. общий server baseline;
2. отдельный learned-профиль для `STABLE`, `DELAYED` или `UNSTABLE`.

Network Profile Selector использует hysteresis, поэтому профиль не переключается после одного случайно плохого цикла. После переключения параметры плавно смешиваются.

Ручная калибровка пользователя не перезаписывается Auto Learning.

## Training Mode

Training Mode позволяет тестировать технику без изменения постоянного learning-профиля:

- bridging, planner, Execution Guard и recovery продолжают работать;
- HUD считает качество и успешность циклов;
- persistent Server Learning не получает новые samples.

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

Подробности по техникам находятся в `docs/TECHNIQUES.md`.
