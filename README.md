# apw (Another Pennyworth)

A desktop task manager built with Java 21, JavaFX, and MySQL. It supports task descriptions and details, completion status, categories, subtasks, and a kanban board (To do / In progress / Done) with drag and drop.

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

## Windows installer

The GitHub Actions workflow builds a Windows x64 installer on every push and pull request. It can also be started manually from the Actions tab. Download the `apw-windows-x64-installer` artifact and run `APW-1.0.0.msi`. The installer includes the Java runtime and creates Start Menu and desktop shortcuts. MySQL Server must be installed and running separately. The default connection is `localhost:3306` with user `root` and an empty password; configure the `APW_DB_*` environment variables above to use different credentials or URLs.

## Kanban

Use the **List / Board** switch at the top to change views. In the board, drag a card to another column to change its status, or click it to edit it in the details panel. The **Status** field in the details panel does the same thing, and *Done* is kept in sync with *Mark as completed*.

Databases created by older versions get the new `status` column automatically at startup (tasks already completed go to *Done*).

## License

This project is licensed under the MIT License.
