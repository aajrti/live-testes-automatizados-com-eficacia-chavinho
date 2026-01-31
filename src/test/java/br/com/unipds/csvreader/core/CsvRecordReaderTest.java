package br.com.unipds.csvreader.core;

import br.com.unipds.csvreader.model.Disciplina;
import br.com.unipds.csvreader.model.ItemCardapio;
import br.com.unipds.csvreader.model.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class CsvReaderTest {

    private CsvRecordReader csvRecordReader;

    @BeforeEach
    void setup() {
        // Configuração Padrão: Com Header
        csvRecordReader = CsvRecordReader.builder()
                .withHeader(true)
                .build();
    }

    @Test
    @DisplayName("Deve converter String CSV em memória para objetos corretamente")
    void deveLerStringParaDisciplina() {
        String csv = """
            id,nome
            1,Java Avançado
            """;

        // CORREÇÃO: Removido o argumento 'true/false'.
        // O leitor já sabe que tem header por causa do setup()
        List<Disciplina> lista = csvRecordReader.readString(csv, Disciplina.class);

        Assertions.assertEquals(1, lista.size());
        Assertions.assertEquals("Java Avançado", lista.getFirst().nome());
    }

    @Test
    @DisplayName("Deve suportar Generics e ler outros tipos (ItemCardapio)")
    void deveLerStringParaItemCardapio() {
        // CSV SEM CABEÇALHO
        String csv = "100;Tacos Pastor";

        // CORREÇÃO: Criamos um leitor específico para este teste
        CsvRecordReader leitorSemHeader = CsvRecordReader.builder()
                .withHeader(false) // <--- Configuração aqui
                .build();

        // Passamos APENAS o csv e a classe. O 'false' já está no leitor.
        List<ItemCardapio> lista = leitorSemHeader.readString(csv, ItemCardapio.class);

        Assertions.assertEquals(1, lista.size());

        // Se ItemCardapio for um RECORD, o método .nome() vai funcionar.
        // Se estiver dando erro, verifique se ItemCardapio.java está salvo como 'record'
        Assertions.assertEquals("Tacos Pastor", lista.getFirst().nome());
    }

    @Test
    @DisplayName("Product: Deve processar arquivo gigante")
    void deveProcessarArquivoGiganteSemEstourarMemoria() {
        // Esse método usa o Product.processarCsv que corrigimos no passo 1
        Product.processarCsv("src/test/resources/products-2000000.csv", produto -> {
            Assertions.assertNotNull(produto.name());
        });
    }

    // ... Mantenha os outros testes de Disciplina.lerCsv ...
}