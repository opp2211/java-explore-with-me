# ExploreWithMe (Исследуй со мной)
Приложение позволяет пользователям делиться информацией об интересных событиях и находить компанию для участия
в них.

Реализовано на базе микросервисной архитектуры (2 реализованных сервиса):  
1 - Основной сервис  
2 - Сервис статистики  
Предполагается, что обращение к основному серверу происходит через шлюз, который в свою очередь использует
систему аутентификации и авторизации, а затем перенаправляет запрос.

Спецификации сервисов для Swagger:
+ [Основной сервис](https://raw.githubusercontent.com/yandex-praktikum/java-explore-with-me/main/ewm-main-service-spec.json)
+ [Сервис статистики](https://raw.githubusercontent.com/yandex-praktikum/java-explore-with-me/main/ewm-stats-service-spec.json)

Стек технологий: Spring Boot, Spring Data JPA, Hibernate, PostgreSQL, Docker.
