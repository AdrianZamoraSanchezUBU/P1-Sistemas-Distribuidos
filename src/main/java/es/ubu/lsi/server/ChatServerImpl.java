package es.ubu.lsi.server;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        private ObjectOutputStream out;
    	
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
        public void sendMessage(ChatMessage message) {
            if (out != null) {
                try {
					out.writeObject(message);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
    	
    	/**
		 * Método que se ejecuta y escucha/envia los mensajes
	     */
        public void run() {
            try (
            		ObjectInputStream  in = new ObjectInputStream(clientSocket.getInputStream());
            		
            ) {
	                out = new ObjectOutputStream(clientSocket.getOutputStream());
	                System.out.println("Cliente conectado: " + clientSocket.getInetAddress() + "/" + clientSocket.getPort());
	
	                while (true && alive == true) {
	                	// Deserializar el objeto ChatMessage
	                    ChatMessage chatMessage = (ChatMessage) in.readObject();
	                    System.out.println("Mensaje de " + clientSocket.getPort() + ": " + chatMessage.getMessage());
	                    broadcast(new ChatMessage(username, MessageType.TEXT, chatMessage.getMessage()));
	                }
            } catch (IOException e) {
                System.out.println("Error I/O: " + e.getMessage());
            } catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		try (
				ServerSocket serverSocket = new ServerSocket(port);
				

			)
        	{
	            // Iniciar hilo separado para leer comandos desde la consola
	            new Thread(this::readServerCommands).start();
	            
				while (true && alive == true){
		            Socket clientSocket = serverSocket.accept();
		            
		        	System.out.println("Nuevo Cliente: " + clientSocket.getInetAddress() + "/" + clientSocket.getPort());
		        	
		        	ServerThreadForClient hilonuevocliente = new ServerThreadForClient(clientSocket, clientid++);
		        	
		        	clients.put(hilonuevocliente.id, hilonuevocliente);
		        	hilonuevocliente.start();
				}
	        } catch (IOException e) {
	        	System.out.println("Exception caught when trying to listen on port " + port + " or listening for a connection");
	        	System.out.println(e.getMessage());
	        }
	}
	
	private void readServerCommands() {
        try (
        		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        	) 
        	{
	            String userInput;
	            Pattern banPattern = Pattern.compile("^ban (\\d+)$", Pattern.CASE_INSENSITIVE);

	            while ((userInput = stdIn.readLine()) != null) {
	                if (userInput.equalsIgnoreCase("shutdown")) {
	                    shutdown();
	                    break;
	                }
	
	                Matcher matcher = banPattern.matcher(userInput);
	                if (matcher.matches()) {
	                    int userId = Integer.parseInt(matcher.group(1));
	                    System.out.println("Baneando al usuario con ID: " + userId);
	                    remove(userId);
	                }
	            }
	        } catch (IOException e) {
	            System.out.println("Error leyendo comandos del servidor: " + e.getMessage());
	        }
    }

	public void shutdown() {
		alive = false;
		System.out.println("Servidor apagado");
	}

	public void broadcast(ChatMessage message) {
        System.out.println("Mensaje patrocinado por Adrián: " + message.getMessage());
        
        for (ServerThreadForClient client : clients.values()) {
            client.sendMessage(message);
        }
    }

	public void remove(int id) {
		// Se comprueba si se puede eliminar al cliente
		if(clients.remove(id) != null) {
			System.out.println("Cliente con ID:" + Integer.toString(id) + " eliminado");
		}else {
			System.out.println("No se ha podido banear al cliente con ID: " + Integer.toString(id));
		}
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
