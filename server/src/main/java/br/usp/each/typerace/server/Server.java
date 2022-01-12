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

    /**
     * Construtor.
     *
     * @param port Indica porta em que socket será criado
     * @param connections Indica mapa em que estão ou serão inseridos os IDs e sockets dos clientes
     */
    public Server(int port, Map<String, WebSocket> connections) {
        super(new InetSocketAddress(port));
        this.connections = connections;
    }

    /**
     * Chamada quando jogador entra (conexão já foi estabelecida). Verifica se jogador que entrou é válido (se já estava antes no servidor), insere no mapa e transmite mensagem de entrada.
     *
     * @param conn Client do jogador que entra; deve ser comparado com o mapa connections
     * @param handshake ?
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        if (connections.containsValue(conn)) {
            conn.send("Conexão duplicada. Desconectando todas as instâncias.");
            conn.closeConnection(4003, "Conexão duplicada.");
        } else if (connections.containsKey(getIDfromSocket(conn))) {
            conn.send("O nome \"" + getIDfromSocket(conn) + "\" já está em uso. Tente novamente.");
            conn.closeConnection(4002, "Nome já utilizado por outro jogador");
        } else {
            connections.put(getIDfromSocket(conn), conn);
            System.out.println(getIDfromSocket(conn) + " conectado.");
            broadcast(getIDfromSocket(conn) + " entrou na partida.");
            // SUGGESTION: enviar para conn mensagem introdutória, com regras do jogo, comando para iniciar, etc.
        }
    }

    /**
     * Chamada quando o jogador sai (conexão já foi encerrada). Informa motivo de saída do jogador
     *
     * @param conn Client do jogador que sai; pode ser comparado com o mapa connections
     * @param code Código de erro (conferir em <a href="https://github.com/Luka967/websocket-close-codes">WebSocket Close Codes</a>)
     * @param reason String descrevendo o erro
     * @param remote Indica se decisão de saída foi tomada local ou remotamente (em relação ao servidor)
     */
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println(getIDfromSocket(conn) + " desconectado. Motivo: " + reason + "(Cód. " + code + ")." + (remote ? "Desconectado pelo cliente." : "Desconectado pelo servidor."));
        broadcast(getIDfromSocket(conn) + " foi desconectado da partida.");
        connections.remove(getIDfromSocket(conn));
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
        conn.closeConnection(4001, "Lançamento da exceção " + ex.toString());
    }

    /**
     * Chamada na inicialização do servidor.
     */
    @Override
    public void onStart() {
        System.out.println("Servidor iniciado com sucesso na porta " + this.getPort() + ".");
    }

    /**
     * Extrai ID do jogador inserido na URI onde o socket (cliente) se conecta.
     *
     * @param conn Cliente do jogador buscado; tem sua URI extraída
     */
    private String getIDfromSocket(WebSocket conn) {
        int start = conn.getResourceDescriptor().indexOf("playerID=") + 9;
        return conn.getResourceDescriptor().substring(start);
    }
}
