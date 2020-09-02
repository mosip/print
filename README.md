# Introduction

This is the *subscriber* entity implementation of the *WebSub protocol*. The *websub* protocol consists of
three entities, namely:

- hub
- publisher
- subscriber

The needed dependencies to build this *subscriber* implementation are the following:

- Java 11
- Spring boot framework jars
- Two **MOSIP** WebSub jars

After cloning this repo, first install the **MOSIP** *WebSub* jars by running the script below:

```
./install_mosip_jars.sh
```

For testing purposes, this *subscriber* uses the configurations values below in `application.properties` setting file and
using https://flask-websub.readthedocs.io/en/latest/ playing both the role as the *publisher* and the *hub*.

```
mosip.event.hubURL=http://localhost:14344/hub
mosip.event.callbackURL=http://localhost:8080/print/enqueue
mosip.partner.id=http://localhost:7070
```

### Notes

Additional work needed to complete the subscription flow.

