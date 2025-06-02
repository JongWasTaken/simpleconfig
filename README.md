# SimpleConfig
A config system designed for simplicity and ease of use.  
Uses [Mojang DFU codecs](https://github.com/Mojang/datafixerupper) for serialization and deserialization.

## Features
- Allows hooking up custom loggers
- Handles primitive types automatically
- Allows adding codecs for any type
- Save and load any object by supplying a codec for that field
- Every key/value is stored in a static field, intuitive to use
- Allows use of custom DynamicOps using ConfigTranscoder
- Provides helper methods for easily creating Minecraft commands

## Installation
Replace `(latest-version)` with the latest version number.  
You may look for it [here](https://smto.dev/maven/#/releases/dev/smto/simpleconfig).
### Maven
```
<dependency>
    <groupId>dev.smto</groupId>
    <artifactId>simpleconfig</artifactId>
    <version>(latest-version)</version>
</dependency>
```

### Gradle
```
implementation 'dev.smto:simpleconfig:(latest-version)'
```

## Usage
Create a class that holds static fields for your config.  
You may annotate your fields with any of the following annotations:
- `@ConfigAnnotations.Section`
  - Inserts a new section into the config BEFORE the field
- `@ConfigAnnotations.Comment`
  - Adds a comment to the config before the field
  - Supports multiline comments! Use `\n` to separate lines

Now create a new instance of `SimpleConfig` and pass in the path to your config file and the class that holds your config.  
Note that there are multiple constructors, so you can specify a logger, transcoder, and codec overrides if needed.  
The config file will automatically be created/read/written during initialization, so you don't need to do anything.  
Interact with this instance to read and write to your config manually.  
  
Use `SimpleConfig#write()` to manually save the config to disk.  
Use `SimpleConfig#read()` to manually load the config from disk, overwriting any potential unsaved changes you've made.  
`SimpleConfig#reload()` is an alias of `SimpleConfig#read()`.  

Finally, use `SimpleConfig#getMinecraftCommandHelper()` to get a helper for easily creating Minecraft commands.  
Note that this method will return `null` if `com.mojang.brigadier` is not present in the classpath.

## A note on codecs
By default, the codec used for serialization/deserialization is determined by the type of the field.  
The following types have builtin codec mappings:  
- `boolean`
- `byte`
- `short`
- `int`
- `long`
- `float`
- `double`
- `char`
- `String`
- `List` of any of the above types
- `Map` of any of the above types
- `Pair` of any of the above types

The override the codec used for a specific field, pass a map of codec overrides to the constructor of `SimpleConfig`.  
The key is the name of the field, and the value is the codec to use for that field.  
Codec overrides always have priority over builtin codecs.  
  
You can also add or change a codec mapping for any type by using `ConfigCodecs#set()`.  
The key is the name of the type in lowercase (e.g. `field.getType().getSimpleName().toLowerCase()`).  
Adding codecs this way also automatically makes them work in lists/maps/pairs.

## Examples
#### Example: SNBT ConfigTranscoder
```
new ConfigTranscoder<NbtElement>() {
    @Override
    public String processEncoderOutput(Optional<NbtElement> optional) {
        return new StringNbtWriter().apply(optional.orElseThrow());
    }

    @Override
    public NbtElement processDecoderInput(String s) {
        try {
            return StringNbtReader.parse(s);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DynamicOps<NbtElement> getOps() {
        return NbtOps.INSTANCE;
    }
};
```

#### Example: Custom ConfigLogger
`ConfigLoggers.create(ModInitializer.LOGGER::debug, ModInitializer.LOGGER::info, ModInitializer.LOGGER::warn, ModInitializer.LOGGER::error);`  
--- or ---  
```
new ConfigLogger() {
    @Override
    public void debug(String message) {
        ModInitializer.LOGGER.debug(message);
    }

    @Override
    public void error(String message) {
        ModInitializer.LOGGER.error(message);
    }

    @Override
    public void info(String message) {
        ModInitializer.LOGGER.info(message);
    }

    @Override
    public void warn(String message) {
        ModInitializer.LOGGER.warn(message);
    }
};
```

#### Example: Using the Minecraft command helper
```
var helper = ModInitializer.CONFIG_MANAGER.getMinecraftCommandHelper();
CommandManager.literal("config")
    .requires(this::checkPermission)
    .executes(Commands::emptyNoArgument)
    .then((ArgumentBuilder<ServerCommandSource, ?>) this.helper.createCommandNode((context, key, value) -> {
        ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.translatable("command.modid.config.get").append(Text.literal(key)).append(Text.literal(" -> ").append(Text.literal(value))), false);
        return 0;
    }, (context, key, value, success) -> {
        if (success) {
            ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.translatable("command.modid.config.set").append(Text.literal(key)).append(Text.literal(" -> ").append(Text.literal(value))), false);
            return 0;
        }
        ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.translatable("command.modid.config.error").formatted(Formatting.RED), false);
        return 1;
    }));
```