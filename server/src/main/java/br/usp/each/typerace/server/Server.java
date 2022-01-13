package br.usp.each.typerace.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.*;

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
     * Lista com 10 elementos, indica quais palavras o jogador ja acertou.
     */
    private final Map<String, boolean[]> palavrasAcertadas;

    /**
     * Banco de palavras para o jogo.
     */
    private final Set<String> wordBank;

    /**
     * Lista com as 10 palavras que terao que ser digitadas pelos jogadores.
     */
    private List<String> matchWords;

    /**
     * Lista com palavras do banco de palavras, cuja ordem é randomizada a cada partida.
     */
    private List<String> bankWords;

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
        this.palavrasAcertadas = new TreeMap<>();
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
            conn.close(4003, "Conexão duplicada.");
        } else if (connections.containsKey(getIDfromSocket(conn))) {
            conn.send("O nome \"" + getIDfromSocket(conn) + "\" já está em uso. Tente novamente.");
            conn.close(4002, "Nome já utilizado por outro jogador");
        } else {
            String connName = getIDfromSocket(conn);
            connections.put(connName, conn);
            playerState.put(connName, 0);
            boolean[] acertos = new boolean[10];
            palavrasAcertadas.put(connName, acertos);
            System.out.println(connName + " conectado.");
            conn.send("-------");
            broadcast(connName + " entrou na partida.");
            broadcast(connections.size() + "jogadores estão conectados no servidor.");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            conn.send("-------");
            conn.send("Bem-vindo ao Typerace Online!");
            conn.send("- REGRAS -");
            conn.send("O objetivo do jogo é escrever o máximo de palavras no menor tempo possível.");
            conn.send("Após o início da partida, os jogadores receberão palavras nas suas telas em uma mesma ordem.");
            conn.send("Se você digitar a palavra corretamente, ganha um ponto.");
            conn.send("A primeira pessoa a atingir 10 pontos vence a partida!");
            conn.send("- INICIANDO O JOGO -");
            conn.send("Quando estiver pronto para jogar, envie o comando /pronto");
            conn.send("Se houverem mais de dois jogadores no servidor e todos estiverem prontos, uma contagem regressiva se iniciará automaticamente");
            conn.send("Você pode pedir para interromper a contagem regressiva e esperar o próximo jogador entrar com o comando /aguardar");
            conn.send("Se você desistir de aguardar, pode utilizar o comando /parar-de-aguardar para continuar a contagem regressiva");
            conn.send("Para sair do servidor, basta enviar o comando /sair");
            conn.send("-------");
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
        System.out.println(getIDfromSocket(conn) + " desconectado. Motivo: " + reason + " (Cód. " + code + "). " + (remote ? "Desconectado pelo cliente." : "Desconectado pelo servidor."));
        broadcast(getIDfromSocket(conn) + " foi desconectado da partida.");
        connections.remove(getIDfromSocket(conn));
        broadcast(connections.size() + "jogadores estão conectados no servidor.");
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
            if (message.equalsIgnoreCase("/pronto") && playerState.get(getIDfromSocket(conn)) == 0) {
                broadcast(getIDfromSocket(conn) + " está pronto para começar.");
                playerState.put(getIDfromSocket(conn), 1);
                if (this.state == 0 && connections.size() > 1 && !playerState.containsValue(0)) {
        			try {
                		startGame();
            		} catch (InterruptedException e) {
                		e.printStackTrace();
            		}
        		} else if (connections.size() == 1) {
                    broadcast("Pelo menos dois jogadores são necessários para iniciar partida.");
                }
            } else if (message.equalsIgnoreCase("/sair")) {
                conn.close(1001, "Solicitação do jogador");
            } else if (message.equalsIgnoreCase("/aguardar")) {
                this.state = 1;
            } else if (message.equalsIgnoreCase("/parar-de-aguardar")) {
                this.state = 0;
                if (connections.size() > 1 && !playerState.containsValue(0)) {
                    try {
                        startGame();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (connections.size() == 1) {
                    broadcast("Pelo menos dois jogadores são necessários para iniciar partida.");
                }
            }

        // Entradas das palavras do jogo.
        } else {
            if (matchWords.contains(message)) {
            	boolean[] acertos = palavrasAcertadas.get(getIDfromSocket(conn));
            	int nroDaPalavra = matchWords.indexOf(message);
            	if (acertos[nroDaPalavra]) {
            		conn.send("Palavra repetida! Insira outra palavra.");
            	}
            	else {
            		conn.send("Palavra "+(nroDaPalavra+1)+" correta!");
            		acertos[nroDaPalavra] = true;
            		palavrasAcertadas.put(getIDfromSocket(conn), acertos);
            		for (int i=0; i<10; i++) {
            			if (!acertos[i]) break;
            			if (i==9) endGame(); // Chamada de fim de jogo.
            		}
            	}
            } else 
            	conn.send("Palavra incorreta. Tente novamente!");
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
        conn.close(4001, "Lançamento da exceção " + ex.toString());
    }

    /**
     * Chamada na inicialização do servidor.
     */
    @Override
    public void onStart() {
        System.out.println("Servidor iniciado com sucesso na porta " + this.getPort() + ".");
        insertWords();
        this.bankWords = new ArrayList<>(wordBank);
    }

    /**
     * Extrai ID do jogador inserido na URI onde o socket (cliente) se conecta.
     *
     * @param conn Cliente do jogador buscado; tem sua URI extraída
     */
    private String getIDfromSocket(WebSocket conn) {
        int start = conn.getResourceDescriptor().indexOf("/", 5) + 2;
        return conn.getResourceDescriptor().substring(start);
    }

    /**
     * Aguarda 5 segundos e inicia a partida.
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

        Collections.shuffle(this.bankWords);
        this.matchWords = new ArrayList();
    	broadcast("\n************************************\n\nPALAVRAS A SEREM DIGITADAS:\n");
    	ListIterator<String> it = bankWords.listIterator();
    	for (int i = 0; i < 10; i++) {
    		String palavra = it.next();
    		broadcast(palavra);
    		matchWords.add(palavra);
    	}
    }

	/**
     * Encerra a Partida.
     * Imprime as pontuações e termina o jogo.
     */
    public void endGame() {

    	//TODO: alterar os playerState para 0.

    	this.state = 0;

    	broadcast("\n************************************\n\nACABOOOOOOU\n");

    	//TODO: imprimir o resultado.
    }

    /**
     * Insere palavras no banco de palavras. Acessar gerador de palavras <a href="https://www.palabrasaleatorias.com/palavras-aleatorias.php?fs=10&fs2=0&Submit=Nova+palavra">aqui</a> ou <a href="https://randomwordsgen.com/palavras-aleatorias">aqui</a>.
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
        wordBank.add("Empurre"); wordBank.add("Bravo");wordBank.add("Supermercado");wordBank.add("Coador");wordBank.add("Ateu");wordBank.add("Muleteiro");
        wordBank.add("Atriz"); wordBank.add("Nariz");wordBank.add("Revista");wordBank.add("Barretina");wordBank.add("Ninja");wordBank.add("Hospitalidade");
        wordBank.add("Defeito"); wordBank.add("Prego");wordBank.add("Pescador");wordBank.add("Pesos");wordBank.add("Guia");wordBank.add("Brilhante");
        wordBank.add("Pugilista"); wordBank.add("Especial");wordBank.add("Pausa");wordBank.add("Menina");wordBank.add("Desalento");wordBank.add("Lugar");
        wordBank.add("Lamente"); wordBank.add("Regojizo");wordBank.add("Nome");wordBank.add("Feio");wordBank.add("Arca");wordBank.add("Dipstick");
        wordBank.add("Paciente"); wordBank.add("Quadrado");wordBank.add("Sismo");wordBank.add("Assunto");wordBank.add("Utilitário");wordBank.add("Ciclo");
        wordBank.add("Incorporar"); wordBank.add("Supremo");wordBank.add("Colher de sopa");wordBank.add("Pressione");wordBank.add("Junta");wordBank.add("Bonito");
        wordBank.add("Advogada"); wordBank.add("Baixa");wordBank.add("Associado");wordBank.add("Ousar");wordBank.add("Aparecer");wordBank.add("Tom");
        wordBank.add("Gravíssimo"); wordBank.add("Desfalcar");wordBank.add("Hotel");wordBank.add("Incapaz");wordBank.add("Seguro");wordBank.add("Aplique");
        wordBank.add("Lugar algum"); wordBank.add("Relatório");wordBank.add("Refeitório");wordBank.add("Asiática");wordBank.add("Feliz");wordBank.add("Suponha");
        wordBank.add("Ameaçar"); wordBank.add("Desconhecido");wordBank.add("Acessibilidade");wordBank.add("O negócio");wordBank.add("Contra");wordBank.add("Pintura");
        wordBank.add("A maioria"); wordBank.add("Conduta");wordBank.add("Além");wordBank.add("Uma");wordBank.add("Católico");wordBank.add("Habilitar");
        wordBank.add("Encorajar"); wordBank.add("Óvulo");wordBank.add("Gangue");wordBank.add("Dedetizadora");wordBank.add("Ribeirão");wordBank.add("Fisionomia");
        wordBank.add("Raro"); wordBank.add("Ferro");wordBank.add("Quieto");wordBank.add("Estimativa");wordBank.add("Quente");wordBank.add("Colega");
        wordBank.add("Call center"); wordBank.add("Quadrático");wordBank.add("Exposição");wordBank.add("Fonte");wordBank.add("Triangulação");wordBank.add("Forte");
        wordBank.add("Cerveja"); wordBank.add("Critério");wordBank.add("Boca");wordBank.add("Admissionalidade");wordBank.add("Sombrio");wordBank.add("Bolo");
        wordBank.add("Orar"); wordBank.add("Civil");wordBank.add("Revirar");wordBank.add("Impeachment");wordBank.add("Caracterização");wordBank.add("Auto-escola");
        wordBank.add("Profundidade"); wordBank.add("Batalha");wordBank.add("Analisar");wordBank.add("Doença");wordBank.add("Familiar");wordBank.add("Orgulho");
        wordBank.add("Ocidental"); wordBank.add("Engenharia");wordBank.add("Legítimo");wordBank.add("Concentração");wordBank.add("Primo");wordBank.add("Desenvoltura");
    }
}
