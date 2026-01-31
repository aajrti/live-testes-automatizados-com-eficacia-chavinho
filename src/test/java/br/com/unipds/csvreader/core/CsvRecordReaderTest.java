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

    @BeforeEach
    void setup() {
        CsvRecordReader csvRecordReader = CsvRecordReader.builder()
                .withHeader(true)
                .build();
    }

    @Test
    @DisplayName("Deve suportar Generics e ler outros tipos (ItemCardapio)")
    void deveLerStringParaItemCardapio() {

        String csv = "100;Tacos Pastor";

        CsvRecordReader leitorSemHeader = CsvRecordReader.builder()
                .withHeader(false)
                .build();

        List<ItemCardapio> lista = leitorSemHeader.readString(csv, ItemCardapio.class);

        Assertions.assertEquals(1, lista.size());
        Assertions.assertEquals("Tacos Pastor", lista.getFirst().getNome());
    }

    @Test
    @DisplayName("Product: Deve processar arquivo gigante")
    void deveProcessarArquivoGiganteSemEstourarMemoria() {
        Product.processarCsv("src/test/resources/products-2000000.csv", produto -> {
            Assertions.assertNotNull(produto.getName());
        });
    }

    @Test
    @DisplayName("Disciplina: Deve lançar exceção se o arquivo não existe (via método estático)")
    void deveLancarExcecaoSeArquivoNaoExiste() {
        Assertions.assertThrows(CsvParsingException.class, () -> {
            Disciplina.lerCsv("caminho/falso/nao/existe.csv");
        });
    }

    @Test
    @DisplayName("Disciplina: Deve ler arquivo real do disco corretamente")
    void deveLerArquivoDeDisciplinas() {
        List<Disciplina> lista = Disciplina.lerCsv("src/test/resources/unipds-disciplinas.csv");

        Assertions.assertFalse(lista.isEmpty(), "A lista não deveria estar vazia");
        Assertions.assertEquals(11, lista.size(), "Deveria ter lido 11 disciplinas");
        Assertions.assertEquals("Introdução ao Java", lista.getFirst().getNome().trim());
    }
}