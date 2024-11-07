package its.incom.webdev.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@ApplicationScoped
public class PhoneService {
    @ConfigProperty(name = "twilio.account.sid")
    String accountSid;

    @ConfigProperty(name = "twilio.auth.token")
    String authToken;

    @ConfigProperty(name = "twilio.phone.number")
    String fromPhoneNumber;

    // A simple in-memory store for OTPs (consider a more robust solution for production)
    private Map<String, String> otpStorage = new HashMap<>();

    // Method to send SMS via Twilio
    public void sendOtp(String toPhoneNumber, String messageBody) {
        System.out.println("Twilio Account SID: " + accountSid);
        System.out.println("Twilio Auth Token: " + authToken);
        System.out.println("Twilio Phone Number: " + fromPhoneNumber);

        // Initialize Twilio (this can be done only once)
        Twilio.init(accountSid, authToken);

        // Create the message
        Message message = Message.creator(
                new PhoneNumber(toPhoneNumber), // Destination number
                new PhoneNumber(fromPhoneNumber), // Twilio number from which to send the SMS
                messageBody // Content of the message
        ).create();

        // Optionally, print the message ID to track the result
        System.out.println("SMS sent with SID: " + message.getSid());
    }

    public boolean validateOtp(String phoneNumber, String otp) {
        try {
            // Validate the OTP against the stored value
            String storedOtp = otpStorage.get(phoneNumber);
            return storedOtp != null && storedOtp.equals(otp);
        } catch (Exception e) {
            System.err.println("Error validating OTP: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Method to generate a random 6-digit OTP
    public String generateOtp() {
        Random random = new Random();
        String otp = String.format("%06d", random.nextInt(1000000)); // Generate OTP
        return otp;
    }

    // Method to store the OTP for validation
    public void storeOtpForValidation(String phoneNumber, String otp) {
        otpStorage.put(phoneNumber, otp);
    }
}
