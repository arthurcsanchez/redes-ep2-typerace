package br.usp.each.typerace.client;

import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;

public class ClientMain {

    private WebSocketClient client;

    public ClientMain(WebSocketClient client) {
        this.client = client;
    }

    public void init(String idCliente) {
        System.out.println("Iniciando cliente: " + idCliente);
        // TODO: Implementar
        client.connect();
    }

    public static void main(String[] args) {
        /*
           FIXME: Remover essas strings fixas
           Opções: fazer interface perguntando URI e ID ou então utilizar args[]
        */
        String removeMe = "ws://localhost:8080";
        String removeMe2 = "idCliente";

        try {
            WebSocketClient client = new Client(new URI(removeMe));

            // Cria instância de execução (ClientMain) a partir do socket (client)
            ClientMain main = new ClientMain(client);
            main.init(removeMe2);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
