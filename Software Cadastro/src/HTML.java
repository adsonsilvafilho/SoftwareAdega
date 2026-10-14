import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

public class HTML {

    static final String CSS = """
            body{font-family:Arial,sans-serif;background:#f3f4f6;margin:0;color:#222}
            header{background:#1e3a8a;color:#fff;padding:16px 24px}
            main{max-width:1000px;margin:auto;padding:16px;display:grid;gap:16px;grid-template-columns:repeat(auto-fit,minmax(440px,1fr))}
            section{background:#fff;border-radius:8px;padding:16px;box-shadow:0 1px 3px #0002}
            h2{margin-top:0;font-size:18px;color:#1e3a8a}
            input,select,button{padding:8px;margin:3px 3px 3px 0;border:1px solid #bbb;border-radius:4px}
            button{background:#1e3a8a;color:#fff;border:0;cursor:pointer}
            table{width:100%;border-collapse:collapse;margin-top:8px}
            td,th{border-bottom:1px solid #eee;padding:6px;text-align:left;font-size:14px}
            .msg{background:#dcfce7;padding:10px 24px}
            .big{font-size:26px;font-weight:bold}
            """;

    static String pagina(Map<String, String> q) {
        LocalDate hoje = LocalDate.now();
        int mes = hoje.getMonthValue(), ano = hoje.getYear();
        try {
            if (q.containsKey("mes")) mes = Integer.parseInt(q.get("mes"));
            if (q.containsKey("ano")) ano = Integer.parseInt(q.get("ano"));
        } catch (NumberFormatException ignored) { }

        StringBuilder h = new StringBuilder();
        h.append("<!DOCTYPE html><html lang='pt-BR'><head><meta charset='UTF-8'>")
                .append("<meta name='viewport' content='width=device-width, initial-scale=1'>")
                .append("<title>Adega do Silva</title><style>").append(CSS).append("</style></head><body>")
                .append("<header><h1 style='margin:0'>Adega do Silva</h1></header>");

        String msg = q.get("msg");
        if (msg != null && !msg.isEmpty()) h.append("<div class='msg'>").append(esc(msg)).append("</div>");

        h.append("<main>");
        estoque(h);
        clientes(h);
        venda(h);
        faturamento(h, mes, ano);
        return h.append("</main></body></html>").toString();
    }

    // ---------- Blocos da página ----------

    static void estoque(StringBuilder h) {
        int itens = 0;
        double valor = 0;
        for (Main.Produto p : Main.produtos) { itens += p.qtd; valor += p.qtd * p.preco; }

        h.append("<section><h2>Estoque</h2><form method='post' action='/produto'>")
                .append("<input name='nome' placeholder='Produto' required>")
                .append("<input name='qtd' type='number' min='1' placeholder='Qtd' required style='width:80px'>")
                .append("<input name='preco' placeholder='Preço (só p/ novo)' style='width:140px'>")
                .append("<button>Adicionar</button></form>")
                .append("<table><tr><th>Produto</th><th>Qtd</th><th>Preço</th></tr>");
        for (Main.Produto p : Main.produtos)
            h.append("<tr><td>").append(esc(p.nome)).append("</td><td>").append(p.qtd)
                    .append("</td><td>").append(moeda(p.preco)).append("</td></tr>");
        h.append("</table><p>Total de itens: <b>").append(itens)
                .append("</b> | Valor em estoque: <b>").append(moeda(valor)).append("</b></p></section>");
    }

    static void clientes(StringBuilder h) {
        h.append("<section><h2>Clientes</h2><form method='post' action='/cliente'>")
                .append("<input name='nome' placeholder='Nome do cliente' required><button>Cadastrar</button></form><table>");
        for (String c : Main.clientes) h.append("<tr><td>").append(esc(c)).append("</td></tr>");
        h.append("</table></section>");
    }

    static void venda(StringBuilder h) {
        h.append("<section><h2>Registrar venda</h2><form method='post' action='/venda'>")
                .append("<input name='cliente' list='lc' placeholder='Cliente' required><datalist id='lc'>");
        for (String c : Main.clientes) h.append("<option value='").append(esc(c)).append("'>");
        h.append("</datalist><select name='produto' required>");
        for (Main.Produto p : Main.produtos) h.append("<option>").append(esc(p.nome)).append("</option>");
        h.append("</select><input name='qtd' type='number' min='1' placeholder='Qtd' required style='width:80px'>")
                .append("<button>Vender</button></form>")
                .append("<table><tr><th>Data</th><th>Cliente</th><th>Produto</th><th>Qtd</th><th>Total</th></tr>");
        for (int i = Main.vendas.size() - 1; i >= 0; i--) {
            Main.Venda v = Main.vendas.get(i);
            h.append("<tr><td>").append(v.data()).append("</td><td>").append(esc(v.cliente()))
                    .append("</td><td>").append(esc(v.produto())).append("</td><td>").append(v.qtd())
                    .append("</td><td>").append(moeda(v.total())).append("</td></tr>");
        }
        h.append("</table></section>");
    }

    static void faturamento(StringBuilder h, int mes, int ano) {
        double total = 0;
        int n = 0;
        for (Main.Venda v : Main.vendas)
            if (v.data().getMonthValue() == mes && v.data().getYear() == ano) { total += v.total(); n++; }

        h.append("<section><h2>Faturamento do mês</h2><form method='get' action='/'>")
                .append("Mês <input name='mes' type='number' min='1' max='12' value='").append(mes).append("' style='width:70px'>")
                .append(" Ano <input name='ano' type='number' value='").append(ano).append("' style='width:90px'>")
                .append("<button>Calcular</button></form>")
                .append("<p class='big'>").append(moeda(total)).append("</p>")
                .append("<p>").append(n).append(" venda(s) em ").append(mes).append("/").append(ano).append("</p></section>");
    }

    // ---------- Utilitários ----------

    static String moeda(double v) {
        return String.format(Locale.of("pt", "BR"), "R$ %,.2f", v);
    }

    static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("'", "&#39;").replace("\"", "&quot;");
    }
}