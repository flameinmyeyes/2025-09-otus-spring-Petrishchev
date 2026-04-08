# Union App

Приложение для профсоюзов с чатами, голосованиями и событиями.

---

# Функционал

## Профсоюзы

* создание и удаление профсоюзов
* управление участниками

## Чаты

* общий чат профсоюза
* приватные сообщения
* редактирование и удаление сообщений
* real-time обновления через WebSocket

## Голосования

* создание голосований
* редактирование
* удаление
* участие пользователей

## События

* создание событий
* редактирование
* удаление

---

# Архитектура

## Backend

* Spring Boot
* Spring Web
* Spring Data JPA
* WebSocket (STOMP)
* H2 DB
---

## Frontend

* React + TypeScript
* Axios — HTTP клиент
* SockJS + STOMP — WebSocket

---
#  Запуск проекта

## Backend

```
cd union
mvn clean install 
mvn spring-boot:run
```

---

## Frontend

```
cd union
cd frontend
npm install
$env:PORT=3000; npm start
$env:PORT=3001; npm start
```

---