package br.com.unipds.CsvReader.model;

import br.com.unipds.CsvReader.CsvRecordReader;

import java.util.List;

public record Disciplina(int numero, String nome) {

    public static List<Disciplina> lerCsv(String caminhoArquivo) {
        CsvRecordReader reader = new CsvRecordReader();
        return reader.read(caminhoArquivo, true, Disciplina.class);
    }
}