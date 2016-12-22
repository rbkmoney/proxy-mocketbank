# Proxy Test

[![Build Status](http://ci.rbkmoney.com/buildStatus/icon?job=rbkmoney_private/proxy-test/master)](http://ci.rbkmoney.com/job/rbkmoney_private/proxy-test/master)

Сервис предназначен для эмулирования запросов между процессингом и банковском


### Developers

- [Anatoly Cherkasov](https://github.com/avcherkasov)


### Оглавление:

1. [Техническая документация](docs/tech.md)
1. [Полезные ссылки](docs/useful_links.md)
1. [FAQ](docs/faq.md)


Отправка запросов на сервис:
```
http(s)//{host}:{port}/proxy/test
```

Конфигурация для docker-compose

```
version: '2'
services:

  proxy_test:
    depends_on:
      - cds
      - proxy_test_mpi
    image: dr.rbkmoney.com/rbkmoney/proxy-test:last
    environment:
      - SERVICE_NAME=proxy_test
    command: |
      -Xms64m -Xmx256m
      -jar /opt/proxy-test/proxy-test.jar
      --logging.file=/var/log/proxy-test/proxy-test.json
      --server.secondary.ports=8080
      --server.port=8022
      --cds.url.storage=http://cds:8022/v1/storage
      --cds.url.keyring=http://cds:8022/v1/keyring
      --hellgate.url=http://hellgate:8022/v1/proxyhost/provider
      --proxy-test.callbackUrl=http://proxy-test:8080
      --proxy-test-mpi.url=http://proxy-test-mpi:8080
    working_dir: /opt/proxy-test
    restart: on-failure:3

  proxy_test_mpi:
    image: dr.rbkmoney.com/rbkmoney/proxy-test-mpi:last
    environment:
      - SERVICE_NAME=proxy_test_mpi
    ports:
      - "8018:8080"
    command: |
      -Xms64m -Xmx256m
      -jar /opt/proxy-test-mpi/proxy-test-mpi.jar
      --logging.file=/var/log/proxy-test-mpi/proxy-test-mpi.json
      --proxy-test-mpi.callbackUrl=http://proxy-test-mpi:8080
    working_dir: /opt/proxy-test-mpi
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
