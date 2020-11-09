# Адаптер для заглушек

[![Build Status](http://ci.rbkmoney.com/buildStatus/icon?job=rbkmoney_private/proxy-mocketbank/master)](http://ci.rbkmoney.com/job/rbkmoney_private/proxy-mocketbank/master)

Сервис предназначен для эмулирования запросов между различными системами


### Developers

- [Anatoly Cherkasov](https://github.com/avcherkasov)


### Оглавление

1. [Настройки](docs/settings.md)
1. [Структура проекта](docs/structure.md)


### Отправка запросов на сервис

##### Для внешних запросов
```
http(s)://{host}:8080/mocketbank
```

##### Для трифтовых запросов
```
http(s)://{host}:8022/proxy/mocketbank - эквайринг
http(s)://{host}:8022/proxy/mocketbank/p2p-credit - выплаты
http(s)://{host}:8022/proxy/mocketbank/p2p - p2p
http(s)://{host}:8022/proxy/mocketbank/terminal - оплата через терминал
http(s)://{host}:8022/proxy/mocketbank/dw - оплата с помощью электронных кошельков
http(s)://{host}:8022/proxy/mocketbank/mobile/operator - определение оператора мобильного телефона
http(s)://{host}:8022/proxy/mocketbank/mobile - оплата с мобильного телефона
```
