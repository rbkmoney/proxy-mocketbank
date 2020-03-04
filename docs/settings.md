## Настройки


#### Общие

Название параметра (при запуске) | Описание | Пример
------------ | ------------- | -------------
**adapter-mock-bank.callbackUrl** | URL адаптера на который будет возвращен ответ с результатом проверки 3DS со стороны банка | http://127.0.0.1:8080
**adapter-mock-bank.pathCallbackUrl** | Путь для URL адаптера на который будет возвращен ответ с результатом проверки 3DS со стороны банка | /mocketbank/term_url{?termination_uri}
**adapter-mock-bank.pathRecurrentCallbackUrl** | Путь для URL адаптера на который будет возвращен ответ с результатом проверки 3DS со стороны банка | /mocketbank/rec_term_url{?termination_uri}
**adapter-mock-mpi.url** | url для взаимодействия с заглушкой mpi | http://127.0.0.1:8079

---

#### CDS

Данные для работы с CDS (Card Data Storage)

Параметры, которые можно переопределить при запуске приложения:

Название | Описание | Пример
------------ | ------------- | -------------
**cds.client.storage.url** | URL для работы с CDS (storage) | http://127.0.0.1:8022/v1/storage
**cds.client.storage.networkTimeout** | таймаут | 5000
**cds.client.identity-document-storage.url** | URL для работы с CDS (storage) | http://127.0.0.1:8022/v1/identity_document_storage
**cds.client.identity-document-storage.networkTimeout** | таймаут | 5000

---

#### HellGate

Данные для работы с HellGate

Параметры, которые можно переопределить при запуске приложения:

Название | Описание | Пример
------------ | ------------- | -------------
**hellgate.client.adapter.url** | URL для работы с процессингом | http://127.0.0.1:8022/v1/proxyhost/provider
**hellgate.client.adapter.networkTimeout** | таймаут | 30000

---

#### Остальные настройки

Название параметра (можно переопределить при запуске) | Название параметра (можно переопределить в настройках) | Описание | Пример
------------ | ------------- | ------------- | -------------
**timer.redirectTimeout** | **redirect_timeout** | Время в течении которого ожидается ответ на suspend (секунды) | 3600
**rest-template.maxTotalPooling** | **-** | размер пула | 200
**rest-template.defaultMaxPerRoute** | **-** | количество потоков на один ендпоинт | 200
**rest-template.requestTimeout** | **-** | таймаут запроса | 60000
**rest-template.poolTimeout** | **-** | таймаут на получение треда из пула | 10000
**rest-template.connectionTimeout** | **-** | таймаут на откртие коннекта к ендпоинту | 10000
**fixture.cards** | **-** | фаил с номерами карт под различное поведение | classpath:fixture/cards.csv
**fixture.mobilephone** | **-** | фаил с номерами телефонов под различное поведение | classpath:fixture/mobilephone.csv
