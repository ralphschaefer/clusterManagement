title: akka-management
class: animation-fade
layout: true

.bottom-bar[
  {{title}} <span class="style1">
    [github.com/ralphschaefer/clusterManagement](https://github.com/ralphschaefer/clusterManagement)
  </span>
]

---

class: impact

# {{title}}
## and beyond :-)

---

# Objectives

--

* run an akka cluster without seed node(s)
* use akka-management for cluster bootstrap and management
* run multiple akka nodes on on single instance

--

[akka-management](https://developer.lightbend.com/docs/akka-management/current/index.html) 
is a cluster node management system build with
[akka-http](https://doc.akka.io/docs/akka-http/current/).

Cluster boostraping is achived with so called "Service Discovery".

Service Discovery is a inital bootstrap step in which all nodes running on Instances
are addressed 

---

# Service discovery in akka-management

* DNS
* Kubernetes
* Marathon API
* AWS API

---

# Test setup for akka-dns

To build our cluster, all instances nodes are running in, must be consolidated via dns

```
# dig nodes.mycluster.local
...
;; ANSWER SECTION:
nodes.mycluster.local.	0	IN	A	127.0.0.14
nodes.mycluster.local.	0	IN	A	127.0.0.10
nodes.mycluster.local.	0	IN	A	127.0.0.12
...
```

---
# Configuration
```
akka {
  ...
  management {
    http {
      hostname = 127.0.0.10
      port = 10001
    }
    cluster.bootstrap {
      contact-point-discovery {
        service-name = "nodes"
        service-namespace = "mycluster.local"
      }
      contact-point {
        fallback-port = 10001
      }
    }
  }
  discovery {
    method = "akka-dns"
  }
  ...
}  
``` 
Config for akka-dns querying "nodes.mycluster,local" domain

---
# Basic layout of application

```scala
object Main extends App {
  val system: ActorSystem = ActorSystem("systemName")
  lazy val httpClusterManagement = AkkaManagement(system)
  lazy val clusterBootstrap = ClusterBootstrap(system)
  lazy val cluster: Cluster = Cluster(system)
  httpClusterManagement.start()
  clusterBootstrap.start()
  
  // play with cluster and actors here
  
  httpClusterManagement.stop()
  CoordinatedShutdown.get(system).run(CoordinatedShutdown.unknownReason)
}
```
---
# Run nodes

--
## Configure DNS

append following lines to your local /etc/hosts

```
127.0.0.10      node1.mycluster.local nodes.mycluster.local
127.0.0.12      node2.mycluster.local nodes.mycluster.local
127.0.0.14      node3.mycluster.local nodes.mycluster.local
```
install und restart `dnsmasq` tool

--
## Startup nodes

```
# sbt -J-Dakka.management.http=127.0.0.10 run &
# sbt -J-Dakka.management.http=127.0.0.12 run &
# sbt -J-Dakka.management.http=127.0.0.14 run &
```
--
## Done !
Cluster is up 

---

class: impact

# Done ?

--
## What's about multiple nodes running on just one instance ??

---
# Use SRV entry of DNS record ?

That is what akka-management documentation says about SRV entry: 

> An improved way of DNS discovery are SRV records, which are not yet supported by 
> akka-discovery-dns, but would then allow the nodes to also advertise which port 
> they are listening on instead of having to assume a shared known port 
> (which in the case of the akka management routes is 19999).

[For version 0.12 !](https://developer.lightbend.com/docs/akka-management/current/discovery.html#mechanism-explanation)

---

# Lets dig into the code

--
* [Definition of DnsSimpleServiceDiscovery](https://github.com/akka/akka-management/blob/5ade633259934df21dcb6796e4f764463d1216bf/discovery-dns/src/main/scala/akka/discovery/dns/DnsSimpleServiceDiscovery.scala#L18):

```scala
/**
 * Looks for A records for a given service.
 */
class DnsSimpleServiceDiscovery(system: ActorSystem) extends SimpleServiceDiscovery {
...
}
```

--
* [Excpeted Answer of SimpleServiceDiscovery lookup](https://github.com/akka/akka-management/blob/5ade633259934df21dcb6796e4f764463d1216bf/discovery/src/main/scala/akka/discovery/SimpleServiceDiscovery.scala#L25):

```scala
object SimpleServiceDiscovery {
  /** Result of a successful resolve request */
  final case class Resolved(serviceName: String, addresses: immutable.Seq[ResolvedTarget])
  ...
  
  final case class ResolvedTarget(host: String, port: Option[Int])
  ...
}  
  
```  

--

* therefor port propagation is possible !

---

class: impact
## Build own SimpleServiceDiscovery

--
* Service must be addressable from *any* akka-node

--
* Service must sustain persistance

--
* What service to use ?

--
## simple key/value REST Service !

---
# REST Service 
- store list of akka nodes, defined by host an port
- query complete list
- remove and add node address from list
- akka node list consists of only uniques entries

--

**implemented as part of the github [project](https://github.com/ralphschaefer/clusterManagement/tree/master/register)**

as an:
- REST micorservice
- with embedded persistance

---
# Service Discovery

--
* Implementation

```scala
class MyServiceDiscovery extends SimpleServiceDiscovery {
  override def lookup(name: String, resolveTimeout: FiniteDuration) = {
    NodeRegistry.list().map { res =>
      val JObject(lst:List[JField]) = res.asInstanceOf[All].list
      SimpleServiceDiscovery.Resolved(name,
        lst.map{node => val n = node._2.extract[ClusterNode]
                        ResolvedTarget(n.host,Some(n.port))        
        }
      )
}}}
```

--
* Config in application.conf
 
```
akka {
  management {
    ...
  }
  discovery {
    method = "my.seedless.MyServiceDiscovery"
  }
```

---
# Subscribe node to REST Service

If an akka-node starts it subscribes to the REST Server with its port and 
hostname.

If an akka-node stops it unsubscribes to the REST Server

--
```scala
object Main extends App {
  val nodeInfo = NodeInfo("managementTest")
  NodeRegistry.init(nodeInfo)
  val reg:WriteResult = Await.result(NodeRegistry.register(
      ClusterNode(host = nodeInfo.config.getString("akka.management.http.hostname"),
                  port = nodeInfo.config.getInt("akka.management.http.port"))
    ),timeout.duration
  ).asInstanceOf[WriteResult]
  nodeInfo.startClusterBootstrap()

  // play with cluster and actors
  
  Await.result(
    NodeRegistry.delete(reg.id),
    timeout.duration
  )
  nodeInfo.shutdown()
}

```