# Better Log4j Config

A simple mod for [Quilt](https://quiltmc.org) and [Fabric](https://fabricmc.net/) to automatically reconfigure [Log4j](https://logging.apache.org/log4j/2.x/index.html) to improve the formatting of log messages.

## Screenshots

### Before

![before](.github/assets/before.png)

### After

![after](.github/assets/after.png)

## What does it do?

As can be seen from the above screenshots, the main difference this mod makes is the addition of the name of the mod responsible for each log message.
This small change makes logs significantly more readable, as without it there is no reliable way to distinguish between log messages from different mods.
The screenshots also highlight how some mods attempt to individually remedy this issue by manually including their name in the content of the log message.
This mod offers a solution to this problem by configuring the logging system itself to always include the mod name in the log messages.
The format of the log messages can also be further customised with a [config file](https://logging.apache.org/log4j/2.x/manual/configuration.html#XML), which by default is `better_log4j_config.xml` in the mod config directory.

## Version Support

Better Log4j Config is compatible with every version of Minecraft that is supported by the Quilt and Fabric mod loaders - so 1.14 onwards. The Forge mod loader already has built-in functionality to reconfigure Log4j, so there are no plans to port the mod to it.

## Reporting Issues

If for any reason the mod isn't working, please [open an issue](https://github.com/Pixelstormer/better_log4j_config/issues/new) so it can be fixed. Remember to include the versions of both Minecraft and the mod loader you're using, as well as any other mods you have installed, and also a copy of the log file. (Hint: You can add `-Dlog4j2.debug=true` as a command-line argument to Minecraft to produce substantially more verbose logs.)

## License

Better Log4j Config is licensed under the [Apache 2.0 license](./LICENSE).
