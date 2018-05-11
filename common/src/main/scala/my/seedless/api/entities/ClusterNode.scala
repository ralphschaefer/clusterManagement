package my.seedless.api.entities

import my.seedless.api.Payload

case class ClusterNode(host:String, port: Int) extends Payload