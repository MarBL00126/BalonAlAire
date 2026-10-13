package basketball.projects.balonAlAire.service;

import java.util.Map;

public record PresignedUpload(String uploadUrl, String publicUrl, Map<String, String> headers) {
}
