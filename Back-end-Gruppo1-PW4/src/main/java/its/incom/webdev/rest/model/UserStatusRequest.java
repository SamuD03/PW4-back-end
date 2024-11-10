package its.incom.webdev.rest.model;

import jakarta.enterprise.context.Dependent;

@Dependent
public class UserStatusRequest {
    private Boolean admin;
    private Boolean verified;

    public Boolean isAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public Boolean isVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }
}