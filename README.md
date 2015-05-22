# Appstax Java SDK

This is the official Java SDK for [Appstax](https://appstax.com), used by the Android SDK. 

Please read the [Android Guide](https://appstax.com/docs/Android-SDK-Guide) to get up and running.

## Example usage

```java
Ax.setAppKey("YourAppKey");

AxObject object = new AxObject("Contacts");
object.put("name", "Foo McBar");
object.put("email", "foo@example.com");
Ax.save(object);
```

See the [Android Guide](https://appstax.com/docs/Android-SDK-Guide) for more info on how to set up your app and data model.

License
-------

[MIT License](LICENSE)
