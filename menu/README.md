## Регистрация и Авторизация

### Регистрация

Создает пользователя в бд
Адрес: ```/create-user```\
Тело:

```
{
"username": string,
"password": string
}
```

### Авторизация:

Авторизация добавляет токен в куки\
Адрес: ```/login2```\
Тело:

```
{
"username": string,
"password": string
}
```

## Поиск игры
Ищет противника + сразу же коннектит их к комнате 
Доступные типы игры: SHORT_BACKGAMMON \
Адрес ```/menu/connect```\
Тело:

```
{
    "type": ТИП_ИГРЫ
}
```
Возвращает id найденной комнаты
