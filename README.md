# apw (Another Pennyworth)

A desktop task manager built with Java 21, JavaFX, and SQLite. It supports task descriptions and details, completion status, categories, subtasks, and a kanban board (To do / In progress / Done) with drag and drop. The database is created automatically; no database server or setup is required.

## Requirements

- JDK 21
- Apache Maven

## Setup

Clone the repository:

```bash
git clone https://github.com/gusutabo/apw
cd apw
```

On first launch, APW creates its SQLite database at `.apw/apw.db` inside the current user's home directory. Back up `apw.db` to preserve task data. The schema is available in [`schema.sql`](schema.sql). Existing data in a MySQL database from an older version is not imported automatically.

## Run

Start the application with Maven:

```bash
mvn clean javafx:run
```

## Windows build

The GitHub Actions workflow builds a self-contained Windows x64 application on every push and pull request. It can also be started manually from the Actions tab. Download the `apw-windows-x64` artifact, extract it, and run `bin/app.exe`. The image includes its Java runtime and does not require MySQL.

## Kanban

Use the **List / Board** switch at the top to change views. In the board, drag a card to another column to change its status, or click it to edit it in the details panel. The **Status** field in the details panel does the same thing, and *Done* is kept in sync with *Mark as completed*.

Databases created by older versions get the new `status` column automatically at startup (tasks already completed go to *Done*).

## License

This project is licensed under the MIT License.
