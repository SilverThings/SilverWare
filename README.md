[![Build Status][Travis badge]][Travis build]

[Travis badge]: https://travis-ci.org/SilverThings/SilverWare.svg?branch=devel
[Travis build]: https://travis-ci.org/SilverThings/SilverWare

# SilverWare

__Enterprise ready, minimalistic, easy to use Microservices implementation.__

SilverWare.io is a minimalistic, yet powerful, modularized, highly configurable and container-free. Builds on technologies you are familiar with. Start creating the Internet of Things today! 

We do not reinvent the wheel. We just want to give programmers a tool to concentrate on what do they best - to develop a meaningful business code, now!
There is no need to learn new technologies as we use good old Java, Camel, CDI, Messaging, REST, Vert.x and more.

A service package is always a standalone executable jar file that carries only the neccessary dependencies. The classloader structure is flat.

We don't want you to need overcome obstacles during the development. For that, we don't force you to any generated stub code, instead we provide simple proxy classes
that are still easy to debug.

We achieve easy services monitoring through JMX by incorporating Jolokia.

## What Can We Do?

Enabling the following technologies in on our near-term roadmap. 

* [__CDI__](https://github.com/SilverThings/SilverWare/wiki/CDI-Microservice-Provider) - _WORKS_ - Enables development of business code using simple java classes and injection. Integrates well with Camel.
* __Camel__ - _WORKS_ - Enables automatic startup of Camel routes and components in both Java classes and XML files. Also friends with CDI.
* [__REST__](https://github.com/SilverThings/SilverWare/wiki/http-server-microservice-provider) - _WORKS_ - It is possible to expose a CDI microservice via the REST interface.  
* __Monitoring__ - _NOT TESTED_ - Theoreticaly completed, but not verified with an external monitoring console.
* __ActiveMQ__ - _WORKS_ - Integration between CDI, Camel and ActiveMQ messaging. Microservices should be able to become an ultimate consumer and 
  producer of messges.
* __Vert.x__ - _WORKS_ - Verticles should be deployed as standalone microservices as well.
* __Clustering__ - _WORKS_ - It is possible to call other microservice in a cluster transparently. 
* __Languages__ - _PLANNED_ - Support of development of microservices in other JVM enabled languages.
* __Transaction__ - _PLANNED_ - Integration with Narayana.
* __Security__ - _PLANNED_ - Possible integration with Apiman.

## How Does It Work?

Just have a look on [our quickstarts](https://github.com/SilverThings/SilverWare-Demos)! It is very easy, just a few maven goals and you have your first service running.
