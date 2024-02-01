//package com.mainthreadlab.weinv.job;
//
//import com.mainthreadlab.weinv.config.security.model.AuthUpdateUserRequest;
//import com.mainthreadlab.weinv.service.UserService;
//import com.mainthreadlab.weinv.service.WeddingService;
//import lombok.Getter;
//import lombok.Setter;
//import lombok.extern.slf4j.Slf4j;
//import org.quartz.JobExecutionContext;
//import org.quartz.JobExecutionException;
//import org.springframework.scheduling.quartz.QuartzJobBean;
//
//@Slf4j
//@Setter
//@Getter
//public class CronManager extends QuartzJobBean {
//
//    private UserService userService;
//
//    private int size;
//
//    @Override
//    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
//        try {
//            log.info("[job] - deleting weddings");
//            AuthUpdateUserRequest test = new AuthUpdateUserRequest();
//            test.setUsername("test");
//            userService.callAuthServer(test, "", null);
//            log.info("[job] - end");
//        } catch (Throwable e) {
//            log.error("[job] - error: {}", e.getMessage(), e);
//        }
//    }
//
//}