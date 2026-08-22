# QuickBridge — Minecraft 1.21.11

**QuickBridge** — клиентский Forge-мод для автоматизированного бриджинга. Он управляет движением, камерой и постановкой блоков, ориентируясь на состояние мира и реальные подтверждения сервера, а не просто проигрывая фиксированный AHK-таймер.

> Автоматизированный bridging может быть запрещён правилами отдельных серверов. В QuickBridge нет обхода античита, маскировки или функций скрытия автоматизации. Используйте мод только там, где такие функции разрешены.

## Что нового в v0.7.0

Главное изменение — **Execution Profiles**. Раньше state machine в основном определяла фазу техники по позиции внутри цикла. Теперь сложные техники дополнительно имеют собственные требования к тому, когда placement действительно можно выполнять.

Для Telly, Speed Telly, Blink и Andromeda отдельно задаются:

- границы `RUNUP / JUMP / TURN / BURST / RESET`;
- начало и конец placement window;
- минимальная скорость для burst-фазы;
- допустимая ошибка yaw/pitch;
- безопасная точка повторного входа после recovery;
- необходимость motion/rotation gate.

Witchly, Breezily и Moonwalk также получили execution-профили, но без жёсткой Telly-последовательности.

### Execution Guard

Новый **Execution Guard** проверяет условия непосредственно перед постановкой блока. Для сложных техник placement проходит только если одновременно выполнены нужная фаза, placement window, допустимое состояние движения и достаточная точность камеры.

Guard можно отключить в настройках. При выключении профиль техники продолжает использовать фазовую логику, но motion/rotation ограничения не блокируют placement.

### Adaptive Placement Window

Placement window теперь не полностью статический. В небольших пределах он корректируется по:

- фактической горизонтальной скорости;
- ACK поставленных блоков;
- reliability текущего запуска.

При высокой скорости окно слегка упреждается. При задержанных подтверждениях допускается небольшая ранняя коррекция, а при низкой reliability поздняя часть окна становится короче.

### Rotation Alignment

Rotation Engine теперь сообщает execution-слою фактическую ошибку относительно целевого yaw/pitch. HUD показывает эту ошибку в градусах. Это позволяет не считать камеру «готовой» только потому, что TURN-фаза уже формально закончилась.

### Phase Resync после Recovery

Recovery по-прежнему работает как:

`HOLD → REALIGN → RETRY → RESUME`

Но после `RESUME` сложная техника больше не продолжает старый процент цикла. Она возвращается к безопасной точке execution-профиля, очищает накопленный placement cadence и начинает следующую часть последовательности синхронно.

HUD показывает количество таких phase-resync и сколько placement-попыток Execution Guard заблокировал как преждевременные.

## Condition-Aware Learning

QuickBridge сохраняет двухслойное обучение:

1. общий server baseline;
2. отдельный learned-профиль для `STABLE`, `DELAYED` или `UNSTABLE`.

Network Profile Selector использует hysteresis, поэтому профиль не переключается после одного случайно плохого цикла. После смены состояния старый и новый профиль плавно смешиваются.

**Network Guard** отдельно добавляет небольшую консервативную runtime-поправку при плохих подтверждениях сервера.

Старые learning-данные v0.4–v0.6 сохраняют совместимость.

## Обучение

QuickBridge использует несколько уровней обратной связи:

- **Placement feedback** — подтверждён ли конкретный блок и сколько тиков занял ACK;
- **Cycle Learning** — насколько успешно прошёл полный state-machine cycle;
- **Server baseline** — общая адаптация конкретной техники к серверу;
- **Condition profile** — дополнительная поправка под текущее состояние соединения;
- **Learning Checkpoints / Auto Rollback** — возврат к последнему хорошему learned-состоянию после устойчивого ухудшения.

Ручная калибровка пользователя не перезаписывается Auto Learning.

## Training Mode

Тренировочный режим позволяет тестировать технику без изменения постоянного learning-профиля:

- bridging, Execution Guard и recovery продолжают работать;
- HUD считает качество и успешность циклов;
- persistent Server Learning не получает новые samples;
- тренировочную статистику можно сбрасывать отдельно.

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

Редактор v0.7 дополнительно показывает readonly execution-параметры выбранной техники: placement window, минимальную burst-speed и допустимые yaw/pitch ошибки.

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
- Execution Guard;
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
- текущий ACK и reliability;
- execution motion score;
- yaw/pitch error;
- состояние placement window;
- READY/GUARD/BYPASS для Execution Guard;
- число заблокированных ранних placements;
- число phase-resync после recovery;
- качество bridge-cycle;
- observed/active Network Condition;
- candidate-condition, hysteresis и transition blend;
- статистику condition bucket;
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
