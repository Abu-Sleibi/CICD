package com.example.hotelproject.email;

import com.example.hotelproject.booking.entity.Booking;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' HH:mm");

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendBookingConfirmation(Booking booking) {
        try {
            String guestEmail = booking.getGuest() != null ? booking.getGuest().getEmail() : null;
            if (guestEmail == null || guestEmail.isBlank()) {
                log.warn("No guest email for booking {}; skipping confirmation email", booking.getBookingReference());
                return;
            }

            String guestName   = booking.getGuest().getFullName() != null ? booking.getGuest().getFullName() : "Valued Guest";
            String hotelName   = booking.getRoomType().getHotel().getName();
            String roomType    = booking.getRoomType().getName();
            String checkIn     = booking.getCheckInDate().format(DATE_FMT);
            String checkOut    = booking.getCheckOutDate().format(DATE_FMT);
            long   nights      = booking.getNights();
            int    rooms       = booking.getNumberOfRooms();
            int    guests      = booking.getNumberOfGuests();
            BigDecimal total   = booking.getTotalPrice();
            String reference   = booking.getBookingReference();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(guestEmail);
            helper.setSubject("Booking Confirmed – " + hotelName);
            helper.setText(buildHtml(guestName, hotelName, roomType, checkIn, checkOut,
                    nights, rooms, guests, total, reference), true);

            mailSender.send(message);
            log.info("Booking confirmation email sent to {} for reference {}", guestEmail, reference);

        } catch (MessagingException e) {
            log.error("Failed to send booking confirmation email for {}: {}", booking.getBookingReference(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending confirmation email for {}: {}", booking.getBookingReference(), e.getMessage());
        }
    }

    @Override
    public void sendBookingCancellation(Booking booking) {
        try {
            String guestEmail = booking.getGuest() != null ? booking.getGuest().getEmail() : null;
            if (guestEmail == null || guestEmail.isBlank()) {
                log.warn("No guest email for booking {}; skipping cancellation email", booking.getBookingReference());
                return;
            }

            String guestName  = booking.getGuest().getFullName() != null ? booking.getGuest().getFullName() : "Valued Guest";
            String hotelName  = booking.getRoomType().getHotel().getName();
            String roomType   = booking.getRoomType().getName();
            String checkIn    = booking.getCheckInDate().format(DATE_FMT);
            String checkOut   = booking.getCheckOutDate().format(DATE_FMT);
            String reference  = booking.getBookingReference();
            String cancelDate = LocalDateTime.now().format(DATETIME_FMT);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(guestEmail);
            helper.setSubject("Booking Cancelled - " + hotelName);
            helper.setText(buildCancellationHtml(guestName, hotelName, roomType, checkIn, checkOut, reference, cancelDate), true);

            mailSender.send(message);
            log.info("Booking cancellation email sent to {} for reference {}", guestEmail, reference);

        } catch (MessagingException e) {
            log.error("Failed to send booking cancellation email for {}: {}", booking.getBookingReference(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending cancellation email for {}: {}", booking.getBookingReference(), e.getMessage());
        }
    }

    private String buildCancellationHtml(String guestName, String hotelName, String roomType,
                                          String checkIn, String checkOut,
                                          String reference, String cancelDate) {
        String details = row("Hotel", hotelName)
                + row("Room Type", roomType)
                + row("Check-in", checkIn)
                + row("Check-out", checkOut)
                + row("Cancelled On", cancelDate)
                + "<tr><td colspan='2' style='padding-top:12px;border-top:1px solid #fecaca;'></td></tr>"
                + "<tr><td style='padding:6px 0;font-size:13px;color:#991b1b;font-weight:600;'>Booking Reference</td>"
                + "<td style='padding:6px 0;font-size:15px;color:#991b1b;font-weight:700;font-family:monospace;'>" + reference + "</td></tr>";

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <title>Booking Cancelled</title>
                </head>
                <body style="margin:0;padding:0;background:#f9fafb;font-family:'Helvetica Neue',Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f9fafb;padding:32px 16px;">
                    <tr><td align="center">
                      <table width="600" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                        <tr>
                          <td style="background:linear-gradient(135deg,#ef4444,#dc2626);padding:32px 40px;text-align:center;">
                            <h1 style="color:#ffffff;margin:0;font-size:28px;font-weight:700;">HotelHub</h1>
                            <p style="color:rgba(255,255,255,0.85);margin:6px 0 0;font-size:15px;">Your booking has been cancelled</p>
                          </td>
                        </tr>
                        <tr>
                          <td style="text-align:center;padding:28px 40px 0;">
                            <div style="display:inline-block;background:#fef2f2;border-radius:50%%;width:56px;height:56px;line-height:56px;font-size:28px;">❌</div>
                            <h2 style="color:#111827;margin:16px 0 4px;font-size:22px;">Hello, %s!</h2>
                            <p style="color:#6b7280;margin:0;font-size:15px;">We're sorry to see you go. Your booking has been successfully cancelled.</p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:24px 40px;">
                            <table width="100%%" cellpadding="0" cellspacing="0"
                                   style="background:#fef2f2;border:1px solid #fecaca;border-radius:12px;padding:20px;">
                              <tr>
                                <td>
                                  <p style="margin:0 0 16px;font-size:13px;font-weight:600;text-transform:uppercase;letter-spacing:0.05em;color:#991b1b;">Cancellation Summary</p>
                                  %s
                                </td>
                              </tr>
                            </table>
                          </td>
                        </tr>
                        <tr>
                          <td style="background:#f3f4f6;padding:20px 40px;text-align:center;border-top:1px solid #e5e7eb;">
                            <p style="margin:0;font-size:13px;color:#9ca3af;">
                              Need help? Contact us at
                              <a href="mailto:support@hotelhub.com" style="color:#ef4444;">support@hotelhub.com</a>
                            </p>
                            <p style="margin:8px 0 0;font-size:12px;color:#d1d5db;">© 2025 HotelHub. All rights reserved.</p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(guestName, details);
    }

    private String buildHtml(String guestName, String hotelName, String roomType,
                              String checkIn, String checkOut, long nights,
                              int rooms, int guests, BigDecimal total, String reference) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Booking Confirmed</title>
                </head>
                <body style="margin:0;padding:0;background:#f9fafb;font-family:'Helvetica Neue',Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f9fafb;padding:32px 16px;">
                    <tr><td align="center">
                      <table width="600" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                        <!-- Header -->
                        <tr>
                          <td style="background:linear-gradient(135deg,#f59e0b,#d97706);padding:32px 40px;text-align:center;">
                            <h1 style="color:#ffffff;margin:0;font-size:28px;font-weight:700;letter-spacing:-0.5px;">HotelHub</h1>
                            <p style="color:rgba(255,255,255,0.85);margin:6px 0 0;font-size:15px;">Your booking is confirmed!</p>
                          </td>
                        </tr>
                        <!-- Checkmark badge -->
                        <tr>
                          <td style="text-align:center;padding:28px 40px 0;">
                            <div style="display:inline-block;background:#ecfdf5;border-radius:50%%;width:56px;height:56px;line-height:56px;font-size:28px;">✅</div>
                            <h2 style="color:#111827;margin:16px 0 4px;font-size:22px;">Hello, %s!</h2>
                            <p style="color:#6b7280;margin:0;font-size:15px;">We're excited to have you stay with us.</p>
                          </td>
                        </tr>
                        <!-- Booking details card -->
                        <tr>
                          <td style="padding:24px 40px;">
                            <table width="100%%" cellpadding="0" cellspacing="0"
                                   style="background:#fffbeb;border:1px solid #fde68a;border-radius:12px;padding:20px;">
                              <tr>
                                <td>
                                  <p style="margin:0 0 16px;font-size:13px;font-weight:600;text-transform:uppercase;letter-spacing:0.05em;color:#92400e;">Booking Summary</p>
                                  %s
                                </td>
                              </tr>
                            </table>
                          </td>
                        </tr>
                        <!-- Reference -->
                        <tr>
                          <td style="padding:0 40px 24px;text-align:center;">
                            <p style="margin:0 0 6px;font-size:13px;color:#6b7280;">Booking Reference</p>
                            <span style="font-size:20px;font-weight:700;font-family:monospace;color:#1f2937;letter-spacing:1px;">%s</span>
                          </td>
                        </tr>
                        <!-- Footer -->
                        <tr>
                          <td style="background:#f3f4f6;padding:20px 40px;text-align:center;border-top:1px solid #e5e7eb;">
                            <p style="margin:0;font-size:13px;color:#9ca3af;">
                              Need help? Contact us at
                              <a href="mailto:support@hotelhub.com" style="color:#f59e0b;">support@hotelhub.com</a>
                            </p>
                            <p style="margin:8px 0 0;font-size:12px;color:#d1d5db;">© 2025 HotelHub. All rights reserved.</p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(
                guestName,
                buildDetailsRows(hotelName, roomType, checkIn, checkOut, nights, rooms, guests, total),
                reference
        );
    }

    private String buildDetailsRows(String hotelName, String roomType, String checkIn, String checkOut,
                                    long nights, int rooms, int guests, BigDecimal total) {
        return row("Hotel", hotelName)
                + row("Room Type", roomType)
                + row("Check-in", checkIn)
                + row("Check-out", checkOut)
                + row("Duration", nights + " night" + (nights != 1 ? "s" : ""))
                + row("Rooms", String.valueOf(rooms))
                + row("Guests", String.valueOf(guests))
                + "<tr><td colspan='2' style='padding-top:12px;border-top:1px solid #fde68a;'></td></tr>"
                + rowBold("Total Price", "$" + total.toPlainString());
    }

    private String row(String label, String value) {
        return "<tr><td style='padding:4px 0;font-size:14px;color:#6b7280;width:120px;'>" + label + "</td>"
                + "<td style='padding:4px 0;font-size:14px;color:#1f2937;font-weight:500;'>" + value + "</td></tr>";
    }

    private String rowBold(String label, String value) {
        return "<tr><td style='padding:6px 0;font-size:15px;color:#92400e;font-weight:600;'>" + label + "</td>"
                + "<td style='padding:6px 0;font-size:18px;color:#92400e;font-weight:700;'>" + value + "</td></tr>";
    }
}
