package es.ubu.lsi.common;

/**
 * Enumeración de tipos de mensajes de la clase ChatMessage
 * 
 * Los tres tipos de mensajes definidos son:
 * - TEXT: Mensajes enviado por el usuario
 * - LOGOUT: Utilizado para cerrar sesión
 * - SHUTDOWN: No se utiliza
 * - BAN: Para banear a otro cliente
 */
public enum MessageType {
    TEXT,
    LOGOUT,
    SHUTDOWN,
    BAN,
    HANDSHAKE // Igual no me hace falta...
}