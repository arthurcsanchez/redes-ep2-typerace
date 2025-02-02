package br.usp.each.typerace.client;

import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.io.IOException;

public class ClientMain {

    private WebSocketClient client;

    /**
     * Construtor.
     * @param client Indica socket com cliente a ser executado
     */
    public ClientMain(WebSocketClient client) {
        this.client = client;
    }

    /**
     * Inicializa cliente.
     * @param idCliente ID do cliente
     */
    public void init(String idCliente) {
        System.out.println("Iniciando cliente: " + idCliente);
        client.connect();
    }



    /**
     * Método inicial do programa executor do cliente.
     * @param args Argumentos passados pela linha de comando
     */
    public static void main(String[] args) {

        String uriInput;
        String idInput;

        Scanner sc = new Scanner(System.in);
        System.out.println("Informe a URI do servidor. Deixe em branco para a URI padrão.");
        uriInput = sc.nextLine().replace("\n", "");
        if (uriInput.isEmpty()) uriInput = "ws://localhost:8080";
        System.out.println();

        do {
            System.out.println("Informe um nome para se identificar no servidor. (sem espaços ou acentos)");
            idInput = sc.nextLine().replace("\n", "");
            if (!idInput.isEmpty()) break;
            System.out.println("Nome inválido. Tente novamente.");
        } while (true);
        System.out.println();

        try {
            WebSocketClient client = new Client(new URI(uriInput + "/" + idInput));
            ClientMain main = new ClientMain(client);

            main.init(idInput);

            while (!main.client.isClosed()) {
                main.client.send(sc.nextLine());
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
