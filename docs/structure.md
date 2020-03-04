## Структура проекта


```
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── rbkmoney
│   │   │           └── proxy
│   │   │               └── mocketbank
│   │   │                   ├── ProxyMocketBankApplication.java
│   │   │                   ├── configuration - конфигурационные файлы
│   │   │                   │   └── properties - параметры
│   │   │                   ├── controller - контроллеры для обработки внешних запросов
│   │   │                   ├── decorator - декораторы
│   │   │                   ├── exception - исключения
│   │   │                   ├── handler - обработчики
│   │   │                   │   ├── mobile - определение мобильного оператора и оплата
│   │   │                   │   ├── oct - выплаты
│   │   │                   │   ├── p2p
│   │   │                   │   ├── payment - эквайринговые платежи
│   │   │                   │   └── terminal - оплата через терминал
│   │   │                   ├── service - сервисы в данном проекте
│   │   │                   │   └── mpi
│   │   │                   ├── servlet - эндпоинты
│   │   │                   ├── utils - вспомогательные классы
│   │   │                   └── validator - валидаторы
│   │   └── resources
│   │       ├── application.yml
│   │       ├── fixture
│   │       │   ├── cards.csv - банковские карты под разное поведение
│   │       │   ├── errors.json - маппинг ошибок
│   │       │   └── mobilephone.csv - список мобильных телефонов для разного поведения
```
