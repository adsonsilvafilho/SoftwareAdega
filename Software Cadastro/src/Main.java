import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

public class Main {

    static class Produto {
        String nome; int qtd; double preco;
        Produto(String nome, int qtd, double preco) { this.nome = nome; this.qtd = qtd; this.preco = preco; }
    }

    record Venda(String cliente, String produto, int qtd, double total, LocalDate data) {}

    static final List<Produto> produtos = new ArrayList<>();
    static final List<String> clientes = new ArrayList<>();
    static final List<Venda> vendas = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        Armazenamento.carregar(); // lê os dados salvos na pasta "dados"

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", Main::tratar);
        server.start();
        System.out.println("Sistema no ar! Abra no navegador: http://localhost:8080");
    }

   // Roteamento

    static synchronized void tratar(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();

        if (ex.getRequestMethod().equals("POST")) {
            Map<String, String> f = form(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String msg;
            try {
                msg = switch (path) {
                    case "/produto" -> addProduto(f);
                    case "/cliente" -> addCliente(f);
                    case "/venda" -> addVenda(f);
                    default -> "";
                };
                Armazenamento.salvar(); // grava em arquivo depois de cada alteração
            } catch (NumberFormatException e) {
                msg = "Erro: digite valores numéricos válidos.";
            }
            ex.getResponseHeaders().set("Location", "/?msg=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
            ex.sendResponseHeaders(303, -1);
            ex.close();
            return;
        }

        String q = ex.getRequestURI().getRawQuery();
        byte[] corpo = HTML.pagina(form(q == null ? "" : q)).getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        ex.sendResponseHeaders(200, corpo.length);
        ex.getResponseBody().write(corpo);
        ex.close();
    }

    //  Ações

    static String addProduto(Map<String, String> f) {
        String nome = f.getOrDefault("nome", "").trim();
        int qtd = Integer.parseInt(f.getOrDefault("qtd", "").trim());
        if (nome.isEmpty() || qtd <= 0) return "Informe um nome e uma quantidade maior que zero.";
        Produto p = buscar(nome);
        if (p != null) {
            p.qtd += qtd;
            return "Estoque de " + p.nome + " atualizado para " + p.qtd + " unidades.";
        }
        double preco = Double.parseDouble(f.getOrDefault("preco", "").trim().replace(",", "."));
        produtos.add(new Produto(nome, qtd, preco));
        return "Produto cadastrado: " + nome;
    }

    static String addCliente(Map<String, String> f) {
        String nome = f.getOrDefault("nome", "").trim();
        if (nome.isEmpty()) return "O nome do cliente não pode ser vazio.";
        if (!clientes.contains(nome)) clientes.add(nome);
        return "Cliente cadastrado: " + nome;
    }

    static String addVenda(Map<String, String> f) {
        String cliente = f.getOrDefault("cliente", "").trim();
        Produto p = buscar(f.getOrDefault("produto", ""));
        int qtd = Integer.parseInt(f.getOrDefault("qtd", "").trim());
        if (cliente.isEmpty()) return "Informe o cliente.";
        if (p == null) return "Produto não encontrado.";
        if (qtd <= 0 || qtd > p.qtd) return "Quantidade inválida. Disponível em estoque: " + p.qtd;
        if (!clientes.contains(cliente)) clientes.add(cliente);
        p.qtd -= qtd;
        double total = qtd * p.preco;
        vendas.add(new Venda(cliente, p.nome, qtd, total, LocalDate.now()));
        return "Venda registrada: " + HTML.moeda(total);
    }

    static Produto buscar(String nome) {
        for (Produto p : produtos) if (p.nome.equalsIgnoreCase(nome.trim())) return p;
        return null;
    }

    // Utilitário

    static Map<String, String> form(String corpo) {
        Map<String, String> m = new HashMap<>();
        for (String par : corpo.split("&")) {
            if (par.isEmpty()) continue;
            String[] kv = par.split("=", 2);
            m.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                    kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "");
        }
        return m;
    }
}