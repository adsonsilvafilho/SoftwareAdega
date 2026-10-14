import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


//  Guarda os dados em arquivos de texto
//  (CSV, separados por ;) dentro da pasta "dados".
public class Armazenamento {

    static final Path PASTA = Paths.get("dados");
    static final Path ARQ_PRODUTOS = PASTA.resolve("produtos.csv");
    static final Path ARQ_CLIENTES = PASTA.resolve("clientes.csv");
    static final Path ARQ_VENDAS = PASTA.resolve("vendas.csv");

    // Remove caracteres que quebrariam o formato do arquivo
    static String limpar(String s) {
        return s.replace(";", ",").replace("\n", " ").replace("\r", " ");
    }

    static void salvar() {
        try {
            Files.createDirectories(PASTA);

            List<String> linhas = new ArrayList<>();
            for (Main.Produto p : Main.produtos)
                linhas.add(limpar(p.nome) + ";" + p.qtd + ";" + p.preco);
            Files.write(ARQ_PRODUTOS, linhas, StandardCharsets.UTF_8);

            linhas = new ArrayList<>();
            for (String c : Main.clientes) linhas.add(limpar(c));
            Files.write(ARQ_CLIENTES, linhas, StandardCharsets.UTF_8);

            linhas = new ArrayList<>();
            for (Main.Venda v : Main.vendas)
                linhas.add(limpar(v.cliente()) + ";" + limpar(v.produto()) + ";" + v.qtd() + ";" + v.total() + ";" + v.data());
            Files.write(ARQ_VENDAS, linhas, StandardCharsets.UTF_8);

        } catch (IOException e) {
            System.out.println("Erro ao salvar os dados: " + e.getMessage());
        }
    }

    static void carregar() {
        try {
            if (Files.exists(ARQ_PRODUTOS))
                for (String linha : Files.readAllLines(ARQ_PRODUTOS, StandardCharsets.UTF_8)) {
                    String[] c = linha.split(";");
                    if (c.length == 3)
                        Main.produtos.add(new Main.Produto(c[0], Integer.parseInt(c[1]), Double.parseDouble(c[2])));
                }

            if (Files.exists(ARQ_CLIENTES))
                for (String linha : Files.readAllLines(ARQ_CLIENTES, StandardCharsets.UTF_8))
                    if (!linha.isBlank()) Main.clientes.add(linha);

            if (Files.exists(ARQ_VENDAS))
                for (String linha : Files.readAllLines(ARQ_VENDAS, StandardCharsets.UTF_8)) {
                    String[] c = linha.split(";");
                    if (c.length == 5)
                        Main.vendas.add(new Main.Venda(c[0], c[1], Integer.parseInt(c[2]),
                                Double.parseDouble(c[3]), LocalDate.parse(c[4])));
                }

            System.out.println("Dados carregados de: " + PASTA.toAbsolutePath());
        } catch (IOException | RuntimeException e) {
            System.out.println("Erro ao carregar os dados: " + e.getMessage());
        }
    }
}