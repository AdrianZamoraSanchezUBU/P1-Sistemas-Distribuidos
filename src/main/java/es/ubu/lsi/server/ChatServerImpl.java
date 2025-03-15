package es.ubu.lsi.server;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import es.ubu.lsi.common.ChatMessage;
import es.ubu.lsi.common.MessageType;

/**
 * Interfaz para el servirdor de chat
 */
public class ChatServerImpl implements ChatServer{
    private final int DEFAULT_PORT = 1500;
    
    private int clientid = 0;
    private SimpleDateFormat sdf = new SimpleDateFormat();
    private int port = DEFAULT_PORT;
    private boolean alive;
    
    private final Map<Integer, ServerThreadForClient> clients = new HashMap<>();
    
    /**
     * Clase interna que escucha los mensajes que vienen del servidor
     * 
     * Implementa la interfaz Runnable para su ejecución
     */
    class ServerThreadForClient extends Thread {
    	private int id;
    	private String username;
    	private Socket clientSocket;
        private PrintWriter out;
    	
    	/**
    	 * Constructor de la clase
    	 * 
    	 * @param socket Socket del cliente
    	 * @param id	 ID del cliente
    	 */
        public ServerThreadForClient(Socket socket, int id) {
            this.clientSocket = socket;
            this.id = id;
        }
        
        /**
         * 
         * 
         * @param message
         */
        public void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }
    	
    	/**
		 * Método que se ejecuta y escucha/envia los mensajes
	     */
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            ) {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress() + "/" + clientSocket.getPort());

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Mensaje de " + clientSocket.getPort() + ": " + inputLine);
                    broadcast(new ChatMessage(username, MessageType.TEXT, inputLine));
                }
            } catch (IOException e) {
                System.out.println("Error I/O: " + e.getMessage());
            } finally {
                remove(id);
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("Error al cerrar socket: " + e.getMessage());
                }
                System.out.println("Cliente desconectado: " + clientSocket.getInetAddress() + "/" + clientSocket.getPort());
            }
        }
    }
		    
    
    /**
     * Constructor de la clase ChatServerImpl
     * 
     * @param port Puerto que queda expuesto por el servidor
     */
    public ChatServerImpl(int port) {
		this.port = port;
    	
    	this.alive = true;
    }

	public void startup() {
		try  (
            	ServerSocket serverSocket = new ServerSocket(port);
   		)
        {
			while (true){
	            Socket clientSocket = serverSocket.accept();
	            
	        	System.out.println("Nuevo Cliente: " + clientSocket.getInetAddress() + "/" + clientSocket.getPort());
	        	
	        	ServerThreadForClient hilonuevocliente = new ServerThreadForClient(clientSocket, clientid++);
	        	
	        	hilonuevocliente.run();
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port " + port + " or listening for a connection");
            System.out.println(e.getMessage());
        }
		
	}

	public void shutdown() {
		alive = false;
		System.out.println("Servidor apagado");
	}

	public void broadcast(ChatMessage message) {
        System.out.println("Mensaje patrocinado por Adrián: " + message.getMessage());
        
        for (ServerThreadForClient client : clients.values()) {
            client.sendMessage(message.getMessage());
        }
    }

	public void remove(int id) {
		clients.remove(id);
        System.out.println("Cliente con ID:" + Integer.toString(id) + " eliminado");
	}
	
	/**
     * Método principal para ejecutar el servidor
     * 
     * @param args Argumentos de línea de comandos
     */
	public static void main(String[] args) {
		ChatServerImpl server = new ChatServerImpl(1500);
        server.startup();
	}
}
