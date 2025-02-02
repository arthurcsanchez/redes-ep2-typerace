package br.usp.each.typerace.client;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

public class Client extends WebSocketClient {

    private int wrongAnswers = 0;
    private int correctAnswers = 0;

    /**
     * Construtor.
     * @param serverUri URI com o qual o cliente se conecta.
     */
    public Client(URI serverUri) {
        super(serverUri);
    }

    /**
     * Chamada quando a conexão com o servidor é estabelecida.
     * @param handshakedata ?
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Conexão estabelecida com o servidor.");
    }

    /**
     * Chamada quando uma mensagem é recebida do servidor. Deve, por exemplo, imprimí-las.
     * @param message String da mensagem.
     */
    @Override
    public void onMessage(String message) {
        if (message.contains(":errors:")) wrongAnswers = Integer.parseInt(message.substring(8));
        else if (message.contains(":correct:")) correctAnswers = Integer.parseInt(message.substring(9));
        else if (message.equals(":informStats:")) System.out.println("Você acertou " + correctAnswers + " e errou " + wrongAnswers + " palavras.");
        else System.out.println(message);
    }

    /**
     * Chamada quando a conexão é encerrada.
     * @param code Código de erro (conferir em <a href="https://github.com/Luka967/websocket-close-codes">WebSocket Close Codes</a>)
     * @param reason String descrevendo o erro
     * @param remote Indica se decisão de saída foi tomada local ou remotamente (em relação ao cliente)
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (code == -1) reason = "URI inválida";
        System.out.println("Desconectado. Motivo: " + reason + " (Cód. " + code + "). " + (remote ? "Desconectado pelo cliente." : "Desconectado pelo servidor."));
        System.exit(0);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
}
