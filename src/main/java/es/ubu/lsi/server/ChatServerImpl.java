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
    private final Map<Integer, Integer> bannedClients = new HashMap<>();
    
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
        private boolean running = true;
    	
    	/**
    	 * Constructor de la clase
    	 * 
    	 * @param socket Socket del cliente
    	 * @param id	 ID del cliente
    	 */
        public ServerThreadForClient(Socket socket, int id, String username) {
            this.clientSocket = socket;
            this.username = username;
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
         * 
         */
        public long getId() {
        	return id;
        }
        
        /**
         * 
         */
        public String getUsername(){
        	return username;
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
	
	                while (running) {
						// Deserializar el objeto ChatMessage
						ChatMessage chatMessage = (ChatMessage) in.readObject();
						
						switch (chatMessage.getType()) {
						    case TEXT:
						        System.out.println("Mensaje de " + id + ": " + chatMessage.getMessage());
						        broadcast(chatMessage);
						        break;
						    case BAN:
						        System.out.println(id + " ha baneado a " + chatMessage.getMessage());
						        ban(chatMessage.getMessage(), id);
						        break;
						    case LOGOUT:
						        System.out.println(id + " se ha desconectado");
						        remove(id);
						        return;
						    default:
						    	break;
						}
	                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error I/O: " + e.getMessage());
			} finally {
                remove(id);
                
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("Error al cerrar socket: " + e.getMessage());
                }
                System.out.println("Cliente desconectado con ID: " + id);
            }
        }

		// Método para detener el hilo
	    public void stopRunning() {
	        running = false;
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
		            
		        	System.out.println("Nuevo Cliente: " + clientSocket.getInetAddress() + "/" + clientSocket.getPort() + " \"" + "TODO" + '"');
		        	
		        	ServerThreadForClient hilonuevocliente = new ServerThreadForClient(clientSocket, clientid++, "TODO");
		        	
		        	clients.put(hilonuevocliente.id, hilonuevocliente);
		        	hilonuevocliente.start();
				}
	        } catch (IOException e) {
	        	System.out.println(e.getMessage());
	        }
	}
	
	private void readServerCommands() {
        try (
        		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        	) 
        	{
	            String userInput;
	            Pattern removePattern = Pattern.compile("^remove (\\d+)$", Pattern.CASE_INSENSITIVE);

	            while ((userInput = stdIn.readLine()) != null) {
	                if (userInput.equalsIgnoreCase("shutdown")) {
	                    shutdown();
	                    break;
	                }
	
	                Matcher matcher = removePattern.matcher(userInput);
	                
	                if (matcher.matches()) {
	                    int userId = Integer.parseInt(matcher.group(1));
	                    remove(userId);
	                }
	            }
	        } catch (IOException e) {
	            System.out.println("Error leyendo comandos del servidor: " + e.getMessage());
	        }
    }
	
	/**
	 * Función que establece un baneo entre dos clietnes
	 * 
	 * @param clientName Nombre del usuario baneado
	 * @param bannerId   ID del usuario que banea
	 */
	public void ban(String clientName, int bannerId) {
        
		ServerThreadForClient bannedClient = null;
        
        for (ServerThreadForClient client : clients.values()) {
            if (client.getUsername().equalsIgnoreCase(clientName)) {
                bannedClient = client;
                break;
            }
        }

        if (bannedClient != null) {
            bannedClients.put(bannerId, (int) bannedClient.getId());
            System.out.println(clientName + " ha sido baneado por ID: " + bannerId);
        } else {
            System.out.println("No se encontró el usuario " + clientName);
        }
    }

	public void shutdown() {
		alive = false;
		System.out.println("Servidor apagado");
		
		ChatMessage desconectionMessage = new ChatMessage("server", -1, MessageType.SHUTDOWN, "Servidor apagado");
		
		// Se envia una señal de desconexión a los clientes
		for (ServerThreadForClient client : clients.values()) {
            client.sendMessage(desconectionMessage);
        }
	}

	public void broadcast(ChatMessage message) {
		String msg = "<Mensaje patrocinado por Adrián> " + message.getClientName() + ": " + message.getMessage();
		
		message.setMessage(msg);
        System.out.println("Reenviado mensaje de cliente con id " + message.getClientid());
        
        for (ServerThreadForClient client : clients.values()) {
            client.sendMessage(message);
        }
    }

	public void remove(int id) {
		// Se comprueba si se puede eliminar al cliente
	    ServerThreadForClient client = clients.remove(id); // Se obtiene el ID
	    if (client != null) {
	        System.out.println("Cliente con ID: " + id + " eliminado");

	        // Se envia un mensaje de desconexión
	        ChatMessage logoutMessage = new ChatMessage("server", -1, MessageType.LOGOUT, "Has sido eliminado del servidor.");

	        // Se envia el mensaje al cliente eliminado
	        client.sendMessage(logoutMessage);
	        client.stopRunning();
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
