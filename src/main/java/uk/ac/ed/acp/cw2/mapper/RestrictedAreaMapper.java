package uk.ac.ed.acp.cw2.mapper;

import uk.ac.ed.acp.cw2.dto.RestrictedAreaDto;
import uk.ac.ed.acp.cw2.entity.RestrictedArea;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class RestrictedAreaMapper {
    private RestrictedAreaMapper() {}

    public static RestrictedArea fromDto(RestrictedAreaDto dto) {

        RestrictedArea.limits limits = mapLimits(dto.limits());
        RestrictedArea.vertices[] vertices = mapVerticesArray(dto.vertices());

        return RestrictedArea.builder()
                .name(dto.name())
                .id(dto.id())
                .limits(limits)
                .vertices(vertices)
                .build();
    }

    public static List<RestrictedArea> fromDtoList(RestrictedAreaDto[] dtos) {
        return Arrays.stream(dtos)
                .map(RestrictedAreaMapper::fromDto)
                .collect(Collectors.toList());
    }

    private static RestrictedArea.limits mapLimits(RestrictedAreaDto.Limits dto) {
        return RestrictedArea.limits.builder()
                .lower(dto.lower())
                .upper(dto.upper())
                .build();
    }

    private static RestrictedArea.vertices mapVertex(RestrictedAreaDto.Vertices dto) {
        return RestrictedArea.vertices.builder()
                .lng(dto.lng())
                .lat(dto.lat())
                .build();
    }

    private static RestrictedArea.vertices[] mapVerticesArray(RestrictedAreaDto.Vertices[] dtos) {
        RestrictedArea.vertices[] arr = new RestrictedArea.vertices[dtos.length];
        for (int i = 0; i < dtos.length; i++) {
            arr[i] = mapVertex(dtos[i]);
        }
        return arr;
    }
}
