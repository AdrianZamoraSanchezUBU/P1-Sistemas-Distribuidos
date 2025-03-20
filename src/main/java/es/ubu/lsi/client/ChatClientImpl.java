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
    private String server = "127.0.0.1"; // Por defecto localhost
    private String username;
    private int port;
    private boolean carryOn;
    private int id;
    
    ObjectOutputStream out;
    
    private final static int DEFAULT_SERVER_PORT = 1500;
    private static int clientCounter = 0;

    /**
     * Clase interna que escucha los mensajes que vienen del servidor
     * 
     * Implementa la interfaz Runnable para su ejecución
     */
    protected class ChatClientListener implements Runnable {
    	private ObjectInputStream in;

        public ChatClientListener(ObjectInputStream in) {
            this.in = in;
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
					    case TEXT:
					    	System.out.println(serverMessage.getMessage());
					        break;
					    case LOGOUT:
					        System.out.println("Has sido desconectado por el servidor.");
					        disconnect();
					        return;
					    case SHUTDOWN:
					        System.out.println("El servidor ha sido desconectado.");
					        return;
					    default:
					    	break;
                	}
                }
            } catch (IOException e) {
                System.out.println("Error al recibir un mensaje del servidor: " + e.getMessage());
            } catch (ClassNotFoundException e) {
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
    	this.server = server;
    	this.port = port;
    	this.username = username;
    	this.carryOn = true;
    	
    	this.id = clientCounter++;
    }

    public boolean start() {
        try {
        	Socket socket = new Socket(server, port);
        	
        	out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            		
            // Iniciar listener para mensajes entrantes
            Thread listenerThread = new Thread(new ChatClientListener(in));
            listenerThread.start();

            // Leer mensajes desde la consola
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String userInput;

            while ((userInput = stdIn.readLine()) != null) {
                if (userInput.equalsIgnoreCase("salir")) {
                    // Enviar mensaje de LOGOUT al servidor
                    ChatMessage logoutMessage = new ChatMessage(username, id, MessageType.LOGOUT, "Desconectado");
                    out.writeObject(logoutMessage);
                    out.flush();
                    disconnect();
                    break;
                }

                ChatMessage msg;
                if (userInput.toLowerCase().startsWith("ban ")) {
                    String bannedUser = userInput.substring(4).trim();
                    if (!bannedUser.isEmpty()) {
                        msg = new ChatMessage(username, id, MessageType.BAN, bannedUser);
                    } else {
                        System.out.println("Error, el mensaje de ban debe ser: ban <nombre>");
                        continue;
                    }
                } else {
                    // Mensaje normal de texto
                    msg = new ChatMessage(username, id, MessageType.TEXT, userInput);
                    System.out.print("Mensaje normal");
                }

                // Enviar el mensaje al servidor
                sendMessage(msg);
            }

            return true;
        } catch (IOException e) {
            System.out.println("Error al conectar con el servidor: " + e.getMessage());
            return false;
        }
    }

    public void sendMessage(ChatMessage msg) {
        // Comprueba que el cliente no está desconectado
        if (carryOn == false) {
            System.out.println("Un cliente desconectado no puede mandar mensajes al servidor");
            return;
        }
        
        try {
            // Se envía el objeto al servidor
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            System.out.println("Error al enviar el mensaje");
        }
    }

    public void disconnect(){
    	// Esto indica el cliente en estado desconectado
    	carryOn = false;
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
    }
}
