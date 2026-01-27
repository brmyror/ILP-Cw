package uk.ac.ed.acp.cw2.testutil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.ac.ed.acp.cw2.dto.LngLat;
import uk.ac.ed.acp.cw2.dto.MedDispatchRecRequest;
import uk.ac.ed.acp.cw2.dto.RestrictedAreaDto;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Small helper for tests: loads the visualiser JSON scenarios and converts them into request DTOs.
 */
public final class TestDataLoader {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private TestDataLoader() {}

    public static List<MedDispatchRecRequest> loadDispatchScenarioUseCase(String useCaseId) {
        JsonNode root = readJsonFromResource("/ilp-visualiser/data/dispatch-scenario.json");
        JsonNode useCases = root.get("useCases");
        if (useCases == null || !useCases.isArray()) {
            throw new IllegalStateException("dispatch-scenario.json missing useCases array");
        }

        for (JsonNode uc : useCases) {
            if (uc != null && useCaseId.equals(uc.path("id").asText())) {
                JsonNode dispatches = uc.get("dispatches");
                if (dispatches == null || !dispatches.isArray()) {
                    throw new IllegalStateException("useCase missing dispatches array: " + useCaseId);
                }

                List<MedDispatchRecRequest> result = new ArrayList<>();
                for (JsonNode d : dispatches) {
                    MedDispatchRecRequest req = new MedDispatchRecRequest();
                    req.setId(d.path("id").asInt());

                    String date = d.path("date").asText(null);
                    if (date != null && !date.isBlank()) req.setDate(LocalDate.parse(date));

                    String time = d.path("time").asText(null);
                    if (time != null && !time.isBlank()) req.setTime(LocalTime.parse(time));

                    MedDispatchRecRequest.Requirements r = new MedDispatchRecRequest.Requirements();
                    JsonNode rj = d.path("requirements");
                    if (rj.isMissingNode() || rj.isNull()) {
                        throw new IllegalStateException("dispatch missing requirements: " + d);
                    }
                    if (rj.has("capacity")) r.setCapacity(rj.get("capacity").asDouble());
                    r.setCooling(rj.path("cooling").asBoolean(false));
                    r.setHeating(rj.path("heating").asBoolean(false));
                    if (rj.has("maxCost") && !rj.get("maxCost").isNull()) r.setMaxCost(rj.get("maxCost").asDouble());
                    req.setRequirements(r);

                    JsonNode del = d.path("delivery");
                    LngLat delivery = LngLat.builder()
                            .lng(del.path("lng").asDouble())
                            .lat(del.path("lat").asDouble())
                            .build();
                    req.setDelivery(delivery);

                    result.add(req);
                }
                return result;
            }
        }

        throw new IllegalArgumentException("Unknown useCase id: " + useCaseId);
    }

    public static RestrictedAreaDto[] loadRestrictedAreas() {
        JsonNode root = readJsonArrayFromResource("/ilp-visualiser/data/restricted-areas.json");
        try {
            return MAPPER.treeToValue(root, RestrictedAreaDto[].class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse restricted-areas.json", e);
        }
    }

    private static JsonNode readJsonFromResource(String path) {
        // First try classpath (works in IDE if resources are configured)
        try (InputStream is = TestDataLoader.class.getResourceAsStream(path)) {
            if (is != null) {
                return MAPPER.readTree(is);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed reading resource: " + path, e);
        }

        // Fallback: read from repo path (works under Maven without copying visualiser files into test resources)
        try {
            String rel = path.startsWith("/") ? path.substring(1) : path;
            Path repoFile = Path.of(rel.replace('/', java.io.File.separatorChar));
            if (!Files.exists(repoFile)) {
                // try explicit location within this repo
                repoFile = Path.of("ilp-visualiser", "src", "data", repoFile.getFileName().toString());
            }
            if (!Files.exists(repoFile)) {
                throw new IllegalStateException("Missing resource: " + path);
            }
            return MAPPER.readTree(Files.readString(repoFile));
        } catch (IOException e) {
            throw new IllegalStateException("Failed reading fallback file for: " + path, e);
        }
    }

    private static JsonNode readJsonArrayFromResource(String path) {
        JsonNode n = readJsonFromResource(path);
        if (n == null || !n.isArray()) throw new IllegalStateException("Expected JSON array in resource: " + path);
        return n;
    }
}
