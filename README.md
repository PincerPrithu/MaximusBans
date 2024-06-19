# MaximusBans

MaximusBans is a comprehensive and configurable banning and muting plugin for Minecraft servers. It supports various databases, including MySQL, Postgres, Microsoft SQL Server, H2, Derby, HSQLDB, and SQLite. The plugin allows server administrators to ban, mute, temp-ban, and temp-mute, blacklist and warn players, as well as manage these punishments with ease.

## Features

- Support for multiple databases (MySQL, Postgres, Microsoft SQL Server, H2, Derby, HSQLDB, and SQLite).
- Permanent and temporary bans and mutes.
- IP-based bans and mutes.
- Configurable ban and mute reasons.
- Supports unban/unmute reasons for better tracking of staff actions.
- Command-based history lookup for player and issuer punishments.
- Fully asynchronous detection and handling of banned or muted players during login and chat events.
- Customizable messages for bans, mutes, and pardons.
- Highly customisable and modular
- Rollback system, roll back the actions of any staff member with one command (coming soon).

## Installation

1. Download the latest release of MaximusBans from the (https://github.com/PincerPrithu/MaximusBans/releases).
2. Place the `MaximusBans.jar` file in your server's `plugins` directory.
3. Start your server to generate the default configuration files.
4. (OPTIONAL) Edit the `config.yml` file in the `plugins/MaximusBans` directory to configure your database settings and other options. Only needed if you are using a proxy or want to sync punishments across multiple servers.
5. Restart your server to apply the changes.

## Configuration

### Database Configuration

The `config.yml` file allows you to specify the database type and connection details. Here is an example configuration:

```yaml
# Defines which database type to use.
# Options are MySQL, Postgres, Microsoft SQL Server, H2, Derby, HSQLDB, and Sqlite.
# If you only have one server use Sqlite or H2. MySQL will be perfect for multiple servers or bungee.
DB-type: MySQL

# Database connection details
DB-host: "localhost"
DB-port: "3306"
DB-name: "maximusbans"
DB-user: "myusername"
DB-password: "supersecretpass"
```

### Main Plugin Configuration

The main plugin configuration file allows you to customize various aspects of the plugin, such as messages and behavior. Edit the `config.yml` file to adjust these settings.

### Ban Messages Configuration

Changes the messages displayed to the user when they are banned. Edit the files in `MaximusBans/messages` to adjust these settings.

## Commands
MaximusBans uses a tag system for simplicity and convenience. All punishments support the -ip tag, which specifies that the punishment is for an IP address rather than a player UUID. The -anticheat and -automod tags can only be added by the console. These tags help differentiate between different types of automated punishments.

If you are auto-banning using an anticheat, use the -anticheat tag in your command. Similarly, if you are auto-banning using an automated moderation tool, use the -automod tag. Here are some examples:

- Staff Ban: /tempban PlayerName 10d This is the reason
- Staff IP Ban: /ban PlayerName 10d This is the reason -ip
- Anticheat IP Ban: /ban PlayerName 30d Hacking -ip -anticheat

### Ban Commands

- `/ban <Player|IP> [reason] (tags -anticheat, -automod, -ip)`: Permanently bans a player or IP address.
- `/tempban <Player|IP> <duration> [reason] (tags -anticheat, -automod, -ip)`: Temporarily bans a player or IP address.

### Mute Commands

- `/mute <Player|IP> [reason] (tags -automod, -ip)`: Permanently mutes a player or IP address.
- `/tempmute <Player|IP> <duration> [reason] (tags -automod, -ip)`: Temporarily mutes a player or IP address.

### Pardon Commands

- `/unban <Player|IP> [reason]`: Unbans a player or IP address.
- `/unmute <Player|IP> [reason]`: Unmutes a player or IP address.

### History Command

- `/history <player/issuer> <Username/UUID> [page]`: Displays the punishment history for a player or issuer.

## Permissions

- `maximusbans.ban`: Allows the use of the `/ban` command.
- `maximusbans.tempban`: Allows the use of the `/tempban` command.
- `maximusbans.mute`: Allows the use of the `/mute` command.
- `maximusbans.tempmute`: Allows the use of the `/tempmute` command.
- `maximusbans.unban`: Allows the use of the `/unban` command.
- `maximusbans.unmute`: Allows the use of the `/unmute` command.
- `maximusbans.history`: Allows the use of the `/history` command.

## Development

### Building from Source

To build MaximusBans from source, you will need to have [Maven](https://maven.apache.org/) installed.

1. Clone the repository:
   ```sh
   git clone https://github.com/your-username/MaximusBans.git
   cd MaximusBans
   ```

2. Compile and package the plugin:
   ```sh
   mvn clean package
   ```

3. The compiled JAR file will be located in the `target` directory.

### Contributing

Contributions are welcome! Please open an issue or submit a pull request on GitHub.

## License

This project is licensed under the MIT License.
## Contact

For support or inquiries, please open an issue on GitHub or join our discord https://discord.gg/SUfjFDVDsn.
