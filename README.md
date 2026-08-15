# sharazan-security

**Sharazan** — модульный бэкенд-фреймворк на Kotlin, вдохновлённый архитектурой Ktor (declare-then-start композиция через Koin, без Spring-магии).

**security** — hand-rolled, Spring-Security-inspired модуль аутентификации/авторизации: Basic/Form/JWT-логин, cookie-based сессии, ролевая авторизация через `AuthorizeHttpRequests` DSL.

## Стек

- jbcrypt (BCrypt)
- jjwt (JWT)
- core, http, logging (sharazan)

## Maven-координаты

```kotlin
implementation("com.github.37hulk37:sharazan-security:1.0.0")
```
