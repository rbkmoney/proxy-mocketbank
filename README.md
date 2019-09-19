# Proxy Test

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

```

Конфигурация для docker-compose

```
version: '2'
services:

  proxy_mocketbank:
    depends_on:
      - cds
      - proxy_mocketbank_mpi
    image: dr.rbkmoney.com/rbkmoney/proxy-mocketbank:last
    environment:
      - SERVICE_NAME=proxy_mocketbank
    command: |
      -Xms64m -Xmx256m
      -jar /opt/proxy-mocketbank/proxy-mocketbank.jar
      --logging.file=/var/log/proxy-mocketbank/proxy-mocketbank.json
      --server.secondary.ports=8080
      --server.port=8022
      --cds.url.storage=http://cds:8022/v1/storage
      --cds.url.keyring=http://cds:8022/v1/keyring
      --hellgate.url=http://hellgate:8022/v1/proxyhost/provider
      --proxy-mocketbank.callbackUrl=http://proxy-mocketbank:8080
      --proxy-mocketbank-mpi.url=http://proxy-mocketbank-mpi:8080
    working_dir: /opt/proxy-mocketbank
    restart: on-failure:3

  proxy_mocketbank_mpi:
    image: dr.rbkmoney.com/rbkmoney/proxy-mocketbank-mpi:last
    environment:
      - SERVICE_NAME=proxy_mocketbank_mpi
    ports:
      - "8018:8080"
    command: |
      -Xms64m -Xmx256m
      -jar /opt/proxy-mocketbank-mpi/proxy-mocketbank-mpi.jar
      --logging.file=/var/log/proxy-mocketbank-mpi/proxy-mocketbank-mpi.json
      --proxy-mocketbank-mpi.callbackUrl=http://proxy-mocketbank-mpi:8080
    working_dir: /opt/proxy-mocketbank-mpi
    restart: on-failure:3
    
  cds:
     image: dr.rbkmoney.com/rbkmoney/cds:last
     environment:
       - SERVICE_NAME=cds
     command: /opt/cds/bin/cds foreground
     restart: on-failure:3
    
networks:
  default:
    driver: bridge
    driver_opts:
      com.docker.network.enable_ipv6: "true"
      com.docker.network.bridge.enable_ip_masquerade: "true"
```
