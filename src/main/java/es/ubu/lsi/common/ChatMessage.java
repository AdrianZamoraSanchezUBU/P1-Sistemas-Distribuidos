package es.ubu.lsi.common;

import java.io.Serializable;

/**
 * Clase de ChatMessage
 * 
 * Define como son los mensajes que se intercambian 
 * los clientes y el servidor
 */
public class ChatMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	private String clientid;
    private MessageType type;
    private String message;
    
    /**
     * Constructor de la clase ChatMessage
     *
     * @param clientid Identificador del cliente que envía el mensaje
     * @param type Tipo de mensaje
     * @param message Contenido del mensaje
     */
    public ChatMessage(String clientid, MessageType type, String message) {
        this.clientid = clientid;
        this.type = type;
        this.message = message;
    }
    
    // Getter de clientid
    public String getClientid() {
        return clientid;
    }

    // Setter de clientid
    public void setClientId(String clientId) {
        this.clientid = clientId;
    }

    // Getter de type
    public MessageType getType() {
        return type;
    }
    
    // Setter de type
    public void setType(MessageType type) {
        this.type = type;
    }
    
    // Getter de message
    public String getMessage() {
        return message;
    }
    
    // Setter de message
    public void setMessage(String message) {
        this.message = message;
    }
}