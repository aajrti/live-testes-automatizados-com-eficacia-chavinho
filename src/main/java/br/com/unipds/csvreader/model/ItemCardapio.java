package br.com.unipds.csvreader.model;

import br.com.unipds.csvreader.core.CsvRecordReader;
import java.util.List;

public class ItemCardapio {

    public ItemCardapio() {
    }

    private long id;
    private String nome;
    private String descricao;
    private double preco;
    private String categoria;
    private boolean emPromocao;
    private double precoComDesconto;
    private boolean impostoIsento;

    public long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public double getPreco() { return preco; }
    public String getCategoria() { return categoria; }
    public boolean isEmPromocao() { return emPromocao; }
    public double getPrecoComDesconto() { return precoComDesconto; }
    public boolean isImpostoIsento() { return impostoIsento; }

    @Override
    public String toString() {
        return "ItemCardapio{id=" + id + ", nome='" + nome + "'}";
    }

    public static List<ItemCardapio> lerCsv(String caminhoArquivo) {
        return CsvRecordReader.builder()
                .withHeader(true)
                .build()
                .read(caminhoArquivo, ItemCardapio.class);
    }
}