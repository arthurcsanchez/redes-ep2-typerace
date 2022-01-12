package br.usp.each.typerace.client;

import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

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
        // TODO: Implementar
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
            do {
                System.out.println("Informe um nome para se identificar no servidor.");
                idInput = sc.nextLine().replace("\n", "");
                if (!idInput.isEmpty()) break;
                System.out.println("Nome inválido. Tente novamente.");
            } while (true);
            System.out.println();

            try {
                // Cria socket baseado na implementação Client
                WebSocketClient client = new Client(new URI(uriInput + "/playerID=" + idInput));
                // Cria instância de execução (ClientMain) a partir do socket (client)
                ClientMain main = new ClientMain(client);

                main.init(idInput);

                if (main.client.isOpen()) break;

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } while (true);
    }
}
