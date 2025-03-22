package es.ubu.lsi.common;

import java.io.Serializable;

/**
 * Clase de ChatMessage
 * 
 * Define como son los mensajes que se intercambian 
 * los clientes y el servidor. Es serializable.
 * 
 * @author Adrián Zamora Sánchez (azs1004@alu.ubu.es)
 */
public class ChatMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int clientid;
	private String clientName;
    private MessageType type;
    private String message;
    
    /**
     * Constructor de la clase ChatMessage
     *
     * @param clientName Nombre del cliente que envía el mensaje
     * @param clientid   Identificador del cliente que envía el mensaje
     * @param type       Tipo de mensaje
     * @param message    Contenido del mensaje
     */
    public ChatMessage(String clientName, int clientid, MessageType type, String message) {
    	this.clientName = clientName;
    	this.clientid = clientid;
        this.type = type;
        this.message = message;
    }
    
    /**
     * Getter de clientId
     * 
     * @return id Numérico del cliente
     */
    public int getClientid() {
        return clientid;
    }

    /**
     * Setter de clientId
     * 
     * @param clientId Identificador del emisor del mensaje
     */
    public void setClientId(int clientId) {
        this.clientid = clientId;
    }

    /**
     * Getter de type
     * 
     * @return Tipo del mensaje
     */
    public MessageType getType() {
        return type;
    }
    
    /**
     * Setter de type
     * 
     * @param type Tipo que se establece en el mensaje
     */
    public void setType(MessageType type) {
        this.type = type;
    }
    
    /**
     * Getter de message
     * 
     * @return Texto del mensaje
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Setter de message
     * 
     * @param message Texto que se establece como mensaje
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Getter de clientName
     * 
     * @return Nombre del cliente
     */
    public String getClientName() {
        return clientName;
    }
}