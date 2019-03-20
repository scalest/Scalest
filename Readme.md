![Result image](assets/scalest.png)
[![Build Status](https://travis-ci.com/0lejk4/Scalest.svg?branch=master)](https://travis-ci.com/0lejk4/Scalest)

## Description

Scalest is set of helpful features for Akka.
First and the main of them is Admin panel which is aggregator of all other features.
In future there will be healthchecks, more CMS-like features, swagger and cors stuff and far more.
The main thing Scalest makes is easier - faster creation of any type of application.
For instance such admin panel can be very helpful for developing chatbots,
as tool for support team or for developer himself.

To start using Scalest add this to `build.sbt`:

```scala
resolvers += Resolver.sonatypeRepo("snapshots")
lazy val scalestV: String = "0.0.0-SNAPSHOT"
libraryDependencies ++= {
  Seq(
    "io.github.0lejk4" %% "scalest-core" % scalestV, // core,
    "io.github.0lejk4" %% "scalest-admin" % scalestV, // generic admin panel
    "io.github.0lejk4" %% "scalest-admin-slick" % scalestV, // slick concrete admin panel
  )
}
```

## Admin panel preview

You only need to provide CRUD repository,
 provide entity scheme to ModelAdmin(or it will be generated automatically)
 and mount adminExtension route in your Akka application,
for example like this:

```scala
val routes = new AdminExtension(ModelAdmin(crudRepository)).route
```

You can find full example in [here](./examples/src/main/scala/pet/)
if you run it you will see such admin panel:
![Models list](assets/admin_panel.png)

## Modules

- *Core* - general features that are helpful for creating applications with Scalest
- *Admin* - admin panel module that creates UI for your entities
- *Admin-Slick* - slick backend for Admin module
- *Examples* - example applications using Scalest
