//package com.mainthreadlab.weinv.config;
//
//import com.mainthreadlab.weinv.job.DeleteWeddingsJob;
//import com.mainthreadlab.weinv.service.UserService;
//import lombok.RequiredArgsConstructor;
//import org.quartz.*;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.beans.factory.config.PropertiesFactoryBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.scheduling.quartz.SchedulerFactoryBean;
//
//import java.io.IOException;
//import java.util.Properties;
//
//@Configuration
//@RequiredArgsConstructor
//public class QuartzConfig {
//
//    @Value("${weinv.job.delete.weddings.deletionCronExpression}")
//    private String deletionCronExpression;
//
//    @Value("${weinv.job.delete.weddings.size}")
//    private int size;
//
//    private final UserService userService;
//
//
//    @Bean
//    public JobDetail jobDeleteWeddingDetail() {
//        JobDataMap jobDataMap = new JobDataMap();
//        jobDataMap.put("userService", userService);
//        jobDataMap.put("size", size);
//
//        return JobBuilder.newJob(DeleteWeddingsJob.class)
//                .setJobData(jobDataMap)
//                .storeDurably()
//                .build();
//    }
//
//    @Bean
//    public Trigger jobDeleteWeddingTrigger() {
//        CronScheduleBuilder cron = CronScheduleBuilder.cronSchedule(deletionCronExpression);
//
//        return TriggerBuilder.newTrigger()
//                .forJob(jobDeleteWeddingDetail())
//                .withIdentity("deletionCronExpression")
//                .withSchedule(cron)
//                .build();
//    }
//
//    @Bean
//    public SchedulerFactoryBean schedulerFactoryBean() throws IOException {
//        SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
//        scheduler.setTriggers(jobDeleteWeddingTrigger());
//
//        scheduler.setQuartzProperties(quartzProperties());
//        scheduler.setJobDetails(jobDeleteWeddingDetail());
//        return scheduler;
//    }
//
//    @Bean
//    public Properties quartzProperties() throws IOException {
//        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
//        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
//        propertiesFactoryBean.afterPropertiesSet();
//        return propertiesFactoryBean.getObject();
//    }
//}
