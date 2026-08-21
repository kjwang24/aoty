package com.kjwang24.aoty.service;

import org.springframework.scheduling.annotation.Scheduled;

import com.kjwang24.aoty.entity.User;

public class Scraper {

    @Scheduled(fixedRate = 21600000) // 6 hrs in milliseconds
    public void scrape(User user) {

    }

}
