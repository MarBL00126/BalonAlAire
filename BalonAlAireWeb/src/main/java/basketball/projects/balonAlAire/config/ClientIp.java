package basketball.projects.balonAlAire.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

import jakarta.servlet.http.HttpServletRequest;

final class ClientIp {

    private ClientIp() {
    }

    /**
     * IP del cliente. X-Forwarded-For solo se considera si la conexión viene de un proxy interno
     * (Railway), y se toma la última entrada, que es la que agregó ese proxy y el cliente no puede falsificar.
     */
    static String of(HttpServletRequest request) {
        String remote = request.getRemoteAddr();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded == null || forwarded.isBlank() || !isInternal(remote)) {
            return remote;
        }
        String[] hops = forwarded.split(",");
        String last = hops[hops.length - 1].trim();
        return last.isEmpty() ? remote : last;
    }

    private static boolean isInternal(String address) {
        try {
            InetAddress ip = InetAddress.getByName(address);
            if (ip.isLoopbackAddress() || ip.isSiteLocalAddress() || ip.isLinkLocalAddress()) {
                return true;
            }
            byte[] bytes = ip.getAddress();
            if (bytes.length == 4) {
                // 100.64.0.0/10 (CGNAT, usado por redes internas de plataformas cloud)
                return (bytes[0] & 0xFF) == 100 && (bytes[1] & 0xC0) == 64;
            }
            // fc00::/7 (unique local)
            return (bytes[0] & 0xFE) == 0xFC;
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
