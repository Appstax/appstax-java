# Appstax Java SDK

This is the official Java SDK for [Appstax](https://appstax.com).
Please read the [Android Guide](https://appstax.com/docs/Android-SDK-Guide) to get up and running.

## Example

```java
Ax.setAppKey("YourAppKey");

AxObject object = new AxObject("Contacts");
object.put("name", "Foo McBar");
object.put("email", "foo@example.com");

Ax.save(object);
```

## License

[MIT License](LICENSE)
