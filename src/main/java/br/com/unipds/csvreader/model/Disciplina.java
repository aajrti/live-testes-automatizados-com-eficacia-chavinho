package br.com.unipds.csvreader.model;

import br.com.unipds.csvreader.core.CsvRecordReader;

import java.util.List;

public class Disciplina {

    private int id;
    private String nome;

    public Disciplina() {
    }

    public static List<Disciplina> lerCsv(String caminhoArquivo) {
        return CsvRecordReader.builder()
                .withHeader(true)
                .build()
                .read(caminhoArquivo, Disciplina.class);
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        return "Disciplina{id=" + id + ", nome='" + nome + "'}";
    }
}