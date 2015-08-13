# Appstax Java SDK

[![Build Status](https://travis-ci.org/Appstax/appstax-java.svg?branch=master)](https://travis-ci.org/Appstax/appstax-java)
[![Download](https://api.bintray.com/packages/appstax/maven/appstax-java/images/download.svg) ](https://bintray.com/appstax/maven/appstax-java/_latestVersion)

This is the official Java SDK for [Appstax](https://appstax.com).

This is a new SDK, so let us know if you encounter any issues.

## Usage example

```java
Ax ax = new Ax("key");

AxObject object = ax.object("contacts");
object.put("name", "Foo McBar");
object.put("email", "foo@example.com");

ax.save(object);
```

## License

[MIT License](LICENSE)

