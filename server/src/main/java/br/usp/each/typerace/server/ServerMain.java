package br.usp.each.typerace.server;

import org.java_websocket.server.WebSocketServer;

import java.util.HashMap;

public class ServerMain {

    private WebSocketServer server;

    /**
     * Construtor.
     *
     * @param server Indica socket com servidor a ser executado.
     */
    public ServerMain(WebSocketServer server) {
        this.server = server;
    }

    /**
     * Inicializa servidor.
     */
    public void init() {
        System.out.println("Iniciando servidor...");
        server.start();
        server.onStart();
        // TODO: implementar forma de encerrar servidor com server.stop() ex. digitar "encerrar" na linha de comando
    }



    /**
     * Método inicial do programa executor do servidor.
     *
     * @param args Argumentos passados pela linha de comando.
     */
    public static void main(String[] args) {
        WebSocketServer server = new Server(8080, new HashMap<>());

        ServerMain main = new ServerMain(server);

        main.init();
    }

}
