# desktop

Десктопный клиент экспедиторской компании на JavaFX 21.

## Запуск

Нужен JDK 21 или новее.

```bash
./mvnw javafx:run
```

На Windows в PowerShell: `.\mvnw.cmd javafx:run`.

## Структура

```text
src/main/java/com/beta/expedition/desktop
├── App.java            точка входа
├── controller/         контроллеры FXML
├── service/            бизнес-правила
├── api/                интерфейсы API и реализации (mock, http)
└── model/              DTO

src/main/resources/com/beta/expedition/desktop
├── view/               FXML-разметка
└── css/                стили
```

Слои: View (FXML) → Controller → Service → Api → mock или http.
