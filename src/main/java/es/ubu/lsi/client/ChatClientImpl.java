package es.ubu.lsi;

import java.utils.*;

/**
 * Implementación de la clase ChatClient
 */
public class ChatClientImpl implements ChatCliente {
    private String server;
    private String username;
    private int port;
    private boolean carryOn;
    private int id;

    protected class ChatClientListener {
        public run(){

        }
    }

    public ChatClientImpl(String server, int port, String username){

    }

    public boolean start(){

        return true;
    }

    public sendMessage(ChatMessage msg){

    }

    public disconect(){

    }

    public main(String[] args){

    }
}
