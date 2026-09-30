# apw (Another Pennyworth)

A desktop task manager built with Java 21, JavaFX, and MySQL. It supports task descriptions and details, completion status, categories, and subtasks.

## Requirements

- JDK 21
- Apache Maven
- MySQL Server

## Setup

Clone the repository:

```bash
git clone https://github.com/gusutabo/apw
cd apw
```

The application creates the `apw` database and its tables at startup. The configured MySQL user must be allowed to create databases and tables. The schema can also be reviewed in [`schema.sql`](schema.sql).

By default, the application connects to `localhost:3306` as `root` with an empty password. Set these environment variables to use different credentials or connection URLs:

| Variable | Default | Purpose |
| --- | --- | --- |
| `APW_DB_USER` | `root` | MySQL username |
| `APW_DB_PASSWORD` | empty | MySQL password |
| `APW_DB_SERVER_URL` | `jdbc:mysql://localhost:3306/?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true` | MySQL server URL used to create the database |
| `APW_DB_URL` | `jdbc:mysql://localhost:3306/apw?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true` | JDBC URL for the application database |

For example, in a Bash-compatible shell:

```bash
export APW_DB_USER=apw_user
export APW_DB_PASSWORD=your_password
```

## Run

Start the application with Maven:

```bash
mvn clean javafx:run
```

## License

This project is licensed under the MIT License.
