package br.com.unipds.csvreader.model;

public class DisciplinaClassica {
    private int id;
    private String nome;

    // Obrigatório ter construtor vazio para o leitor funcionar
    public DisciplinaClassica() {}

    public int getId() { return id; }
    public String getNome() { return nome; }
}
