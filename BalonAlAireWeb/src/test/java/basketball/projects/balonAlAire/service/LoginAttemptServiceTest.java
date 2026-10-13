package basketball.projects.balonAlAire.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LoginAttemptServiceTest {

    private final LoginAttemptService service = new LoginAttemptService();

    @Test
    void blocksUserAfterMaxFailuresRegardlessOfCaseAndIp() {
        for (int i = 0; i < LoginAttemptService.MAX_FAILURES_PER_USER; i++) {
            assertThat(service.isLoginBlocked("1.1.1." + i, "Admin")).isFalse();
            service.recordLoginFailure("1.1.1." + i, "Admin");
        }

        assertThat(service.isLoginBlocked("9.9.9.9", "admin ")).isTrue();
        assertThat(service.isLoginBlocked("9.9.9.9", "other")).isFalse();
    }

    @Test
    void blocksIpAfterMaxFailuresAcrossUsernames() {
        for (int i = 0; i < LoginAttemptService.MAX_FAILURES_PER_IP; i++) {
            service.recordLoginFailure("2.2.2.2", "user" + i);
        }

        assertThat(service.isLoginBlocked("2.2.2.2", "fresh")).isTrue();
        assertThat(service.isLoginBlocked("3.3.3.3", "fresh")).isFalse();
    }

    @Test
    void successResetsUserCounter() {
        for (int i = 0; i < LoginAttemptService.MAX_FAILURES_PER_USER - 1; i++) {
            service.recordLoginFailure("4.4.4.4", "maria");
        }
        service.recordLoginSuccess("maria");
        service.recordLoginFailure("5.5.5.5", "maria");

        assertThat(service.isLoginBlocked("6.6.6.6", "maria")).isFalse();
    }

    @Test
    void limitsPasswordResetRequestsPerIp() {
        for (int i = 0; i < LoginAttemptService.MAX_RESET_REQUESTS_PER_IP; i++) {
            assertThat(service.allowPasswordResetRequest("7.7.7.7")).isTrue();
        }

        assertThat(service.allowPasswordResetRequest("7.7.7.7")).isFalse();
        assertThat(service.allowPasswordResetRequest("8.8.8.8")).isTrue();
    }
}
