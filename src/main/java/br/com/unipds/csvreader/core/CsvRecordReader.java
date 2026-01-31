package br.com.unipds.csvreader.core;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class CsvRecordReader {

    // --- CONFIGURAÇÕES DE ESTADO (Definidas pelo Builder) ---
    private final boolean hasHeader;
    private final String separator;

    // Construtor é PRIVADO. Só o Builder pode chamar.
    private CsvRecordReader(boolean hasHeader, String separator) {
        this.hasHeader = hasHeader;
        this.separator = separator;
    }

    // --- LÓGICA DO BUILDER ---
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean hasHeader = true; // Valor padrão
        private String separator = null;  // Valor padrão (auto-detect)

        public Builder withHeader(boolean hasHeader) {
            this.hasHeader = hasHeader;
            return this;
        }

        public Builder withSeparator(String separator) {
            this.separator = separator;
            return this;
        }

        public CsvRecordReader build() {
            return new CsvRecordReader(hasHeader, separator);
        }
    }

    // --- MÉTODOS PÚBLICOS (Agora usam a config da instância) ---

    // 1. Ler Arquivo
    public <T> List<T> read(String fileName, Class<T> targetClass) {
        try (Stream<T> stream = streamFromFile(fileName, targetClass)) {
            return stream.toList();
        }
    }

    // 2. Ler String (O erro estava aqui: removemos o argumento boolean hasHeader)
    public <T> List<T> readString(String csvContent, Class<T> targetClass) {
        if (csvContent == null || csvContent.isBlank()) {
            return List.of();
        }

        // Usa o separador do Builder OU detecta automático
        String effectiveSeparator = (this.separator != null) ?
                this.separator :
                detectSeparatorInLine(csvContent.lines().findFirst().orElse(""));

        Stream<String> lines = csvContent.lines();

        // Usa a configuração 'hasHeader' da instância (criada pelo Builder)
        if (this.hasHeader) {
            lines = lines.skip(1);
        }

        return mapToStream(lines, effectiveSeparator, targetClass).toList();
    }

    // 3. Processar (Stream)
    public <T> void process(String fileName, Class<T> targetClass, Consumer<T> processor) {
        try (Stream<T> stream = streamFromFile(fileName, targetClass)) {
            stream.forEach(processor);
        }
    }

    // --- CORE (Lógica Interna) ---
    // (Mantive os métodos auxiliares que já funcionavam)

    private <T> Stream<T> streamFromFile(String fileName, Class<T> targetClass) {
        Path path = Paths.get(fileName);
        String finalSeparator = (this.separator != null) ? this.separator : detectSeparator(path);

        try {
            Stream<String> fileLines = Files.lines(path);
            if (this.hasHeader) {
                fileLines = fileLines.skip(1);
            }
            return mapToStream(fileLines, finalSeparator, targetClass);
        } catch (IOException e) {
            throw new CsvParsingException("Erro de IO: " + e.getMessage(), e);
        }
    }

    private <T> Stream<T> mapToStream(Stream<String> lines, String separator, Class<T> targetClass) {
        return lines
                .filter(line -> !line.trim().isEmpty())
                .map(line -> {
                    if (targetClass.isRecord()) return mapToRecord(line, separator, targetClass);
                    else return mapToPojo(line, separator, targetClass);
                });
    }

    // --- MAPPER PARA RECORDS ---
    private <T> T mapToRecord(String line, String separator, Class<T> recordClass) {
        try {
            RecordComponent[] components = recordClass.getRecordComponents();
            Class<?>[] types = Arrays.stream(components).map(RecordComponent::getType).toArray(Class<?>[]::new);
            Constructor<T> constructor = recordClass.getDeclaredConstructor(types);
            return constructor.newInstance(parseLineValues(line, separator, types));
        } catch (Exception e) {
            throw new CsvParsingException("Erro Record: " + line, e);
        }
    }

    // --- MAPPER PARA POJO (Classes Normais) ---
    private <T> T mapToPojo(String line, String separator, Class<T> pojoClass) {
        try {
            Constructor<T> constructor = pojoClass.getDeclaredConstructor();
            T instance = constructor.newInstance();
            Field[] fields = pojoClass.getDeclaredFields();
            Class<?>[] types = Arrays.stream(fields).map(Field::getType).toArray(Class<?>[]::new);
            Object[] values = parseLineValues(line, separator, types);

            for (int i = 0; i < fields.length; i++) {
                if (i < values.length) {
                    fields[i].setAccessible(true);
                    fields[i].set(instance, values[i]);
                }
            }
            return instance;
        } catch (Exception e) {
            throw new CsvParsingException("Erro POJO: " + line, e);
        }
    }

    // --- PARSERS E UTILITÁRIOS ---

    private Object[] parseLineValues(String line, String separator, Class<?>[] types) {
        String regex = separator + "(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
        String[] cols = line.split(regex, -1);
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            String val = (i < cols.length) ? cols[i] : null;
            if (val != null && val.startsWith("\"") && val.endsWith("\"")) val = val.substring(1, val.length() - 1);
            args[i] = convert(types[i], val);
        }
        return args;
    }

    private String detectSeparator(Path path) {
        try (Stream<String> lines = Files.lines(path)) {
            return detectSeparatorInLine(lines.findFirst().orElse(""));
        } catch (IOException e) { return ","; }
    }

    private String detectSeparatorInLine(String line) {
        if (line.isEmpty()) return ",";
        int commas = line.replaceAll("[^,]", "").length();
        int semis = line.replaceAll("[^;]", "").length();
        return semis >= commas ? ";" : ",";
    }

    private static final Map<Class<?>, Function<String, Object>> CONVERTERS = Map.of(
            int.class, Integer::parseInt, long.class, Long::parseLong,
            boolean.class, Boolean::parseBoolean, String.class, s -> s,
            double.class, s -> (s != null && !s.isEmpty()) ? Double.parseDouble(s) : 0.0
    );

    private Object convert(Class<?> type, String value) {
        Function<String, Object> conv = CONVERTERS.get(type);
        if (conv == null) throw new CsvParsingException("Tipo não suportado: " + type.getName());
        if (value == null) {
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == double.class) return 0.0;
            if (type == boolean.class) return false;
            return null;
        }
        return conv.apply(value);
    }
}