![Result image](assets/scalest.png)

## Description
Scalest is Productivity-oriented Scala Web framework inspired by Python Django and based on Akka. 
The main problem it tries to solve is faster creation of web applications with more focus on 
business domain rather than on writing routine CRUD code and UI for working with it.

## Preview
You only need to provide CRUD repository and mount generated route in your application,
for example like this:
```scala
val adminExtension = new AdminExtension(ModelAdmin(petRepository))

val routes = cors() {
  adminExtension.route
}
```
And this is how you get such admin panel:
>Item list:

![Models list](assets/list.png)

>Create item:

![Models create](assets/create.png)

>Edit item:

![Models edit](assets/edit.png)

## Modules
- Core - distage based akka application aka Scalest application
- Admin - automatic admin panel for Models
- Admin-Slick - slick backend for Scalest-Admin module
- Examples - example applications using Scalest