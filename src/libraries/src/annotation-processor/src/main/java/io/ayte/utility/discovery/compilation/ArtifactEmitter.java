package io.ayte.utility.discovery.compilation;

import io.ayte.utility.discovery.api.v1.SymbolCollection;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public interface ArtifactEmitter {
    List<Artifact> emit(Map<String, ? extends SymbolCollection> elements) throws IOException;

    @Data
    @ToString(of = {"type", "path"})
    @EqualsAndHashCode(of = {"type", "path"})
    class Artifact {
        public enum Type {
            SOURCE,
            CLASS,
            RESOURCE
        }

        private final Type type;
        private final String path;
        private final InputStream content;

        public static Artifact createResource(String path, InputStream content) {
            return new Artifact(Type.RESOURCE, path, content);
        }

        public static Artifact createResource(String path, String content) {
            return createResource(path, new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        }

        public static Artifact createSource(String path, InputStream content) {
            return new Artifact(Type.SOURCE, path, content);
        }

        public static Artifact createSource(String path, String content) {
            return new Artifact(Type.SOURCE, path, new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        }
    }
}
