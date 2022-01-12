package br.usp.each.typerace.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.TreeMap;

public class Server extends WebSocketServer {

    /**
     * Mapeia ID dos jogadores com seus sockets (na forma de um objeto Client)
     */
    private final Map<String, WebSocket> connections;

    /**
     * Indica estado atual do servidor.
     * 0: aguardando "pronto" dos jogadores atuais
     * 1: aguardando próximo jogador
     * 2: partida em andamento
     */
    public int state;

    /**
     * Indica estado atual de cada jogador.
     * 0: aguardando
     * 1: pronto para partida
     * 2: em partida
     */
    private final Map<String, Integer> playerState;

    /**
     * Construtor.
     *
     * @param port Indica porta em que socket será criado
     * @param connections Indica mapa em que estão ou serão inseridos os IDs e sockets dos clientes
     */
    public Server(int port, Map<String, WebSocket> connections) {
        super(new InetSocketAddress(port));
        this.connections = connections;
        this.state = 0;
        this.playerState = new TreeMap<>();
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
            String connName = getIDfromSocket(conn);
            connections.put(connName, conn);
            playerState.put(connName, 0);
            System.out.println(connName + " conectado.");
            broadcast(connName + " entrou na partida.");
            // SUGGESTION: enviar para conn mensagem introdutória, com regras do jogo, comando para iniciar, etc.
        }
        if (this.state == 1) this.state = 0;
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
        if (message.equalsIgnoreCase("pronto")) {
            broadcast(getIDfromSocket(conn) + " está pronto para começar.");
            playerState.put(getIDfromSocket(conn), 1);
        } else if (message.equalsIgnoreCase("sair")) {
            conn.closeConnection(1001, "Solicitação do jogador");
        } else if (message.equalsIgnoreCase("aguardar")) {
            this.state = 1;
        }
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
        do {
            try {
                startGame();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (this.state != 2);
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

    /**
     * Verifica se há mais de um jogador e se todos estão prontos. Caso positivo, aguarda 5 segundos e inicia a partida.
     */
    public void startGame() throws InterruptedException {
        if (this.state == 0 && connections.size() > 1 && !playerState.containsValue(0)) {
            System.out.println("Iniciando contagem regressiva para início de partida.");
            broadcast("Iniciando partida em: ");
            Thread.sleep(1000);
            if (this.state == 1) {
                System.out.println("Contagem regressiva para início de partida interrompida por solicitação de aguardo.");
                broadcast("Contagem regressiva interrompida; aguardando o próximo jogador.");
                return;
            }
            broadcast("5");
            Thread.sleep(1000);
            if (this.state == 1) {
                System.out.println("Contagem regressiva para início de partida interrompida por solicitação de aguardo.");
                broadcast("Contagem regressiva interrompida; aguardando o próximo jogador.");
                return;
            }
            broadcast("4");
            Thread.sleep(1000);
            if (this.state == 1) {
                System.out.println("Contagem regressiva para início de partida interrompida por solicitação de aguardo.");
                broadcast("Contagem regressiva interrompida; aguardando o próximo jogador.");
                return;
            }
            broadcast("3");
            Thread.sleep(1000);
            if (this.state == 1) {
                System.out.println("Contagem regressiva para início de partida interrompida por solicitação de aguardo.");
                broadcast("Contagem regressiva interrompida; aguardando o próximo jogador.");
                return;
            }
            broadcast("2");
            Thread.sleep(1000);
            if (this.state == 1) {
                System.out.println("Contagem regressiva para início de partida interrompida por solicitação de aguardo.");
                broadcast("Contagem regressiva interrompida; aguardando o próximo jogador.");
                return;
            }
            broadcast("1");
            Thread.sleep(1000);
            if (this.state == 1) {
                System.out.println("Contagem regressiva para início de partida interrompida por solicitação de aguardo.");
                broadcast("Contagem regressiva interrompida; aguardando o próximo jogador.");
                return;
            }
            this.state = 2;
            playerState.replaceAll((k, v) -> 2);
            System.out.println("Iniciando partida.");
            broadcast("Iniciando partida.");
            // TODO: realizar partida
        }
    }
}
