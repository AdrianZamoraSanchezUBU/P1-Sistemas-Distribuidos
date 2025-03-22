package es.ubu.lsi.server;

import es.ubu.lsi.common.ChatMessage;

/**
 * Clase del servidor de chat por sockets
 * 
 * @author Adrián Zamora Sánchez (azs1004@alu.ubu.es)
 */
public interface ChatServer {
	/**
	 * Método que inicia el servidor de chat
	 */
    public void startup();
    
    /**
     * Método que detiene el servidor de chat
     */
    public void shutdown();
    
    /**
     * Método que reenvia el mensaje a todos los clientes que tienen
     * conexión con el servidor.
     * 
     * @param message Mensaje que se reenvia a todos los clientes
     */
    public void broadcast(ChatMessage message);
    
    /**
     * Método que elimina a un cliente
     * 
     * @param id Identificador del cliente cuya conexión se elimina
     */
    public void remove(int id);
}
