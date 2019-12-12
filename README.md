# Адаптер для заглушек

[![Build Status](http://ci.rbkmoney.com/buildStatus/icon?job=rbkmoney_private/proxy-mocketbank/master)](http://ci.rbkmoney.com/job/rbkmoney_private/proxy-mocketbank/master)

Сервис предназначен для эмулирования запросов между процессингом и банком


### Developers

- [Anatoly Cherkasov](https://github.com/avcherkasov)


### Оглавление:

1. [Техническая документация](docs/tech.md)
1. [Полезные ссылки](docs/useful_links.md)
1. [FAQ](docs/faq.md)


Отправка запросов на сервис:
```
http(s)//{host}:8080/mocketbank - для внешних запросов
http(s)//{host}:8022/proxy/mocketbank - для трифтовых запросов (эквайринг)
http(s)//{host}:8022/proxy/mocketbank/p2p-credit - для трифтовых запросов (p2p credit)
http(s)//{host}:8022/proxy/mocketbank/mobile/operator - для трифтовых запросов для определения оператора мобильного телефона

```
