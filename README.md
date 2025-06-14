# User Config Resolver Library
The User Config Resolver library is a Java-based utility that provides a flexible way to resolve user configuration settings 
based on user groups and custom expressions. It allows you to define configuration override rules,
apply them conditionally, and generate resolved configuration objects or strings.

## Use Case
You have a user based application, and you group your users in different target groups, and what to offer different features for different target groups.
Ex: 
- users < 18 to not see certain features
- new users to simplify the UI
- A/B testing, show different UI to two different user groups and measure various KPI

In order to implement this kind of behaviors you need to have this logic somewhere, and usually is in code:
```
if(user.age < 18){
    // show banner
}else if(user.isNew()){
    // simplify UI
}else if(user.isInGroup("group-a")){
    // show this value 
}else if(user.isInGroup("group-a")){
    // show other value
}else
...
```
But this conditions usually change very often, due to the business doing A/B testing or environment changes, promotions etc...
To change this we need code changes, which will take developers time, testing effort, etc...
Having this logic in configuration, will simplify the process of change, you only update a configuration, no developer effort.
Configuration is in Json that is easy to understand by many non-technical people. 

Ideal would be that you have a UI, where operational team can update configuration, and from where you generate this json configuration.
This Json configuration is feed into the application, that uses the configuration. 

## Features
- <b>User Group-Based Configuration Override:</b> You can define configuration override rules based on user groups. These rules specify which configuration properties should be overridden when a user belongs to specific groups.
- <b>Custom Expression Support:</b> In addition to group-based rules, you can define custom expressions using Spring Expression Language (SpEL) to create more complex conditions for configuration overrides.
- <b>Configurable Transformation:</b> The library supports resolving configurations into different target classes, such as Java objects or JSON strings, giving you flexibility in how you use the resolved configuration.

## How to Use
<b>Dependency Setup:</b>

Add the config-resolver-api dependency to your project, this will provide the interfaces for config resolving.
```
<dependency>
    <groupId>org.godari.config-resolver</groupId>
    <artifactId>config-resolver-api</artifactId>
    <version>1.0.0</version>
</dependency>
```
Add the config-resolver-json dependency to your project. This will provide the json implementation of this config resolver.
Currently only json config is supported, but in the future other implementations can come as well.
```
<dependency>
    <groupId>org.godari.config-resolver</groupId>
    <artifactId>config-resolver-json</artifactId>
    <version>1.0.0</version>
</dependency>
```
<b>Define Configuration Override Rules:</b> Define your configuration override rules by specifying user groups, 
custom expressions, and the properties to override in your configuration JSON.

```
{
  "override-rules": [
    {
      "user-is-in-all-groups": ["paid-user","premium-user"],
      "override": {
        "show-adds": false
      }
    },
    {
      "user-is-in-any-group": ["new-joiner"],
      "override": {
        "show-new-joiner-banner": true,
        "show-full-layout": false
      }
    },
    {
      "user-is-none-of-the-groups": ["button-blue"],
      "override": {
        "button-color": "gray"
      }
    },
    {
      "custom-expression": "#user.contains('discount') or #user.contains('black-friday')",
      "override": {
        "shop.no-of-products": 20,
        "shop.price-multiplier": 0
      }
    }
  ],
  "default-properties": {
    "show-new-joiner-banner": false,
    "show-adds": true,
    "show-full-layout": true,
    "button-color": "blue",
    "shop": {
      "no-of-products": 10,
      "price-multiplier": 2
    }
  }
}
```
There is are some default properties, that apply to all users, and then based on what group is the user, 
override rules apply. Override rules apply from top to bottom, meaning that the bottom one has priority and will override the already overriden properties. 

<b>Resolve Configuration:</b> Use your custom ConfigResolver service to resolve configurations based on user groups and custom expressions.
```
// inject bean
@Autowired
ConfigResolverService configResolverService;

// Resolve config as Java object    
Config resolvedConfig = configResolverService.resolveConfig(inputConfig, userGroups, Config.class);

// Resolve config as JSON string
String resolvedConfig = configResolverService.resolveConfig(inputConfig, userGroups);

// Resolve configuration as a Java object
configResolverService.setConfigToResolve(inputConfig);
Config resolvedConfig = configResolverService.resolveConfig(userGroups, Config.class);

// Resolve configuration as a JSON string
configResolverService.setConfigToResolve(inputConfig);
String resolvedConfigJson = configResolverService.resolveConfig(userGroups);

```
<b>Enjoy Flexible Configuration Resolution:</b> Your application can now dynamically resolve configuration settings based on user groups and custom conditions, adapting to different scenarios.

## Example Usage
For a detailed example of how to use the Config Resolver library, refer to the provided unit tests in the config-resolver-json-tests module. These tests showcase various configuration scenarios and demonstrate how to use the library to resolve configurations.

## Dependencies
 - Jackson Databind for JSON parsing and serialization.
 - Lombok for simplified Java code.
 - Spring Expression Language (SpEL) for defining custom expressions.
 - Spring Framework for dependency injection (used in the provided JsonConfigResolverService).

## License
This library is released under the MIT License, allowing you to use it in your projects, modify it, and distribute it as needed.

## Contributions
Contributions to this library are welcome! If you have suggestions, bug reports, or want to contribute code improvements, please create issues and pull requests on the project's GitHub repository.

## Contact
For any questions or further assistance, please contact the project maintainers at kristo.godari@gmail.com.

Happy configuring!