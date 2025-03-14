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
    
    private final static int DEFAULT_SERVER_PORT = 1500;
    private static int clientCounter = 0;

    /**
     * Clase interna que escucha los mensajes que vienen del servidor
     * 
     * Implementa la interfaz Runnable para su ejecución
     */
    protected class ChatClientListener implements Runnable {
    	private BufferedReader in;

        public ChatClientListener(BufferedReader in) {
            this.in = in;
        }

        /**
		 * Método que se ejecuta y escucha los mensajes
	     */
        public void run() {
            try {
                String serverMessage;
                
                // Si hay un mensaje y el cliente esta coenctado lo muestra
                while ((serverMessage = in.readLine()) != null && carryOn) {
                    System.out.println("Mensaje del servidor:" + serverMessage);
                }
            } catch (IOException e) {
                System.out.println("Error al recibir un mensajes del server");
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

    public boolean start(){
    	try (
    			// Sockets de conexión con el servidor
                Socket socket = new Socket(server, port);
                
    			// Objeto encargado de recibir los datos
    			BufferedReader in = 
                		new BufferedReader(
                				new InputStreamReader(socket.getInputStream()))
            ) {
    			// TODO BORRAR, mensaje introductorio para probar cuando implemente el servidor
	    		ChatMessage newConnectionMessage = new ChatMessage(username, MessageType.TEXT, "Conexión de: " + username);
	    		sendMessage(newConnectionMessage);

                // Se inicia un listener para mensajes entrantes
                Thread listenerThread = new Thread(new ChatClientListener(in));
                listenerThread.start(); // Se inicia la escucha

                // Objeto que lee de la entrada estandar los mensajes que envia el cliente
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
                
                String userInput;
                
                // Funciona mientras
                while ((userInput = stdIn.readLine()) != null) {
                    if (userInput.equalsIgnoreCase("salir")) {
                    	// En este caso desconecta el cliente
                        disconnect();
                        break;
                    }
                    // Genera y envia al servidor el mensaje
                    ChatMessage msg = new ChatMessage(username, MessageType.TEXT, userInput);
    	    		sendMessage(msg);
                }
                
                return true;
	    	} catch (IOException e) {
	            System.out.println("Error al conectar con el servidor de chat: " + server + "::" + Integer.toString(port));
	            return false;
	    	}
    }

    public void sendMessage(ChatMessage msg) {
    	// Comprueba que el clietne no está desconectado
    	if (!carryOn) {
            System.out.println("Un cliente desconectado no puede mandar mensajes al servidor");
            return;
        }
    	
        try (
        	// Se genera una forma de envio al servidor
            Socket socket = new Socket(server, port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())
        ) {
        	// Se envía el objeto al servidor
            out.writeObject(msg);
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
