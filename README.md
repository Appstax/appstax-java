# Appstax Java SDK 

[![Build Status](https://travis-ci.org/Appstax/appstax-java.svg?branch=master)](https://travis-ci.org/Appstax/appstax-java)
[![Download](https://api.bintray.com/packages/appstax/maven/appstax-java/images/download.svg) ](https://bintray.com/appstax/maven/appstax-java/_latestVersion)

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

