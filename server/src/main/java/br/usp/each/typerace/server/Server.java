package br.usp.each.typerace.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.*;
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
     * Banco de palavras para o jogo.
     */
    private final Set<String> wordBank;

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
        this.wordBank = new HashSet<>();
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
     * Chamada a cada recebimento de mensagem dos clientes. Realiza funções como início de partida e encerramento de conexão, quando não há jogo em andamento, e checa respostas quando há.
     *
     * @param conn Client do jogador que envia mensagem; pode ser comparado com o mapa connections
     * @param message String de texto enviada pelo jogador
     */
    @Override
    public void onMessage(WebSocket conn, String message) {
        if (this.state != 2) {
            if (message.equalsIgnoreCase("pronto") && playerState.get(getIDfromSocket(conn)) == 0) {
                broadcast(getIDfromSocket(conn) + " está pronto para começar.");
                playerState.put(getIDfromSocket(conn), 1);
                if (this.state == 0 && connections.size() > 1 && !playerState.containsValue(0)) {
        			try {
                		startGame();
            		} catch (InterruptedException e) {
                		e.printStackTrace();
            		}
        		}
            } else if (message.equalsIgnoreCase("sair")) {
                conn.closeConnection(1001, "Solicitação do jogador");
            } else if (message.equalsIgnoreCase("aguardar")) {
                this.state = 1;
            }
        } else {
            // TODO: lidar com input durante jogo
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

        insertWords();
    }

    /**
     * Extrai ID do jogador inserido na URI onde o socket (cliente) se conecta.
     *
     * @param conn Cliente do jogador buscado; tem sua URI extraída
     */
    private String getIDfromSocket(WebSocket conn) {
        int start = conn.getResourceDescriptor().indexOf("/", 5) + 1;
        return conn.getResourceDescriptor().substring(start);
    }

    /**
     * Verifica se há mais de um jogador e se todos estão prontos. Caso positivo, aguarda 5 segundos e inicia a partida.
     */
    public void startGame() throws InterruptedException {
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
        List<String> matchWords = new LinkedList<>();
        findMatchWords(matchWords, 10);
    }

    /**
     * Preenche lista de palavras de uma partida
     * @param l Lista a ser preenchida
     * @param amount Quantidade de palavras a inserir na lista
     */
    private void findMatchWords(List<String> l, int amount) {
        int item = new Random().nextInt(wordBank.size());
        int i = 0, count = 0;
        for (String s : wordBank) {
            if (i == item) {
                l.add(s);
                count++;
            }
            i++;
            if (count >= amount)
                return;
        }
    }

    /**
     * Insere palavras no banco de palavras. Acessar gerador de palavras <a href="https://www.palabrasaleatorias.com/palavras-aleatorias.php?fs=10&fs2=0&Submit=Nova+palavra">aqui</a>.
     */
    private void insertWords() {
        wordBank.add("Palavras"); wordBank.add("Banco"); wordBank.add("Computação"); wordBank.add("Redes"); wordBank.add("Sistema"); wordBank.add("Informações");
        wordBank.add("Marte"); wordBank.add("Acoplado");wordBank.add("Cavalete");wordBank.add("Bilhete");wordBank.add("Beterraba");wordBank.add("Ampliar");
        wordBank.add("Capital"); wordBank.add("Adorável");wordBank.add("Tapa");wordBank.add("Vinho");wordBank.add("Veneza");wordBank.add("Vão");
        wordBank.add("Mosaico"); wordBank.add("Escape");wordBank.add("Nogueira");wordBank.add("Azul");wordBank.add("Feridos");wordBank.add("Prisioneiro");
        wordBank.add("Caderno"); wordBank.add("Marcha");wordBank.add("Escritor");wordBank.add("Grade");wordBank.add("Lobisomem");wordBank.add("Piranha");
        wordBank.add("Vulcão"); wordBank.add("Pistola");wordBank.add("Camping");wordBank.add("Revistas");wordBank.add("Vender");wordBank.add("Dobrável");
        wordBank.add("Diamante"); wordBank.add("Aldeola");wordBank.add("Valsa");wordBank.add("Bumbum");wordBank.add("Chicote");wordBank.add("Esmeralda");
        wordBank.add("Queimar"); wordBank.add("Cicatriz");wordBank.add("Cabo");wordBank.add("Cenoura");wordBank.add("Tijolo");wordBank.add("Fátima");
        wordBank.add("Conde"); wordBank.add("Conta");wordBank.add("Cinto");wordBank.add("Bazuca");wordBank.add("Troféu");wordBank.add("Noiva");
        wordBank.add("Gotejamento"); wordBank.add("Assistente");wordBank.add("Artista");wordBank.add("Crocodilo");wordBank.add("Giratória");wordBank.add("Ritmo");
        wordBank.add("Beijos"); wordBank.add("Índios");wordBank.add("Piso");wordBank.add("Peludo");wordBank.add("Jogos");wordBank.add("Passeio");
        wordBank.add("Silicone"); wordBank.add("Cinquenta");wordBank.add("Costureira");wordBank.add("Rooftop");wordBank.add("Veleiro");wordBank.add("Assassinato");
        wordBank.add("Botinha"); wordBank.add("Chaleira");wordBank.add("Semeadura");wordBank.add("Direito");wordBank.add("Universitário");wordBank.add("Órfão");
        wordBank.add("Manicure"); wordBank.add("Arroto");wordBank.add("Botoadura");wordBank.add("Bocal");wordBank.add("Jato");wordBank.add("Trabalhador");
        wordBank.add("Fundação"); wordBank.add("Procurar");wordBank.add("Espingarda");wordBank.add("Fatiado");wordBank.add("Porta-estandarte");wordBank.add("Voz");
        wordBank.add("Guarda-roupa"); wordBank.add("Atirar");wordBank.add("Micro-ondas");wordBank.add("Esculpir");wordBank.add("Safira");wordBank.add("Remover");
        wordBank.add("Verruga"); wordBank.add("Vacina");wordBank.add("Coronavírus");wordBank.add("Recital");wordBank.add("Seringa");wordBank.add("Contagem");
        wordBank.add("Miar"); wordBank.add("Falsificar");wordBank.add("Polígono");wordBank.add("Vestibulando");wordBank.add("Sinal");wordBank.add("Semaninha");
        wordBank.add("Hibernar"); wordBank.add("Pesado");wordBank.add("Facção");wordBank.add("Tubo");wordBank.add("Arco");wordBank.add("Adicionar");
        wordBank.add("Bolso"); wordBank.add("Transpirar");wordBank.add("Cantores");wordBank.add("Aluno");wordBank.add("Coringa");wordBank.add("Cornija");
        wordBank.add("Quadro"); wordBank.add("Estresse");wordBank.add("Cobertura");wordBank.add("Pai");wordBank.add("Vez");wordBank.add("Ambientalismo");
    }
}
