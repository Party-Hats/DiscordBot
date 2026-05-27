# DiscordBot

A small Spring Boot Discord bot that manages temporary ("join to create") voice channels.

When a member joins a voice channel named **`create-voice-channel`**, the bot creates a new
voice channel (`Talk_1`, `Talk_2`, …) and moves the member into it. Created channels are
removed automatically once they become empty.

## Tech stack

- Java 21
- Spring Boot 3.5
- Discord4J 3.2

## Prerequisites

- JDK 21+ (the build is pinned to `--release 21`)
- Maven 3.9+ — or just use Docker (see below), which builds inside a container
- A Discord bot token from the [Discord Developer Portal](https://discord.com/developers/applications)

## Token setup

The bot reads its token from a plain-text file (the value is trimmed, so a trailing newline
is harmless). The path depends on the active Spring profile:

| Profile                 | Token path          |
|-------------------------|---------------------|
| `development` (default) | `./token`           |
| `container` (Docker)    | `/mnt/secret/token` |

Create a `token` file in the project root containing your bot token:

```bash
echo "YOUR_BOT_TOKEN" > token
```

`token` is already in `.gitignore`, so it will not be committed.

## Run from sources

### Option A — Maven (local JVM)

Build the executable jar and run it:

```bash
mvn clean package
java -jar target/TwitchBot.jar
```

Or run without packaging first:

```bash
mvn spring-boot:run
```

Both use the default `development` profile and read the token from `./token`.

### Option B — Docker Compose

`docker-compose.yml` mounts the project root to `/mnt/secret/` in the container and runs with
the `container` profile, so the same `token` file in the project root is picked up.

```bash
docker compose build      # builds and tags the discord-bot:latest image
docker compose up -d      # start in the background (add --build to force a rebuild)
docker compose logs -f    # follow the logs
docker compose down       # stop
```
