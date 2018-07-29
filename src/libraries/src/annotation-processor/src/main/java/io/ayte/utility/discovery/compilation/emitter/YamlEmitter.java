package io.ayte.utility.discovery.compilation.emitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.ayte.utility.discovery.api.v1.SymbolCollection;
import io.ayte.utility.discovery.compilation.ArtifactEmitter;
import io.ayte.utility.discovery.infrastructure.Constants;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class YamlEmitter implements ArtifactEmitter {

    private final ObjectMapper encoder;

    public YamlEmitter() {
        this(new ObjectMapper(new YAMLFactory()).enableDefaultTyping());
    }

    @Override
    public List<Artifact> emit(Map<String, ? extends SymbolCollection> elements) throws IOException {
        val destination = Constants.GENERATED_RESOURCES_TARGET;
        val accumulator = new ArrayList<Artifact>(elements.size() + 1);
        val content = encoder.writeValueAsString(elements);
        accumulator.add(Artifact.createResource(destination.resolve("$combined.yml").toString(), content));
        for (val entry : elements.entrySet()) {
            val path = destination.resolve(entry.getKey() + ".yml").toString();
            accumulator.add(Artifact.createResource(path, encoder.writeValueAsString(entry.getValue())));
        }
        return accumulator;
    }
}
