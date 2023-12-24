//package com.mainthreadlab.weinv;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.event.ContextStoppedEvent;
//import org.springframework.context.event.EventListener;
//import org.springframework.core.env.Environment;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class StartupShutdownEventListener {
//
//    private final Environment env;
//    @Value("${server.port}")
//    private String port;
//
//    @EventListener
//    void onStartup(ApplicationReadyEvent event) {
////        String profiles;
////        if (env.getActiveProfiles().length > 0) {
////            profiles = "with profile " + Arrays.toString(env.getActiveProfiles());
////        } else {
////            profiles = "with dev profile";
////        }
////        log.info("Authorization-server startup completed {} [PORT: {}]", profiles, port);
//        log.info("Weinv-server started on port(s): {}", port);
//
//    }
//
//    @EventListener
//    void onShutdown(ContextStoppedEvent event) {
//        log.info("Weinv-server shutdown completed");
//    }
//
//}
