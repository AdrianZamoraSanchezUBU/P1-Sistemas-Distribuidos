package es.ubu.lsi.client;

import java.io.*;
import java.net.*;

import es.ubu.lsi.common.*;


/**
 * Implementación de la clase ChatClient
 * 
 * Esta clase realiza las comunicaciones del cliente con el servidor
 * de chat, enviando y recibiendo los mensajes
 */
public class ChatClientImpl implements ChatClient {
    private final static int DEFAULT_SERVER_PORT = 1500;
    
    private String server = "127.0.0.1"; // Por defecto localhost
    private String username;
    private int port;
    private int id = -2; // Por defecto -2 hasta recibir uno
    private boolean carryOn;
    
    Socket socket;
    ObjectOutputStream out;

    /**
     * Clase interna que escucha los mensajes que vienen del servidor
     * 
     * Implementa la interfaz Runnable para su ejecución
     */
    protected class ChatClientListener implements Runnable {
    	private ObjectInputStream in;
    	private ChatClientImpl client;

    	/**
    	 * Constructor del hilo para la escucha de mensajes en el cliente
    	 * 
    	 * @param in     Entrada de objetos desde el servidor
    	 * @param client El propio cliente, se le pasa para poder asignar el ID
    	 */
        public ChatClientListener(ObjectInputStream in, ChatClientImpl client) {
            this.in = in;
            this.client = client;
        }

        /**
		 * Método que se ejecuta y escucha los mensajes
	     */
        public void run() {
        	try {
                ChatMessage serverMessage;
                
                // Si hay un mensaje y el cliente está conectado, lo muestra
                while ((serverMessage = (ChatMessage) in.readObject()) != null && carryOn) {
 
                	// Se comoprueba el tipo de mensaje
	                switch (serverMessage.getType()) {
	                	case HANDSHAKE: // Saludo inicial
	                		// Recibe el ID asignado por el servidor
	                		client.setid(Integer.parseInt(serverMessage.getMessage()));
	                		break;
	                	case TEXT:
	                		// Muestra un nuevo mensaje 
					    	System.out.println(serverMessage.getMessage());
					        break;
					    case LOGOUT:
					    	// Informa de la desconexión y cierra el cliente
					        System.out.println("Has sido desconectado por el servidor.");
					        disconnect();
					        return;
					    case SHUTDOWN:
					    	// En teoría no se usa
					        System.out.println("El servidor ha sido desconectado.");
					        return;
					    default:
					    	break;
                	}
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error al recibir un mensaje del servidor: " + e.getMessage());
            }
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
    	// Si no es correcto (x.y.z.w) el parámetro se usa el servidor por defecto 
    	if(server.length() < 7) {
    		this.server = server;
    	}
    	
    	this.port = port;
    	this.username = username;
    	this.carryOn = true;
    }
    
    /**
     * Setter para id
     * 
     * @param id
     */
    public void setid(int id) {
    	this.id = id; 
    }

    /**
     * Inicia la conexión con el servidor y hace el handshake
     * 
     * @return Devuelve true si la conexión se inicia correctamente y false si hubo error
     */
    public boolean start() {
        try {
        	// Inicia la conexión
        	socket = new Socket(server, port);
        	
        	// Salida y entradas de objetos del cliente
        	out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            		
            // Iniciar listener para mensajes entrantes
            Thread listenerThread = new Thread(new ChatClientListener(in, this));
            listenerThread.start();
            
            // Se envia el handshake
            ChatMessage handshake = new ChatMessage(username, id, MessageType.HANDSHAKE, username);
            out.writeObject(handshake);

            // Se leen los mensajes desde la consola
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String userInput;

            // Se procesan los mensajes / comandos escritos en el cliente
            while ((userInput = stdIn.readLine()) != null) {
            	
            	// Comando de cierre
                if (userInput.equalsIgnoreCase("salir")) {
                    // Desconecta al cliente
                    disconnect();
                    break;
                }

                // Genera el mensaje
                ChatMessage msg = new ChatMessage(username, -2, MessageType.TEXT, "");
                
                // Se comprueba si es un baneo, desbaneo o mensaje de texto
                if (userInput.toLowerCase().startsWith("ban ")) {
                	// Extrae al usuario baneado
                    String bannedUser = userInput.substring(4).trim();
                    
                    // Comprueba que se pueda banear
                    if (!bannedUser.isEmpty()) {
                    	// Genera una solicitud de baneo al servidor
                        msg = new ChatMessage(username, id, MessageType.BAN, bannedUser);
                    } else {
                        System.out.println("Error, el mensaje de ban debe ser: ban <nombre>");
                        continue;
                    }
                }else if(userInput.toLowerCase().startsWith("unban ")) {
                	// Extrae al usuario desbaneado
                    String unbannedUser = userInput.substring(6).trim();
                    
                    if (!unbannedUser.isEmpty()) {
                    	// Genera una solicitud de desbaneo al servidor
                        msg = new ChatMessage(username, id, MessageType.UNBAN, unbannedUser);
                    } else {
                        System.out.println("Error, el mensaje de ban debe ser: unban <nombre>");
                        continue;
                    }
                } else if(userInput.length() > 0){
                    // Genera el mensaje de texto
                    msg = new ChatMessage(username, id, MessageType.TEXT, userInput);              
                }
                
                // Enviar el mensaje al servidor
                sendMessage(msg);
            }
            disconnect();
            return true;
        } catch (IOException e) {
            System.out.println("Error al conectar con el servidor: " + e.getMessage());
            return false;
        }
    }

    /**
     * Envía un mensaje al servidor
     * 
     * @param msg Mensaje a enviar
     */
    public void sendMessage(ChatMessage msg) {
        // Comprueba que el cliente no está desconectado
        if (carryOn == false) {
            System.out.println("Un cliente desconectado no puede mandar mensajes al servidor");
            return;
        }
        
        try {
            // Se envía el mensaje al servidor
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            System.out.println("Error al enviar el mensaje");
        }
    }

    /**
     * Desconecta el cliente del servidor
     */
    public void disconnect(){
    	// Indica el cliente en estado desconectado
    	carryOn = false;
    	
    	// Mensaje de desconexión para el servidor
    	ChatMessage logoutMessage = new ChatMessage(username, id, MessageType.LOGOUT, "Desconectado");
        
    	// Se envia el mensaje y finaliza el programa del cliente
    	try {
			out.writeObject(logoutMessage);
			out.flush();
			
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
     * Método principal para ejecutar el cliente
     * 
     * @param args Argumentos de línea de comandos
     */
    public static void main(String[] args){
    	// Comprueba que los argumentos de entrada sean correctos
		if (args.length < 2) {
			System.out.println("Error, no hay suficientes parámetros para crear un cliente");
		    return;
		}
		 
		// Inicializa el cliente con los datos de los parámetros
		ChatClientImpl client = new ChatClientImpl(args[0], DEFAULT_SERVER_PORT, args[1]);
		client.start();
		return;
    }
}
