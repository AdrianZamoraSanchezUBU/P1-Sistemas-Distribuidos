package es.ubu.lsi;

/**
 * Interfaz de la clase cliente
 */

public interface ChatClient  {

    public boolean start();

    public sendMessage(CharMessage msg);

    public disconect();
}
