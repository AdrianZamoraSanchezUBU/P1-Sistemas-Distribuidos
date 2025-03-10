package es.ubu.lsi.client;

import es.ubu.lsi.common.ChatMessage;

/**
 * Implementación de la clase ChatClient
 * 
 * Esta clase realiza las comunicaciones del cliente con el servidor
 * de chat, enviando y recibiendo los mensajes
 */
public class ChatClientImpl implements ChatClient {
    private String server = "127.0.0.1"; // Por defecto localhost
    private String username;
    private int port = 1500; // Por defecto 1500
    private boolean carryOn;
    private int id;

    /**
     * Clase interna que escucha los mensajes que vienen del servidor
     * 
     * Implementa la interfaz Runnable para su ejecución
     */
    protected class ChatClientListener implements Runnable {
		/**
		 * Método que se ejecuta y escucha los mensajes
	     */
        public void run(){

        }
    }
    
    /**
     * Constructor de la clase ChatClientImpl
     * 
     * @param server   Dirección del servidor de chat
     * @param port     Puerto del servidor de chat
     * @param username Nombre de usuario del cliente
     */
    public ChatClientImpl(String server, int port, String username){
    	this.server = server;
    	this.port = port;
    	this.username = username;
    }

    public boolean start(){
    	// TODO
        return true;
    }

    public void sendMessage(ChatMessage msg){
    	// TODO
    }

    public void disconect(){
    	// TODO
    }

    /**
     * Método principal para ejecutar el cliente
     * 
     * @param args Argumentos de línea de comandos
     */
    public void main(String[] args){
    	// TODO
    }
}
