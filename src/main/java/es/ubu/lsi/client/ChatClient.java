package es.ubu.lsi.client;

import es.ubu.lsi.common.ChatMessage;

/**
 * Interfaz de la clase cliente
 * 
 * @author Adrián Zamora Sánchez (azs1004@alu.ubu.es)
 */
public interface ChatClient {

	/**
     * Inicia la conexión con el servidor y hace el handshake
     * 
     * @return Devuelve true si la conexión se inicia correctamente 
     */
	boolean start();

    /**
     * Envía un mensaje al servidor
     * 
     * @param msg Mensaje a enviar
     */
    void sendMessage(ChatMessage msg);

    /**
     * Desconecta el cliente del servidor
     */
    void disconnect();
}
