package br.usp.each.typerace.client;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

public class Client extends WebSocketClient {


    public Client(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        // TODO: Implementar
    }

    @Override
    public void onMessage(String message) {
        // TODO: Implementar
    }

    /**
     * Chamada quando a conexão é encerrada.
     * @param code Código de erro (conferir em <a href="https://github.com/Luka967/websocket-close-codes">WebSocket Close Codes</a>)
     * @param reason String descrevendo o erro
     * @param remote Indica se decisão de saída foi tomada local ou remotamente (em relação ao cliente)
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Desconectado. Motivo: " + reason + "(Cód. " + code + ")." + (remote ? "Desconectado pelo cliente." : "Desconectado pelo servidor."));
        if (code != 4002) System.exit(0);
    }

    @Override
    public void onError(Exception ex) {
        // TODO: Implementar
    }
}
