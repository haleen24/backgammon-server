### Присоединиться к комнате:

Коннектит плеера к игре\
Возвращает пустое тело\
Адрес: ```/game/backgammon/connect/{gameId}```\
gameId - id комнаты\
Тело:

### Подписка на ивенты в комнате

Ходить в обход gateway (на данном этапе gateway блокирует sse)
Адрес: ```/game/backgammon/view/{gameId}```\
gameId - id комнаты\

## Бросок кубиков

Адрес ```/game/backgammon/zar/{gameId}```\
gameId - id комнаты\
Возвращает список чисел

## Ход

Доступные типы игры: SHORT_BACKGAMMON \
Адрес ```/game/backgammon/move/{gameId}```

Ход с бара эквивалентен from == null
Выбивание на бар эквивалентно тому, что первое число == null в теле ответа

gameId - id комнаты\
Тело:

```
{
    "moves": 
    [ 
        {
            "from": int?,
            "to": int?
        },
        {
            "from" : int?,
            "to": int?
        }
    ]
}
```

Возвращает:

```
{
    "moves": {
        int?: int?,
        ...
        int?: int?
    },
    "user": int (userId)
}
```

### Получение конфига комнаты

Адрес: ```/game/backgammon/сonfig/{gameId}```\
gameId - id комнаты\
Пример тела:

```
{
    "color": "WHITE",
    "turn": "BLACK",
    "bar": {
        "BLACK": 0,
        "WHITE": 0
    },
    "deck": [
        {
            "color": "BLACK",
            "count": 2,
            "id": 0
        },
        {
            "color": "WHITE",
            "count": 5,
            "id": 5
        },
        {
            "color": "WHITE",
            "count": 3,
            "id": 7
        },
        {
            "color": "BLACK",
            "count": 5,
            "id": 11
        },
        {
            "color": "WHITE",
            "count": 5,
            "id": 12
        },
        {
            "color": "BLACK",
            "count": 3,
            "id": 16
        },
        {
            "color": "BLACK",
            "count": 5,
            "id": 18
        },
        {
            "color": "WHITE",
            "count": 2,
            "id": 23
        }
    ],
    "zar": [
        3,
        1
    ]
}
```
color - твой цвет\
turn - кто ходит\
bar - инфа по бару\
deck - инфа по деке (только не пустые позиции)\
zar - инфа по кубикам