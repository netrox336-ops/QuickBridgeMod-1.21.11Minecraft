# QuickBridge — Minecraft 1.21.11

**QuickBridge** — клиентский Forge-мод для автоматизированного бриджинга. Он управляет движением, камерой и постановкой блоков, ориентируясь на состояние мира и реальные подтверждения сервера, а не просто проигрывая фиксированный AHK-таймер.

> Автоматизированный bridging может быть запрещён правилами отдельных серверов. В QuickBridge нет обхода античита, маскировки или функций скрытия автоматизации. Используйте мод только там, где такие функции разрешены.

## Что нового в v0.6.0

Главное изменение — **Condition-Aware Learning**. Теперь QuickBridge хранит не один learned-профиль на сервер и технику, а использует два слоя:

1. общий server baseline, в котором остаётся накопленное обучение прошлых версий;
2. отдельный профиль для текущего состояния сети: `STABLE`, `DELAYED` или `UNSTABLE`.

Это позволяет одной и той же технике иметь разные небольшие поправки для хорошего и плохого соединения, не смешивая их в один профиль.

### Network Profile Selector

Состояние сети оценивается по тем данным, которые реально видит клиент:

- задержка подтверждения поставленных блоков;
- reliability placements;
- частота recovery;
- качество законченных bridge-циклов.

Переключение профиля не происходит после одного плохого цикла. Используется hysteresis:

- ухудшение состояния подтверждается быстрее;
- возврат к более стабильному профилю требует большей серии успешных циклов;
- после переключения параметры старого и нового профиля плавно смешиваются.

HUD показывает активный профиль, candidate-condition, число подтверждающих циклов, progress blend и количество переключений.

### Network Guard

Отдельный **Network Guard** добавляет небольшую консервативную runtime-поправку при `DELAYED` и `UNSTABLE`: цикл слегка замедляется, placement lead становится осторожнее, а cadence снижается. Guard можно отключить отдельно от Auto Learning и Network Profiles.

### Обратная совместимость

Старые learning-данные v0.4/v0.5 не теряются. Они остаются общим baseline. Новые condition-бакеты сохраняются рядом в `config/quickbridge-learning.properties`.

Для каждого сервера и каждой техники появляются отдельные:

- `STABLE` profile;
- `DELAYED` profile;
- `UNSTABLE` profile.

В статистике можно сбросить только активный сетевой профиль, не удаляя общий baseline и данные других условий.

## Обучение

QuickBridge использует несколько уровней обратной связи:

- **Placement feedback** — подтверждён ли конкретный блок и сколько тиков занял ACK;
- **Cycle Learning** — насколько успешно прошёл полный state-machine cycle;
- **Server baseline** — общая адаптация конкретной техники к серверу;
- **Condition profile** — дополнительная поправка под текущее состояние соединения;
- **Learning Checkpoints / Auto Rollback** — возврат к последнему хорошему learned-состоянию после устойчивого ухудшения.

Ручная калибровка пользователя никогда не перезаписывается Auto Learning.

## Training Mode

Тренировочный режим позволяет тестировать технику без загрязнения постоянного learning-профиля:

- bridging и recovery продолжают работать;
- HUD считает качество и успешность циклов;
- persistent Server Learning не получает новые samples;
- тренировочную статистику можно сбрасывать отдельно.

## Recovery

Recovery работает отдельной последовательностью:

`HOLD → REALIGN → RETRY → RESUME`

Если сервер вовремя не подтвердил блок, QuickBridge останавливает обычный цикл, удерживает игрока, доворачивает камеру к проблемной позиции, повторяет placement и только затем возвращается к bridging.

Для Telly, Speed Telly, Andromeda и Blink используются более осторожные recovery-параметры.

## Техники

В QuickBridge доступны 14 профилей:

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

## Калибровка

Для каждой техники отдельно доступны:

- **Длина цикла**;
- **Упреждение блока**;
- **Скорость поворота**;
- **Cadence bias**.

Редактор показывает ручные, learned и итоговые effective-параметры. Ручную калибровку и обучение можно сбрасывать независимо.

## Управление по умолчанию

| Клавиша | Действие |
| --- | --- |
| `B` | запуск/удержание QuickBridge |
| `N` | следующая техника |
| `K` | аварийный STOP |
| `O` | настройки |

Все клавиши можно переназначить через стандартное меню Minecraft **Controls**.

## Настройки

Доступны:

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

## HUD

Диагностический HUD может показывать:

- текущую технику и фазу;
- количество блоков;
- confirmed / recovery / failed placements;
- edge distance;
- скорость игрока;
- adaptive cadence;
- текущий ACK;
- reliability;
- качество последнего bridge-cycle;
- observed Network Condition;
- активный Network Profile;
- candidate-condition и hysteresis progress;
- transition blend;
- статистику активного condition bucket;
- rollback и training statistics.

## Требования

- Minecraft Java **1.21.11**
- Forge **61.x** — проект собирается на **61.2.0**
- Java **21**

## Установка

1. Установите Forge для Minecraft 1.21.11.
2. Скачайте JAR из GitHub Releases.
3. Поместите JAR в `.minecraft/mods`.
4. Запустите Minecraft с профилем Forge.

Более подробное описание профилей находится в `docs/TECHNIQUES.md`.
