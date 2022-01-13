package br.usp.each.typerace.server;

import org.java_websocket.server.WebSocketServer;

import java.util.HashMap;
import java.util.Scanner;

public class ServerMain {

    private WebSocketServer server;

    /**
     * Construtor.
     *
     * @param server Indica socket com servidor a ser executado.
     */
    public ServerMain(WebSocketServer server) {
        this.server = server;
    }

    /**
     * Inicializa servidor.
     */
    public void init() {
        System.out.println("Iniciando servidor...");
        server.start();
    }



    /**
     * Método inicial do programa executor do servidor.
     *
     * @param args Argumentos passados pela linha de comando.
     */
    public static void main(String[] args) throws InterruptedException {
        Scanner sc = new Scanner(System.in);
        WebSocketServer server = new Server(8080, new HashMap<>());

        ServerMain main = new ServerMain(server);

        main.init();


        /*
            NAO FUNCIONA!!
        Por algum motivo, o System.in parece ter sido fechado anteriormente,
        e fechada de forma global, nao sendo aberta novamente em uma mesma 
        execucao (pela parte do servidor, pelo cliente ainda funciona normal-
        mente).
        Nao parece ser pela classe Server nem pelo server.start(), pois mesmo
        no comeco do main o scanner nao funciona corretamente. Tentei de 
        varias formas e inclusive usei o BufferedReader em vez do Scanner,
        mas nada funciona. Me avise se este comando funcionar na sua maquina,
        ou caso consiga fazer esta funcao funcionar. Caso contrario, esta 
        funcionalidade nao eh pedida em enunciado, e talvez fosse melhor 
        nao gastar tanto tempo nela (ja perdi bastante tempo tentando
        resolver :p). Qualquer coisa, ou se eu estiver falando merda,
        manda mensagem no telegram :)

        while (true) {
            String input = sc.nextLine();
            if (input.equalsIgnoreCase("/encerrar")) {
                System.out.println("Encerrando servidor...");
                main.server.stop();
                break;
            }
        } */
    }

}