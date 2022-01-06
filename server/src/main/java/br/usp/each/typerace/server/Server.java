package br.usp.each.typerace.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Map;

public class Server extends WebSocketServer {

    /**
     * Mapeia ID dos jogadores com seus sockets (na forma de um objeto Client)
     */
    private final Map<String, WebSocket> connections;

    public Server(int port, Map<String, WebSocket> connections) {
        super(new InetSocketAddress(port));
        this.connections = connections;
    }

    /**
     * Chamada quando jogador entra. Verifica se jogador que entrou é válido (se já estava antes no servidor)
     *
     * @param conn Client do jogador que entra; deve ser comparado com o mapa connections
     * @param handshake ?
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        // TODO: Implementar
    }

    /**
     * Chamada quando o jogador sai. Informa motivo de saída do jogador
     *
     * @param conn Client do jogador que sai; pode ser comparado com o mapa connections
     * @param code Código de erro
     * @param reason String descrevendo o erro
     * @param remote ?
     */
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        // TODO: Implementar
    }

    /**
     * Chamada a cada recebimento de mensagem dos jogadores. Realiza funções como início de partida e encerramento de conexão, quando não há jogo em andamento, e checa respostas quando há.
     *
     * @param conn Client do jogador que envia mensagem; pode ser comparado com o mapa connections
     * @param message String de texto enviada pelo jogador
     */
    @Override
    public void onMessage(WebSocket conn, String message) {
        // TODO: Implementar
    }

    /**
     * Chamada caso alguma exceção seja lançada por um cliente.
     *
     * @param conn Client do jogador que envia exceção; pode ser comparado com o mapa connections
     * @param ex Exceção a ser lançada
     */
    @Override
    public void onError(WebSocket conn, Exception ex) {
        // TODO: Implementar
        ex.printStackTrace();
    }

    /**
     * Chamada na inicialização do servidor.
     */
    @Override
    public void onStart() {
        // TODO: Implementar
    }
}
