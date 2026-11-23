package SEMANA18.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class ServidorCooperativa {

    public static void responder(HttpExchange ex, int codigo, String json)
            throws IOException {
        byte[] datos = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(codigo, datos.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(datos);
        }
    }

    static String error(String mensaje) {
        return "{\"error\": \"" + mensaje + "\"}";
    }

    public static void main(String[] args) throws IOException {
        HttpServer servidor = HttpServer.create(new InetSocketAddress(8080), 0);
        servidor.createContext("/socios", ex -> responder(ex, 200, repo.listarJson()));
        servidor.createContext("/socio", ex -> {
            String consulta = ex.getRequestURI().getQuery();
            String id = consulta == null ? "" : consulta.replace("id=", "");
            try {
                responder(ex, 200, repo.buscar(id).toJson());
            } catch (SocioNoEncontradoException e) {
                responder(ex, 404, error(e.getMessage()));
            } catch (DatoInvalidoException e) {
                responder(ex, 400, error(e.getMessage()));
            }
        });

        servidor.createContext("/hilo", ex -> {
            String hilo = Thread.currentThread().getName();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
            responder(ex, 200, "{\"atendido_por\": \"" + hilo + "\"}");
        });

        servidor.setExecutor(Executors.newFixedThreadPool(4));
        servidor.start();
        System.out.println("Servidor en http://localhost:8080");
    }
}
