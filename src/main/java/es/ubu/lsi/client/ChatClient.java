package es.ubu.lsi.client;

import es.ubu.lsi.common.ChatMessage;

/**
 * Interfaz de la clase cliente
 */
public interface ChatClient {

	/**
     * Inicia la conexión con el servidor
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
