package basketball.projects.balonAlAire.dto;

import java.util.Map;

/** Datos para subir un archivo directo al storage: PUT a uploadUrl con estos headers; publicUrl es la URL final. */
public record PresignResponse(String uploadUrl, String publicUrl, Map<String, String> headers) {
}
