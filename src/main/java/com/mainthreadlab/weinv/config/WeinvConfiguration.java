package com.mainthreadlab.weinv.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.RestTemplate;

import java.util.Properties;

@Configuration
public class WeinvConfiguration {


    @Value("${weinv.httpClient.timeout}")
    private int httpClientTimeout;

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private int mailPort;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${spring.mail.password}")
    private String mailPassword;

    @Value("${spring.mail.properties.mail.smtp.auth}")
    private boolean smtpAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private boolean starttlsEnabled;

    @Value("${spring.mail.properties.mail.debug}")
    private boolean mailDebug;

    @Value("${spring.mail.properties.mail.transport.protocol}")
    private String mailProtocol;


    @Bean
    public ObjectMapper createObjectMapper() {
        return JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                //.setConnectTimeout(Duration.ofSeconds(httpClientTimeout))
                //  .setReadTimeout(Duration.ofSeconds(httpClientTimeout))
                .build();
    }

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);

        mailSender.setUsername(mailUsername);
        mailSender.setPassword(mailPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", mailProtocol);
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.starttls.enable", starttlsEnabled);
        props.put("mail.debug", mailDebug);

        return mailSender;
    }


//    @PostConstruct
//    public void init() {
//        Random rand = new Random();
//        User guest = new User();
//        guest.setId(2);
//        guest.setRoles("guest");
//        guest.setLanguage(LanguageEnum.FR);
//        guest.setEmail("nicy.lab.noreply@gmail.com");
//        guest.setFirstName("guestTest");
//        guest.setLastName("guestTest");
//        guest.setEnabled(true);
//        guest.setUuid(UUID.randomUUID().toString());
//        guest.setUsername(guest.getFirstName());
//        guest = userRepository.save(guest);
//
//        User responsible = new User();
//        responsible.setId(3);
//        responsible.setRoles("user");
//        responsible.setLanguage(LanguageEnum.FR);
//        responsible.setEmail("nicy.lab.noreply@gmail.com");
//        responsible.setFirstName("responsibleTest");
//        responsible.setLastName("responsibleTest");
//        responsible.setEnabled(true);
//        responsible.setUuid(UUID.randomUUID().toString());
//        responsible.setUsername(responsible.getFirstName());   //  + rand.nextInt(20)
//        responsible = userRepository.save(responsible);
//
//        Event event = new Event();
//        event.setUuid(UUID.randomUUID().toString());
//        event.setResponsible(responsible);
//        event.setDate(Date.from(LocalDate.now().plusMonths(6).atStartOfDay(ZoneId.systemDefault()).toInstant()));
//        event.setDescription("Alessandro et Christelle Mariage");
//        event.setCeremonyStartime(Date.from(LocalDateTime.now().plusMonths(6).plusHours(3).atZone(ZoneId.systemDefault()).toInstant()));
//    event.setCeremonyVenue("Eglise C.R.S, 3, avenue Lokango, Barumbu, Kinshasa");
//    event.setHusbandName("Alessandro");
//    event.setWifeName("Christelle");
//    event.setMaxReceptionSeats(500);
//    event.setReceptionStartime(Date.from(LocalDateTime.now().plusMonths(6).plusHours(8).atZone(ZoneId.systemDefault()).toInstant()));
//    event.setReceptionVenue("Salle Royale, 12, avenue Faradje, Kasa-Vubu, Kinshasa");
//        event.setDeadlineConfirmInvitation(Date.from(LocalDate.now().plusMonths(5).atStartOfDay(ZoneId.systemDefault()).toInstant()));
//        event = weddingRepository.save(event);
//
//        WeddingGuest weddingGuest = new WeddingGuest();
//        weddingGuest.setGuest(guest);
//        weddingGuest.setWedding(event);
//        weddingGuest.setSeatNumber("15");
//        WeddingGuestId weddingGuestId = new WeddingGuestId();
//        weddingGuestId.setWeddingId(event.getId());
//        weddingGuestId.setUserId(guest.getId());
//        weddingGuest.setId(weddingGuestId);
//        weddingGuestRepository.save(weddingGuest);
//    }

}