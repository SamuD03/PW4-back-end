package its.incom.webdev.service;

import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class PhoneService {

    // Inject Twilio credentials from application.properties
    @ConfigProperty(name = "twilio.account.sid")
    String accountSid;

    @ConfigProperty(name = "twilio.auth.token")
    String authToken;

    @ConfigProperty(name = "twilio.service.sid")
    String serviceSid;

    @ConfigProperty(name = "twilio.default.country.code", defaultValue = "+39")
    String defaultCountryCode;

    // Initialize Twilio in a @PostConstruct method
    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    // Method to send OTP via Twilio's Verify service
    public void sendOtp(String phoneNumber) {
        // Ensure phone number is in E.164 format
        if (!phoneNumber.startsWith("+")) {
            phoneNumber = defaultCountryCode + phoneNumber; // Add the default country code
        }

        Verification verification = Verification.creator(
                serviceSid,  // Twilio Verify Service SID
                phoneNumber, // Recipient's phone number in E.164 format
                "sms"        // Method: "sms" or "call"
        ).create();

        System.out.println("OTP sent, SID: " + verification.getSid());
    }

    // Method to validate the OTP
    public boolean validateOtp(String phoneNumber, String otp) {
        if (!phoneNumber.startsWith("+")) {
            phoneNumber = defaultCountryCode + phoneNumber; // Add the default country code
        }

        VerificationCheck verificationCheck = VerificationCheck.creator(
                        serviceSid
                ).setTo(phoneNumber)
                .setCode(otp)
                .create();

        return "approved".equals(verificationCheck.getStatus());
    }
}
