package br.com.unipds.csvreader.model;

import br.com.unipds.csvreader.core.CsvRecordReader;
import java.util.List;

// GARANTA QUE É "public record" E NÃO "public class"
public record ItemCardapio(
        long id,
        String nome,
        String descricao,
        double preco,
        String categoria,
        boolean emPromocao,
        double precoComDesconto,
        boolean impostoIsento) {

    public static List<ItemCardapio> lerCsv(String caminhoArquivo) {
        // CORREÇÃO: Usar o Builder aqui!
        return CsvRecordReader.builder()
                .withHeader(true)
                .build()
                .read(caminhoArquivo, ItemCardapio.class);
    }
}