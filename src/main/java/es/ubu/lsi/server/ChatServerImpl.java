package es.ubu.lsi.server;

import java.text.SimpleDateFormat;

import es.ubu.lsi.common.ChatMessage;

/**
 * Interfaz para el servirdor de chat
 */
public class ChatServerImpl implements ChatServer{
    final int DEFAULT_PORT = 1500;
    int clientid = 0;
    SimpleDateFormat sdf = new SimpleDateFormat();
    int port = 0;
    boolean alive = false;
    
    /**
     * Clase interna que escucha los mensajes que vienen del servidor
     * 
     * Implementa la interfaz Runnable para su ejecución
     */
    class serverThreadForClient extends Thread {
    	private int id;
    	private String username;
    	
    	/**
		 * Método que se ejecuta y escucha los mensajes
	     */
    	public void run() {
    		// TODO
    	}
    }
    
    /**
     * Constructor de la clase ChatServerImpl
     * 
     * @param port Puerto que queda expuesto por el servidor
     */
    public ChatServerImpl(int port) {
    	this.port = port;
    }

	public void startup() {
		// TODO
		
	}

	public void shutdown() {
		// TODO
		
	}

	public void broadcast(ChatMessage message) {
		// TODO
		
	}

	public void remove(int id) {
		// TODO
		
	}
	
	/**
     * Método principal para ejecutar el servidor
     * 
     * @param args Argumentos de línea de comandos
     */
	public void main(String[] args) {
		// TODO
	}
}
