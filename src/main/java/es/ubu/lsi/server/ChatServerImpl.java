package es.ubu.lsi.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
 * 
 * @author Adrián Zamora Sánchez (azs1004@alu.ubu.es)
 */
public class ChatServerImpl implements ChatServer{
    private final int DEFAULT_PORT = 1500;
    
    private SimpleDateFormat sdf = new SimpleDateFormat();
    private ServerSocket serverSocket;
    
    private int clientid = 0; // Los IDs de cliente empiezan en 0
    private int port = DEFAULT_PORT;
    private boolean alive;
    
    // Listas de clientes y baneos
    private final Map<Integer, ServerThreadForClient> clients = new HashMap<>();
    private final Map<String, String> bannedClients = new HashMap<>();
    
    /**
     * Clase interna que escucha los mensajes que vienen del servidor
     * 
     * Implementa la interfaz Runnable para su ejecución
     */
    class ServerThreadForClient extends Thread {
    	private int id;
    	private String username;
        private boolean running = true;
        
    	private Socket clientSocket;
        private ObjectOutputStream out;
    	
    	/**
    	 * Constructor de la clase
    	 * 
    	 * @param socket   Socket del cliente
    	 * @param id	   ID del cliente
    	 * @param username Nombre del cliente
    	 */
        public ServerThreadForClient(Socket socket, int id, String username) {
            this.clientSocket = socket;
            this.username = username;
            this.id = id;
        }
        
        /**
         * Método que envia mensajes utilizando el socket
         * 
         * @param message Mensaje a enviar
         */
        public void sendMessage(ChatMessage message) {
            if (out != null) {
                try {
					out.writeObject(message);
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        }
        
        /**
         * Getter de id
         * 
         * @return id ID del cliente
         */
        public long getId() {
        	return id;
        }
        
        /**
         * Getter de username
         * 
         * @return username Nombre del cliente
         */
        public String getUsername(){
        	return username;
        }
    	
    	/**
		 * Método que se ejecuta y escucha/envia los mensajes
	     */
        public void run() {
            try {
            	// Objetos de lectura y envio de objetos
	        	ObjectInputStream  in = new ObjectInputStream(clientSocket.getInputStream());
	            out = new ObjectOutputStream(clientSocket.getOutputStream());
	            
	            // Deja un log para el nuevo cliente conectado
	            System.out.println("Cliente conectado: " + clientSocket.getInetAddress() + "/" + clientSocket.getPort());

                while (running) {
					// Deserializar el objeto ChatMessage
					ChatMessage chatMessage = (ChatMessage) in.readObject();
					
					// Comprueba de que tipo de mensaje se trata
					switch (chatMessage.getType()) {
						case HANDSHAKE:
							// El servidor recibe en el handsake el username del cliente
							this.username = chatMessage.getMessage().replace("\"", "");
							clientid++; // Se aumenta el id para el siguiente cliente
							
							// El servidor devuelve el hadshake asignandole un ID
							ChatMessage handshake = new ChatMessage("server", -1 ,MessageType.HANDSHAKE, Integer.toString(id));
							out.writeObject(handshake);
							break;
						case TEXT:
							// Deja un log del mensaje recibido y lo reenvia a todos
					        System.out.println("Mensaje de " + id + ": " + chatMessage.getMessage());
					        broadcast(chatMessage);
					        break;
					    case BAN:
					    	// Deja un log de la solicitud de baneo y la ejecuta
					        System.out.println(id + " ha baneado a " + chatMessage.getMessage());
					        ban(chatMessage.getMessage(), username);
					        break;
					    case UNBAN:
					    	// Deja un log de la solicitud de desbaneo y la ejecuta
					        System.out.println(id + " ha desbaneado a " + chatMessage.getMessage());
					        unban(chatMessage.getMessage(), username);
					        break;
					    case LOGOUT:
					    	// Deja un log de la desconexión y elimina el cliente
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
				// Se elimina el cliente y cierra el socket
                remove(id);
                
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("Error al cerrar socket: " + e.getMessage());
                }
                
                System.out.println("Cliente desconectado con ID: " + id);
            }
        }

		/**
		 * Metodo para detener el hilo desde el servidor
		 */
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

    /**
	 * Método que inicia el servidor de chat
	 */
	public void startup() {
		try {
			// 
			serverSocket = new ServerSocket(port);
			
            // Iniciar hilo separado para leer comandos desde la consola
            new Thread(this::readServerCommands).start();
            
            while (alive) {
                try {
                	// Espera conexiones de clientes
                    Socket clientSocket = serverSocket.accept();

                    // Genera e inicia un hilo para este nuevo cliente
                    ServerThreadForClient hilonuevocliente = new ServerThreadForClient(clientSocket, clientid, "NULL");
                    hilonuevocliente.start();

                    // Añade el nuevo cliente a la lista
                    clients.put(hilonuevocliente.id, hilonuevocliente);
                } catch (IOException e) {
                	// Si el servidor se ha apagado termina el bucle
                    if (!alive) break;
                    System.out.println("Error en la conexión: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }
	
	/**
	 * Hilo que lee los comandos en el servidor, como salir o eliminar
	 */
	private void readServerCommands() {
        try {
        	// Lee la entrada estándar
    		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String userInput;
            
            // Expresión regular de una eliminación
            Pattern removePattern = Pattern.compile("^eliminar (\\d+)$", Pattern.CASE_INSENSITIVE);

            while ((userInput = stdIn.readLine()) != null && alive) {
            	// Apagado del servidor
                if (userInput.equalsIgnoreCase("shutdown")) {
                    shutdown();
                    break;
                }

                // Se comprueba si es una eliminación de un cliente
                Matcher matcher = removePattern.matcher(userInput);
                if (matcher.matches()) {
                	// Se toma el ID a eliminar y se elimina el cliente
                    int userId = Integer.parseInt(matcher.group(1));
                    remove(userId);
                }
            }
            
            return;
        } catch (IOException e) {
            System.out.println("Error leyendo comandos del servidor: " + e.getMessage());
        }
    }
	
	/**
	 * Función que establece un baneo entre dos clietnes
	 * 
	 * @param bannedClient Nombre del usuario baneado
	 * @param bannerClient ID del usuario que banea
	 */
	public void ban(String bannedClient, String bannerClient) {
	    // Verifica que el cliente no se banee a si mismo
		if(bannedClient == bannerClient) {
			System.out.println("Un cliente no se puede banear a si mismo");
			return;
		}
		
		// Verificar si ya está baneado
	    if (bannedClients.containsKey(bannerClient) && bannedClients.get(bannerClient).equals(bannedClient)) {
	        System.out.println(bannedClient + " ya estaba baneado por " + bannerClient);
	        return;
	    }

	    // Agregar baneo
	    bannedClients.put(bannerClient, bannedClient);
	    System.out.println(bannedClient + " ha sido baneado por: " + bannerClient);
    }
	
	/**
	 * Función que desbanea a un cliente previamente baneado
	 * 
	 * @param unbannedClient Nombre del usuario baneado
	 * @param bannerClient   ID del usuario que banea
	 */
	public void unban(String unbannedClient, String bannerClient) {
		// Verificar si el usuario estaba baneado anteriormente
	    if (!bannedClients.containsKey(bannerClient) || !bannedClients.get(bannerClient).equals(unbannedClient)) {
	        System.out.println("No se puede desbanear a " + unbannedClient + ": no estaba baneado por " + bannerClient);
	        return;
	    }

	    // Levanta el baneo
	    bannedClients.remove(bannerClient);
	    System.out.println(unbannedClient + " ha sido desbaneado por: " + bannerClient);
    }

	/**
     * Método que detiene el servidor de chat
     */
	public void shutdown() {
		// Establece como apagado el servidor
		alive = false;
		System.out.println("Servidor apagado");
		
		// Mensaje de desconexión por servidor apagado
		ChatMessage desconectionMessage = new ChatMessage("server", -1, MessageType.LOGOUT, "Servidor apagado");
		
		// Se envia una señal de desconexión a los clientes
		for (ServerThreadForClient client : clients.values()) {
            client.sendMessage(desconectionMessage);
        }
		
		// Se cierra el ServerSocket
	    try {
	        serverSocket.close();
	    } catch (IOException e) {
	        System.out.println("Error cerrando el servidor: " + e.getMessage());
	    }
	}

	/**
     * Método que reenvia el mensaje a todos los clientes que tienen
     * conexión con el servidor.
     * 
     * @param message Mensaje que se reenvia a todos los clientes
     */
	public void broadcast(ChatMessage message) {
		/* Se añade el texto <Mensaje patrocinado por Adrián> para 
		   cumplir el requisito de la práctica */
		String msg = "<Mensaje patrocinado por Adrián> " + message.getClientName().replace("\"", "").trim() + ": " + message.getMessage();
		message.setMessage(msg);
        
        for (ServerThreadForClient client : clients.values()) {
        	// Se verifica que el cliente receptor ha baneado al cliente emisor
            if (!bannedClients.containsKey(client.getUsername())) {
                // Si el cliente receptor no ha baneado al emisor, enviar el mensaje
                client.sendMessage(message);
            }
        }
    }

	/**
     * Método que elimina a un cliente
     * 
     * @param id Identificador del cliente cuya conexión se elimina
     */
	public void remove(int id) {
		// Se comprueba si se puede eliminar al cliente y se obtiene su ID
	    ServerThreadForClient client = clients.remove(id);
	    
	    // Comprueba que exissta el cliente
	    if (client != null) {
	        System.out.println("Cliente con ID: " + id + " eliminado");

	        // Se envia un mensaje de desconexión
	        ChatMessage logoutMessage = new ChatMessage("server", -1, MessageType.LOGOUT, "Has sido eliminado del servidor.");

	        // Se envia el mensaje al cliente eliminado y detiene el hilo
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
		// Inicia el servidor en el puerto 1500 por defecto
		ChatServerImpl server = new ChatServerImpl(1500);
        server.startup();
	}
}
