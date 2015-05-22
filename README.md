# Appstax Java SDK

This is the official Java SDK for [Appstax](https://appstax.com), used by the Android SDK.

## Example usage

```java
Ax.setAppKey("YourAppKey");

AxObject object = new AxObject("Contacts");
object.put("name", "Foo McBar");
object.put("email", "foo@example.com");
Ax.save(object);
```
