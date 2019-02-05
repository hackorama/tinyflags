# Tiny Flags

Minimal version of Flags micro service.

```
$ mvn spring-boot:run
...
Usage: java App properties.xml

```

```
$ mvn spring-boot:run -Dspring-boot.run.arguments="continents.txt"
...

2019-02-04 16:56:33.331  INFO 52504 --- [main] com.hackorama.flags.App : Started App in 2.657 seconds (JVM running for 7.282)

```

```
$ curl http://localhost:8080/flags/USA
{"USA":"🇺🇸"}
```
