package es.ubu.lsi.common;

/**
 * Enumeración de tipos de mensajes de la clase ChatMessage
 * 
 * Los tipos de mensajes definidos son:
 * - TEXT: Para enviar mensajes entre servidor y cliente
 * - LOGOUT: Ppara cerrar sesión
 * - SHUTDOWN: No se utiliza
 * - BAN: Para banear a otro cliente
 * - UNBAN: Para desbanear a un cliente baneado anteriormente
 * - HANDSHAKE: Para intercambiar información al inicio de la conexión
 * 
 * @author Adrián Zamora Sánchez (azs1004@alu.ubu.es)
 */
public enum MessageType {
    TEXT,
    LOGOUT,
    SHUTDOWN,
    BAN,
    UNBAN,
    HANDSHAKE
}