package com.example.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {
	@PostConstruct
	public void init() {
		// 解决Netty启动冲突的问题（Redis和Elasticsearch都依赖了Netty,Netty在启动一次后再次启动会抛出异常、所以需要在bean构造后进行设置
		// see Netty4Utils.setAvailableProcessors()
		System.setProperty("es.set.netty.runtime.available.processors", "false");
	}

	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}

}
