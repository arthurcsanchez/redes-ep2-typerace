package br.usp.each.typerace.server;

import org.java_websocket.server.WebSocketServer;

import java.util.HashMap;
import java.util.Scanner;
import java.util.NoSuchElementException;

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
    }



    /**
     * Método inicial do programa executor do servidor.
     *
     * @param args Argumentos passados pela linha de comando.
     */
    public static void main(String[] args) throws InterruptedException{
        Scanner sc = new Scanner(System.in);
        WebSocketServer server = new Server(8080, new HashMap<>());

        ServerMain main = new ServerMain(server);

        main.init();

        String input = "";
        while (true) {
            if (sc.hasNext()) 
                input = sc.nextLine();
            if (input.equalsIgnoreCase("/encerrar")) {
                System.out.println("Encerrando servidor...");
                main.server.stop();
                break;
            }
        }
    }

}
