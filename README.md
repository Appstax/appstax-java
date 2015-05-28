# Appstax Java SDK

This is the official Java SDK for [Appstax](https://appstax.com).

It's a work in progress, and not ready for use quite yet.

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
