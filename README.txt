P1-Sistemas-Distribuidos

Práctica donde se modela un sistema distribuido de comunicación tipo chat por línea de comandos. Las comunicaciones entre clientes y servidores funcionan mediante sockets.

Funciones que implementa el sistema:
- Posibilidad de envío y recepción de mensajes
- Capacidad para banear a un cliente
- Capacidad para desbanear a un cliente

Este proyecto se puede probar y ejecutar con las ordenes de comandos:

Maven
- mvn clean
- mvn compile
- mvn javadoc:javadoc
- mvn exec:java@server
- mvn exec:java@cliente1
- mvn exec:java@cliente2

Ant
- ant clean
- ant compile
- ant jar
- ant javadoc
- ant run-client1
- ant run-client2
- ant run-server