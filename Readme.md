![Result image](assets/scalest_logo.png)
[![Build Status](https://travis-ci.com/0lejk4/Scalest.svg?branch=master)](https://travis-ci.com/0lejk4/Scalest)

## Description

Scalest is a set of helpful features for Akka.
The main one of them is the admin panel which is an aggregator of all other features.
In future there will be healthchecks, more CMS-like features, swagger, cors stuff and much more.
The main thing Scalest makes easier is fast creation of any type of application.
For example the admin panel can be very helpful for developing chatbots,
as a tool for the support team or for the developers themselves.

To start using Scalest add this to `build.sbt`:

```scala
resolvers += Resolver.sonatypeRepo("snapshots")
lazy val scalestV: String = "0.0.2-SNAPSHOT"
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
val routes = AdminExtension(ModelAdmin(crudRepository)).route
```

You can find full example in [here](./examples/src/main/scala/pet/)
if you run it you will see such admin panel:
![Models list](assets/admin_panel.png)

## Contribution

You can easily create Backend implementation for your language. 
For more details contact me.

## Modules

- *Core* - general features that are helpful for creating applications with Scalest
- *Admin* - admin panel module that creates UI for your entities
- *Admin-Slick* - slick backend for Admin module
- *Examples* - example applications using Scalest
- *Admin-Vue* - Frontend implementation of Scalest Admin Panel
